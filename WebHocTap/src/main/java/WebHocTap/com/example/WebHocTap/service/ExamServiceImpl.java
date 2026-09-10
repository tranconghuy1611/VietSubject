package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.exam.*;
import WebHocTap.com.example.WebHocTap.entity.*;
import WebHocTap.com.example.WebHocTap.enums.ExamStatus;
import WebHocTap.com.example.WebHocTap.enums.PerformanceLevel;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import WebHocTap.com.example.WebHocTap.exception.BadRequestException;
import WebHocTap.com.example.WebHocTap.exception.ForbiddenException;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.repository.*;
import WebHocTap.com.example.WebHocTap.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private static final float WEAK_THRESHOLD = 0.5f;
    private static final float GOOD_THRESHOLD = 0.8f;
    private static final String MULTI_SELECT_PREFIX = "IDS:";

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final UserPerformanceRepository userPerformanceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ExamStartResponseDTO startExam(Long examId) {
        User currentUser = SecurityUtils.getCurrentUser();

        Exam exam = examRepository.findByIdAndIsActiveTrue(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        ExamResult result = new ExamResult();
        result.setUser(currentUser);
        result.setExam(exam);
        result.setStatus(ExamStatus.IN_PROGRESS);
        result.setTotalQuestions(examQuestionRepository.countByExamId(examId));

        ExamResult saved = examResultRepository.save(result);
        log.info("Exam {} started as result {} by user {}", examId, saved.getId(), currentUser.getId());

        return ExamStartResponseDTO.builder()
                .resultId(saved.getId())
                .examId(exam.getId())
                .examName(exam.getName())
                .durationMinutes(exam.getDurationMinutes())
                .totalQuestions(saved.getTotalQuestions())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExamResponseDTO> listExams(Pageable pageable) {
        Page<ExamResponseDTO> page = examRepository.findByIsActiveTrue(pageable).map(this::toExamResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResponseDTO getExamById(Long examId) {
        Exam exam = examRepository.findByIdAndIsActiveTrue(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
        return toExamResponse(exam);
    }

    @Override
    @Transactional
    public List<ExamQuestionDTO> getQuestions(Long resultId) {
        ExamResult result = findOwnedResult(resultId);
        autoSubmitIfExpired(result);
        if (result.getStatus() == ExamStatus.SUBMITTED) {
            throw new BadRequestException("Exam already submitted");
        }

        return examQuestionRepository.findByExamIdOrderByQuestionOrder(result.getExam().getId())
                .stream()
                .map(this::mapToExamQuestionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubmitExamAnswerResponseDTO submitAnswer(Long resultId, SubmitExamAnswerRequestDTO request) {
        ExamResult result = findOwnedResult(resultId);
        autoSubmitIfExpired(result);

        if (result.getStatus() != ExamStatus.IN_PROGRESS) {
            throw new BadRequestException("Exam already submitted");
        }

        Long examId = result.getExam().getId();
        ExamQuestion matchedEq = examQuestionRepository.findByExamIdOrderByQuestionOrder(examId).stream()
                .filter(eq -> eq.getQuestion().getId().equals(request.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Question " + request.getQuestionId() + " does not belong to exam " + examId));

        Question question = matchedEq.getQuestion();
        validateAnswerPayload(question.getQuestionType(), request);

        Answer selectedAnswer = resolvePrimarySelectedAnswer(question, request);
        String submittedText = resolveSubmittedText(question.getQuestionType(), request);

        ExamAnswer examAnswer = examAnswerRepository
                .findByExamResultIdAndQuestionId(result.getId(), question.getId())
                .orElseGet(ExamAnswer::new);

        examAnswer.setExamResult(result);
        examAnswer.setQuestion(question);
        examAnswer.setSelectedAnswer(selectedAnswer);
        examAnswer.setSubmittedText(submittedText);

        ExamAnswer saved = examAnswerRepository.save(examAnswer);
        log.debug("Saved exam answer for result {} question {}", resultId, question.getId());

        return SubmitExamAnswerResponseDTO.builder()
                .examAnswerId(saved.getId())
                .questionId(question.getId())
                .message("Answer saved.")
                .build();
    }

    @Override
    @Transactional
    public ExamResultDTO submitExam(Long resultId) {
        ExamResult result = findOwnedResult(resultId);

        if (result.getStatus() == ExamStatus.SUBMITTED) {
            throw new BadRequestException("Exam already submitted");
        }

        return gradeAndSubmit(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExamResultDTO> getMyResults(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<ExamResultDTO> page = examResultRepository.findByUserId(userId, pageable)
                .map(this::toResultDto);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResultDTO getResultDetail(Long resultId) {
        ExamResult result = findOwnedResult(resultId);
        return toResultDto(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamSummaryDTO getResultSummary(Long resultId) {
        ExamResult result = findOwnedResult(resultId);
        long timeSpent = 0L;
        if (result.getStartedAt() != null) {
            LocalDateTime end = result.getSubmittedAt() != null ? result.getSubmittedAt() : LocalDateTime.now();
            timeSpent = Duration.between(result.getStartedAt(), end).getSeconds();
        }
        return ExamSummaryDTO.builder()
                .resultId(result.getId())
                .totalQuestions(result.getTotalQuestions())
                .correctCount(result.getCorrectCount())
                .score(result.getScore())
                .timeSpentSeconds(timeSpent)
                .build();
    }

    // ───────────────────────── private ─────────────────────────

    private ExamResult findOwnedResult(Long resultId) {
        ExamResult result = examResultRepository.findByIdWithUserAndExam(resultId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exam result not found with id: " + resultId));
        verifyOwnership(result);
        return result;
    }

    private void verifyOwnership(ExamResult result) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!result.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Access denied");
        }
    }

    /**
     * If duration exceeded, auto-submit (grade) the exam.
     */
    private void autoSubmitIfExpired(ExamResult result) {
        if (result.getStatus() != ExamStatus.IN_PROGRESS) {
            return;
        }
        Integer durationMinutes = result.getExam().getDurationMinutes();
        if (durationMinutes == null || result.getStartedAt() == null) {
            return;
        }
        LocalDateTime deadline = result.getStartedAt().plusMinutes(durationMinutes);
        if (LocalDateTime.now().isAfter(deadline)) {
            log.info("Auto-submitting expired exam result {}", result.getId());
            gradeAndSubmit(result);
        }
    }

    private ExamResultDTO gradeAndSubmit(ExamResult result) {
        List<ExamAnswer> answers = examAnswerRepository.findByResultIdWithDetails(result.getId());
        Long examId = result.getExam().getId();
        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExamIdOrderByQuestionOrder(examId);

        Map<Long, ExamAnswer> answerByQuestionId = answers.stream()
                .collect(Collectors.toMap(ea -> ea.getQuestion().getId(), ea -> ea));

        int correctCount = 0;
        for (ExamQuestion eq : examQuestions) {
            Question question = eq.getQuestion();
            ExamAnswer answer = answerByQuestionId.get(question.getId());
            if (answer == null) {
                continue;
            }
            boolean isCorrect = evaluateAnswer(question, answer);
            answer.setIsCorrect(isCorrect);
            examAnswerRepository.save(answer);
            if (isCorrect) {
                correctCount++;
            }
        }

        int totalQuestions = examQuestions.size();
        float score = totalQuestions > 0 ? ((float) correctCount / totalQuestions) * 10f : 0f;

        result.setScore(score);
        result.setCorrectCount(correctCount);
        result.setTotalQuestions(totalQuestions);
        result.setStatus(ExamStatus.SUBMITTED);
        result.setSubmittedAt(LocalDateTime.now());
        examResultRepository.save(result);

        updatePerformancePerTopic(result.getUser().getId(), examQuestions, answerByQuestionId);
        log.info("Exam result {} submitted score={}", result.getId(), score);

        return toResultDto(result);
    }

    private void validateAnswerPayload(QuestionType type, SubmitExamAnswerRequestDTO request) {
        switch (type) {
            case MULTIPLE_CHOICE -> {
                if (request.getAnswerId() == null) {
                    throw new BadRequestException("MULTIPLE_CHOICE requires answerId");
                }
                if (request.getAnswerIds() != null && !request.getAnswerIds().isEmpty()) {
                    throw new BadRequestException("MULTIPLE_CHOICE must use answerId, not answerIds");
                }
            }
            case MULTIPLE_SELECT -> {
                if (request.getAnswerIds() == null || request.getAnswerIds().isEmpty()) {
                    throw new BadRequestException("MULTIPLE_SELECT requires answerIds");
                }
            }
            case FILL_BLANK, LISTENING -> {
                if (request.getSubmittedText() == null || request.getSubmittedText().isBlank()) {
                    throw new BadRequestException(type + " requires submittedText");
                }
            }
            default -> { }
        }
    }

    private Answer resolvePrimarySelectedAnswer(Question question, SubmitExamAnswerRequestDTO request) {
        if (request.getAnswerId() != null) {
            return findAnswerOnQuestion(question, request.getAnswerId());
        }
        if (request.getAnswerIds() != null && !request.getAnswerIds().isEmpty()) {
            return findAnswerOnQuestion(question, request.getAnswerIds().get(0));
        }
        return null;
    }

    private String resolveSubmittedText(QuestionType type, SubmitExamAnswerRequestDTO request) {
        if (type == QuestionType.MULTIPLE_SELECT && request.getAnswerIds() != null) {
            return MULTI_SELECT_PREFIX + request.getAnswerIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
        return request.getSubmittedText();
    }

    private Answer findAnswerOnQuestion(Question question, Long answerId) {
        if (question.getAnswers() == null) {
            throw new BadRequestException("Question has no answers");
        }
        return question.getAnswers().stream()
                .filter(a -> a.getId().equals(answerId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Answer " + answerId + " does not belong to question " + question.getId()));
    }

    private boolean evaluateAnswer(Question question, ExamAnswer answer) {
        QuestionType type = question.getQuestionType();

        if (type == QuestionType.FILL_BLANK || type == QuestionType.LISTENING) {
            String submitted = answer.getSubmittedText();
            if (submitted == null || submitted.isBlank()) return false;
            submitted = submitted.trim().toLowerCase();

            if (question.getCorrectTextAnswer() != null
                    && submitted.equals(question.getCorrectTextAnswer().trim().toLowerCase())) {
                return true;
            }
            if (question.getAcceptedTextAnswers() != null) {
                for (String alt : question.getAcceptedTextAnswers().split(",")) {
                    if (submitted.equals(alt.trim().toLowerCase())) return true;
                }
            }
            return false;
        }

        if (type == QuestionType.MULTIPLE_SELECT) {
            Set<Long> correctIds = question.getAnswers() == null ? Set.of() : question.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .map(Answer::getId)
                    .collect(Collectors.toSet());
            Set<Long> selectedIds = parseMultiSelectIds(answer.getSubmittedText());
            return !correctIds.isEmpty() && correctIds.equals(selectedIds);
        }

        if (answer.getSelectedAnswer() == null) return false;
        return Boolean.TRUE.equals(answer.getSelectedAnswer().getIsCorrect());
    }

    private Set<Long> parseMultiSelectIds(String submittedText) {
        if (submittedText == null || !submittedText.startsWith(MULTI_SELECT_PREFIX)) {
            return Set.of();
        }
        String raw = submittedText.substring(MULTI_SELECT_PREFIX.length());
        if (raw.isBlank()) return Set.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    private ExamQuestionDTO mapToExamQuestionDTO(ExamQuestion eq) {
        Question q = eq.getQuestion();
        List<ExamAnswerDTO> answerDTOs = (q.getAnswers() == null)
                ? Collections.emptyList()
                : q.getAnswers().stream()
                .map(a -> ExamAnswerDTO.builder()
                        .id(a.getId())
                        .content(a.getContent())
                        .imageUrl(a.getImageUrl())
                        .audioUrl(a.getAudioUrl())
                        .build())
                .collect(Collectors.toList());

        return ExamQuestionDTO.builder()
                .questionId(q.getId())
                .questionOrder(eq.getQuestionOrder())
                .content(q.getContent())
                .questionType(q.getQuestionType())
                .difficulty(q.getDifficulty())
                .imageUrl(q.getImageUrl())
                .audioUrl(q.getAudioUrl())
                .answers(answerDTOs)
                .build();
    }

    private ExamResponseDTO toExamResponse(Exam exam) {
        return ExamResponseDTO.builder()
                .id(exam.getId())
                .name(exam.getName())
                .subjectId(exam.getSubject() != null ? exam.getSubject().getId() : null)
                .subjectName(exam.getSubject() != null ? exam.getSubject().getName() : null)
                .gradeId(exam.getGrade() != null ? exam.getGrade().getId() : null)
                .gradeName(exam.getGrade() != null ? exam.getGrade().getName() : null)
                .durationMinutes(exam.getDurationMinutes())
                .totalQuestions(exam.getTotalQuestions())
                .build();
    }

    private ExamResultDTO toResultDto(ExamResult result) {
        float accuracy = 0f;
        if (result.getTotalQuestions() != null && result.getTotalQuestions() > 0
                && result.getCorrectCount() != null) {
            accuracy = ((float) result.getCorrectCount() / result.getTotalQuestions()) * 100f;
        }
        return ExamResultDTO.builder()
                .resultId(result.getId())
                .examId(result.getExam().getId())
                .examName(result.getExam().getName())
                .score(result.getScore())
                .correctCount(result.getCorrectCount())
                .totalQuestions(result.getTotalQuestions())
                .accuracyPercent(accuracy)
                .status(result.getStatus())
                .startedAt(result.getStartedAt())
                .submittedAt(result.getSubmittedAt())
                .build();
    }

    private void updatePerformancePerTopic(
            Long userId,
            List<ExamQuestion> examQuestions,
            Map<Long, ExamAnswer> answerByQuestionId) {

        Map<Long, List<ExamQuestion>> byTopic = examQuestions.stream()
                .collect(Collectors.groupingBy(eq -> eq.getQuestion().getTopic().getId()));

        for (Map.Entry<Long, List<ExamQuestion>> entry : byTopic.entrySet()) {
            Long topicId = entry.getKey();
            List<ExamQuestion> topicQuestions = entry.getValue();

            int topicTotal = topicQuestions.size();
            int topicCorrect = (int) topicQuestions.stream()
                    .filter(eq -> {
                        ExamAnswer a = answerByQuestionId.get(eq.getQuestion().getId());
                        return a != null && Boolean.TRUE.equals(a.getIsCorrect());
                    })
                    .count();

            Topic topic = topicQuestions.get(0).getQuestion().getTopic();

            UserPerformance perf = userPerformanceRepository
                    .findByUserIdAndTopicId(userId, topicId)
                    .orElseGet(() -> {
                        UserPerformance np = new UserPerformance();
                        np.setUser(userRepository.getReferenceById(userId));
                        np.setTopic(topic);
                        np.setTotalAttempts(0);
                        np.setCorrectAnswers(0);
                        np.setAccuracy(0f);
                        np.setLevel(PerformanceLevel.MEDIUM);
                        return np;
                    });

            perf.setTotalAttempts(perf.getTotalAttempts() + topicTotal);
            perf.setCorrectAnswers(perf.getCorrectAnswers() + topicCorrect);

            float accuracy = (float) perf.getCorrectAnswers() / perf.getTotalAttempts();
            perf.setAccuracy(accuracy);

            PerformanceLevel newLevel;
            if (accuracy < WEAK_THRESHOLD) {
                newLevel = PerformanceLevel.WEAK;
            } else if (accuracy <= GOOD_THRESHOLD) {
                newLevel = PerformanceLevel.MEDIUM;
            } else {
                newLevel = PerformanceLevel.GOOD;
            }
            perf.setLevel(newLevel);
            userPerformanceRepository.save(perf);
        }
    }
}

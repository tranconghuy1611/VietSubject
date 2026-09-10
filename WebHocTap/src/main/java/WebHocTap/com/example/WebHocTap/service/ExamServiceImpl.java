package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.exam.*;
import WebHocTap.com.example.WebHocTap.entity.*;
import WebHocTap.com.example.WebHocTap.enums.ExamStatus;
import WebHocTap.com.example.WebHocTap.enums.PerformanceLevel;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import WebHocTap.com.example.WebHocTap.exception.InvalidRequestException;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.repository.*;
import WebHocTap.com.example.WebHocTap.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private static final float WEAK_THRESHOLD = 0.5f;
    private static final float GOOD_THRESHOLD = 0.8f;

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final UserPerformanceRepository userPerformanceRepository;
    private final UserRepository userRepository;


    @Override
    public List<ExamListDTO> getAllExams() {
        return examRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(e -> ExamListDTO.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .durationMinutes(e.getDurationMinutes())
                        .totalQuestions(
                                examQuestionRepository.countByExamId(e.getId())
                        )
                        .build())
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<ExamResultDTO> getMyResults() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<ExamResult> results =
                examResultRepository.findByUserIdOrderBySubmittedAtDesc(userId);

        return results.stream()
                .map(this::mapToResultDTO)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public ExamResultDTO getResultDetail(Long resultId) {
        ExamResult result = findResultOrThrow(resultId);

        // ✅ check ownership
        verifyOwnership(result);

        return mapToResultDTO(result);
    }
    private ExamResultDTO mapToResultDTO(ExamResult result) {
        float accuracy = result.getTotalQuestions() > 0
                ? ((float) result.getCorrectCount() / result.getTotalQuestions()) * 100f
                : 0f;

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
    @Override
    public List<ExamReviewDTO> reviewExam(Long resultId) {

        ExamResult result = findResultOrThrow(resultId);
        verifyOwnership(result);

        List<ExamAnswer> answers =
                examAnswerRepository.findByResultIdWithDetails(resultId);

        return answers.stream()
                .map(a -> {
                    Question q = a.getQuestion();

                    String correctAnswer = q.getAnswers().stream()
                            .filter(ans -> Boolean.TRUE.equals(ans.getIsCorrect()))
                            .map(Answer::getContent)
                            .findFirst()
                            .orElse(null);

                    String userAnswer = a.getSelectedAnswer() != null
                            ? a.getSelectedAnswer().getContent()
                            : a.getSubmittedText();

                    return ExamReviewDTO.builder()
                            .questionId(q.getId())
                            .questionContent(q.getContent())
                            .yourAnswer(userAnswer)
                            .correctAnswer(correctAnswer)
                            .isCorrect(a.getIsCorrect())
                            .build();
                })
                .toList();
    }
    // ──────────────────────────────────────────────────────────────────────────
    // 1. Start Exam
    //    userId ← JWT (SecurityContextHolder) — NEVER from the request body.
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ExamStartResponseDTO startExam(Long examId) {
        // ✅ Extract the authenticated user from the JWT — never trust client-supplied userId
        User currentUser = SecurityUtils.getCurrentUser();

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exam not found with id: " + examId));

        if (Boolean.FALSE.equals(exam.getIsActive())) {
            throw new InvalidRequestException("Exam is not active: " + examId);
        }

        ExamResult result = new ExamResult();
        result.setUser(currentUser);   // ✅ from JWT, not request
        result.setExam(exam);
        result.setStatus(ExamStatus.IN_PROGRESS);
        result.setTotalQuestions(examQuestionRepository.countByExamId(examId));

        ExamResult saved = examResultRepository.save(result);

        return ExamStartResponseDTO.builder()
                .resultId(saved.getId())
                .examId(exam.getId())
                .examName(exam.getName())
                .durationMinutes(exam.getDurationMinutes())
                .totalQuestions(saved.getTotalQuestions())
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. Get Questions
    //    userId ← exam_result.user_id — NEVER from the request.
    //    Optional security check: JWT user == result owner.
    // ──────────────────────────────────────────────────────────────────────────


    @Override
    @Transactional(readOnly = true)
    public List<ExamQuestionDTO> getQuestions(Long resultId) {
        ExamResult result = findResultOrThrow(resultId);

        // ✅ userId comes from the persisted result entity — not the request
        verifyOwnership(result);

        Long examId = result.getExam().getId();
        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExamIdOrderByQuestionOrder(examId);

        return examQuestions.stream()
                .map(this::mapToExamQuestionDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. Submit Answer
    //    userId ← exam_result.user_id — NEVER from the request.
    //    Upsert-safe: if the user re-submits for the same question, overwrite.
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubmitExamAnswerResponseDTO submitAnswer(SubmitExamAnswerRequestDTO request) {
        ExamResult result = findResultOrThrow(request.getResultId());

        // Guard: can only answer while exam is in progress
        if (result.getStatus() != ExamStatus.IN_PROGRESS) {
            throw new InvalidRequestException(
                    "Cannot submit answer — exam result " + request.getResultId()
                            + " is already " + result.getStatus());
        }

        // ✅ Security check — JWT user must match the result owner
        verifyOwnership(result);

        // Validate the question belongs to this exam
        Long examId = result.getExam().getId();
        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExamIdOrderByQuestionOrder(examId);

        ExamQuestion matchedEq = examQuestions.stream()
                .filter(eq -> eq.getQuestion().getId().equals(request.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(
                        "Question " + request.getQuestionId()
                                + " does not belong to exam " + examId));

        Question question = matchedEq.getQuestion();

        // Resolve selected answer entity for MC questions
        Answer selectedAnswer = null;
        if (request.getAnswerId() != null) {
            selectedAnswer = question.getAnswers().stream()
                    .filter(a -> a.getId().equals(request.getAnswerId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidRequestException(
                            "Answer " + request.getAnswerId()
                                    + " does not belong to question " + question.getId()));
        }

        // Upsert: overwrite previous answer for the same question if it exists
        ExamAnswer examAnswer = examAnswerRepository
                .findByExamResultIdAndQuestionId(result.getId(), question.getId())
                .orElseGet(ExamAnswer::new);

        examAnswer.setExamResult(result);          // ✅ userId carried via result → user
        examAnswer.setQuestion(question);
        examAnswer.setSelectedAnswer(selectedAnswer);
        examAnswer.setSubmittedText(request.getSubmittedText());
        // isCorrect deliberately left null — evaluated only during submitExam()

        ExamAnswer saved = examAnswerRepository.save(examAnswer);

        return SubmitExamAnswerResponseDTO.builder()
                .examAnswerId(saved.getId())
                .questionId(question.getId())
                .message("Answer saved.")
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. Submit Exam  (CRITICAL)
    //    userId ← exam_result.user_id — NEVER from the request.
    //    Cannot submit twice.  Score = (correct / total) * 10.
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ExamResultDTO submitExam(Long resultId) {
        // 1. Validate result exists
        ExamResult result = findResultOrThrow(resultId);

        // 2. userId from result entity — NOT from request
        // 3. Verify ownership via JWT
        verifyOwnership(result);

        // Constraint: cannot submit twice
        if (result.getStatus() == ExamStatus.SUBMITTED) {
            throw new InvalidRequestException(
                    "Exam result " + resultId + " has already been submitted.");
        }

        // 4. Load all answers for this result (single JOIN FETCH query — no N+1)
        List<ExamAnswer> answers = examAnswerRepository.findByResultIdWithDetails(resultId);

        // 5. Load the full question list to handle unanswered questions correctly
        Long examId = result.getExam().getId();
        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExamIdOrderByQuestionOrder(examId);

        // Build a lookup map: questionId → submitted ExamAnswer
        Map<Long, ExamAnswer> answerByQuestionId = answers.stream()
                .collect(Collectors.toMap(
                        ea -> ea.getQuestion().getId(),
                        ea -> ea));

        // 5. Check correctness and persist isCorrect on each answer
        int correctCount = 0;
        for (ExamQuestion eq : examQuestions) {
            Question question = eq.getQuestion();
            ExamAnswer answer = answerByQuestionId.get(question.getId());

            if (answer == null) {
                // Question was not answered — treated as incorrect, nothing to persist
                continue;
            }

            boolean isCorrect = evaluateAnswer(question, answer);
            answer.setIsCorrect(isCorrect);
            examAnswerRepository.save(answer);

            if (isCorrect) {
                correctCount++;
            }
        }

        // 6. Calculate score
        int totalQuestions = examQuestions.size();
        float score = totalQuestions > 0
                ? ((float) correctCount / totalQuestions) * 10f
                : 0f;

        // 7. Update exam_results
        result.setScore(score);
        result.setCorrectCount(correctCount);
        result.setTotalQuestions(totalQuestions);
        result.setStatus(ExamStatus.SUBMITTED);
        result.setSubmittedAt(LocalDateTime.now());
        examResultRepository.save(result);

        // 8. Update user_performance per topic for each answered question
        Long userId = result.getUser().getId();
        updatePerformancePerTopic(userId, examQuestions, answerByQuestionId);

        float accuracy = totalQuestions > 0
                ? ((float) correctCount / totalQuestions) * 100f
                : 0f;

        return ExamResultDTO.builder()
                .resultId(result.getId())
                .examId(result.getExam().getId())
                .examName(result.getExam().getName())
                .score(score)
                .correctCount(correctCount)
                .totalQuestions(totalQuestions)
                .accuracyPercent(accuracy)
                .status(ExamStatus.SUBMITTED)
                .startedAt(result.getStartedAt())
                .submittedAt(result.getSubmittedAt())
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load ExamResult with its user and exam eagerly to prevent lazy-load issues.
     */
    private ExamResult findResultOrThrow(Long resultId) {
        return examResultRepository.findByIdWithUserAndExam(resultId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exam result not found with id: " + resultId));
    }

    /**
     * Verify that the authenticated JWT user owns this exam result.
     * userId is read from the result entity — NEVER from the request.
     *
     * @throws InvalidRequestException if the JWT user does not match result owner
     */
    private void verifyOwnership(ExamResult result) {
        Long jwtUserId = SecurityUtils.getCurrentUserId();
        Long resultOwnerId = result.getUser().getId();
        if (!jwtUserId.equals(resultOwnerId)) {
            throw new InvalidRequestException(
                    "Access denied: exam result does not belong to the current user.");
        }
    }

    /**
     * Evaluate correctness of a submitted exam answer based on question type.
     *
     * <ul>
     *   <li>MULTIPLE_CHOICE / MULTIPLE_SELECT → {@code answer.is_correct} on the selected option</li>
     *   <li>FILL_BLANK / LISTENING → case-insensitive text match (with accepted alternatives)</li>
     * </ul>
     */
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

        // MULTIPLE_CHOICE / MULTIPLE_SELECT
        if (answer.getSelectedAnswer() == null) return false;
        return Boolean.TRUE.equals(answer.getSelectedAnswer().getIsCorrect());
    }

    /**
     * Map ExamQuestion → ExamQuestionDTO, stripping the {@code isCorrect} flag from answers.
     */
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
                        // isCorrect intentionally omitted
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

    /**
     * After exam submission, update (or create) a {@link UserPerformance} row for every
     * topic that appears in the exam, based on how the user answered questions in that topic.
     *
     * <p>userId is sourced from the ExamResult — NOT from any request parameter.
     */
    private void updatePerformancePerTopic(
            Long userId,
            List<ExamQuestion> examQuestions,
            Map<Long, ExamAnswer> answerByQuestionId) {

        // Group questions by topic
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

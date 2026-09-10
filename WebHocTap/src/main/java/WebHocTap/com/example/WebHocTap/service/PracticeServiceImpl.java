package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.practice.*;
import WebHocTap.com.example.WebHocTap.entity.*;
import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.PerformanceLevel;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import WebHocTap.com.example.WebHocTap.enums.RecommendationStatus;
import WebHocTap.com.example.WebHocTap.exception.BadRequestException;
import WebHocTap.com.example.WebHocTap.exception.ForbiddenException;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.repository.*;
import WebHocTap.com.example.WebHocTap.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PracticeServiceImpl implements PracticeService {

    private static final int QUESTION_LIMIT = 10;
    private static final int RECENT_ANSWERS_LIMIT = 20;
    private static final float WEAK_THRESHOLD = 0.5f;
    private static final float GOOD_THRESHOLD = 0.8f;

    private final PracticeSessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserPerformanceRepository userPerformanceRepository;
    private final UserStreakRepository userStreakRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    @Override
    @Transactional
    public StartSessionResponseDTO startSession(StartSessionRequestDTO request) {
        User currentUser = SecurityUtils.getCurrentUser();

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with id: " + request.getSubjectId()));

        Topic topic = null;
        if (request.getTopicId() != null) {
            topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Topic not found with id: " + request.getTopicId()));
        }

        PracticeSession session = new PracticeSession();
        session.setUser(currentUser);
        session.setSubject(subject);
        session.setTopic(topic);

        PracticeSession saved = sessionRepository.save(session);
        log.info("Practice session {} started by user {}", saved.getId(), currentUser.getId());
        return StartSessionResponseDTO.builder()
                .sessionId(saved.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDetailDTO getSessionDetail(Long sessionId) {
        PracticeSession session = findOwnedSession(sessionId);

        int answered = userAnswerRepository.countBySessionId(sessionId);
        int correct = userAnswerRepository.countCorrectBySessionId(sessionId);
        int totalQuestions = QUESTION_LIMIT;
        double progress = totalQuestions > 0 ? (answered * 100.0) / totalQuestions : 0.0;

        return SessionDetailDTO.builder()
                .sessionId(session.getId())
                .subjectId(session.getSubject().getId())
                .topicId(session.getTopic() != null ? session.getTopic().getId() : null)
                .totalQuestions(totalQuestions)
                .answeredCount(answered)
                .correctCount(correct)
                .progressPercent(progress)
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PracticeHistoryItemDTO> getHistory(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<PracticeHistoryItemDTO> page = sessionRepository.findByUserId(userId, pageable)
                .map(session -> {
                    int answered = userAnswerRepository.countBySessionId(session.getId());
                    int correct = userAnswerRepository.countCorrectBySessionId(session.getId());
                    double accuracy = answered > 0 ? (double) correct / answered : 0.0;
                    return PracticeHistoryItemDTO.builder()
                            .sessionId(session.getId())
                            .subjectId(session.getSubject().getId())
                            .subjectName(session.getSubject().getName())
                            .topicId(session.getTopic() != null ? session.getTopic().getId() : null)
                            .topicName(session.getTopic() != null ? session.getTopic().getName() : null)
                            .answeredCount(answered)
                            .correctCount(correct)
                            .accuracy(accuracy)
                            .startedAt(session.getStartedAt())
                            .endedAt(session.getEndedAt())
                            .build();
                });
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestions(Long sessionId) {
        PracticeSession session = findOwnedSession(sessionId);

        Long userId = session.getUser().getId();
        Long subjectId = session.getSubject().getId();
        Topic topic = session.getTopic();

        Difficulty difficulty = resolveDifficulty(userId, topic);

        List<Long> recentIds = userAnswerRepository.findRecentAnsweredQuestionIds(
                userId, PageRequest.of(0, RECENT_ANSWERS_LIMIT));
        if (recentIds.isEmpty()) {
            recentIds = Collections.singletonList(-1L);
        }

        List<Question> questions;
        if (topic != null) {
            questions = questionRepository.findByTopicAndDifficultyExcluding(
                    topic.getId(), difficulty, recentIds, PageRequest.of(0, QUESTION_LIMIT));
            if (questions.size() < QUESTION_LIMIT) {
                questions = questionRepository.findByTopicAndDifficulty(
                        topic.getId(), difficulty, PageRequest.of(0, QUESTION_LIMIT));
            }
        } else {
            questions = questionRepository.findBySubjectAndDifficultyExcluding(
                    subjectId, difficulty, recentIds, PageRequest.of(0, QUESTION_LIMIT));
            if (questions.size() < QUESTION_LIMIT) {
                questions = questionRepository.findBySubjectAndDifficulty(
                        subjectId, difficulty, PageRequest.of(0, QUESTION_LIMIT));
            }
        }

        return questions.stream().map(this::mapToQuestionDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubmitAnswerResponseDTO submitAnswer(Long sessionId, SubmitAnswerRequestDTO request) {
        PracticeSession session = findOwnedSession(sessionId);
        ensureSessionActive(session);

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + request.getQuestionId()));

        validateAnswerPayload(question.getQuestionType(), request);

        boolean isCorrect = evaluateAnswer(question, request);
        Answer selectedAnswer = resolvePrimarySelectedAnswer(question, request);

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUser(session.getUser());
        userAnswer.setSession(session);
        userAnswer.setQuestion(question);
        userAnswer.setSelectedAnswer(selectedAnswer);
        userAnswer.setSubmittedText(request.getSubmittedText());
        userAnswer.setIsCorrect(isCorrect);
        userAnswer.setTimeSpent(request.getTimeSpent());

        if (question.getQuestionType() == QuestionType.MULTIPLE_SELECT
                && request.getAnswerIds() != null) {
            for (Long answerId : request.getAnswerIds()) {
                Answer answer = findAnswerOnQuestion(question, answerId);
                UserAnswerSelection selection = new UserAnswerSelection();
                selection.setUserAnswer(userAnswer);
                selection.setAnswer(answer);
                userAnswer.getSelections().add(selection);
            }
        }

        userAnswerRepository.save(userAnswer);

        Long userId = session.getUser().getId();
        updatePerformance(userId, question.getTopic(), isCorrect);
        updateStreak(session.getUser());

        log.debug("User {} answered question {} in session {} correct={}",
                userId, question.getId(), sessionId, isCorrect);

        return SubmitAnswerResponseDTO.builder()
                .isCorrect(isCorrect)
                .explanation(question.getExplanation())
                .correctAnswerId(resolveCorrectAnswerId(question))
                .correctTextAnswer(question.getCorrectTextAnswer())
                .build();
    }

    @Override
    @Transactional
    public SessionSummaryDTO finishSession(Long sessionId) {
        PracticeSession session = findOwnedSession(sessionId);

        if (session.getEndedAt() == null) {
            session.setEndedAt(LocalDateTime.now());
            sessionRepository.save(session);
            log.info("Practice session {} finished", sessionId);
        }

        int total = userAnswerRepository.countBySessionId(sessionId);
        int correct = userAnswerRepository.countCorrectBySessionId(sessionId);
        double accuracy = total > 0 ? (double) correct / total : 0.0;

        return SessionSummaryDTO.builder()
                .sessionId(sessionId)
                .totalQuestions(total)
                .correctCount(correct)
                .accuracy(accuracy)
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .build();
    }

    @Override
    @Transactional
    public void skipQuestion(Long sessionId, SkipQuestionRequestDTO request) {
        PracticeSession session = findOwnedSession(sessionId);
        ensureSessionActive(session);

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + request.getQuestionId()));

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUser(session.getUser());
        userAnswer.setSession(session);
        userAnswer.setQuestion(question);
        userAnswer.setIsCorrect(false);
        userAnswer.setSubmittedText(null);
        userAnswerRepository.save(userAnswer);

        updatePerformance(session.getUser().getId(), question.getTopic(), false);
        log.debug("User {} skipped question {} in session {}",
                session.getUser().getId(), question.getId(), sessionId);
    }

    // ───────────────────────── helpers ─────────────────────────

    private PracticeSession findOwnedSession(Long sessionId) {
        PracticeSession session = sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Practice session not found with id: " + sessionId));
        verifyOwnership(session);
        return session;
    }

    private void verifyOwnership(PracticeSession session) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!session.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Access denied");
        }
    }

    private void ensureSessionActive(PracticeSession session) {
        if (session.getEndedAt() != null) {
            throw new BadRequestException("This practice session has already ended.");
        }
    }

    private void validateAnswerPayload(QuestionType type, SubmitAnswerRequestDTO request) {
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
            default -> {
                // MATCHING / ORDERING not graded in practice yet
            }
        }
    }

    private Difficulty resolveDifficulty(Long userId, Topic topic) {
        if (topic != null) {
            return userPerformanceRepository
                    .findByUserIdAndTopicId(userId, topic.getId())
                    .map(p -> levelToDifficulty(p.getLevel()))
                    .orElse(Difficulty.EASY);
        }

        List<UserPerformance> allPerf = userPerformanceRepository.findByUserId(userId);
        if (allPerf.isEmpty()) return Difficulty.EASY;

        boolean hasWeak = allPerf.stream().anyMatch(p -> p.getLevel() == PerformanceLevel.WEAK);
        if (hasWeak) return Difficulty.EASY;

        boolean hasMedium = allPerf.stream().anyMatch(p -> p.getLevel() == PerformanceLevel.MEDIUM);
        if (hasMedium) return Difficulty.MEDIUM;

        return Difficulty.HARD;
    }

    private Difficulty levelToDifficulty(PerformanceLevel level) {
        return switch (level) {
            case WEAK -> Difficulty.EASY;
            case MEDIUM -> Difficulty.MEDIUM;
            case GOOD -> Difficulty.HARD;
        };
    }

    private boolean evaluateAnswer(Question question, SubmitAnswerRequestDTO request) {
        QuestionType type = question.getQuestionType();

        if (type == QuestionType.FILL_BLANK || type == QuestionType.LISTENING) {
            if (request.getSubmittedText() == null) return false;
            String submitted = request.getSubmittedText().trim().toLowerCase();

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
            Set<Long> correctIds = question.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .map(Answer::getId)
                    .collect(Collectors.toSet());
            Set<Long> selectedIds = new HashSet<>(request.getAnswerIds());
            return !correctIds.isEmpty() && correctIds.equals(selectedIds);
        }

        // MULTIPLE_CHOICE
        if (request.getAnswerId() == null) return false;
        return question.getAnswers().stream()
                .anyMatch(a -> a.getId().equals(request.getAnswerId())
                        && Boolean.TRUE.equals(a.getIsCorrect()));
    }

    private Answer resolvePrimarySelectedAnswer(Question question, SubmitAnswerRequestDTO request) {
        if (request.getAnswerId() != null) {
            return findAnswerOnQuestion(question, request.getAnswerId());
        }
        if (request.getAnswerIds() != null && !request.getAnswerIds().isEmpty()) {
            return findAnswerOnQuestion(question, request.getAnswerIds().get(0));
        }
        return null;
    }

    private Answer findAnswerOnQuestion(Question question, Long answerId) {
        if (question.getAnswers() == null) {
            throw new BadRequestException("Question has no answers");
        }
        return question.getAnswers().stream()
                .filter(a -> a.getId().equals(answerId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Answer id " + answerId + " does not belong to question " + question.getId()));
    }

    private void updatePerformance(Long userId, Topic topic, boolean isCorrect) {
        UserPerformance perf = userPerformanceRepository
                .findByUserIdAndTopicId(userId, topic.getId())
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

        perf.setTotalAttempts(perf.getTotalAttempts() + 1);
        if (isCorrect) {
            perf.setCorrectAnswers(perf.getCorrectAnswers() + 1);
        }

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

        if (accuracy < WEAK_THRESHOLD) {
            Recommendation rec = new Recommendation();
            rec.setUser(userRepository.getReferenceById(userId));
            rec.setTopic(topic);
            rec.setReason(String.format("Low accuracy (%.0f%%) on topic: %s",
                    accuracy * 100, topic.getName()));
            rec.setPriority(1);
            rec.setStatus(RecommendationStatus.PENDING);
            rec.setExpiresAt(LocalDateTime.now().plusDays(7));
            recommendationRepository.save(rec);
        }
    }

    private void updateStreak(User user) {
        UserStreak streak = userStreakRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserStreak ns = new UserStreak();
                    ns.setUser(user);
                    ns.setCurrentStreak(0);
                    ns.setLongestStreak(0);
                    return ns;
                });

        LocalDate today = LocalDate.now();
        LocalDate last = streak.getLastPracticedDate();

        if (last == null) {
            streak.setCurrentStreak(1);
        } else if (last.equals(today)) {
            return;
        } else if (last.equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1);
        }

        streak.setLastPracticedDate(today);
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        userStreakRepository.save(streak);
    }

    private QuestionDTO mapToQuestionDTO(Question question) {
        List<AnswerDTO> answerDTOs = question.getAnswers() == null
                ? Collections.emptyList()
                : question.getAnswers().stream()
                .map(a -> AnswerDTO.builder()
                        .id(a.getId())
                        .content(a.getContent())
                        .imageUrl(a.getImageUrl())
                        .audioUrl(a.getAudioUrl())
                        .build())
                .collect(Collectors.toList());

        return QuestionDTO.builder()
                .id(question.getId())
                .content(question.getContent())
                .questionType(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .imageUrl(question.getImageUrl())
                .audioUrl(question.getAudioUrl())
                .topicId(question.getTopic().getId())
                .topicName(question.getTopic().getName())
                .answers(answerDTOs)
                .build();
    }

    private Long resolveCorrectAnswerId(Question question) {
        if (question.getAnswers() == null) return null;
        return question.getAnswers().stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .map(Answer::getId)
                .findFirst()
                .orElse(null);
    }
}

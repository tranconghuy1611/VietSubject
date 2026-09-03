package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.practice.*;
import WebHocTap.com.example.WebHocTap.entity.*;
import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.PerformanceLevel;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import WebHocTap.com.example.WebHocTap.enums.RecommendationStatus;
import WebHocTap.com.example.WebHocTap.exception.InvalidRequestException;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.repository.*;
import WebHocTap.com.example.WebHocTap.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // ──────────────────────────────────────────────────────────────────────────
    // 1. Start Session
    // userId is extracted from the JWT via SecurityContextHolder — NOT from the
    // request body.
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StartSessionResponseDTO startSession(StartSessionRequestDTO request) {
        // ✅ Extract user from JWT — never trust userId from request body
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
        return StartSessionResponseDTO.builder()
                .sessionId(saved.getId())
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. Generate Questions
    // userId comes from the persisted session entity — NOT from the request.
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestions(Long sessionId) {
        PracticeSession session = findSessionOrThrow(sessionId);

        // ✅ userId from session entity — never from the request
        Long userId = session.getUser().getId();
        Long subjectId = session.getSubject().getId();
        Topic topic = session.getTopic();

        Difficulty difficulty = resolveDifficulty(userId, topic);

        // IDs of questions answered in the last 20 attempts by this user
        List<Long> recentIds = userAnswerRepository.findRecentAnsweredQuestionIds(
                userId, PageRequest.of(0, RECENT_ANSWERS_LIMIT));
        if (recentIds.isEmpty()) {
            recentIds = Collections.singletonList(-1L); // avoid empty IN clause
        }

        List<Question> questions;
        if (topic != null) {
            questions = questionRepository.findByTopicAndDifficultyExcluding(
                    topic.getId(), difficulty, recentIds, PageRequest.of(0, QUESTION_LIMIT));
            // Fallback: drop exclusion if not enough results
            if (questions.size() < QUESTION_LIMIT) {
                questions = questionRepository.findByTopicAndDifficulty(
                        topic.getId(), difficulty, PageRequest.of(0, QUESTION_LIMIT));
            }
        } else {
            // Mixed topics — query by subject
            questions = questionRepository.findBySubjectAndDifficultyExcluding(
                    subjectId, difficulty, recentIds, PageRequest.of(0, QUESTION_LIMIT));
            if (questions.size() < QUESTION_LIMIT) {
                questions = questionRepository.findBySubjectAndDifficulty(
                        subjectId, difficulty, PageRequest.of(0, QUESTION_LIMIT));
            }
        }

        return questions.stream()
                .map(this::mapToQuestionDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. Submit Answer
    // userId comes from session.getUser() — NOT from the request body.
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SubmitAnswerResponseDTO submitAnswer(SubmitAnswerRequestDTO request) {
        PracticeSession session = findSessionOrThrow(request.getSessionId());
        if (session.getEndedAt() != null) {
            throw new InvalidRequestException("This practice session has already ended.");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + request.getQuestionId()));

        // ── Determine correctness ──
        boolean isCorrect = evaluateAnswer(question, request);

        // ── Resolve the selected Answer entity (if MC) ──
        Answer selectedAnswer = null;
        if (request.getAnswerId() != null) {
            selectedAnswer = question.getAnswers().stream()
                    .filter(a -> a.getId().equals(request.getAnswerId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidRequestException(
                            "Answer id " + request.getAnswerId()
                                    + " does not belong to question " + question.getId()));
        }

        // ── Persist UserAnswer (userId from session, not request) ──
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setUser(session.getUser());   // ✅ from session entity
        userAnswer.setSession(session);
        userAnswer.setQuestion(question);
        userAnswer.setSelectedAnswer(selectedAnswer);
        userAnswer.setSubmittedText(request.getSubmittedText());
        userAnswer.setIsCorrect(isCorrect);
        userAnswer.setTimeSpent(request.getTimeSpent());
        userAnswerRepository.save(userAnswer);

        // ── Update UserPerformance ──
        Long userId = session.getUser().getId();
        updatePerformance(userId, question.getTopic(), isCorrect);

        // ── Update practice streak ──
        updateStreak(session.getUser());

        // ── Build response ──
        return SubmitAnswerResponseDTO.builder()
                .isCorrect(isCorrect)
                .explanation(question.getExplanation())
                .correctAnswerId(resolveCorrectAnswerId(question))
                .correctTextAnswer(question.getCorrectTextAnswer())
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. Finish Session
    // userId ownership is validated implicitly — session entity holds user.
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SessionSummaryDTO finishSession(Long sessionId) {
        PracticeSession session = findSessionOrThrow(sessionId);

        // Idempotent — only set endedAt once
        if (session.getEndedAt() == null) {
            session.setEndedAt(LocalDateTime.now());
            sessionRepository.save(session);
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

    // ──────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────────────

    private PracticeSession findSessionOrThrow(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Practice session not found with id: " + sessionId));
    }

    /**
     * Determine target question difficulty based on the user's performance level.
     * <ul>
     *   <li>Single topic  → look up that topic's performance record</li>
     *   <li>Mixed topics  → pick the difficulty for the weakest topic</li>
     *   <li>No record yet → default to EASY (new user / new topic)</li>
     * </ul>
     */
    private Difficulty resolveDifficulty(Long userId, Topic topic) {
        if (topic != null) {
            return userPerformanceRepository
                    .findByUserIdAndTopicId(userId, topic.getId())
                    .map(p -> levelToDifficulty(p.getLevel()))
                    .orElse(Difficulty.EASY); // no record → start easy
        }

        // Mixed mode — pick weakest difficulty across all topics
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
            case WEAK   -> Difficulty.EASY;
            case MEDIUM -> Difficulty.MEDIUM;
            case GOOD   -> Difficulty.HARD;
        };
    }

    /**
     * Evaluate correctness based on question type:
     * <ul>
     *   <li>MULTIPLE_CHOICE / MULTIPLE_SELECT → {@code answer.is_correct}</li>
     *   <li>FILL_BLANK / LISTENING → case-insensitive text match (with accepted alternatives)</li>
     * </ul>
     */
    private boolean evaluateAnswer(Question question, SubmitAnswerRequestDTO request) {
        QuestionType type = question.getQuestionType();

        if (type == QuestionType.FILL_BLANK || type == QuestionType.LISTENING) {
            if (request.getSubmittedText() == null) return false;
            String submitted = request.getSubmittedText().trim().toLowerCase();

            if (question.getCorrectTextAnswer() != null
                    && submitted.equals(question.getCorrectTextAnswer().trim().toLowerCase())) {
                return true;
            }
            // Check comma-separated accepted alternatives
            if (question.getAcceptedTextAnswers() != null) {
                for (String alt : question.getAcceptedTextAnswers().split(",")) {
                    if (submitted.equals(alt.trim().toLowerCase())) return true;
                }
            }
            return false;
        }

        // MULTIPLE_CHOICE / MULTIPLE_SELECT
        if (request.getAnswerId() == null) return false;
        return question.getAnswers().stream()
                .anyMatch(a -> a.getId().equals(request.getAnswerId())
                        && Boolean.TRUE.equals(a.getIsCorrect()));
    }

    /**
     * Find or create a {@link UserPerformance} record, then update stats and level.
     * Also triggers a {@link Recommendation} if accuracy drops below {@value #WEAK_THRESHOLD}.
     */
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

        // Insert recommendation when user is struggling
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

    /**
     * Update the user's practice streak:
     * <ul>
     *   <li>Never practiced → streak = 1</li>
     *   <li>Practiced yesterday → streak + 1</li>
     *   <li>Practiced today → no change (idempotent)</li>
     *   <li>Gap in days → reset to 1</li>
     * </ul>
     */
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
            // Already practiced today — nothing to do
            return;
        } else if (last.equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            // Streak broken
            streak.setCurrentStreak(1);
        }

        streak.setLastPracticedDate(today);
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        userStreakRepository.save(streak);
    }

    /** Map Question entity → QuestionDTO, omitting the {@code isCorrect} flag from answers. */
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

    /** Returns the id of the first correct answer for MC questions, null otherwise. */
    private Long resolveCorrectAnswerId(Question question) {
        if (question.getAnswers() == null) return null;
        return question.getAnswers().stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .map(Answer::getId)
                .findFirst()
                .orElse(null);
    }
}

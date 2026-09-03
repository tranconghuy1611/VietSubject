package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.practice.*;

import java.util.List;

public interface PracticeService {

    /**
     * Start a new practice session.
     * userId is resolved internally from Spring SecurityContextHolder — never passed by the caller.
     */
    StartSessionResponseDTO startSession(StartSessionRequestDTO request);

    /**
     * Generate up to 10 adaptive questions for the session.
     * userId is retrieved from the persisted session entity — never trusted from the request.
     */
    List<QuestionDTO> getQuestions(Long sessionId);

    /**
     * Submit a single answer and receive correctness + explanation.
     * userId is retrieved from the session entity identified by sessionId.
     */
    SubmitAnswerResponseDTO submitAnswer(SubmitAnswerRequestDTO request);

    /**
     * Finish the session and return a performance summary.
     * userId is retrieved from the session entity.
     */
    SessionSummaryDTO finishSession(Long sessionId);
}


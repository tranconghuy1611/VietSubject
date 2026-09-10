package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.practice.*;

import java.util.List;

public interface PracticeService {


    StartSessionResponseDTO startSession(StartSessionRequestDTO request);

    List<QuestionDTO> getQuestions(Long sessionId);
    SessionDetailDTO getSession(Long sessionId);
    List<PracticeHistoryDTO> getHistory();

    SubmitAnswerResponseDTO submitAnswer(SubmitAnswerRequestDTO request);

    SessionSummaryDTO finishSession(Long sessionId);
}


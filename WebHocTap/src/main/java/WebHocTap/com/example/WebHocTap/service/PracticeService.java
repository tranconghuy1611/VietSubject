package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.practice.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PracticeService {

    StartSessionResponseDTO startSession(StartSessionRequestDTO request);

    SessionDetailDTO getSessionDetail(Long sessionId);

    PageResponse<PracticeHistoryItemDTO> getHistory(Pageable pageable);

    List<QuestionDTO> getQuestions(Long sessionId);

    SubmitAnswerResponseDTO submitAnswer(Long sessionId, SubmitAnswerRequestDTO request);

    SessionSummaryDTO finishSession(Long sessionId);

    void skipQuestion(Long sessionId, SkipQuestionRequestDTO request);
}

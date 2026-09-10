package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.exam.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExamService {

    ExamStartResponseDTO startExam(Long examId);

    PageResponse<ExamResponseDTO> listExams(Pageable pageable);

    ExamResponseDTO getExamById(Long examId);

    List<ExamQuestionDTO> getQuestions(Long resultId);

    SubmitExamAnswerResponseDTO submitAnswer(Long resultId, SubmitExamAnswerRequestDTO request);

    ExamResultDTO submitExam(Long resultId);

    PageResponse<ExamResultDTO> getMyResults(Pageable pageable);

    ExamResultDTO getResultDetail(Long resultId);

    ExamSummaryDTO getResultSummary(Long resultId);
}

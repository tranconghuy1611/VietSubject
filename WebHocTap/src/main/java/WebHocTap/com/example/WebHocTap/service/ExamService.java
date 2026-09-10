package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.exam.*;

import java.util.List;

/**
 * Contract for the full exam flow.
 *
 * <p><strong>Security invariant:</strong> userId is NEVER accepted as a parameter.
 * It is always derived from either the JWT (via {@code SecurityUtils}) or the
 * persisted {@code exam_result} row.
 */
public interface ExamService {

    /** Start an exam, create an exam_result row, return its id. */
    ExamStartResponseDTO startExam(Long examId);

    /** Return ordered questions (without correct flags) for an active result. */
    List<ExamQuestionDTO> getQuestions(Long resultId);
    List<ExamListDTO> getAllExams();
    List<ExamResultDTO> getMyResults();
    ExamResultDTO getResultDetail(Long resultId);
    List<ExamReviewDTO> reviewExam(Long resultId);
    /** Save (or update) a single answer for an active result. */
    SubmitExamAnswerResponseDTO submitAnswer(SubmitExamAnswerRequestDTO request);

    /** Grade all answers, persist the score, mark result as SUBMITTED. */
    ExamResultDTO submitExam(Long resultId);
}

package WebHocTap.com.example.WebHocTap.dto.exam;

import WebHocTap.com.example.WebHocTap.enums.ExamStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Full exam result returned after the exam is submitted.
 */
@Data
@Builder
public class ExamResultDTO {
    private Long resultId;
    private Long examId;
    private String examName;

    private Float score;            // 0.0 – 10.0
    private Integer correctCount;
    private Integer totalQuestions;
    private Float accuracyPercent;  // (correctCount / totalQuestions) * 100

    private ExamStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}

package WebHocTap.com.example.WebHocTap.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamSummaryDTO {
    private Long resultId;
    private Integer totalQuestions;
    private Integer correctCount;
    private Float score;
    private Long timeSpentSeconds;
}

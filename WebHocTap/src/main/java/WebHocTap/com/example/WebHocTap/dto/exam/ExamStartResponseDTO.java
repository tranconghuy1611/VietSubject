package WebHocTap.com.example.WebHocTap.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamStartResponseDTO {
    private Long resultId;
    private Long examId;
    private String examName;
    private Integer durationMinutes;
    private Integer totalQuestions;
}

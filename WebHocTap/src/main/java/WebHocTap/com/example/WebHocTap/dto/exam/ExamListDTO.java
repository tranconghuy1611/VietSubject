package WebHocTap.com.example.WebHocTap.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamListDTO {
    private Long id;
    private String name;
    private Integer durationMinutes;
    private Integer totalQuestions;
}

package WebHocTap.com.example.WebHocTap.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamResponseDTO {
    private Long id;
    private String name;
    private Long subjectId;
    private String subjectName;
    private Long gradeId;
    private String gradeName;
    private Integer durationMinutes;
    private Integer totalQuestions;
}

package WebHocTap.com.example.WebHocTap.dto.grade;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradeResponse {
    private Long id;
    private String name;
    private Integer level;
}

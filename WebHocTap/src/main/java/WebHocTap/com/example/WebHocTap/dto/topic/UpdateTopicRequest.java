package WebHocTap.com.example.WebHocTap.dto.topic;

import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import lombok.Data;

@Data
public class UpdateTopicRequest {
    private String name;
    private Long subjectId;
    private Long gradeId;
    private Difficulty difficulty;
    private Boolean isActive;
}

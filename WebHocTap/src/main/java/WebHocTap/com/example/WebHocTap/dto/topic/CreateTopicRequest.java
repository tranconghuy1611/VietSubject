package WebHocTap.com.example.WebHocTap.dto.topic;

import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import lombok.Data;

@Data
public class CreateTopicRequest {
    private String name;
    private Long subjectId;
    private Long gradeId;
    private Difficulty difficulty;
}

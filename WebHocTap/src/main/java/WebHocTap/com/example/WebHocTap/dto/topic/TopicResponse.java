package WebHocTap.com.example.WebHocTap.dto.topic;

import WebHocTap.com.example.WebHocTap.dto.grade.GradeResponse;
import WebHocTap.com.example.WebHocTap.dto.subject.SubjectResponse;
import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicResponse {
    private Long id;
    private String name;
    private Difficulty difficulty;
    private Boolean isActive;
    private SubjectResponse subject;
    private GradeResponse grade;
}

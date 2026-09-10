package WebHocTap.com.example.WebHocTap.dto.practice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartSessionRequestDTO {
    @NotNull
    private Long subjectId;
    private Long topicId; // nullable — null means mix all topics in the subject
}

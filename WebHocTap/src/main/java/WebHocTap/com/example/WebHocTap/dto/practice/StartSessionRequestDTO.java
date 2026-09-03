package WebHocTap.com.example.WebHocTap.dto.practice;

import lombok.Data;

/**
 * Request body for POST /api/practice/start.
 * userId is intentionally absent — it is extracted from the JWT by the service layer.
 */
@Data
public class StartSessionRequestDTO {
    private Long subjectId;
    private Long topicId; // nullable — null means "mix all topics in the subject"
}

package WebHocTap.com.example.WebHocTap.dto.practice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SessionDetailDTO {
    private Long sessionId;
    private String subjectName;
    private String topicName;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
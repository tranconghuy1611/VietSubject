package WebHocTap.com.example.WebHocTap.dto.practice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SessionDetailDTO {
    private Long sessionId;
    private Long subjectId;
    private Long topicId;
    private Integer totalQuestions;
    private Integer answeredCount;
    private Integer correctCount;
    private Double progressPercent;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}

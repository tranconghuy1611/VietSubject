package WebHocTap.com.example.WebHocTap.dto.practice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PracticeHistoryItemDTO {
    private Long sessionId;
    private Long subjectId;
    private String subjectName;
    private Long topicId;
    private String topicName;
    private Integer answeredCount;
    private Integer correctCount;
    private Double accuracy;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}

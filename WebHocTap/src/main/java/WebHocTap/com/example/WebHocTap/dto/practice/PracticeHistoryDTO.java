package WebHocTap.com.example.WebHocTap.dto.practice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PracticeHistoryDTO {
    private Long sessionId;
    private String subjectName;
    private String topicName;
    private int totalQuestions;
    private int correctCount;
    private double accuracy;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
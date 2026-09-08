package WebHocTap.com.example.WebHocTap.dto.exam;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a single answer choice for an exam question.
 * The {@code isCorrect} flag is intentionally omitted — it must never be sent to the client.
 */
@Data
@Builder
public class ExamAnswerDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private String audioUrl;
}

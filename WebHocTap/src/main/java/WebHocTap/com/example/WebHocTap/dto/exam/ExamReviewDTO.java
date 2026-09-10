package WebHocTap.com.example.WebHocTap.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamReviewDTO {
    private Long questionId;
    private String questionContent;

    private String yourAnswer;
    private String correctAnswer;

    private Boolean isCorrect;
}

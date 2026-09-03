package WebHocTap.com.example.WebHocTap.dto.practice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitAnswerResponseDTO {
    private boolean isCorrect;
    private String explanation;
    private Long correctAnswerId;       // for MULTIPLE_CHOICE
    private String correctTextAnswer;   // for FILL_BLANK
}

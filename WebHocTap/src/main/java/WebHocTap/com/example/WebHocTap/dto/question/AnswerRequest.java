package WebHocTap.com.example.WebHocTap.dto.question;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotBlank
    private String content;
    private String imageUrl;
    private Boolean isCorrect;
}

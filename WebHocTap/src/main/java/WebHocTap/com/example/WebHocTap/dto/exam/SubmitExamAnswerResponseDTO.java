package WebHocTap.com.example.WebHocTap.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitExamAnswerResponseDTO {
    private Long examAnswerId;
    private Long questionId;
    private String message;
}

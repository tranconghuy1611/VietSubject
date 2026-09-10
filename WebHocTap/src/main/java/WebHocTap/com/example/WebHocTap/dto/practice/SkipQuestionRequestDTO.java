package WebHocTap.com.example.WebHocTap.dto.practice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkipQuestionRequestDTO {
    @NotNull
    private Long questionId;
}

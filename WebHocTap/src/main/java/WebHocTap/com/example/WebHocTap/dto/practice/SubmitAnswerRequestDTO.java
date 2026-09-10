package WebHocTap.com.example.WebHocTap.dto.practice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswerRequestDTO {
    @NotNull
    private Long questionId;

    /** Single selection — MULTIPLE_CHOICE */
    private Long answerId;

    /** Multi selection — MULTIPLE_SELECT */
    private List<Long> answerIds;

    /** FILL_BLANK / LISTENING */
    private String submittedText;

    private Integer timeSpent;
}

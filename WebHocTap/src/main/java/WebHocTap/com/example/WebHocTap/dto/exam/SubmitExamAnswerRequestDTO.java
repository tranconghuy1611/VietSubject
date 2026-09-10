package WebHocTap.com.example.WebHocTap.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitExamAnswerRequestDTO {
    @NotNull
    private Long questionId;

    /** MULTIPLE_CHOICE */
    private Long answerId;

    /** MULTIPLE_SELECT */
    private List<Long> answerIds;

    /** FILL_BLANK / LISTENING */
    private String submittedText;
}

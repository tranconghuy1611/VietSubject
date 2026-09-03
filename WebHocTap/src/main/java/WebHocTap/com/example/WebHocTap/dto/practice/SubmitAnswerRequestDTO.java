package WebHocTap.com.example.WebHocTap.dto.practice;

import lombok.Data;

@Data
public class SubmitAnswerRequestDTO {
    private Long sessionId;
    private Long questionId;
    private Long answerId;       // for MULTIPLE_CHOICE / MULTIPLE_SELECT
    private String submittedText; // for FILL_BLANK / LISTENING
    private Integer timeSpent;   // seconds spent on this question
}

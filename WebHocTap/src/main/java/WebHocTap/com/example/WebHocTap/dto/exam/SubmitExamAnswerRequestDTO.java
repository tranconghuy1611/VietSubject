package WebHocTap.com.example.WebHocTap.dto.exam;

import lombok.Data;

/**
 * Request body for submitting a single exam answer.
 *
 * <p>Exactly one of {@code answerId} or {@code submittedText} should be provided,
 * depending on the question type:
 * <ul>
 *   <li>MULTIPLE_CHOICE → provide {@code answerId}</li>
 *   <li>FILL_BLANK / LISTENING → provide {@code submittedText}</li>
 * </ul>
 *
 * <p><strong>Security note:</strong> userId is NEVER read from this DTO.
 * It is resolved from the exam_result row, which was created with the authenticated user's id.
 */
@Data
public class SubmitExamAnswerRequestDTO {
    private Long resultId;
    private Long questionId;
    private Long answerId;         // for MULTIPLE_CHOICE / MULTIPLE_SELECT
    private String submittedText;  // for FILL_BLANK / LISTENING
}

package WebHocTap.com.example.WebHocTap.dto.exam;

import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Question projection returned to the client during an active exam.
 * No correct-answer flags are exposed.
 */
@Data
@Builder
public class ExamQuestionDTO {
    private Long questionId;
    private Integer questionOrder;
    private String content;
    private QuestionType questionType;
    private Difficulty difficulty;
    private String imageUrl;
    private String audioUrl;
    private List<ExamAnswerDTO> answers;
}

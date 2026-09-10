package WebHocTap.com.example.WebHocTap.dto.question;

import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateQuestionRequest {
    private Long topicId;

    @Size(max = 5000)
    private String content;

    private QuestionType questionType;
    private Difficulty difficulty;
    private String imageUrl;
    private String audioUrl;
    private String correctTextAnswer;
    private String acceptedTextAnswers;
    private String explanation;
    private Boolean isActive;

    @Valid
    private List<AnswerRequest> answers;
}

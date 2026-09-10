package WebHocTap.com.example.WebHocTap.dto.question;

import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionResponse {
    private Long id;
    private Long topicId;
    private String content;
    private QuestionType type;
    private Difficulty difficulty;
    private String imageUrl;
    private String audioUrl;
    private List<AnswerResponse> answers;
}

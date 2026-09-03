package WebHocTap.com.example.WebHocTap.dto.practice;

import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionDTO {
    private Long id;
    private String content;
    private QuestionType questionType;
    private Difficulty difficulty;
    private String imageUrl;
    private String audioUrl;
    private Long topicId;
    private String topicName;
    private List<AnswerDTO> answers;
}

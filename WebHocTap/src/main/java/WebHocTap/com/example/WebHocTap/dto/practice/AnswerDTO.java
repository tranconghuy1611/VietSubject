package WebHocTap.com.example.WebHocTap.dto.practice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private String audioUrl;
}

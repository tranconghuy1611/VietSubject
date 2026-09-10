package WebHocTap.com.example.WebHocTap.dto.question;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerResponse {
    private Long id;
    private String content;
    private String imageUrl;
}

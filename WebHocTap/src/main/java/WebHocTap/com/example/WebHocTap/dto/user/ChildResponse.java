package WebHocTap.com.example.WebHocTap.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChildResponse {
    private Long id;
    private String fullName;
    private String username;
    private String avatarUrl;
}

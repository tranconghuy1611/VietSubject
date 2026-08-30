package WebHocTap.com.example.WebHocTap.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String fullName;
    private String role;
}

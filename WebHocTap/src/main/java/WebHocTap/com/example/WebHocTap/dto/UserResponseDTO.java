package WebHocTap.com.example.WebHocTap.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String fullName;
    private String role;
    private Boolean isActive;
    private String avatarUrl;
    private LocalDateTime createdAt;
}

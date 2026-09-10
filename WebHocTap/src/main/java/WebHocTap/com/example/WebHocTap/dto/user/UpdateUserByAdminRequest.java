package WebHocTap.com.example.WebHocTap.dto.user;

import WebHocTap.com.example.WebHocTap.enums.Role;
import lombok.Data;

@Data
public class UpdateUserByAdminRequest {
    private String fullName;
    private Role role;
    private Boolean isActive;
}

package WebHocTap.com.example.WebHocTap.mapper;

import WebHocTap.com.example.WebHocTap.dto.UserResponseDTO;
import WebHocTap.com.example.WebHocTap.dto.user.ChildResponse;
import WebHocTap.com.example.WebHocTap.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserMapper {

    public UserResponseDTO toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(resolveDisplayUsername(user))
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .isActive(user.getIsActive())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public ChildResponse toChildResponse(User child) {
        if (child == null) {
            return null;
        }
        return ChildResponse.builder()
                .id(child.getId())
                .fullName(child.getFullName())
                .username(resolveDisplayUsername(child))
                .avatarUrl(child.getAvatarUrl())
                .build();
    }

    /**
     * Returns the account username column when present; otherwise email.
     * Note: {@link User#getUsername()} is overridden for Spring Security and may return email.
     */
    private String resolveDisplayUsername(User user) {
        if (StringUtils.hasText(user.getAccountUsername())) {
            return user.getAccountUsername();
        }
        return user.getEmail();
    }
}

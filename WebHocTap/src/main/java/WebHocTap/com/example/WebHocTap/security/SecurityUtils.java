package WebHocTap.com.example.WebHocTap.security;

import WebHocTap.com.example.WebHocTap.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility to extract the currently-authenticated {@link User} from the
 * Spring {@link SecurityContextHolder}.
 *
 * <p>The JWT filter sets a {@link CustomUserDetails} instance as the principal,
 * so we can safely cast and call {@code getUser()} to obtain the full User entity.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the {@link User} entity of the currently authenticated principal.
     *
     * @throws IllegalStateException if there is no authenticated user in the context
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in SecurityContext.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }
        throw new IllegalStateException(
                "Unexpected principal type: " + principal.getClass().getName());
    }

    /**
     * Convenience method — returns only the userId of the current user.
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}

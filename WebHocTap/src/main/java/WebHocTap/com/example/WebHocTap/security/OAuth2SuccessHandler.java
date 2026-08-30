package WebHocTap.com.example.WebHocTap.security;

import WebHocTap.com.example.WebHocTap.entity.User;
import WebHocTap.com.example.WebHocTap.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 🔎 check user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(name);
                    newUser.setRole("USER"); // hoặc enum của bạn
                    newUser.setProvider("GOOGLE"); // nếu có field
                    return userRepository.save(newUser);
                });

        // 🔐 tạo JWT
        String accessToken = jwtService.generateToken(user);

        // 🚀 redirect về frontend
        response.sendRedirect("http://localhost:5173/login-success?token=" + accessToken);
    }
}
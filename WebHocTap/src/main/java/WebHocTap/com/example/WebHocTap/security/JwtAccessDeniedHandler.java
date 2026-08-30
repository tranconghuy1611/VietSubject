package WebHocTap.com.example.WebHocTap.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import WebHocTap.com.example.WebHocTap.dto.ApiResponse;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .status(HttpServletResponse.SC_FORBIDDEN)
                .message("Forbidden: Không có quyền truy cập")
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), apiResponse);
    }
}

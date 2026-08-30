package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.*;
import WebHocTap.com.example.WebHocTap.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponseDTO>builder()
                .status(200).message("Đăng ký thành công")
                .data(authService.register(request))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponseDTO>builder()
                .status(200).message("Đăng nhập thành công")
                .data(authService.login(request))
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken(@RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponseDTO>builder()
                .status(200).message("Refresh token thành công")
                .data(authService.refreshToken(request))
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequestDTO request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200).message("Đăng xuất thành công")
                .build());
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getMe(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .status(200).message("Lấy thông tin thành công")
                .data(authService.getMe(authentication.getName()))
                .build());
    }
}

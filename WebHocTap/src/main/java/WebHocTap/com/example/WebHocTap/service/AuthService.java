package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.*;
import WebHocTap.com.example.WebHocTap.entity.RefreshToken;
import WebHocTap.com.example.WebHocTap.entity.User;
import WebHocTap.com.example.WebHocTap.enums.Role;
import WebHocTap.com.example.WebHocTap.repository.RefreshTokenRepository;
import WebHocTap.com.example.WebHocTap.repository.UserRepository;
import WebHocTap.com.example.WebHocTap.security.CustomUserDetails;
import WebHocTap.com.example.WebHocTap.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Role.STUDENT);
        user.setIsActive(true);
        userRepository.save(user);

        return generateTokens(user);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        
        return generateTokens(user);
    }

    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));

        if (refreshToken.getIsRevoked() || refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token đã hết hạn hoặc bị thu hồi");
        }

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(mapToUserResponse(user))
                .build();
    }

    public void logout(RefreshTokenRequestDTO request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
    
    public UserResponseDTO getMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        return mapToUserResponse(user);
    }

    private AuthResponseDTO generateTokens(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshTokenString = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .user(mapToUserResponse(user))
                .build();
    }
    
    private UserResponseDTO mapToUserResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getAccountUsername() != null ? user.getAccountUsername() : user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

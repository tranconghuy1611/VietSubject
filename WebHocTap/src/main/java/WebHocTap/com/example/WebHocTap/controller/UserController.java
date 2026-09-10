package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.AvatarUploadResponse;
import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.UserResponseDTO;
import WebHocTap.com.example.WebHocTap.dto.user.UpdateUserByAdminRequest;
import WebHocTap.com.example.WebHocTap.dto.user.UpdateUserRequest;
import WebHocTap.com.example.WebHocTap.enums.Role;
import WebHocTap.com.example.WebHocTap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ───────────────────────── Self (/me) ─────────────────────────

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getMe() {
        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(userService.getCurrentUserProfile())
                .build());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateMe(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Profile updated successfully")
                .data(userService.updateCurrentUserProfile(request))
                .build());
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AvatarUploadResponse>> updateAvatar(
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(ApiResponse.<AvatarUploadResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Upload avatar successfully")
                .data(userService.updateAvatar(file))
                .build());
    }

    // ───────────────────────── Admin ─────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDTO>>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(userService.getUsers(role, isActive, pageable))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(userService.getUserById(id))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUserByAdmin(
            @PathVariable Long id,
            @RequestBody UpdateUserByAdminRequest request) {

        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("User updated successfully")
                .data(userService.updateUserByAdmin(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("User deleted successfully")
                .build());
    }
}

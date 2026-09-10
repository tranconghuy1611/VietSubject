package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.AvatarUploadResponse;
import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.UserResponseDTO;
import WebHocTap.com.example.WebHocTap.dto.user.UpdateUserByAdminRequest;
import WebHocTap.com.example.WebHocTap.dto.user.UpdateUserRequest;
import WebHocTap.com.example.WebHocTap.enums.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponseDTO getCurrentUserProfile();

    UserResponseDTO updateCurrentUserProfile(UpdateUserRequest request);

    AvatarUploadResponse updateAvatar(MultipartFile file);

    PageResponse<UserResponseDTO> getUsers(Role role, Boolean isActive, Pageable pageable);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO updateUserByAdmin(Long id, UpdateUserByAdminRequest request);

    void softDeleteUser(Long id);
}

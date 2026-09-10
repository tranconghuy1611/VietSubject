package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.AvatarUploadResponse;
import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.UserResponseDTO;
import WebHocTap.com.example.WebHocTap.dto.user.UpdateUserByAdminRequest;
import WebHocTap.com.example.WebHocTap.dto.user.UpdateUserRequest;
import WebHocTap.com.example.WebHocTap.entity.User;
import WebHocTap.com.example.WebHocTap.enums.Role;
import WebHocTap.com.example.WebHocTap.exception.BadRequestException;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.mapper.UserMapper;
import WebHocTap.com.example.WebHocTap.repository.UserRepository;
import WebHocTap.com.example.WebHocTap.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUserProfile() {
        User user = loadCurrentUser();
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateCurrentUserProfile(UpdateUserRequest request) {
        User user = loadCurrentUser();
        ensureActive(user);

        if (request == null || !StringUtils.hasText(request.getFullName())) {
            throw new BadRequestException("fullName is required");
        }

        user.setFullName(request.getFullName().trim());
        User saved = userRepository.save(user);
        log.info("Profile updated for userId={}", saved.getId());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AvatarUploadResponse updateAvatar(MultipartFile file) {
        User user = loadCurrentUser();
        ensureActive(user);

        String oldPublicId = user.getAvatarPublicId();
        CloudinaryService.ImageUploadResult uploaded = cloudinaryService.uploadImageWithResult(file);

        user.setAvatarUrl(uploaded.url());
        user.setAvatarPublicId(uploaded.publicId());
        userRepository.save(user);

        if (oldPublicId != null && !oldPublicId.equals(uploaded.publicId())) {
            cloudinaryService.deleteImage(oldPublicId);
        }

        log.info("Avatar updated for userId={}", user.getId());
        return AvatarUploadResponse.builder()
                .avatarUrl(uploaded.url())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDTO> getUsers(Role role, Boolean isActive, Pageable pageable) {
        log.debug("Admin listing users role={}, isActive={}, page={}", role, isActive, pageable.getPageNumber());
        Page<UserResponseDTO> page = userRepository.findAllFiltered(role, isActive, pageable)
                .map(userMapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        return userMapper.toResponse(findUserOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserByAdmin(Long id, UpdateUserByAdminRequest request) {
        if (request == null) {
            throw new BadRequestException("Update request body is required");
        }

        User user = findUserOrThrow(id);

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        } else if (request.getFullName() != null) {
            throw new BadRequestException("fullName must not be blank");
        }

        if (request.getRole() != null) {
            validateRole(request.getRole());
            user.setRole(request.getRole());
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        User saved = userRepository.save(user);
        log.info("Admin updated userId={} by adminId={}", saved.getId(), SecurityUtils.getCurrentUserId());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(id)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        User user = findUserOrThrow(id);
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BadRequestException("User is already inactive");
        }

        user.setIsActive(false);
        userRepository.save(user);
        log.info("Admin soft-deleted userId={} by adminId={}", id, currentUserId);
    }

    private User loadCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void ensureActive(User user) {
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BadRequestException("Cannot update inactive user");
        }
    }

    private void validateRole(Role role) {
        // Enum deserialization already restricts values; guard for null-safe completeness
        if (role != Role.STUDENT && role != Role.PARENT && role != Role.ADMIN) {
            throw new BadRequestException("Invalid role: " + role);
        }
    }
}

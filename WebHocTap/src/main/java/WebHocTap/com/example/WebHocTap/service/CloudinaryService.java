package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.exception.BadRequestException;
import WebHocTap.com.example.WebHocTap.exception.CloudinaryUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:vietsubject/avatars}")
    private String folder;

    /**
     * Uploads an image to Cloudinary and returns the secure URL.
     */
    public String uploadImage(MultipartFile file) {
        return uploadImageWithResult(file).url();
    }

    /**
     * Uploads an image and returns both secure URL and public_id
     * (needed to delete the previous avatar later).
     */
    public ImageUploadResult uploadImageWithResult(MultipartFile file) {
        validateFile(file);

        String publicId = folder + "/avatar_" + UUID.randomUUID();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );

            String secureUrl = (String) result.get("secure_url");
            String returnedPublicId = (String) result.get("public_id");

            if (!StringUtils.hasText(secureUrl)) {
                throw new CloudinaryUploadException("Cloudinary did not return a secure_url");
            }

            log.info("Uploaded image to Cloudinary publicId={}", returnedPublicId);
            return new ImageUploadResult(secureUrl, returnedPublicId);
        } catch (IOException ex) {
            throw new CloudinaryUploadException("Failed to read upload file", ex);
        } catch (CloudinaryUploadException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudinaryUploadException("Failed to upload image to Cloudinary", ex);
        }
    }

    /**
     * Best-effort delete. Failures are logged but do not fail the avatar update.
     */
    public void deleteImage(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            log.info("Deleted old Cloudinary image publicId={}", publicId);
        } catch (Exception ex) {
            log.warn("Failed to delete Cloudinary image publicId={}: {}", publicId, ex.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size must not exceed 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Only JPG and PNG images are allowed");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only JPG and PNG images are allowed");
        }
    }

    private String extractExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    public record ImageUploadResult(String url, String publicId) {}
}

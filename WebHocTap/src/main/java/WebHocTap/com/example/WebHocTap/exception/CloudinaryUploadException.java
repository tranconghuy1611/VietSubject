package WebHocTap.com.example.WebHocTap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CloudinaryUploadException extends RuntimeException {
    public CloudinaryUploadException(String message) {
        super(message);
    }

    public CloudinaryUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}

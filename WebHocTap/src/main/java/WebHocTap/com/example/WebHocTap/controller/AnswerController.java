package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.question.AnswerRequest;
import WebHocTap.com.example.WebHocTap.dto.question.AnswerResponse;
import WebHocTap.com.example.WebHocTap.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnswerController {

    private final AnswerService answerService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AnswerResponse>> updateAnswer(
            @PathVariable Long id,
            @Valid @RequestBody AnswerRequest request) {

        return ResponseEntity.ok(ApiResponse.<AnswerResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Answer updated successfully")
                .data(answerService.updateAnswer(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Answer deleted successfully")
                .build());
    }
}

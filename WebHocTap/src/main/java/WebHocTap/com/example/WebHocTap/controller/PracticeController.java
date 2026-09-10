package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.practice.*;
import WebHocTap.com.example.WebHocTap.service.PracticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Practice session APIs.
 * <p>userId is NEVER accepted from the client — resolved from JWT.
 */
@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PracticeController {

    private final PracticeService practiceService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StartSessionResponseDTO>> startSession(
            @Valid @RequestBody StartSessionRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<StartSessionResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Practice session started successfully.")
                        .data(practiceService.startSession(request))
                        .build());
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<PracticeHistoryItemDTO>>> getHistory(
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.<PageResponse<PracticeHistoryItemDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(practiceService.getHistory(pageable))
                .build());
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<SessionDetailDTO>> getSessionDetail(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.<SessionDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(practiceService.getSessionDetail(sessionId))
                .build());
    }

    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionDTO>>> getQuestions(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.<List<QuestionDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Questions retrieved successfully.")
                .data(practiceService.getQuestions(sessionId))
                .build());
    }

    @PostMapping("/{sessionId}/answers")
    public ResponseEntity<ApiResponse<SubmitAnswerResponseDTO>> submitAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswerRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.<SubmitAnswerResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Answer submitted.")
                .data(practiceService.submitAnswer(sessionId, request))
                .build());
    }

    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<ApiResponse<SessionSummaryDTO>> finishSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.<SessionSummaryDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Session finished.")
                .data(practiceService.finishSession(sessionId))
                .build());
    }

    @PostMapping("/{sessionId}/skip")
    public ResponseEntity<ApiResponse<Void>> skipQuestion(
            @PathVariable Long sessionId,
            @Valid @RequestBody SkipQuestionRequestDTO request) {

        practiceService.skipQuestion(sessionId, request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Question skipped.")
                .build());
    }
}

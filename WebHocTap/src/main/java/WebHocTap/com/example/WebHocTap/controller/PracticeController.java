package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.practice.*;
import WebHocTap.com.example.WebHocTap.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for all practice-session operations.
 *
 * <p><strong>Security note:</strong> userId is NEVER read from the request body.
 * It is resolved from the JWT by the service layer via {@code SecurityContextHolder}.
 */
@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PracticeController {

    private final PracticeService practiceService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/practice/start
    // ──────────────────────────────────────────────────────────────────────────


    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StartSessionResponseDTO>> startSession(
            @RequestBody StartSessionRequestDTO request) {

        StartSessionResponseDTO data = practiceService.startSession(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<StartSessionResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Practice session started successfully.")
                        .data(data)
                        .build());
    }
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<SessionDetailDTO>> getSession(
            @PathVariable Long sessionId) {

        SessionDetailDTO data = practiceService.getSession(sessionId);

        return ResponseEntity.ok(ApiResponse.<SessionDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Session retrieved successfully.")
                .data(data)
                .build());
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PracticeHistoryDTO>>> getHistory() {

        List<PracticeHistoryDTO> data = practiceService.getHistory();

        return ResponseEntity.ok(ApiResponse.<List<PracticeHistoryDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("History retrieved successfully.")
                .data(data)
                .build());
    }
    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/practice/questions?sessionId=
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Retrieve up to 10 adaptive questions for the session.
     * The userId for exclusion/difficulty logic comes from the session entity — not the request.
     */
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<QuestionDTO>>> getQuestions(
            @RequestParam Long sessionId) {

        List<QuestionDTO> questions = practiceService.getQuestions(sessionId);
        return ResponseEntity.ok(ApiResponse.<List<QuestionDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Questions retrieved successfully.")
                .data(questions)
                .build());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/practice/submit
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SubmitAnswerResponseDTO>> submitAnswer(
            @RequestBody SubmitAnswerRequestDTO request) {

        SubmitAnswerResponseDTO result = practiceService.submitAnswer(request);
        return ResponseEntity.ok(ApiResponse.<SubmitAnswerResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Answer submitted.")
                .data(result)
                .build());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/practice/finish/{sessionId}
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Finish the session and return a performance summary.
     * Calling this endpoint is idempotent — subsequent calls return the same summary
     * without updating {@code ended_at} again.
     */
    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<ApiResponse<SessionSummaryDTO>> finishSession(
            @PathVariable Long sessionId) {

        SessionSummaryDTO summary = practiceService.finishSession(sessionId);
        return ResponseEntity.ok(ApiResponse.<SessionSummaryDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Session finished.")
                .data(summary)
                .build());
    }
}

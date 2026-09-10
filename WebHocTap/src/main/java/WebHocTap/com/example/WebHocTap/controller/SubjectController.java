package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.subject.SubjectResponse;
import WebHocTap.com.example.WebHocTap.dto.topic.TopicResponse;
import WebHocTap.com.example.WebHocTap.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SubjectResponse>>> getAllSubjects(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        PageResponse<SubjectResponse> data = subjectService.getAllSubjects(pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<SubjectResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(data)
                .build());
    }

    @GetMapping("/{id}/topics")
    public ResponseEntity<ApiResponse<PageResponse<TopicResponse>>> getTopicsBySubject(
            @PathVariable Long id,
            @RequestParam(required = false) Long gradeId,
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<TopicResponse> data = subjectService.getTopicsBySubject(id, gradeId, pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<TopicResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(data)
                .build());
    }
}

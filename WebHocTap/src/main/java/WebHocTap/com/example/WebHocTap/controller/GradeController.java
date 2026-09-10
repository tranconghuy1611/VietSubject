package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.grade.GradeResponse;
import WebHocTap.com.example.WebHocTap.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read APIs for grades (sorted by level ASC).
 * Accessible to all authenticated roles (STUDENT, PARENT, ADMIN).
 */
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GradeResponse>>> getAllGrades(
            @PageableDefault(size = 50, sort = "level") Pageable pageable) {

        PageResponse<GradeResponse> data = gradeService.getAllGrades(pageable);
        return ResponseEntity.ok(ApiResponse.<PageResponse<GradeResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(data)
                .build());
    }
}

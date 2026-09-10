package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.user.ChildResponse;
import WebHocTap.com.example.WebHocTap.dto.user.LinkChildRequest;
import WebHocTap.com.example.WebHocTap.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARENT')")
public class ParentController {

    private final ParentService parentService;

    @PostMapping("/link-child")
    public ResponseEntity<ApiResponse<ChildResponse>> linkChild(@RequestBody LinkChildRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ChildResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Child linked successfully")
                        .data(parentService.linkChild(request))
                        .build());
    }

    @GetMapping("/children")
    public ResponseEntity<ApiResponse<List<ChildResponse>>> getChildren() {
        return ResponseEntity.ok(ApiResponse.<List<ChildResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(parentService.getChildren())
                .build());
    }

    @DeleteMapping("/unlink/{childId}")
    public ResponseEntity<ApiResponse<Void>> unlinkChild(@PathVariable Long childId) {
        parentService.unlinkChild(childId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Child unlinked successfully")
                .build());
    }
}

package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.topic.CreateTopicRequest;
import WebHocTap.com.example.WebHocTap.dto.topic.TopicResponse;
import WebHocTap.com.example.WebHocTap.dto.topic.UpdateTopicRequest;
import WebHocTap.com.example.WebHocTap.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<TopicResponse>> getTopicById(@PathVariable Long id) {
        TopicResponse data = topicService.getTopicById(id);
        return ResponseEntity.ok(ApiResponse.<TopicResponse>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(data)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(@RequestBody CreateTopicRequest request) {
        TopicResponse data = topicService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TopicResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Topic created successfully")
                        .data(data)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TopicResponse>> updateTopic(
            @PathVariable Long id,
            @RequestBody UpdateTopicRequest request) {

        TopicResponse data = topicService.updateTopic(id, request);
        return ResponseEntity.ok(ApiResponse.<TopicResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Topic updated successfully")
                .data(data)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Topic deleted successfully")
                .build());
    }
}

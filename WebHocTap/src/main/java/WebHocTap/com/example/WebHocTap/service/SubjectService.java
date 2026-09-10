package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.subject.SubjectResponse;
import WebHocTap.com.example.WebHocTap.dto.topic.TopicResponse;
import org.springframework.data.domain.Pageable;

public interface SubjectService {

    PageResponse<SubjectResponse> getAllSubjects(Pageable pageable);

    PageResponse<TopicResponse> getTopicsBySubject(Long subjectId, Long gradeId, Pageable pageable);
}

package WebHocTap.com.example.WebHocTap.mapper;

import WebHocTap.com.example.WebHocTap.dto.subject.SubjectResponse;
import WebHocTap.com.example.WebHocTap.entity.Subject;
import org.springframework.stereotype.Component;

@Component
public class SubjectMapper {

    public SubjectResponse toResponse(Subject subject) {
        if (subject == null) {
            return null;
        }
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .code(subject.getCode())
                .build();
    }
}

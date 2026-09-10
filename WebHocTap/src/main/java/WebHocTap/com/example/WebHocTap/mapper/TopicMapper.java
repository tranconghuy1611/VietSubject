package WebHocTap.com.example.WebHocTap.mapper;

import WebHocTap.com.example.WebHocTap.dto.topic.TopicResponse;
import WebHocTap.com.example.WebHocTap.entity.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopicMapper {

    private final SubjectMapper subjectMapper;
    private final GradeMapper gradeMapper;

    public TopicResponse toResponse(Topic topic) {
        if (topic == null) {
            return null;
        }
        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .difficulty(topic.getDifficulty())
                .isActive(topic.getIsActive())
                .subject(subjectMapper.toResponse(topic.getSubject()))
                .grade(gradeMapper.toResponse(topic.getGrade()))
                .build();
    }
}

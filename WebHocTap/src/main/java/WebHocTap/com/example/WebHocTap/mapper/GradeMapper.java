package WebHocTap.com.example.WebHocTap.mapper;

import WebHocTap.com.example.WebHocTap.dto.grade.GradeResponse;
import WebHocTap.com.example.WebHocTap.entity.Grade;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeResponse toResponse(Grade grade) {
        if (grade == null) {
            return null;
        }
        return GradeResponse.builder()
                .id(grade.getId())
                .name(grade.getName())
                .level(grade.getLevel())
                .build();
    }
}

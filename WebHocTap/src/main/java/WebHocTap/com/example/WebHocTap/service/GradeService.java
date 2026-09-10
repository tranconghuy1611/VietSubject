package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.grade.GradeResponse;
import org.springframework.data.domain.Pageable;

public interface GradeService {

    PageResponse<GradeResponse> getAllGrades(Pageable pageable);
}

package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.user.ChildResponse;
import WebHocTap.com.example.WebHocTap.dto.user.LinkChildRequest;

import java.util.List;

public interface ParentService {

    ChildResponse linkChild(LinkChildRequest request);

    List<ChildResponse> getChildren();

    void unlinkChild(Long childId);
}

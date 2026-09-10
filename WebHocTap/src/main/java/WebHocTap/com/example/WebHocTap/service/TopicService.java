package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.topic.CreateTopicRequest;
import WebHocTap.com.example.WebHocTap.dto.topic.TopicResponse;
import WebHocTap.com.example.WebHocTap.dto.topic.UpdateTopicRequest;

public interface TopicService {

    TopicResponse getTopicById(Long id);

    TopicResponse createTopic(CreateTopicRequest request);

    TopicResponse updateTopic(Long id, UpdateTopicRequest request);

    void deleteTopic(Long id);
}

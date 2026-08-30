package WebHocTap.com.example.WebHocTap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_matching_pairs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionMatchingPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Question question;

    @Column(name = "left_content", columnDefinition = "TEXT", nullable = false)
    private String leftContent;

    @Column(name = "left_image_url")
    private String leftImageUrl;

    @Column(name = "right_content", columnDefinition = "TEXT", nullable = false)
    private String rightContent;

    @Column(name = "right_image_url")
    private String rightImageUrl;
}

package WebHocTap.com.example.WebHocTap.entity;

import WebHocTap.com.example.WebHocTap.enums.PerformanceLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_performance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "total_attempts")
    private Integer totalAttempts = 0;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    private Float accuracy = 0f;

    @Enumerated(EnumType.STRING)
    private PerformanceLevel level = PerformanceLevel.MEDIUM;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

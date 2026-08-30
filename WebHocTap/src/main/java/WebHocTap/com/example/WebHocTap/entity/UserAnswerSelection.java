package WebHocTap.com.example.WebHocTap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_answer_selections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_answer_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private UserAnswer userAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;
}

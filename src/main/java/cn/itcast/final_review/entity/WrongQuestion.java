package cn.itcast.final_review.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WrongQuestion {
    private Long id;
    private Long questionId;
    private Long subjectId;
    private Integer consecutiveCorrect;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

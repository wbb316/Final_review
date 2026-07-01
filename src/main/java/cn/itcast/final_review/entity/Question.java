package cn.itcast.final_review.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Question {
    private Long id;
    private Long subjectId;
    private String type;       // SELECT / JUDGE
    private String content;
    private String options;    // JSON数组字符串，判断题null
    private String answer;
    private LocalDateTime createTime;
}

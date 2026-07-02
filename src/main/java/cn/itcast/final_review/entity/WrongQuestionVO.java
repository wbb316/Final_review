package cn.itcast.final_review.entity;

import lombok.Data;

@Data
public class WrongQuestionVO {
    private Long id;
    private Long questionId;
    private Long subjectId;
    private String subjectName;
    private Integer consecutiveCorrect;
    private String type;
    private String content;
    private String options;
    private String answer;
}

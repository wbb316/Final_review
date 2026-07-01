package cn.itcast.final_review.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Subject {
    private Long id;
    private String name;
    private LocalDateTime createTime;
}

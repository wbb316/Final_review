package cn.itcast.final_review.controller;

import cn.itcast.final_review.entity.Question;
import cn.itcast.final_review.service.QuestionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    private final QuestionService questionService;

    public ReviewController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/{subjectId}/random")
    public List<Question> getRandomQuestions(
            @PathVariable Long subjectId,
            @RequestParam(defaultValue = "10") int count) {
        return questionService.findRandom(subjectId, count);
    }
}

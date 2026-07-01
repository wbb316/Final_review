package cn.itcast.final_review.controller;

import cn.itcast.final_review.entity.Question;
import cn.itcast.final_review.service.QuestionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<Question> findBySubjectId(@RequestParam Long subjectId) {
        return questionService.findBySubjectId(subjectId);
    }

    @PostMapping
    public Question save(@RequestBody Question question) {
        return questionService.save(question);
    }

    @PutMapping("/{id}")
    public Question update(@PathVariable Long id, @RequestBody Question question) {
        return questionService.update(id, question);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        questionService.delete(id);
    }
}

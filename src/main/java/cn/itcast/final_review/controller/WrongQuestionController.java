package cn.itcast.final_review.controller;

import cn.itcast.final_review.entity.WrongQuestionVO;
import cn.itcast.final_review.service.WrongQuestionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wrong-questions")
public class WrongQuestionController {
    private final WrongQuestionService wrongQuestionService;

    public WrongQuestionController(WrongQuestionService wrongQuestionService) {
        this.wrongQuestionService = wrongQuestionService;
    }

    @GetMapping
    public List<WrongQuestionVO> findAll() {
        return wrongQuestionService.findAll();
    }

    @GetMapping("/subject/{subjectId}")
    public List<WrongQuestionVO> findBySubjectId(@PathVariable Long subjectId) {
        return wrongQuestionService.findBySubjectId(subjectId);
    }

    @PostMapping("/record-wrong")
    public void recordWrong(@RequestBody Map<String, Long> body) {
        Long questionId = body.get("questionId");
        Long subjectId = body.get("subjectId");
        wrongQuestionService.recordWrong(questionId, subjectId);
    }

    @PostMapping("/record-correct")
    public Map<String, Boolean> recordCorrect(@RequestBody Map<String, Long> body) {
        Long questionId = body.get("questionId");
        boolean mastered = wrongQuestionService.recordCorrect(questionId);
        return Map.of("mastered", mastered);
    }
}

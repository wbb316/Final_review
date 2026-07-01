package cn.itcast.final_review.controller;

import cn.itcast.final_review.entity.Subject;
import cn.itcast.final_review.service.SubjectService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<Subject> findAll() {
        return subjectService.findAll();
    }

    @PostMapping
    public Subject save(@RequestBody Subject subject) {
        return subjectService.save(subject);
    }

    @PutMapping("/{id}")
    public Subject update(@PathVariable Long id, @RequestBody Subject subject) {
        return subjectService.update(id, subject);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        subjectService.delete(id);
    }
}

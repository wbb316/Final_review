package cn.itcast.final_review.service;

import cn.itcast.final_review.entity.Question;
import cn.itcast.final_review.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QuestionService {
    private final QuestionMapper questionMapper;

    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    public List<Question> findBySubjectId(Long subjectId) {
        return questionMapper.findBySubjectId(subjectId);
    }

    public Question findById(Long id) {
        return questionMapper.findById(id);
    }

    public Question save(Question question) {
        questionMapper.insert(question);
        return question;
    }

    public Question update(Long id, Question question) {
        question.setId(id);
        questionMapper.update(question);
        return question;
    }

    public void delete(Long id) {
        questionMapper.deleteById(id);
    }

    public List<Question> findRandom(Long subjectId, int count) {
        long total = questionMapper.countBySubjectId(subjectId);
        int actualCount = (int) Math.min(count, total);
        return questionMapper.findRandom(subjectId, actualCount);
    }
}

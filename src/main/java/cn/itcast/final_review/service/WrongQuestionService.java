package cn.itcast.final_review.service;

import cn.itcast.final_review.entity.WrongQuestion;
import cn.itcast.final_review.entity.WrongQuestionVO;
import cn.itcast.final_review.mapper.WrongQuestionMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WrongQuestionService {
    private final WrongQuestionMapper wrongQuestionMapper;

    public WrongQuestionService(WrongQuestionMapper wrongQuestionMapper) {
        this.wrongQuestionMapper = wrongQuestionMapper;
    }

    public List<WrongQuestionVO> findAll() {
        return wrongQuestionMapper.findAllWithQuestion();
    }

    public List<WrongQuestionVO> findBySubjectId(Long subjectId) {
        return wrongQuestionMapper.findBySubjectIdWithQuestion(subjectId);
    }

    public WrongQuestion findByQuestionId(Long questionId) {
        return wrongQuestionMapper.findByQuestionId(questionId);
    }

    /**
     * Record a wrong answer: if not in wrong question set, insert with consecutive_correct=0;
     * if already exists, reset consecutive_correct to 0.
     */
    public void recordWrong(Long questionId, Long subjectId) {
        WrongQuestion existing = wrongQuestionMapper.findByQuestionId(questionId);
        if (existing == null) {
            WrongQuestion wq = new WrongQuestion();
            wq.setQuestionId(questionId);
            wq.setSubjectId(subjectId);
            wq.setConsecutiveCorrect(0);
            wrongQuestionMapper.insert(wq);
        } else {
            existing.setConsecutiveCorrect(0);
            wrongQuestionMapper.updateConsecutiveCorrect(existing.getId(), 0);
        }
    }

    /**
     * Record a correct answer: increment consecutive_correct by 1.
     * If consecutive_correct reaches 3, remove the record.
     * @return true if the question has been mastered (removed), false otherwise
     */
    public boolean recordCorrect(Long questionId) {
        WrongQuestion wq = wrongQuestionMapper.findByQuestionId(questionId);
        if (wq == null) {
            return false;
        }
        int newCount = wq.getConsecutiveCorrect() + 1;
        if (newCount >= 3) {
            wrongQuestionMapper.deleteById(wq.getId());
            return true;
        } else {
            wrongQuestionMapper.updateConsecutiveCorrect(wq.getId(), newCount);
            return false;
        }
    }

    public void delete(Long id) {
        wrongQuestionMapper.deleteById(id);
    }
}

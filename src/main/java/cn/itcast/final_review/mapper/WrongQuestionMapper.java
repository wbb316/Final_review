package cn.itcast.final_review.mapper;

import cn.itcast.final_review.entity.WrongQuestion;
import cn.itcast.final_review.entity.WrongQuestionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WrongQuestionMapper {
    List<WrongQuestion> findAll();
    List<WrongQuestionVO> findAllWithQuestion();
    List<WrongQuestionVO> findBySubjectIdWithQuestion(@Param("subjectId") Long subjectId);
    WrongQuestion findByQuestionId(@Param("questionId") Long questionId);
    List<WrongQuestion> findBySubjectId(@Param("subjectId") Long subjectId);
    Long countByQuestionId(@Param("questionId") Long questionId);
    void insert(WrongQuestion wrongQuestion);
    void updateConsecutiveCorrect(@Param("id") Long id, @Param("consecutiveCorrect") int consecutiveCorrect);
    void deleteById(Long id);
}

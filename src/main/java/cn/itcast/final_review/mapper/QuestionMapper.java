package cn.itcast.final_review.mapper;

import cn.itcast.final_review.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QuestionMapper {
    List<Question> findBySubjectId(@Param("subjectId") Long subjectId);
    Question findById(Long id);
    void insert(Question question);
    void update(Question question);
    void deleteById(Long id);
    void deleteBySubjectId(Long subjectId);
    List<Question> findRandom(@Param("subjectId") Long subjectId, @Param("count") int count);
    Long countBySubjectId(@Param("subjectId") Long subjectId);
}

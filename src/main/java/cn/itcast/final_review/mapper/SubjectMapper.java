package cn.itcast.final_review.mapper;

import cn.itcast.final_review.entity.Subject;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SubjectMapper {
    List<Subject> findAll();
    Subject findById(Long id);
    void insert(Subject subject);
    void update(Subject subject);
    void deleteById(Long id);
}

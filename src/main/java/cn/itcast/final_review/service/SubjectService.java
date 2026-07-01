package cn.itcast.final_review.service;

import cn.itcast.final_review.entity.Subject;
import cn.itcast.final_review.mapper.SubjectMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubjectService {
    private final SubjectMapper subjectMapper;

    public SubjectService(SubjectMapper subjectMapper) {
        this.subjectMapper = subjectMapper;
    }

    public List<Subject> findAll() {
        return subjectMapper.findAll();
    }

    public Subject findById(Long id) {
        return subjectMapper.findById(id);
    }

    public Subject save(Subject subject) {
        subjectMapper.insert(subject);
        return subject;
    }

    public Subject update(Long id, Subject subject) {
        subject.setId(id);
        subjectMapper.update(subject);
        return subject;
    }

    public void delete(Long id) {
        subjectMapper.deleteById(id);
    }
}

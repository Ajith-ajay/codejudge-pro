package com.ajith.codejudge.exam.repository;

import com.ajith.codejudge.exam.entity.SectionQuestion;
import com.ajith.codejudge.exam.entity.SectionQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionQuestionRepository extends JpaRepository<SectionQuestion, SectionQuestionId> {

    List<SectionQuestion> findBySectionIdOrderByOrderIndexAsc(Long sectionId);
}

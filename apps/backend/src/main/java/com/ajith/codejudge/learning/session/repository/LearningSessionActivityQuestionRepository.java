package com.ajith.codejudge.learning.session.repository;

import com.ajith.codejudge.learning.session.entity.LearningSessionActivityQuestion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LearningSessionActivityQuestionRepository
        extends JpaRepository<LearningSessionActivityQuestion, Long> {

    @Query("""
        select distinct q.question.id
        from LearningSessionActivityQuestion q
        where q.activity.session.user.id = :userId
          and q.activity.session.status in (
              com.ajith.codejudge.learning.session.entity.LearningSessionStatus.NOT_STARTED,
              com.ajith.codejudge.learning.session.entity.LearningSessionStatus.IN_PROGRESS
          )
        """)
    List<Long> findQuestionIdsAssignedToActiveSessions(@Param("userId") Long userId);
}

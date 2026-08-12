package com.ajith.codejudge.learning.session.repository;

import com.ajith.codejudge.learning.session.entity.LearningSession;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
    Optional<LearningSession> findByIdAndUserId(Long id, Long userId);
    Optional<LearningSession> findByUserIdAndSessionDate(Long userId, LocalDate sessionDate);

    @Query("""
        select count(s) from LearningSession s
        where s.user.id = :userId
          and s.status = com.ajith.codejudge.learning.session.entity.LearningSessionStatus.COMPLETED
        """)
    long countCompletedByUserId(@Param("userId") Long userId);

    @Query("""
        select max(s.sessionDate) from LearningSession s
        where s.user.id = :userId
          and s.status = com.ajith.codejudge.learning.session.entity.LearningSessionStatus.COMPLETED
        """)
    LocalDate findLastCompletedDate(@Param("userId") Long userId);
}

package com.ajith.codejudge.learning.session.repository;

import com.ajith.codejudge.learning.session.entity.LearningSessionActivity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LearningSessionActivityRepository extends JpaRepository<LearningSessionActivity, Long> {
    @Query("""
        select a from LearningSessionActivity a
        join fetch a.session s
        join fetch a.roadmapActivity ra
        where a.id = :id and s.user.id = :userId
        """)
    Optional<LearningSessionActivity> findOwned(
            @Param("id") Long id, @Param("userId") Long userId);
}

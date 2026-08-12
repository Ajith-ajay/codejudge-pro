package com.ajith.codejudge.learning.roadmap.repository;

import com.ajith.codejudge.learning.roadmap.entity.LearningRoadmap;
import com.ajith.codejudge.learning.roadmap.entity.LearningRoadmapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LearningRoadmapRepository extends JpaRepository<LearningRoadmap, Long> {

    @Query("""
            select r from LearningRoadmap r
            where r.id = :id and r.user.id = :userId
            """)
    Optional<LearningRoadmap> findOwned(
            @Param("id") Long id,
            @Param("userId") Long userId);

    Optional<LearningRoadmap> findFirstByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, LearningRoadmapStatus status);
}

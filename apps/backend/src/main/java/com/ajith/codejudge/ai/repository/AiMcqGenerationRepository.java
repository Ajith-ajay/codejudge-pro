package com.ajith.codejudge.ai.repository;

import com.ajith.codejudge.ai.entity.AiMcqGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiMcqGenerationRepository extends JpaRepository<AiMcqGeneration, Long> {
}

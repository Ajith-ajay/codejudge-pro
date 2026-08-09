package com.ajith.codejudge.problems.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ajith.codejudge.problems.entity.Problem;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("select p from Problem p "
            + "where (:search is null or lower(p.title) like lower(concat('%', :search, '%')) "
            + "       or :search is null or lower(p.tags) like lower(concat('%', :search, '%'))) "
            + "  and (:difficulty is null or p.difficulty = :difficulty) "
            + "  and (:tag is null or lower(p.tags) like lower(concat('%', :tag, '%')))")
    Page<Problem> findAllFiltered(@Param("search") String search,
            @Param("difficulty") String difficulty,
            @Param("tag") String tag,
            Pageable pageable);
}

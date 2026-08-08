package com.ajith.codejudge.question.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ajith.codejudge.question.entity.Question;

@Repository
public interface ProblemRepository extends JpaRepository<Question, Long> {

    @Query(
            value = """
            SELECT
                q.id AS id,
                q.title AS title,
                q.difficulty AS difficulty,

                CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM submissions us
                        WHERE us.question_id = q.id
                          AND us.user_id = :userId
                          AND us.candidate_id IS NULL
                          AND us.status = 'ACCEPTED'
                    )
                    THEN 'SOLVED'

                    WHEN EXISTS (
                        SELECT 1
                        FROM submissions us
                        WHERE us.question_id = q.id
                          AND us.user_id = :userId
                          AND us.candidate_id IS NULL
                          AND us.status NOT IN ('PENDING', 'RUNNING')
                    )
                    THEN 'ATTEMPTED'

                    ELSE 'NOT_ATTEMPTED'
                END AS status,

                COALESCE(stats.acceptance_rate, 0.0) AS acceptance_rate,

                COALESCE(stats.solved_users, 0) AS solved_users,

                COALESCE(stats.total_submissions, 0) AS total_submissions

            FROM questions q

            LEFT JOIN (
                SELECT
                    s.question_id,

                    ROUND(
                        100.0 *
                        COUNT(*) FILTER (
                            WHERE s.status = 'ACCEPTED'
                        )
                        /
                        NULLIF(COUNT(*), 0),
                        2
                    ) AS acceptance_rate,

                    COUNT(DISTINCT s.user_id) FILTER (
                        WHERE s.status = 'ACCEPTED'
                    ) AS solved_users,

                    COUNT(*) AS total_submissions

                FROM submissions s

                WHERE s.candidate_id IS NULL
                  AND s.status NOT IN ('PENDING', 'RUNNING')

                GROUP BY s.question_id

            ) stats
                ON stats.question_id = q.id

            WHERE q.type = 'CODING'

              AND NOT EXISTS (
                  SELECT 1
                  FROM section_questions sq

                  JOIN sections sec
                    ON sec.id = sq.section_id

                  JOIN exams e
                    ON e.id = sec.exam_id

                  WHERE sq.question_id = q.id
                    AND e.end_time > CURRENT_TIMESTAMP
              )

              AND (
                  :difficulty IS NULL
                  OR q.difficulty = :difficulty
              )

              AND (
                  :search IS NULL
                  OR LOWER(q.title)
                     LIKE LOWER(CONCAT('%', :search, '%'))
              )

              AND (
                  :status IS NULL

                  OR (
                      :status = 'SOLVED'

                      AND EXISTS (
                          SELECT 1
                          FROM submissions ss

                          WHERE ss.question_id = q.id
                            AND ss.user_id = :userId
                            AND ss.candidate_id IS NULL
                            AND ss.status = 'ACCEPTED'
                      )
                  )

                  OR (
                      :status = 'ATTEMPTED'

                      AND NOT EXISTS (
                          SELECT 1
                          FROM submissions sa

                          WHERE sa.question_id = q.id
                            AND sa.user_id = :userId
                            AND sa.candidate_id IS NULL
                            AND sa.status = 'ACCEPTED'
                      )

                      AND EXISTS (
                          SELECT 1
                          FROM submissions st

                          WHERE st.question_id = q.id
                            AND st.user_id = :userId
                            AND st.candidate_id IS NULL
                            AND st.status NOT IN ('PENDING', 'RUNNING')
                      )
                  )

                  OR (
                      :status = 'NOT_ATTEMPTED'

                      AND NOT EXISTS (
                          SELECT 1
                          FROM submissions sn

                          WHERE sn.question_id = q.id
                            AND sn.user_id = :userId
                            AND sn.candidate_id IS NULL
                            AND sn.status NOT IN ('PENDING', 'RUNNING')
                      )
                  )
              )
            """,
            countQuery = """
            SELECT COUNT(*)

            FROM questions q

            WHERE q.type = 'CODING'

              AND NOT EXISTS (
                  SELECT 1
                  FROM section_questions sq

                  JOIN sections sec
                    ON sec.id = sq.section_id

                  JOIN exams e
                    ON e.id = sec.exam_id

                  WHERE sq.question_id = q.id
                    AND e.end_time > CURRENT_TIMESTAMP
              )

              AND (
                  :difficulty IS NULL
                  OR q.difficulty = :difficulty
              )

              AND (
                  :search IS NULL
                  OR LOWER(q.title)
                     LIKE LOWER(CONCAT('%', :search, '%'))
              )

              AND (
                  :status IS NULL

                  OR (
                      :status = 'SOLVED'

                      AND EXISTS (
                          SELECT 1
                          FROM submissions ss

                          WHERE ss.question_id = q.id
                            AND ss.user_id = :userId
                            AND ss.candidate_id IS NULL
                            AND ss.status = 'ACCEPTED'
                      )
                  )

                  OR (
                      :status = 'ATTEMPTED'

                      AND NOT EXISTS (
                          SELECT 1
                          FROM submissions sa

                          WHERE sa.question_id = q.id
                            AND sa.user_id = :userId
                            AND sa.candidate_id IS NULL
                            AND sa.status = 'ACCEPTED'
                      )

                      AND EXISTS (
                          SELECT 1
                          FROM submissions st

                          WHERE st.question_id = q.id
                            AND st.user_id = :userId
                            AND st.candidate_id IS NULL
                            AND st.status NOT IN ('PENDING', 'RUNNING')
                      )
                  )

                  OR (
                      :status = 'NOT_ATTEMPTED'

                      AND NOT EXISTS (
                          SELECT 1
                          FROM submissions sn

                          WHERE sn.question_id = q.id
                            AND sn.user_id = :userId
                            AND sn.candidate_id IS NULL
                            AND sn.status NOT IN ('PENDING', 'RUNNING')
                      )
                  )
              )
            """,
            nativeQuery = true
    )
    Page<ProblemListProjection> findProblemList(
            @Param("userId") Long userId,
            @Param("difficulty") String difficulty,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );
}

package com.ajith.codejudge.question.repository;

public interface ProblemListProjection {

    Long getId();

    String getTitle();

    String getDifficulty();

    String getStatus();

    Double getAcceptanceRate();

    Long getSolvedUsers();

    Long getTotalSubmissions();
}

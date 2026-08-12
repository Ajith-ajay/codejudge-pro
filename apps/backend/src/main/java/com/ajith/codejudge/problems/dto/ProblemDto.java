package com.ajith.codejudge.problems.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ajith.codejudge.problems.entity.Problem;

public class ProblemDto {

    private Long id;
    private String title;
    private String slug;
    private List<String> tags;
    private String difficulty;
    private Double acceptanceRate;

    public ProblemDto() {
    }

    public ProblemDto(Problem p) {
        this.id = p.getId();
        this.title = p.getTitle();
        this.slug = p.getSlug();
        this.difficulty = p.getDifficulty();
        this.acceptanceRate = p.getAcceptanceRate();
        this.tags = p.getTags() == null ? List.of()
                : Arrays.stream(p.getTags().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
    }

    // getters / setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(Double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }
}

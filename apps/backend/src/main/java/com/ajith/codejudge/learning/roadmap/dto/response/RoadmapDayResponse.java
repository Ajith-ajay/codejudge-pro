package com.ajith.codejudge.learning.roadmap.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoadmapDayResponse {
    private int weekNumber;
    private int dayNumber;
    private String title;
    private String focus;
    private List<RoadmapActivityResponse> activities;
}

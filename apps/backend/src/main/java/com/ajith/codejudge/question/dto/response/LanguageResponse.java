package com.ajith.codejudge.question.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageResponse {
    private Long id;
    private String name;
    private String code;
    private String compilerVersion;
}

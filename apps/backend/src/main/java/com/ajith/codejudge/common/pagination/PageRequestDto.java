package com.ajith.codejudge.common.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDto {

    @Min(value = 0, message = "Page index must be 0 or greater")
    @Builder.Default
    private int page = 0;

    @Min(value = 1, message = "Page size must be 1 or greater")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String direction = "DESC";

    public Pageable toPageable() {
        Sort sort = Sort.by(Sort.Direction.fromString(direction.toUpperCase()), sortBy);
        return PageRequest.of(page, size, sort);
    }
}

package com.ajith.codejudge.exam.mapper;

import com.ajith.codejudge.exam.dto.response.ActivityLogResponse;
import com.ajith.codejudge.exam.entity.ActivityLog;
import org.mapstruct.Mapper;

@Mapper(
    componentModel = "spring",
    builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface ActivityLogMapper {

    ActivityLogResponse toResponse(ActivityLog activityLog);
}

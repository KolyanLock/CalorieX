package org.nikolait.assignment.caloriex.controller.v1;

import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.dto.ActivityLevelResponseDto;
import org.nikolait.assignment.caloriex.mapper.ActivityLevelMapper;
import org.nikolait.assignment.caloriex.service.ActivityLevelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity-levels")
@RequiredArgsConstructor
public class ActivityLevelController {

    private final ActivityLevelService activityLevelService;
    private final ActivityLevelMapper activityLevelMapper;

    @GetMapping
    public List<ActivityLevelResponseDto> getAllActivityLevels() {
        return activityLevelMapper.toResponseDtoList(activityLevelService.getAllActivityLevels());
    }
}

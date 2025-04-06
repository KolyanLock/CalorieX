package org.nikolait.assignment.caloriex.controller.v1;

import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.dto.GoalResponseDto;
import org.nikolait.assignment.caloriex.mapper.GoalMapper;
import org.nikolait.assignment.caloriex.service.GoalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final GoalMapper goalMapper;

    @GetMapping
    public List<GoalResponseDto> getAllGoals() {
        return goalMapper.toResponseDtoList(goalService.getAllGoals());
    }

}

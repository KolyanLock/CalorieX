package org.nikolait.assignment.caloriex.service.impl;

import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.model.Goal;
import org.nikolait.assignment.caloriex.repository.GoalRepository;
import org.nikolait.assignment.caloriex.service.GoalService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;

    @Override
    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }
}

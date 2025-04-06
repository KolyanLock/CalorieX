package org.nikolait.assignment.caloriex.service.impl;

import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.model.ActivityLevel;
import org.nikolait.assignment.caloriex.repository.ActivityLevelRepository;
import org.nikolait.assignment.caloriex.service.ActivityLevelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLevelServiceImpl implements ActivityLevelService {

    private final ActivityLevelRepository activityLevelRepository;

    @Override
    public List<ActivityLevel> getAllActivityLevels() {
        return activityLevelRepository.findAll();
    }

}

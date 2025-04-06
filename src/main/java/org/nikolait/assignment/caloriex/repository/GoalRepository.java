package org.nikolait.assignment.caloriex.repository;

import org.nikolait.assignment.caloriex.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {
}

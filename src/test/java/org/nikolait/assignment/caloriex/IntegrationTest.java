package org.nikolait.assignment.caloriex;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nikolait.assignment.caloriex.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTest {

    @Autowired
    protected ActivityLevelRepository activityLevelRepository;

    @Autowired
    protected GoalRepository goalRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MealRepository mealRepository;

    @Autowired
    protected DishRepository dishRepository;

    @BeforeAll
    void contextLoads() {
        assertEquals(5, activityLevelRepository.findAll().size());
        assertEquals(3, goalRepository.findAll().size());
    }

    @AfterEach
    void cleanup() {
        mealRepository.deleteAll();
        dishRepository.deleteAll();
        userRepository.deleteAll();
    }

}

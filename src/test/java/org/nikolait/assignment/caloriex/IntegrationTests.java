package org.nikolait.assignment.caloriex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.nikolait.assignment.caloriex.repository.DishRepository;
import org.nikolait.assignment.caloriex.repository.MealRepository;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = "/db/sql/init_test_data.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "/db/sql/clear_test_data.sql", executionPhase = AFTER_TEST_METHOD)
public class IntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private MealRepository mealRepository;

    @Test
    void contextLoads() {
        assertEquals(3, userRepository.findAll().size());
        assertEquals(9, dishRepository.findAll().size());
        assertEquals(24, mealRepository.findAll().size());
    }

}

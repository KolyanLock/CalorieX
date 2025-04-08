package org.nikolait.assignment.caloriex;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.nikolait.assignment.caloriex.repository.DishRepository;
import org.nikolait.assignment.caloriex.repository.MealRepository;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@Sql(scripts = "/db/sql/init_test_data.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "/db/sql/clear_test_data.sql", executionPhase = AFTER_TEST_METHOD)
public class RestAssuredTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private MealRepository mealRepository;

    @LocalServerPort
    protected int port;

    @BeforeAll
    void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void contextLoads() {
        assertEquals(3, userRepository.findAll().size());
        assertEquals(9, dishRepository.findAll().size());
        assertEquals(24, mealRepository.findAll().size());
    }

}

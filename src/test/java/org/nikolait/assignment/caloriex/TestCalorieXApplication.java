package org.nikolait.assignment.caloriex;

import org.springframework.boot.SpringApplication;

public class TestCalorieXApplication {

    public static void main(String[] args) {
        SpringApplication.from(CalorieXApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}

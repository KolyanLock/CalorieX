package org.nikolait.assignment.caloriex.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Profile("dev")
@DependsOn("flyway")
@RequiredArgsConstructor
public class TestDataInitializer implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Long userIdSeqLastValue = jdbcTemplate.queryForObject(
                "SELECT last_value FROM users_id_seq",
                Long.class
        );
        if (userIdSeqLastValue != null && userIdSeqLastValue == 1) {
            new ResourceDatabasePopulator(new ClassPathResource("db/sql/init_test_data.sql")).execute(dataSource);
        }
    }
}

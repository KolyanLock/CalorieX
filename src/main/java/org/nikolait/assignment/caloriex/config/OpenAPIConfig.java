package org.nikolait.assignment.caloriex.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        description = "Enter User ID to authorize"
)
public class OpenAPIConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.license.name}")
    private String licenseName;

    @Bean
    public OpenAPI calorieXAPI() {
        return new OpenAPI()
                .info(new Info().title("%s API".formatted(appName))
                        .description("This is the REST API for " + appName)
                        .version(appVersion)
                        .license(new License().name(licenseName)))
                .externalDocs(new ExternalDocumentation()
                        .description("You can refer to the CalorieX Wiki Documentation")
                        .url("https://caloriex-dummy-url.com/docs"));
    }
}

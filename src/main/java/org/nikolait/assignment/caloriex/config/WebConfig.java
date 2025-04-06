package org.nikolait.assignment.caloriex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${api.prefix:/api}")
    private String apiPrefix;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(
                apiPrefix + "/v1", c -> c.getPackage().getName().endsWith("controller.v1")
        );
    }

}

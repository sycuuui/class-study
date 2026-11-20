package com.musinsa.course.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI(Swagger) 문서 메타데이터.
 * Swagger UI: /swagger-ui.html, 스펙(JSON): /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI courseOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("수강신청 API")
            .description("H2 + JPA 기반 수강신청 시스템 API 문서")
            .version("v1"));
    }
}

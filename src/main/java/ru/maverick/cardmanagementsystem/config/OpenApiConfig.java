package ru.maverick.cardmanagementsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Card Management System API")
                        .description("API для управления банковскими картами")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Ваше Имя")
                                .email("your.email@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .description("Локальная среда")
                                .url("http://localhost:8080"),
                        new Server()
                                .description("Продакшен")
                                .url("https://api.card-management.com")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Token Auth", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }


    @Bean
    public OpenApiCustomizer schemaSortingOpenAPICustomizer() {
        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            if (schemas != null) {
                List<Map.Entry<String, Schema>> schemaList = new ArrayList<>(schemas.entrySet());

                schemaList.sort((entry1, entry2) -> {
                    String name1 = entry1.getKey();
                    String name2 = entry2.getKey();

                    // Приоритет для Card, User, BlockRequest
                    boolean isPriority1 = name1.contains("Card") || name1.contains("User") || name1.contains("BlockRequest");
                    boolean isPriority2 = name2.contains("Card") || name2.contains("User") || name2.contains("BlockRequest");
                    if (isPriority1 != isPriority2) {
                        return isPriority1 ? -1 : 1;
                    }

                    return name1.compareTo(name2);
                });

                Map<String, Schema> sortedSchemas = new LinkedHashMap<>();
                schemaList.forEach(entry -> sortedSchemas.put(entry.getKey(), entry.getValue()));
                openApi.getComponents().setSchemas(sortedSchemas);
            }
        };
    }
}
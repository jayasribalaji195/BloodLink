package com.bloodlink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bloodLinkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BloodLink — Blood Donor Registry & Search API")
                        .description("RESTful backend API for blood donor registration, emergency donor search, " +
                                "automatic 90-day cooldown enforcement, donation records tracking, and real-time inventory metrics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BloodLink Support Team")
                                .email("support@bloodlink.org")
                                .url("https://github.com/bloodlink"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}

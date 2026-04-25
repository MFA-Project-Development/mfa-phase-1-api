package kr.com.mfa.mfaphase1api.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${keycloak.token-endpoint}")
    private String tokenEndpoint;

    @Bean
    public OpenAPI assessmentServiceOpenAPI() {

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .clientCredentials(
                                new OAuthFlow()
                                        .tokenUrl(tokenEndpoint)
                                        .scopes(new Scopes()
                                                .addString("openid", "OpenID Connect scope")
                                        )
                        )
                        .password(
                                new OAuthFlow()
                                        .tokenUrl(tokenEndpoint)
                                        .scopes(new Scopes()
                                                .addString("openid", "OpenID Connect scope")
                                        )
                        )
                );

        return new OpenAPI()
                .info(new Info()
                        .title("MFA Assessment Service API")
                        .version("v1")
                        .description("REST API for managing educational assessments, classes, questions, submissions, and student engagement.")
                        .contact(new Contact()
                                .name("MFA")
                                .email("mfa@gmail.com"))
                )
                .servers(List.of(new Server().url("/").description("Default")))
                .components(new Components()
                        .addSecuritySchemes("mfa", securityScheme)
                )
                .addSecurityItem(new SecurityRequirement().addList("mfa"));
    }
}
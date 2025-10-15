package kr.com.mfa.mfaphase1api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(
        info = @Info(
                title = "MFA Phase 1 Service API",
                version = "v1",
                description = "REST API for MFA Phase 1 subject, assessment, question and option.",
                contact = @Contact(name = "Example Team", email = "example@example.com"),
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
        ),
        servers = {
                @Server(url = "/"),
        }
)
@SecurityScheme(
        name = "mfa",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                clientCredentials = @OAuthFlow(
                        tokenUrl = "http://localhost:8080/realms/mfa/protocol/openid-connect/token",
                        scopes = {
                                @OAuthScope(name = "openid", description = "OpenID Connect scope")
                        }
                ),
                password = @OAuthFlow(
                        tokenUrl = "http://localhost:8080/realms/mfa/protocol/openid-connect/token",
                        scopes = {
                                @OAuthScope(name = "openid", description = "OpenID Connect scope")
                        }
                )
        )
)
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class MfaPhase1ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MfaPhase1ApiApplication.class, args);
    }

}

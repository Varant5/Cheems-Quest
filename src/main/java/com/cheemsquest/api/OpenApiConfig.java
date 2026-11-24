package com.cheemsquest.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI cheemsQuestOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cheems Quest API")
                        .version("0.3.0")
                        .description("""
                                A turn-based text RPG played over REST. Much bonk. Very API.

                                **How to play**
                                1. `POST /api/auth/register` (or `/api/auth/login`) and copy `accessToken`.
                                2. Click **Authorize** and paste the token.
                                3. `POST /api/characters` to create Cheems. You start in Cheems' Doghouse.
                                4. `POST /api/characters/{id}/move` to explore. Dangerous places may start a fight.
                                5. `POST /api/characters/{id}/encounter/action` with `BONK`, `BITE` or `BONKING_SPIN` \
                                until someone is down.
                                6. Walk back to the Doghouse to nap and restore HP. Buff Doge awaits in his tower.
                                """))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}

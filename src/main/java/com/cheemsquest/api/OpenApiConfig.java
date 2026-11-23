package com.cheemsquest.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

    @Bean
    OpenAPI cheemsQuestOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Cheems Quest API")
                .version("0.2.0")
                .description("""
                        A turn-based text RPG played over REST. Much bonk. Very API.

                        **How to play**
                        1. `POST /api/characters` to create Cheems. You start in Cheems' Doghouse.
                        2. `POST /api/characters/{id}/move` to explore. Dangerous places may start a fight.
                        3. `POST /api/characters/{id}/encounter/action` with `BONK`, `BITE` or `BONKING_SPIN` until \
                        someone is down.
                        4. Walk back to the Doghouse to nap and restore HP. Buff Doge awaits in his tower.
                        """));
    }
}

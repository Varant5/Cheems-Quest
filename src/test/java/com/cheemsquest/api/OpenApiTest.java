package com.cheemsquest.api;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class OpenApiTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = TestPlayers.anonymous(context);
    }

    @Test
    void documentsTheGameEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Cheems Quest API"))
                .andExpect(jsonPath("$.paths['/api/auth/register'].post").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/characters'].post").exists())
                .andExpect(jsonPath("$.paths['/api/characters'].get").exists())
                .andExpect(jsonPath("$.paths['/api/characters/{id}'].get.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/characters/{id}/move'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/characters/{id}/encounter'].get").exists())
                .andExpect(jsonPath("$.paths['/api/characters/{id}/encounter/action'].post").exists())
                .andExpect(jsonPath("$.components.schemas.ActionRequest.properties.skill.enum",
                        containsInAnyOrder("BONK", "BITE", "BONKING_SPIN")));
    }

    @Test
    void requiresBearerTokenByDefault() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.security[0].bearerAuth").exists());
    }

    @Test
    void servesSwaggerUiWithoutLogin() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}

package com.cheemsquest.api;

import com.cheemsquest.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CharacterApiTest {

    @Autowired
    private MockMvc mvc;

    private ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String createCharacter() throws Exception {
        String body = postJson("/api/characters", """
                {"username": "bonk_master", "name": "Cheems"}
                """)
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void createsCharacterInTheDoghouse() throws Exception {
        postJson("/api/characters", """
                {"username": "new_doge", "name": "Cheems"}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/characters/")))
                .andExpect(jsonPath("$.name").value("Cheems"))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.hp").value(65))
                .andExpect(jsonPath("$.maxHp").value(65))
                .andExpect(jsonPath("$.location.code").value("CHEEMS_DOGHOUSE"))
                .andExpect(jsonPath("$.location.exits", contains("NORTH", "EAST")));
    }

    @Test
    void returnsCreatedCharacter() throws Exception {
        String id = createCharacter();

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void movingChangesLocation() throws Exception {
        String id = createCharacter();

        postJson("/api/characters/" + id + "/move", """
                {"direction": "NORTH"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You enter Wow Forest."))
                .andExpect(jsonPath("$.location.code").value("WOW_FOREST"))
                .andExpect(jsonPath("$.location.exits", contains("NORTH", "SOUTH", "WEST")));

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.location.code").value("WOW_FOREST"));
    }

    @Test
    void cannotWalkIntoAWall() throws Exception {
        String id = createCharacter();

        postJson("/api/characters/" + id + "/move", """
                {"direction": "WEST"}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Not allowed"))
                .andExpect(jsonPath("$.detail", containsString("Much wall")));
    }

    @Test
    void unknownCharacterIs404() throws Exception {
        mvc.perform(get("/api/characters/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail", endsWith("not found")));
    }

    @Test
    void invalidCreateRequestListsFieldErrors() throws Exception {
        postJson("/api/characters", """
                {"username": "x!", "name": ""}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.username").value("must be 3-32 letters, digits or underscores"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void unknownDirectionIs400() throws Exception {
        String id = createCharacter();

        postJson("/api/characters/" + id + "/move", """
                {"direction": "UP"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedIdIs400() throws Exception {
        mvc.perform(get("/api/characters/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}

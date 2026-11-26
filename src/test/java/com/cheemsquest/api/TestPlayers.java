package com.cheemsquest.api;

import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;
import java.util.function.Consumer;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

final class TestPlayers {

    static final String PASSWORD = "much-secret-very-safe";

    private TestPlayers() {
    }

    static String uniqueUsername() {
        return "doge_" + UUID.randomUUID().toString().substring(0, 8);
    }

    static MockMvc anonymous(WebApplicationContext context) {
        return webAppContextSetup(context).apply(springSecurity()).build();
    }

    static MockMvc newPlayer(WebApplicationContext context) throws Exception {
        return as(context, register(anonymous(context), uniqueUsername()));
    }

    static MockMvc as(WebApplicationContext context, String token) {
        return webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .build();
    }

    static String register(MockMvc mvc, String username) throws Exception {
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"" + username + "\", \"password\": \"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    static String createCharacter(MockMvc mvc) throws Exception {
        String body = mvc.perform(post("/api/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Cheems\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    static void gainExperience(WebApplicationContext context, String characterId, int amount) {
        update(context, characterId, character -> character.gainExperience(amount));
    }

    static void update(WebApplicationContext context, String characterId, Consumer<GameCharacter> change) {
        GameCharacterRepository characters = context.getBean(GameCharacterRepository.class);
        context.getBean(TransactionTemplate.class).executeWithoutResult(status ->
                change.accept(characters.findById(UUID.fromString(characterId)).orElseThrow()));
    }
}

package com.cheemsquest.api;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class AuthApiTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    private String username;

    @BeforeEach
    void setUp() {
        mvc = TestPlayers.anonymous(context);
        username = TestPlayers.uniqueUsername();
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions login(String password) throws Exception {
        return postJson("/api/auth/login",
                "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");
    }

    @Test
    void registrationReturnsAWorkingToken() throws Exception {
        String body = postJson("/api/auth/register",
                "{\"username\": \"" + username + "\", \"password\": \"" + TestPlayers.PASSWORD + "\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.username").value(username))
                .andReturn().getResponse().getContentAsString();

        TestPlayers.as(context, JsonPath.read(body, "$.accessToken"))
                .perform(get("/api/characters"))
                .andExpect(status().isOk());
    }

    @Test
    void loginReturnsAWorkingToken() throws Exception {
        TestPlayers.register(mvc, username);

        String body = login(TestPlayers.PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn().getResponse().getContentAsString();

        MockMvc player = TestPlayers.as(context, JsonPath.read(body, "$.accessToken"));
        String id = TestPlayers.createCharacter(player);
        player.perform(get("/api/characters/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        TestPlayers.register(mvc, username);

        login("such-guess-much-wrong")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Much wrong. Such username or password."));
    }

    @Test
    void unknownUserIs401() throws Exception {
        login(TestPlayers.PASSWORD)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usernameCanBeRegisteredOnlyOnce() throws Exception {
        TestPlayers.register(mvc, username);

        postJson("/api/auth/register",
                "{\"username\": \"" + username + "\", \"password\": \"another-password\"}")
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRegistrationListsFieldErrors() throws Exception {
        postJson("/api/auth/register", """
                {"username": "x!", "password": "short"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("must be 3-32 letters, digits or underscores"))
                .andExpect(jsonPath("$.errors.password").exists());
    }
}

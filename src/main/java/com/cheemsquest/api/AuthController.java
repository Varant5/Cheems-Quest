package com.cheemsquest.api;

import com.cheemsquest.domain.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Auth", description = "Register, log in and get a JWT. Such security.")
class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a player", description = "Creates the account and returns a token right away.")
    @ApiResponse(responseCode = "201", description = "Registered")
    @ApiResponse(responseCode = "400", description = "Invalid username or password",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Username already taken",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    TokenView register(@Valid @RequestBody RegisterRequest request) {
        return tokenService.issue(authService.register(request.username(), request.password()));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Exchanges username and password for a token.")
    @ApiResponse(responseCode = "200", description = "Logged in")
    @ApiResponse(responseCode = "401", description = "Wrong username or password",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    TokenView login(@Valid @RequestBody LoginRequest request) {
        return tokenService.issue(authService.authenticate(request.username(), request.password()));
    }
}

package com.cheemsquest.api;

import com.cheemsquest.domain.PlayerView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
class TokenService {

    static final String ISSUER = "cheems-quest";

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    TokenView issue(PlayerView player) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(properties.ttl()))
                .subject(player.id().toString())
                .claim("username", player.username())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenView(token, "Bearer", properties.ttl().toSeconds(), player.username());
    }
}

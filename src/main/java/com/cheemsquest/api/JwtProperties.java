package com.cheemsquest.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("cheemsquest.jwt")
record JwtProperties(String secret, Duration ttl) {
}

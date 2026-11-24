package com.cheemsquest.domain;

import com.cheemsquest.persistence.Player;
import com.cheemsquest.persistence.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PlayerRepository players;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PlayerView register(String username, String password) {
        if (players.existsByUsername(username)) {
            throw new GameRuleException("Username " + username + " is already taken. Much popular.");
        }
        Player player = players.save(new Player(username, passwordEncoder.encode(password)));
        return PlayerView.from(player);
    }

    @Transactional(readOnly = true)
    public PlayerView authenticate(String username, String password) {
        return players.findByUsername(username)
                .filter(player -> player.passwordHash()
                        .map(hash -> passwordEncoder.matches(password, hash))
                        .orElse(false))
                .map(PlayerView::from)
                .orElseThrow(InvalidCredentialsException::new);
    }
}

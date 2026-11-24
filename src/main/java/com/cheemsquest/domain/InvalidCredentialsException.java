package com.cheemsquest.domain;

public class InvalidCredentialsException extends GameException {

    public InvalidCredentialsException() {
        super("Much wrong. Such username or password.");
    }
}

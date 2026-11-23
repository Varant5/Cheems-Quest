package com.cheemsquest.domain;

public abstract class GameException extends RuntimeException {

    protected GameException(String message) {
        super(message);
    }
}

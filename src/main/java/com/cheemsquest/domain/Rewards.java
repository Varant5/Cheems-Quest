package com.cheemsquest.domain;

import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.Item;

final class Rewards {

    private Rewards() {
    }

    static void grantExperience(GameCharacter character, int amount, GameLog log) {
        int levelsGained = character.gainExperience(amount);
        log.add(character.getName() + " gains " + amount + " EXP.");
        if (levelsGained > 0) {
            log.add("LEVEL UP! " + character.getName() + " is now level " + character.getLevel() + ". Much stronger!");
        }
    }

    static void grantItem(GameCharacter character, Item item, GameLog log) {
        character.addItem(item);
        log.add(character.getName() + " receives " + item.getName() + " (" + item.getRarity() + ").");
    }
}

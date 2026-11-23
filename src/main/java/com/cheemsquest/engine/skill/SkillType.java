package com.cheemsquest.engine.skill;

public enum SkillType {
    BONK(new Bonk()),
    BITE(new Bite()),
    BONKING_SPIN(new BonkingSpin());

    private final Skill skill;

    SkillType(Skill skill) {
        this.skill = skill;
    }

    public Skill skill() {
        return skill;
    }
}

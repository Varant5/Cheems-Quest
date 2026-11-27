package com.cheemsquest.engine.skill;

public enum SkillType {
    BONK(new Bonk(), true),
    BITE(new Bite(), true),
    BONKING_SPIN(new BonkingSpin(), true),
    TOXIC_DROOL(new ToxicDrool(), false),
    HEADBUTT(new Headbutt(), false);

    private final Skill skill;
    private final boolean playerSkill;

    SkillType(Skill skill, boolean playerSkill) {
        this.skill = skill;
        this.playerSkill = playerSkill;
    }

    public Skill skill() {
        return skill;
    }

    public boolean isPlayerSkill() {
        return playerSkill;
    }
}

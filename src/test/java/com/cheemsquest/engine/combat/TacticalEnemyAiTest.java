package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.ConstantRandom;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.StatusEffect;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.random.WeightedTable;
import com.cheemsquest.engine.skill.Bite;
import com.cheemsquest.engine.skill.Bonk;
import com.cheemsquest.engine.skill.BonkingSpin;
import com.cheemsquest.engine.skill.Headbutt;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.ToxicDrool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TacticalEnemyAiTest {

    private final Skill bonk = new Bonk();
    private final Skill bite = new Bite();
    private final Skill spin = new BonkingSpin();
    private final Skill drool = new ToxicDrool();
    private final Skill headbutt = new Headbutt();
    private final Combatant shibe = new Combatant("Soggy Shibe", new Stats(28, 8, 1));
    private final Combatant cheems = new Combatant("Cheems", new Stats(50, 20, 2));

    private static EnemyAi ai(Object... skillsAndWeights) {
        WeightedTable.Builder<Skill> table = WeightedTable.builder();
        for (int i = 0; i < skillsAndWeights.length; i += 2) {
            table.add((Skill) skillsAndWeights[i], (Integer) skillsAndWeights[i + 1]);
        }
        return new TacticalEnemyAi(table.build());
    }

    @Test
    void neverWastesATurnOnAreaSkillsAgainstOneTarget() {
        EnemyAi ai = ai(spin, 9, bonk, 1);

        assertThat(ai.chooseSkill(shibe, cheems, new ConstantRandom(0.0))).isSameAs(bonk);
    }

    @Test
    void doesNotPoisonATargetThatIsAlreadyPoisoned() {
        EnemyAi ai = ai(drool, 9, bonk, 1);

        assertThat(ai.chooseSkill(shibe, cheems, new ConstantRandom(0.0))).isSameAs(drool);
        cheems.afflict(StatusEffect.poison(2, 2));
        assertThat(ai.chooseSkill(shibe, cheems, new ConstantRandom(0.0))).isSameAs(bonk);
    }

    @Test
    void doesNotStunATargetThatIsAlreadyStunned() {
        EnemyAi ai = ai(headbutt, 9, bonk, 1);
        cheems.afflict(StatusEffect.stun(1));

        assertThat(ai.chooseSkill(shibe, cheems, new ConstantRandom(0.0))).isSameAs(bonk);
    }

    @Test
    void goesForTheFinisherWhenTheTargetIsLow() {
        EnemyAi ai = ai(bonk, 1, bite, 1);
        Combatant hurt = new Combatant("Cheems", new Stats(50, 20, 2), 15);

        assertThat(ai.chooseSkill(shibe, cheems, new ConstantRandom(0.3))).isSameAs(bonk);
        assertThat(ai.chooseSkill(shibe, hurt, new ConstantRandom(0.3))).isSameAs(bite);
    }

    @Test
    void fallsBackToTheOriginalTableWhenNothingIsLeft() {
        EnemyAi ai = ai(drool, 1);
        cheems.afflict(StatusEffect.poison(2, 2));

        assertThat(ai.chooseSkill(shibe, cheems, new ConstantRandom(0.5))).isSameAs(drool);
    }
}

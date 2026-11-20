package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.ConstantRandom;
import com.cheemsquest.engine.FixedRandom;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.random.WeightedTable;
import com.cheemsquest.engine.skill.Bite;
import com.cheemsquest.engine.skill.Bonk;
import com.cheemsquest.engine.skill.BonkingSpin;
import com.cheemsquest.engine.skill.Skill;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeightedEnemyAiTest {

    private final Skill bonk = new Bonk();
    private final Skill bite = new Bite();
    private final Skill spin = new BonkingSpin();
    private final Combatant karen = new Combatant("Karen Doge", new Stats(30, 10, 0));

    @Test
    void uniformAiSpreadsRollsEvenlyAcrossSkills() {
        EnemyAi ai = WeightedEnemyAi.uniform(bonk, bite, spin);

        assertThat(ai.chooseSkill(karen, new ConstantRandom(0.0))).isSameAs(bonk);
        assertThat(ai.chooseSkill(karen, new ConstantRandom(0.5))).isSameAs(bite);
        assertThat(ai.chooseSkill(karen, new ConstantRandom(0.9))).isSameAs(spin);
    }

    @Test
    void weightedAiPrefersHeavierSkills() {
        EnemyAi ai = new WeightedEnemyAi(WeightedTable.<Skill>builder()
                .add(bonk, 9)
                .add(bite, 1)
                .build());

        assertThat(ai.chooseSkill(karen, new ConstantRandom(0.89))).isSameAs(bonk);
        assertThat(ai.chooseSkill(karen, new ConstantRandom(0.9))).isSameAs(bite);
    }

    @Test
    void battleUsesSkillChosenByAi() {
        Combatant cheems = new Combatant("Cheems", new Stats(50, 20, 2));
        Combatant karen = new Combatant("Karen Doge", new Stats(100, 10, 0));
        EnemyAi alwaysBite = WeightedEnemyAi.uniform(bite);
        // Cheems Bonk: no crit, neutral roll. Karen: AI roll, then Bite misses.
        Battle battle = new Battle(cheems, List.of(karen), alwaysBite, FixedRandom.of(0.5, 0.5, 0.0, 0.1));

        RoundResult round = battle.act(bonk, karen);

        assertThat(round.actions().get(1).skillName()).isEqualTo("Bite");
        assertThat(cheems.getHp()).isEqualTo(50);
    }
}

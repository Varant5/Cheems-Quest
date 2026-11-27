package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.ConstantRandom;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.PhaseShift;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.skill.Bite;
import com.cheemsquest.engine.skill.Bonk;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.SkillResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhasedEnemyAiTest {

    private static final Skill BONK = new Bonk();
    private static final Skill BITE = new Bite();
    private static final String ENRAGED = "Karen Doge is ENRAGED!";

    private final PhasedEnemyAi ai = new PhasedEnemyAi((enemy, target, rng) -> BONK,
            List.of(new PhasedEnemyAi.Phase(0.5, ENRAGED, (enemy, target, rng) -> BITE)));
    private final Combatant cheems = new Combatant("Cheems", new Stats(50, 20, 2));

    private static Combatant karen(int hp) {
        return new Combatant("Karen Doge", new Stats(30, 10, 0), hp);
    }

    @Test
    void switchesSkillsBelowTheThreshold() {
        assertThat(ai.chooseSkill(karen(30), cheems, ConstantRandom.NEUTRAL)).isSameAs(BONK);
        assertThat(ai.chooseSkill(karen(16), cheems, ConstantRandom.NEUTRAL)).isSameAs(BONK);
        assertThat(ai.chooseSkill(karen(15), cheems, ConstantRandom.NEUTRAL)).isSameAs(BITE);
    }

    @Test
    void announcesOnlyTheRoundThatCrossesTheThreshold() {
        assertThat(ai.phaseShift(karen(10), 30)).contains(ENRAGED);
        assertThat(ai.phaseShift(karen(10), 12)).isEmpty();
        assertThat(ai.phaseShift(karen(20), 30)).isEmpty();
    }

    @Test
    void battleAnnouncesThePhaseBeforeTheEnragedAttack() {
        Combatant karen = karen(30);
        Battle battle = new Battle(cheems, List.of(karen), new PerEnemyAi().assign(karen, ai), ConstantRandom.NEUTRAL);

        RoundResult round = battle.act(BONK, karen);

        assertThat(round.actions().get(1)).isEqualTo(new PhaseShift(karen, ENRAGED));
        assertThat(round.skillResults()).extracting(SkillResult::skillName).containsExactly("Bonk", "Bite");

        RoundResult next = battle.act(BONK, karen);
        assertThat(next.actions()).noneMatch(PhaseShift.class::isInstance);
    }

    @Test
    void phaseThresholdsMustGoDown() {
        EnemyAi any = (enemy, target, rng) -> BONK;

        assertThatThrownBy(() -> new PhasedEnemyAi(any, List.of(
                new PhasedEnemyAi.Phase(0.3, "first", any),
                new PhasedEnemyAi.Phase(0.6, "second", any))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PhasedEnemyAi.Phase(1.0, "never", any))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void perEnemyAiRejectsUnknownEnemies() {
        assertThatThrownBy(() -> new PerEnemyAi().chooseSkill(karen(30), cheems, ConstantRandom.NEUTRAL))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

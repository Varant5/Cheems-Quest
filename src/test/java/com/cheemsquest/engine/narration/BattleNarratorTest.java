package com.cheemsquest.engine.narration;

import com.cheemsquest.engine.combat.BattleStatus;
import com.cheemsquest.engine.combat.RoundResult;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.skill.Hit;
import com.cheemsquest.engine.skill.HitOutcome;
import com.cheemsquest.engine.skill.SkillResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BattleNarratorTest {

    private final Combatant cheems = new Combatant("Cheems", new Stats(65, 14, 3));

    private static Combatant enemy(String name, int hp) {
        return new Combatant(name, new Stats(30, 8, 1), hp);
    }

    @Test
    void narratesRegularHit() {
        Combatant karen = enemy("Wild Karen Doge", 16);

        List<String> lines = BattleNarrator.narrate(new SkillResult("Bonk", cheems,
                List.of(new Hit(karen, 14, HitOutcome.HIT))));

        assertThat(lines).containsExactly("Cheems uses Bonk!", "Wild Karen Doge loses 14 HP.");
    }

    @Test
    void narratesMiss() {
        Combatant karen = enemy("Wild Karen Doge", 30);

        List<String> lines = BattleNarrator.narrate(new SkillResult("Bite", cheems, List.of(Hit.miss(karen))));

        assertThat(lines).containsExactly("Cheems uses Bite!", "Wild Karen Doge dodges. Such dodge.");
    }

    @Test
    void narratesCriticalHitThatKnocksTheTargetDown() {
        Combatant karen = enemy("Wild Karen Doge", 0);

        List<String> lines = BattleNarrator.narrate(new SkillResult("Bonk", cheems,
                List.of(new Hit(karen, 21, HitOutcome.CRITICAL))));

        assertThat(lines).containsExactly(
                "Cheems uses Bonk!",
                "CRITICAL! Wild Karen Doge loses 21 HP.",
                "Wild Karen Doge is down!");
    }

    @Test
    void zeroDamageOnAFaintedTargetDoesNotAnnounceItAgain() {
        Combatant lilDoge = enemy("Neighbor's Lil Doge", 0);

        List<String> lines = BattleNarrator.narrate(new SkillResult("Bonking Spin", cheems,
                List.of(new Hit(lilDoge, 0, HitOutcome.HIT))));

        assertThat(lines).doesNotContain("Neighbor's Lil Doge is down!");
    }

    @Test
    void narratesWholeRoundInOrder() {
        Combatant karen = enemy("Wild Karen Doge", 16);
        RoundResult round = new RoundResult(List.of(
                new SkillResult("Bonk", cheems, List.of(new Hit(karen, 14, HitOutcome.HIT))),
                new SkillResult("Bite", karen, List.of(Hit.miss(cheems)))),
                BattleStatus.PLAYER_TURN);

        assertThat(BattleNarrator.narrate(round)).containsExactly(
                "Cheems uses Bonk!",
                "Wild Karen Doge loses 14 HP.",
                "Wild Karen Doge uses Bite!",
                "Cheems dodges. Such dodge.");
    }
}

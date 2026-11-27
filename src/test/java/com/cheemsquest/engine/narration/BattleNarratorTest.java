package com.cheemsquest.engine.narration;

import com.cheemsquest.engine.combat.BattleStatus;
import com.cheemsquest.engine.combat.RoundResult;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.PhaseShift;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.model.StatusEffect;
import com.cheemsquest.engine.model.StatusTick;
import com.cheemsquest.engine.model.StatusType;
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
    void narratesItemsUsedInAFight() {
        assertThat(BattleNarrator.narrate(new SkillResult("Cheemsburger", cheems,
                List.of(new Hit(cheems, 30, HitOutcome.HEAL)))))
                .containsExactly("Cheems uses Cheemsburger!", "Cheems heals 30 HP. Much yum.");
        assertThat(BattleNarrator.narrate(new SkillResult("Lucky Dice", cheems,
                List.of(new Hit(cheems, 0, HitOutcome.LUCK)))))
                .containsExactly("Cheems uses Lucky Dice!", "Cheems feels lucky. The next attack will crit!");
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

    @Test
    void narratesInflictedStatuses() {
        Combatant shibe = enemy("Soggy Shibe", 28);

        assertThat(BattleNarrator.narrate(new SkillResult("Toxic Drool", shibe,
                List.of(new Hit(cheems, 5, HitOutcome.HIT, StatusEffect.poison(3, 3)))))).containsExactly(
                "Soggy Shibe uses Toxic Drool!",
                "Cheems loses 5 HP.",
                "Cheems is poisoned for 3 turns! Much sick.");
        assertThat(BattleNarrator.narrate(new SkillResult("Headbutt", shibe,
                List.of(new Hit(cheems, 12, HitOutcome.HIT, StatusEffect.stun(1)))))).containsExactly(
                "Soggy Shibe uses Headbutt!",
                "Cheems loses 12 HP.",
                "Cheems is stunned! Such dizzy.");
    }

    @Test
    void narratesPhaseShiftsVerbatim() {
        assertThat(BattleNarrator.narrate(new PhaseShift(enemy("Abominable Snow Doge", 10), "It is ENRAGED!")))
                .containsExactly("It is ENRAGED!");
    }

    @Test
    void narratesStatusTicks() {
        Combatant karen = enemy("Wild Karen Doge", 0);

        assertThat(BattleNarrator.narrate(new StatusTick(cheems, StatusType.POISON, 3)))
                .containsExactly("Cheems takes 3 poison damage.");
        assertThat(BattleNarrator.narrate(new StatusTick(karen, StatusType.POISON, 2)))
                .containsExactly("Wild Karen Doge takes 2 poison damage.", "Wild Karen Doge is down!");
        assertThat(BattleNarrator.narrate(new StatusTick(cheems, StatusType.STUN, 0)))
                .containsExactly("Cheems is stunned and can't move.");
    }
}

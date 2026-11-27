package com.cheemsquest.engine.narration;

import com.cheemsquest.engine.combat.RoundResult;
import com.cheemsquest.engine.model.BattleEvent;
import com.cheemsquest.engine.model.PhaseShift;
import com.cheemsquest.engine.model.StatusEffect;
import com.cheemsquest.engine.model.StatusTick;
import com.cheemsquest.engine.skill.Hit;
import com.cheemsquest.engine.skill.SkillResult;

import java.util.ArrayList;
import java.util.List;

public final class BattleNarrator {

    private BattleNarrator() {
    }

    public static List<String> narrate(RoundResult round) {
        return round.actions().stream()
                .flatMap(action -> narrate(action).stream())
                .toList();
    }

    public static List<String> narrate(BattleEvent event) {
        return switch (event) {
            case SkillResult result -> narrateSkill(result);
            case StatusTick tick -> narrateTick(tick);
            case PhaseShift shift -> List.of(shift.announcement());
            default -> throw new IllegalArgumentException("Unknown battle event " + event);
        };
    }

    private static List<String> narrateSkill(SkillResult result) {
        List<String> lines = new ArrayList<>();
        lines.add(result.user().getName() + " uses " + result.skillName() + "!");
        for (Hit hit : result.hits()) {
            String target = hit.target().getName();
            lines.add(switch (hit.outcome()) {
                case MISS -> target + " dodges. Such dodge.";
                case CRITICAL -> "CRITICAL! " + target + " loses " + hit.damage() + " HP.";
                case HIT -> target + " loses " + hit.damage() + " HP.";
                case HEAL -> target + " heals " + hit.damage() + " HP. Much yum.";
                case LUCK -> target + " feels lucky. The next attack will crit!";
            });
            if (hit.inflicted() != null) {
                lines.add(inflicted(target, hit.inflicted()));
            }
            if (hit.outcome().dealsDamage() && hit.damage() > 0 && !hit.target().isAlive()) {
                lines.add(target + " is down!");
            }
        }
        return lines;
    }

    private static String inflicted(String target, StatusEffect effect) {
        return switch (effect.type()) {
            case POISON -> target + " is poisoned for " + effect.turns() + " turns! Much sick.";
            case STUN -> target + " is stunned! Such dizzy.";
        };
    }

    private static List<String> narrateTick(StatusTick tick) {
        String name = tick.actor().getName();
        return switch (tick.type()) {
            case POISON -> tick.actor().isAlive()
                    ? List.of(name + " takes " + tick.damage() + " poison damage.")
                    : List.of(name + " takes " + tick.damage() + " poison damage.", name + " is down!");
            case STUN -> List.of(name + " is stunned and can't move.");
        };
    }
}

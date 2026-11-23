package com.cheemsquest.engine.narration;

import com.cheemsquest.engine.combat.RoundResult;
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

    public static List<String> narrate(SkillResult result) {
        List<String> lines = new ArrayList<>();
        lines.add(result.user().getName() + " uses " + result.skillName() + "!");
        for (Hit hit : result.hits()) {
            String target = hit.target().getName();
            lines.add(switch (hit.outcome()) {
                case MISS -> target + " dodges. Such dodge.";
                case CRITICAL -> "CRITICAL! " + target + " loses " + hit.damage() + " HP.";
                case HIT -> target + " loses " + hit.damage() + " HP.";
            });
            if (hit.damage() > 0 && !hit.target().isAlive()) {
                lines.add(target + " is down!");
            }
        }
        return lines;
    }
}

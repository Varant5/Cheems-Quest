package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.skill.Skill;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

public final class PhasedEnemyAi implements EnemyAi {

    public record Phase(double hpThreshold, String announcement, EnemyAi ai) {

        public Phase {
            if (hpThreshold <= 0 || hpThreshold >= 1) {
                throw new IllegalArgumentException("phase threshold must be in (0, 1), got " + hpThreshold);
            }
            Objects.requireNonNull(announcement, "announcement");
            Objects.requireNonNull(ai, "ai");
        }
    }

    private final EnemyAi opening;
    private final List<Phase> phases;

    public PhasedEnemyAi(EnemyAi opening, List<Phase> phases) {
        this.opening = Objects.requireNonNull(opening, "opening");
        this.phases = List.copyOf(phases);
        for (int i = 1; i < this.phases.size(); i++) {
            if (this.phases.get(i).hpThreshold() >= this.phases.get(i - 1).hpThreshold()) {
                throw new IllegalArgumentException("phase thresholds must go down, got " + phases);
            }
        }
    }

    @Override
    public Skill chooseSkill(Combatant enemy, Combatant target, RandomGenerator rng) {
        int phase = phaseAt(enemy.getHp(), enemy.getStats().maxHp());
        EnemyAi ai = phase == 0 ? opening : phases.get(phase - 1).ai();
        return ai.chooseSkill(enemy, target, rng);
    }

    @Override
    public Optional<String> phaseShift(Combatant enemy, int hpAtRoundStart) {
        int maxHp = enemy.getStats().maxHp();
        int now = phaseAt(enemy.getHp(), maxHp);
        if (now > phaseAt(hpAtRoundStart, maxHp)) {
            return Optional.of(phases.get(now - 1).announcement());
        }
        return Optional.empty();
    }

    int phaseAt(int hp, int maxHp) {
        int phase = 0;
        for (Phase candidate : phases) {
            if (hp <= candidate.hpThreshold() * maxHp) {
                phase++;
            }
        }
        return phase;
    }
}

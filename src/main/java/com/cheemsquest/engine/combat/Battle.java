package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.SkillResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public class Battle {

    private final Combatant player;
    private final List<Combatant> enemies;
    private final EnemyAi enemyAi;
    private final RandomGenerator rng;
    private final List<SkillResult> log = new ArrayList<>();
    private BattleState state = new PlayerTurn();

    public Battle(Combatant player, List<Combatant> enemies, EnemyAi enemyAi, RandomGenerator rng) {
        this.player = Objects.requireNonNull(player, "player");
        this.enemies = List.copyOf(enemies);
        this.enemyAi = Objects.requireNonNull(enemyAi, "enemyAi");
        this.rng = Objects.requireNonNull(rng, "rng");
        if (!player.isAlive()) {
            throw new IllegalArgumentException("player must be alive to start a battle");
        }
        if (livingEnemies().isEmpty()) {
            throw new IllegalArgumentException("battle needs at least one living enemy");
        }
    }

    public RoundResult act(Skill skill, Combatant target) {
        Objects.requireNonNull(skill, "skill");
        int logSizeBefore = log.size();

        state = state.onPlayerAction(this, skill, target);
        while (state.status() == BattleStatus.ENEMY_TURN) {
            state = state.advance(this);
        }

        return new RoundResult(log.subList(logSizeBefore, log.size()), state.status());
    }

    /**
     * Convenience overload: AoE skills hit everyone, single-target skills
     * pick the only living enemy (fails if there is more than one).
     */
    public RoundResult act(Skill skill) {
        List<Combatant> living = livingEnemies();
        Combatant target = living.size() == 1 ? living.getFirst() : null;
        return act(skill, target);
    }

    public BattleStatus getStatus() {
        return state.status();
    }

    public Combatant getPlayer() {
        return player;
    }

    public List<Combatant> getEnemies() {
        return enemies;
    }

    public List<SkillResult> getLog() {
        return List.copyOf(log);
    }

    List<Combatant> livingEnemies() {
        return enemies.stream().filter(Combatant::isAlive).toList();
    }

    List<Combatant> resolveTargets(Skill skill, Combatant target) {
        return switch (skill.targeting()) {
            case ALL_ENEMIES -> livingEnemies();
            case SELF -> List.of(player);
            case SINGLE -> {
                if (target == null) {
                    throw new IllegalArgumentException(skill.name() + " needs a target");
                }
                if (!enemies.contains(target)) {
                    throw new IllegalArgumentException(target.getName() + " is not an enemy in this battle");
                }
                if (!target.isAlive()) {
                    throw new IllegalArgumentException(target.getName() + " is already defeated");
                }
                yield List.of(target);
            }
        };
    }

    void record(SkillResult result) {
        log.add(result);
    }

    EnemyAi enemyAi() {
        return enemyAi;
    }

    RandomGenerator rng() {
        return rng;
    }
}

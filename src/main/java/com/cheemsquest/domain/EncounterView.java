package com.cheemsquest.domain;

import com.cheemsquest.engine.combat.BattleStatus;
import com.cheemsquest.persistence.Encounter;
import com.cheemsquest.persistence.EncounterEnemy;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record EncounterView(UUID id, BattleStatus status, List<EnemyView> enemies, boolean lucky) {

    public record EnemyView(int position, String code, String name, int hp, int maxHp) {
    }

    static EncounterView from(Encounter encounter) {
        List<EncounterEnemy> enemies = encounter.getEnemies();
        List<EnemyView> views = IntStream.range(0, enemies.size())
                .mapToObj(i -> {
                    EncounterEnemy e = enemies.get(i);
                    return new EnemyView(i, e.getEnemy().getCode(), e.getEnemy().getName(),
                            e.getCurrentHp(), e.getEnemy().getMaxHp());
                })
                .toList();
        return new EncounterView(encounter.getId(), encounter.getStatus(), views, encounter.isPlayerLucky());
    }

    String enemyNames() {
        return enemies.stream().map(EnemyView::name).collect(Collectors.joining(" and "));
    }

    String introduction() {
        return enemyNames() + (enemies.size() == 1 ? " blocks" : " block") + " the path!";
    }
}

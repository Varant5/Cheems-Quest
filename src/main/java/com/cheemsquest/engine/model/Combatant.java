package com.cheemsquest.engine.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Combatant {

    private final String name;
    private final Stats stats;
    private final Map<StatusType, StatusEffect> statuses = new EnumMap<>(StatusType.class);
    private int hp;
    private boolean lucky;

    public Combatant(String name, Stats stats) {
        this(name, stats, stats.maxHp());
    }

    public Combatant(String name, Stats stats, int hp) {
        this.name = Objects.requireNonNull(name, "name");
        this.stats = Objects.requireNonNull(stats, "stats");
        if (hp < 0 || hp > stats.maxHp()) {
            throw new IllegalArgumentException("hp must be in [0, " + stats.maxHp() + "], got " + hp);
        }
        this.hp = hp;
    }

    public int takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("damage cannot be negative, got " + amount);
        }
        int dealt = Math.min(amount, hp);
        hp -= dealt;
        return dealt;
    }

    public int heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("healing cannot be negative, got " + amount);
        }
        if (!isAlive()) {
            throw new IllegalStateException(name + " is down and cannot be healed");
        }
        int healed = Math.min(amount, stats.maxHp() - hp);
        hp += healed;
        return healed;
    }

    public void afflict(StatusEffect effect) {
        Objects.requireNonNull(effect, "effect");
        statuses.merge(effect.type(), effect, StatusEffect::strongest);
    }

    public List<StatusEffect> getStatuses() {
        return List.copyOf(statuses.values());
    }

    public boolean has(StatusType type) {
        return statuses.containsKey(type);
    }

    public List<StatusTick> startTurn() {
        List<StatusTick> ticks = new ArrayList<>();
        StatusEffect poison = statuses.get(StatusType.POISON);
        if (poison != null && isAlive()) {
            ticks.add(new StatusTick(this, StatusType.POISON, takeDamage(poison.power())));
        }
        if (statuses.containsKey(StatusType.STUN) && isAlive()) {
            ticks.add(new StatusTick(this, StatusType.STUN, 0));
        }
        Iterator<Map.Entry<StatusType, StatusEffect>> entries = statuses.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<StatusType, StatusEffect> entry = entries.next();
            StatusEffect effect = entry.getValue();
            if (effect.turns() == 1) {
                entries.remove();
            } else {
                entry.setValue(new StatusEffect(effect.type(), effect.turns() - 1, effect.power()));
            }
        }
        return ticks;
    }

    public void bless() {
        lucky = true;
    }

    public boolean isLucky() {
        return lucky;
    }

    public boolean consumeLuck() {
        boolean wasLucky = lucky;
        lucky = false;
        return wasLucky;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public String getName() {
        return name;
    }

    public Stats getStats() {
        return stats;
    }

    public int getHp() {
        return hp;
    }

    @Override
    public String toString() {
        return name + " (" + hp + "/" + stats.maxHp() + " HP)";
    }
}

package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.FixedRandom;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.Stats;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkillTest {

    private static final double NEUTRAL_ROLL = 0.5;

    private final Combatant cheems = new Combatant("Cheems", new Stats(50, 20, 2));

    private static Combatant enemy(int defense) {
        return new Combatant("Karen Doge", new Stats(100, 10, defense));
    }

    @Nested
    class BonkTest {

        private final Skill bonk = new Bonk();

        @Test
        void dealsAttackMinusDefense() {
            Combatant karen = enemy(4);

            SkillResult result = bonk.use(cheems, List.of(karen), FixedRandom.of(0.9, NEUTRAL_ROLL));

            assertThat(result.hits()).singleElement().satisfies(hit -> {
                assertThat(hit.damage()).isEqualTo(16);
                assertThat(hit.outcome()).isEqualTo(HitOutcome.HIT);
            });
            assertThat(karen.getHp()).isEqualTo(84);
        }

        @Test
        void criticalHitMultipliesDamage() {
            Combatant karen = enemy(4);

            SkillResult result = bonk.use(cheems, List.of(karen), FixedRandom.of(0.05, NEUTRAL_ROLL));

            // 20 * 1.5 - 4
            assertThat(result.hits().getFirst().damage()).isEqualTo(26);
            assertThat(result.hits().getFirst().outcome()).isEqualTo(HitOutcome.CRITICAL);
        }

        @Test
        void alwaysDealsAtLeastOneDamage() {
            Combatant weakling = new Combatant("Baby Cheems", new Stats(10, 1, 0));
            Combatant tank = enemy(50);

            SkillResult result = bonk.use(weakling, List.of(tank), FixedRandom.of(0.9, NEUTRAL_ROLL));

            assertThat(result.totalDamage()).isEqualTo(1);
        }

        @Test
        void reportsOnlyDamageActuallyDealt() {
            Combatant almostDead = new Combatant("Karen Doge", new Stats(100, 10, 0), 5);

            SkillResult result = bonk.use(cheems, List.of(almostDead), FixedRandom.of(0.9, NEUTRAL_ROLL));

            assertThat(result.totalDamage()).isEqualTo(5);
            assertThat(almostDead.isAlive()).isFalse();
        }

        @Test
        void rejectsMultipleTargets() {
            assertThatThrownBy(() -> bonk.use(cheems, List.of(enemy(0), enemy(0)), FixedRandom.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class BiteTest {

        private final Skill bite = new Bite();

        @Test
        void hitsHard() {
            Combatant karen = enemy(4);

            SkillResult result = bite.use(cheems, List.of(karen), FixedRandom.of(0.9, NEUTRAL_ROLL));

            // 20 * 1.6 - 4
            assertThat(result.totalDamage()).isEqualTo(28);
            assertThat(result.hits().getFirst().outcome()).isEqualTo(HitOutcome.HIT);
        }

        @Test
        void canMiss() {
            Combatant karen = enemy(4);

            SkillResult result = bite.use(cheems, List.of(karen), FixedRandom.of(0.1));

            assertThat(result.hits().getFirst().outcome()).isEqualTo(HitOutcome.MISS);
            assertThat(result.totalDamage()).isZero();
            assertThat(karen.getHp()).isEqualTo(100);
        }
    }

    @Nested
    class BonkingSpinTest {

        private final Skill spin = new BonkingSpin();

        @Test
        void hitsEveryLivingEnemy() {
            Combatant armored = enemy(4);
            Combatant naked = enemy(0);
            Combatant dead = new Combatant("Dead Doge", new Stats(10, 1, 0), 0);

            SkillResult result = spin.use(cheems, List.of(armored, dead, naked),
                    FixedRandom.of(NEUTRAL_ROLL, NEUTRAL_ROLL));

            // 20 * 0.6 = 12 bazowo
            assertThat(result.hits()).extracting(Hit::target, Hit::damage)
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple(armored, 8),
                            org.assertj.core.groups.Tuple.tuple(naked, 12));
            assertThat(result.totalDamage()).isEqualTo(20);
        }

        @Test
        void rejectsEmptyTargets() {
            assertThatThrownBy(() -> spin.use(cheems, List.of(), FixedRandom.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}

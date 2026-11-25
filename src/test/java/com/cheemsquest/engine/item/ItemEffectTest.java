package com.cheemsquest.engine.item;

import com.cheemsquest.engine.ConstantRandom;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.skill.Hit;
import com.cheemsquest.engine.skill.HitOutcome;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.SkillResult;
import com.cheemsquest.engine.skill.Targeting;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemEffectTest {

    private final Combatant cheems = new Combatant("Cheems", new Stats(65, 14, 3), 20);

    @Test
    void healingItemHealsTheUser() {
        Skill burger = ItemEffect.HEAL.inBattle("Cheemsburger", 30).orElseThrow();

        SkillResult result = burger.use(cheems, List.of(cheems), ConstantRandom.NEUTRAL);

        assertThat(burger.name()).isEqualTo("Cheemsburger");
        assertThat(burger.targeting()).isEqualTo(Targeting.SELF);
        assertThat(result.hits()).containsExactly(new Hit(cheems, 30, HitOutcome.HEAL));
        assertThat(result.totalDamage()).isZero();
        assertThat(cheems.getHp()).isEqualTo(50);
    }

    @Test
    void luckyCharmBlessesTheUser() {
        Skill dice = ItemEffect.GUARANTEED_CRIT.inBattle("Lucky Dice", 0).orElseThrow();

        SkillResult result = dice.use(cheems, List.of(cheems), ConstantRandom.NEUTRAL);

        assertThat(dice.targeting()).isEqualTo(Targeting.SELF);
        assertThat(result.hits()).containsExactly(new Hit(cheems, 0, HitOutcome.LUCK));
        assertThat(cheems.isLucky()).isTrue();
    }

    @Test
    void permanentUpgradesAreNotForBattle() {
        assertThat(ItemEffect.PERMANENT_ATTACK.inBattle("Doge Coin", 10)).isEmpty();
    }
}

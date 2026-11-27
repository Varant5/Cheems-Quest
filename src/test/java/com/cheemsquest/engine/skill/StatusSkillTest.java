package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.ConstantRandom;
import com.cheemsquest.engine.FixedRandom;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.StatusEffect;
import com.cheemsquest.engine.model.Stats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatusSkillTest {

    private final Combatant shibe = new Combatant("Soggy Shibe", new Stats(28, 12, 1));
    private final Combatant cheems = new Combatant("Cheems", new Stats(50, 10, 2));

    @Test
    void toxicDroolDealsLightDamageAndPoisons() {
        SkillResult result = new ToxicDrool().use(shibe, List.of(cheems), ConstantRandom.NEUTRAL);

        Hit hit = result.hits().getFirst();
        assertThat(hit.damage()).isEqualTo(5);
        assertThat(hit.inflicted()).isEqualTo(StatusEffect.poison(3, 3));
        assertThat(cheems.getStatuses()).containsExactly(StatusEffect.poison(3, 3));
    }

    @Test
    void toxicDroolDoesNotPoisonAKnockedOutTarget() {
        Combatant fainting = new Combatant("Cheems", new Stats(50, 10, 2), 3);

        Hit hit = new ToxicDrool().use(shibe, List.of(fainting), ConstantRandom.NEUTRAL).hits().getFirst();

        assertThat(hit.inflicted()).isNull();
        assertThat(fainting.getStatuses()).isEmpty();
    }

    @Test
    void luckyToxicDroolCrits() {
        shibe.bless();

        Hit hit = new ToxicDrool().use(shibe, List.of(cheems), ConstantRandom.NEUTRAL).hits().getFirst();

        assertThat(hit.outcome()).isEqualTo(HitOutcome.CRITICAL);
        assertThat(hit.damage()).isEqualTo(9);
    }

    @Test
    void headbuttStunsWhenTheRollIsLow() {
        Hit hit = new Headbutt().use(shibe, List.of(cheems), FixedRandom.of(0.5, 0.1)).hits().getFirst();

        assertThat(hit.damage()).isEqualTo(12);
        assertThat(hit.inflicted()).isEqualTo(StatusEffect.stun(1));
        assertThat(cheems.getStatuses()).containsExactly(StatusEffect.stun(1));
    }

    @Test
    void headbuttUsuallyJustHurts() {
        Hit hit = new Headbutt().use(shibe, List.of(cheems), FixedRandom.of(0.5, 0.9)).hits().getFirst();

        assertThat(hit.outcome()).isEqualTo(HitOutcome.HIT);
        assertThat(hit.inflicted()).isNull();
        assertThat(cheems.getStatuses()).isEmpty();
    }

    @Test
    void luckyHeadbuttCrits() {
        shibe.bless();

        Hit hit = new Headbutt().use(shibe, List.of(cheems), FixedRandom.of(0.5, 0.9)).hits().getFirst();

        assertThat(hit.outcome()).isEqualTo(HitOutcome.CRITICAL);
    }

    @Test
    void frostBreathHitsHardAndOftenFreezes() {
        Hit frozen = new FrostBreath().use(shibe, List.of(cheems), FixedRandom.of(0.5, 0.4)).hits().getFirst();

        assertThat(frozen.damage()).isEqualTo(14);
        assertThat(frozen.inflicted()).isEqualTo(StatusEffect.stun(1));

        Combatant other = new Combatant("Cheems", new Stats(50, 10, 2));
        shibe.bless();
        Hit crit = new FrostBreath().use(shibe, List.of(other), FixedRandom.of(0.5, 0.6)).hits().getFirst();
        assertThat(crit.outcome()).isEqualTo(HitOutcome.CRITICAL);
        assertThat(crit.inflicted()).isNull();
    }

    @Test
    void onlyTheOriginalSkillsArePlayerSkills() {
        assertThat(SkillType.values()).filteredOn(SkillType::isPlayerSkill)
                .containsExactly(SkillType.BONK, SkillType.BITE, SkillType.BONKING_SPIN);
    }
}

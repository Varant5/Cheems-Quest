package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.ConstantRandom;
import com.cheemsquest.engine.item.ItemEffect;
import com.cheemsquest.engine.model.BattleEvent;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.model.StatusEffect;
import com.cheemsquest.engine.model.StatusTick;
import com.cheemsquest.engine.model.StatusType;
import com.cheemsquest.engine.skill.Bonk;
import com.cheemsquest.engine.skill.BonkingSpin;
import com.cheemsquest.engine.skill.HitOutcome;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.SkillResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// ConstantRandom.NEUTRAL: no crits, no misses, exact base damage.
// Cheems Bonk vs defense 0 = 20 dmg, Karen Bonk vs defense 2 = 8 dmg.
class BattleTest {

    private static final Skill BONK = new Bonk();
    private static final EnemyAi ALWAYS_BONK = (enemy, rng) -> BONK;

    private final Combatant cheems = new Combatant("Cheems", new Stats(50, 20, 2));

    private static Combatant karen(int hp) {
        return new Combatant("Karen Doge", new Stats(30, 10, 0), hp);
    }

    private Battle battle(Combatant player, Combatant... enemies) {
        return new Battle(player, List.of(enemies), ALWAYS_BONK, ConstantRandom.NEUTRAL);
    }

    @Test
    void startsOnPlayerTurn() {
        assertThat(battle(cheems, karen(30)).getStatus()).isEqualTo(BattleStatus.PLAYER_TURN);
    }

    @Test
    void enemyCounterattacksAndTurnReturnsToPlayer() {
        Combatant karen = karen(30);
        Battle battle = battle(cheems, karen);

        RoundResult round = battle.act(BONK, karen);

        assertThat(round.status()).isEqualTo(BattleStatus.PLAYER_TURN);
        assertThat(round.actions()).extracting(BattleEvent::actor).containsExactly(cheems, karen);
        assertThat(karen.getHp()).isEqualTo(10);
        assertThat(cheems.getHp()).isEqualTo(42);
    }

    @Test
    void killingLastEnemyEndsInVictoryWithoutCounterattack() {
        Combatant karen = karen(30);
        Battle battle = battle(cheems, karen);
        battle.act(BONK, karen);

        RoundResult round = battle.act(BONK, karen);

        assertThat(round.status()).isEqualTo(BattleStatus.VICTORY);
        assertThat(round.actions()).hasSize(1);
        assertThat(cheems.getHp()).isEqualTo(42);
        assertThat(battle.getLog()).hasSize(3);
    }

    @Test
    void playerDyingEndsInDefeat() {
        Combatant fragileCheems = new Combatant("Cheems", new Stats(50, 20, 2), 5);
        Combatant karen = karen(30);
        Battle battle = battle(fragileCheems, karen);

        RoundResult round = battle.act(BONK, karen);

        assertThat(round.status()).isEqualTo(BattleStatus.DEFEAT);
        assertThat(fragileCheems.isAlive()).isFalse();
    }

    @Test
    void enemiesStopAttackingOncePlayerIsDead() {
        Combatant fragileCheems = new Combatant("Cheems", new Stats(50, 20, 2), 5);
        Battle battle = battle(fragileCheems, karen(30), karen(30));

        RoundResult round = battle.act(new BonkingSpin());

        assertThat(round.status()).isEqualTo(BattleStatus.DEFEAT);
        assertThat(round.actions()).hasSize(2);
    }

    @Test
    void aoeHitsEveryoneAndOnlySurvivorsCounterattack() {
        Combatant weakKaren = karen(10);
        Combatant toughKaren = karen(30);
        Battle battle = battle(cheems, weakKaren, toughKaren);

        RoundResult round = battle.act(new BonkingSpin());

        assertThat(weakKaren.isAlive()).isFalse();
        assertThat(toughKaren.getHp()).isEqualTo(18);
        assertThat(round.actions()).extracting(BattleEvent::actor).containsExactly(cheems, toughKaren);
        assertThat(cheems.getHp()).isEqualTo(42);
    }

    @Test
    void cannotActAfterBattleIsOver() {
        Combatant karen = karen(1);
        Battle battle = battle(cheems, karen);
        battle.act(BONK, karen);

        assertThatThrownBy(() -> battle.act(BONK, karen))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void singleTargetSkillNeedsTargetWhenSeveralEnemiesAlive() {
        Battle battle = battle(cheems, karen(30), karen(30));

        assertThatThrownBy(() -> battle.act(BONK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("needs a target");
    }

    @Test
    void cannotTargetSomeoneOutsideTheBattle() {
        Battle battle = battle(cheems, karen(30));

        assertThatThrownBy(() -> battle.act(BONK, karen(30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not an enemy");
    }

    @Test
    void cannotTargetDefeatedEnemy() {
        Combatant deadKaren = karen(10);
        Combatant aliveKaren = karen(30);
        Battle battle = battle(cheems, deadKaren, aliveKaren);
        battle.act(BONK, deadKaren);

        assertThatThrownBy(() -> battle.act(BONK, deadKaren))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already defeated");
    }

    @Test
    void rejectedActionDoesNotChangeState() {
        Combatant karen = karen(30);
        Battle battle = battle(cheems, karen);

        assertThatThrownBy(() -> battle.act(BONK, karen(30))).isInstanceOf(IllegalArgumentException.class);

        assertThat(battle.getStatus()).isEqualTo(BattleStatus.PLAYER_TURN);
        assertThat(battle.getLog()).isEmpty();
        assertThat(karen.getHp()).isEqualTo(30);
    }

    @Test
    void healingItemTakesTheTurnAndEnemyStillAttacks() {
        Combatant hurt = new Combatant("Cheems", new Stats(50, 20, 2), 20);
        Combatant karen = karen(30);
        Battle battle = battle(hurt, karen);

        RoundResult round = battle.act(ItemEffect.HEAL.inBattle("Cheemsburger", 30).orElseThrow());

        assertThat(round.status()).isEqualTo(BattleStatus.PLAYER_TURN);
        assertThat(round.actions()).extracting(BattleEvent::actor).containsExactly(hurt, karen);
        assertThat(hurt.getHp()).isEqualTo(42);
        assertThat(karen.getHp()).isEqualTo(30);
    }

    @Test
    void luckyCharmMakesTheNextAttackCrit() {
        Combatant karen = karen(30);
        Battle battle = battle(cheems, karen);
        battle.act(ItemEffect.GUARANTEED_CRIT.inBattle("Lucky Dice", 0).orElseThrow());

        RoundResult round = battle.act(BONK, karen);

        assertThat(round.status()).isEqualTo(BattleStatus.VICTORY);
        assertThat(round.skillResults().getFirst().hits().getFirst().outcome()).isEqualTo(HitOutcome.CRITICAL);
        assertThat(cheems.isLucky()).isFalse();
    }

    @Test
    void cannotStartWithAFaintedPlayer() {
        Combatant fainted = new Combatant("Cheems", new Stats(50, 20, 2), 0);

        assertThatThrownBy(() -> battle(fainted, karen(30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("player must be alive");
    }

    @Test
    void cannotStartWithoutLivingEnemies() {
        assertThatThrownBy(() -> battle(cheems, karen(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one living enemy");
    }

    @Test
    void poisonedEnemyCanFaintOnItsOwnTurn() {
        Combatant karen = karen(25);
        karen.afflict(StatusEffect.poison(2, 10));
        Battle battle = battle(cheems, karen);

        RoundResult round = battle.act(BONK, karen);

        assertThat(round.status()).isEqualTo(BattleStatus.VICTORY);
        assertThat(round.actions()).last().isEqualTo(new StatusTick(karen, StatusType.POISON, 5));
        assertThat(cheems.getHp()).isEqualTo(50);
    }

    @Test
    void stunnedEnemyLosesItsAttack() {
        Combatant karen = karen(30);
        karen.afflict(StatusEffect.stun(1));
        Battle battle = battle(cheems, karen);

        RoundResult round = battle.act(BONK, karen);

        assertThat(round.status()).isEqualTo(BattleStatus.PLAYER_TURN);
        assertThat(round.actions()).last().isEqualTo(new StatusTick(karen, StatusType.STUN, 0));
        assertThat(cheems.getHp()).isEqualTo(50);
        assertThat(karen.getHp()).isEqualTo(10);
    }

    @Test
    void stunnedPlayerLosesTheirAction() {
        Combatant karen = karen(30);
        cheems.afflict(StatusEffect.stun(1));
        Battle battle = battle(cheems, karen);

        RoundResult round = battle.act(BONK, karen);

        assertThat(round.actions().getFirst()).isEqualTo(new StatusTick(cheems, StatusType.STUN, 0));
        assertThat(round.skillResults()).extracting(SkillResult::user).containsExactly(karen);
        assertThat(karen.getHp()).isEqualTo(30);
        assertThat(cheems.getHp()).isEqualTo(42);
    }

    @Test
    void poisonCanFinishThePlayerBeforeTheyAct() {
        Combatant hurt = new Combatant("Cheems", new Stats(50, 20, 2), 3);
        hurt.afflict(StatusEffect.poison(2, 5));
        Combatant karen = karen(30);
        Battle battle = battle(hurt, karen);

        RoundResult round = battle.act(BONK, karen);

        assertThat(round.status()).isEqualTo(BattleStatus.DEFEAT);
        assertThat(round.actions()).containsExactly(new StatusTick(hurt, StatusType.POISON, 3));
        assertThat(karen.getHp()).isEqualTo(30);
    }

    @Test
    void invalidTargetDoesNotTickStatuses() {
        cheems.afflict(StatusEffect.poison(2, 5));
        Battle battle = battle(cheems, karen(30), karen(30));

        assertThatThrownBy(() -> battle.act(BONK)).isInstanceOf(IllegalArgumentException.class);

        assertThat(cheems.getStatuses()).containsExactly(StatusEffect.poison(2, 5));
        assertThat(cheems.getHp()).isEqualTo(50);
    }
}

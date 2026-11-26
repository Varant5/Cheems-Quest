package com.cheemsquest.persistence;

import com.cheemsquest.TestcontainersConfiguration;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.skill.SkillType;
import com.cheemsquest.engine.world.Direction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PersistenceTest {

    @Autowired
    private LocationRepository locations;

    @Autowired
    private EnemyRepository enemies;

    @Autowired
    private PlayerRepository players;

    @Autowired
    private GameCharacterRepository characters;

    @Autowired
    private TestEntityManager em;

    @Test
    void seedsTheWorld() {
        assertThat(locations.count()).isEqualTo(8);
        assertThat(enemies.count()).isEqualTo(7);
    }

    @Test
    void locationExitsLeadToOtherLocations() {
        Location forest = locations.findById("WOW_FOREST").orElseThrow();

        assertThat(forest.getExits()).containsOnlyKeys(Direction.NORTH, Direction.SOUTH, Direction.WEST);
        assertThat(forest.exit(Direction.NORTH)).get().extracting(Location::getCode).isEqualTo("BUFF_DOGE_TOWER");
        assertThat(forest.exit(Direction.EAST)).isEmpty();
    }

    @Test
    void locationSpawnsReferenceEnemiesWithWeights() {
        Location basement = locations.findById("KARENS_BASEMENT").orElseThrow();

        assertThat(basement.getSpawns())
                .extracting(spawn -> spawn.getEnemy().getCode(), SpawnEntry::getWeight)
                .containsExactlyInAnyOrder(tuple("KAREN_DOGE", 3), tuple("LIL_DOGE", 1));
    }

    @Test
    void enemyHasStatsAndWeightedSkills() {
        Enemy buffDoge = enemies.findById("BUFF_DOGE").orElseThrow();

        assertThat(buffDoge.stats()).isEqualTo(new Stats(120, 16, 5));
        assertThat(buffDoge.getSkills())
                .extracting(EnemySkill::getSkill, EnemySkill::getWeight)
                .containsExactlyInAnyOrder(
                        tuple(SkillType.BONK, 2),
                        tuple(SkillType.BITE, 1),
                        tuple(SkillType.BONKING_SPIN, 1));
    }

    @Test
    void savesAndReloadsCharacter() {
        Player player = players.save(new Player("cheems_fan", "{noop}secret"));
        Location doghouse = locations.getReferenceById("CHEEMS_DOGHOUSE");
        GameCharacter cheems = characters.save(new GameCharacter(player, "Cheems", new Stats(65, 14, 3), doghouse));
        em.flush();
        em.clear();

        GameCharacter reloaded = characters.findById(cheems.getId()).orElseThrow();

        assertThat(reloaded.getName()).isEqualTo("Cheems");
        assertThat(reloaded.getLevel()).isEqualTo(1);
        assertThat(reloaded.getCurrentHp()).isEqualTo(65);
        assertThat(reloaded.getLocation().getCode()).isEqualTo("CHEEMS_DOGHOUSE");
        assertThat(reloaded.visitedLocationCodes()).containsExactly("CHEEMS_DOGHOUSE");
        assertThat(reloaded.getPlayer().getUsername()).isEqualTo("cheems_fan");
    }

    @Test
    void usernamesAreUnique() {
        players.saveAndFlush(new Player("doge", "{noop}secret"));

        assertThatThrownBy(() -> players.saveAndFlush(new Player("doge", "{noop}secret")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

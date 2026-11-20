package com.cheemsquest.console;

import com.cheemsquest.engine.combat.Battle;
import com.cheemsquest.engine.combat.BattleStatus;
import com.cheemsquest.engine.combat.EnemyAi;
import com.cheemsquest.engine.combat.RoundResult;
import com.cheemsquest.engine.combat.WeightedEnemyAi;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.random.WeightedTable;
import com.cheemsquest.engine.skill.Bite;
import com.cheemsquest.engine.skill.Bonk;
import com.cheemsquest.engine.skill.BonkingSpin;
import com.cheemsquest.engine.skill.Hit;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.SkillResult;
import com.cheemsquest.engine.skill.Targeting;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.random.RandomGenerator;

public final class ConsoleGame {

    private record SkillOption(Skill skill, String description) {
    }

    private static final List<SkillOption> PLAYER_SKILLS = List.of(
            new SkillOption(new Bonk(), "pewny cios, czasem krytyk"),
            new SkillOption(new Bite(), "mocne gryzienie, ale łatwo chybić"),
            new SkillOption(new BonkingSpin(), "słabszy obrót, trafia wszystkich"));

    private final Scanner in;
    private final PrintStream out;
    private final RandomGenerator rng;

    ConsoleGame(InputStream in, PrintStream out, RandomGenerator rng) {
        this.in = new Scanner(in);
        this.out = out;
        this.rng = rng;
    }

    public static void main(String[] args) {
        new ConsoleGame(System.in, System.out, RandomGenerator.getDefault()).run();
    }

    void run() {
        out.println("=== CHEEMS QUEST ===");
        out.println("Dziki Karen Doge i Mały Doge Sąsiada blokują ścieżkę!");

        Battle battle = createBattle();
        try {
            while (!battle.getStatus().isOver()) {
                playRound(battle);
            }
        } catch (EndOfInput e) {
            out.println();
            out.println("Cheems ucieka z walki. Do zobaczenia!");
            return;
        }

        out.println();
        if (battle.getStatus() == BattleStatus.VICTORY) {
            out.println("ZWYCIĘSTWO! Cheems zdobywa much respekt.");
        } else {
            out.println("PORAŻKA. Cheems idzie spać do budy...");
        }
    }

    private Battle createBattle() {
        Combatant cheems = new Combatant("Cheems", new Stats(65, 14, 3));
        Combatant karen = new Combatant("Dziki Karen Doge", new Stats(35, 9, 2));
        Combatant smallDoge = new Combatant("Mały Doge Sąsiada", new Stats(20, 6, 0));

        EnemyAi ai = new WeightedEnemyAi(WeightedTable.<Skill>builder()
                .add(new Bonk(), 3)
                .add(new Bite(), 1)
                .build());

        return new Battle(cheems, List.of(karen, smallDoge), ai, rng);
    }

    private void playRound(Battle battle) {
        printStatus(battle);
        Skill skill = chooseSkill();
        Combatant target = chooseTarget(battle, skill);

        RoundResult round = battle.act(skill, target);

        out.println();
        round.actions().forEach(this::narrate);
    }

    private void printStatus(Battle battle) {
        out.println();
        out.println(battle.getPlayer());
        out.println("Wrogowie:");
        battle.getEnemies().stream()
                .filter(Combatant::isAlive)
                .forEach(enemy -> out.println("  " + enemy));
    }

    private Skill chooseSkill() {
        out.println("Wybierz atak:");
        for (int i = 0; i < PLAYER_SKILLS.size(); i++) {
            SkillOption option = PLAYER_SKILLS.get(i);
            out.printf("  %d) %s - %s%n", i + 1, option.skill().name(), option.description());
        }
        return PLAYER_SKILLS.get(readChoice(PLAYER_SKILLS.size()) - 1).skill();
    }

    private Combatant chooseTarget(Battle battle, Skill skill) {
        if (skill.targeting() == Targeting.ALL_ENEMIES) {
            return null;
        }
        List<Combatant> living = battle.getEnemies().stream().filter(Combatant::isAlive).toList();
        if (living.size() == 1) {
            return living.getFirst();
        }
        out.println("Kogo bonkujesz?");
        for (int i = 0; i < living.size(); i++) {
            out.printf("  %d) %s%n", i + 1, living.get(i));
        }
        return living.get(readChoice(living.size()) - 1);
    }

    private int readChoice(int max) {
        while (true) {
            out.print("> ");
            if (!in.hasNextLine()) {
                throw new EndOfInput();
            }
            String line = in.nextLine().trim();
            try {
                int choice = Integer.parseInt(line);
                if (choice >= 1 && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the retry message
            }
            out.printf("Much confuse. Wybierz liczbę od 1 do %d.%n", max);
        }
    }

    private void narrate(SkillResult result) {
        out.println(result.user().getName() + " używa " + result.skillName() + "!");
        for (Hit hit : result.hits()) {
            String target = hit.target().getName();
            switch (hit.outcome()) {
                case MISS -> out.println("  " + target + " unika ataku. Such dodge.");
                case CRITICAL -> out.println("  KRYTYK! " + target + " traci " + hit.damage() + " HP.");
                case HIT -> out.println("  " + target + " traci " + hit.damage() + " HP.");
            }
            if (hit.damage() > 0 && !hit.target().isAlive()) {
                out.println("  " + target + " pada!");
            }
        }
    }

    private static final class EndOfInput extends RuntimeException {
    }
}

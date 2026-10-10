package com.sarah.skilltree.domain.catalog;

import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.domain.model.ExperienceReward;
import com.sarah.skilltree.domain.model.ItemReward;
import com.sarah.skilltree.domain.model.Objective;
import com.sarah.skilltree.domain.model.ObjectiveType;
import com.sarah.skilltree.domain.model.Quest;

import java.util.List;

/**
 * The quests that ship with the mod.
 *
 * <p>Every branch has a chain of quests that get harder and pay more, and ends
 * in a repeatable quest so skill points never run out.</p>
 */
public final class DefaultQuests {

    private DefaultQuests() {
    }

    public static QuestCatalog createCatalog() {
        return new QuestCatalog(List.of(
                // ── Mining ──────────────────────────────────────────────────
                Quest.builder("first_dig", Branch.MINING, Objective.of(ObjectiveType.MINE_STONE, 32))
                        .skillPoints(1).build(),
                Quest.builder("ore_hunter", Branch.MINING, Objective.of(ObjectiveType.MINE_ORE, 16))
                        .skillPoints(2).after("first_dig").build(),
                Quest.builder("deep_delver", Branch.MINING, Objective.of(ObjectiveType.MINE_ORE, 64))
                        .skillPoints(3).reward(new ExperienceReward(5)).after("ore_hunter").build(),
                Quest.builder("motherlode", Branch.MINING, Objective.of(ObjectiveType.MINE_ORE, 100))
                        .skillPoints(2).after("deep_delver").repeatable().build(),

                // ── Combat ──────────────────────────────────────────────────
                Quest.builder("monster_hunter", Branch.COMBAT, Objective.of(ObjectiveType.KILL_MONSTER, 10))
                        .skillPoints(1).build(),
                Quest.builder("slayer", Branch.COMBAT, Objective.of(ObjectiveType.KILL_MONSTER, 50))
                        .skillPoints(2).after("monster_hunter").build(),
                Quest.builder("exterminator", Branch.COMBAT, Objective.of(ObjectiveType.KILL_MONSTER, 150))
                        .skillPoints(3).reward(new ItemReward("minecraft:golden_apple", 2)).after("slayer").build(),
                Quest.builder("bounty", Branch.COMBAT, Objective.of(ObjectiveType.KILL_MONSTER, 100))
                        .skillPoints(2).after("exterminator").repeatable().build(),
                Quest.builder("wither_slayer", Branch.COMBAT,
                                Objective.ofTarget(ObjectiveType.KILL_MONSTER, "minecraft:wither", 1))
                        .skillPoints(5).reward(new ExperienceReward(10)).after("slayer").build(),

                // ── Exploration ─────────────────────────────────────────────
                Quest.builder("wanderer", Branch.EXPLORATION, Objective.of(ObjectiveType.TRAVEL_DISTANCE, 1000))
                        .skillPoints(1).build(),
                Quest.builder("pathfinder", Branch.EXPLORATION, Objective.of(ObjectiveType.TRAVEL_DISTANCE, 5000))
                        .skillPoints(2).after("wanderer").build(),
                Quest.builder("globetrotter", Branch.EXPLORATION, Objective.of(ObjectiveType.TRAVEL_DISTANCE, 10000))
                        .skillPoints(2).after("pathfinder").repeatable().build(),
                Quest.builder("swimmer", Branch.EXPLORATION, Objective.of(ObjectiveType.SWIM_DISTANCE, 300))
                        .skillPoints(1).build(),
                Quest.builder("into_the_nether", Branch.EXPLORATION,
                                Objective.ofTarget(ObjectiveType.ENTER_DIMENSION, "minecraft:the_nether", 1))
                        .skillPoints(2).build(),
                Quest.builder("the_end", Branch.EXPLORATION,
                                Objective.ofTarget(ObjectiveType.ENTER_DIMENSION, "minecraft:the_end", 1))
                        .skillPoints(3).after("into_the_nether").build(),
                Quest.builder("enchanter", Branch.EXPLORATION, Objective.of(ObjectiveType.ENCHANT_ITEM, 5))
                        .skillPoints(2).build(),

                // ── Nature ──────────────────────────────────────────────────
                Quest.builder("timber", Branch.NATURE, Objective.of(ObjectiveType.CHOP_LOG, 16))
                        .skillPoints(1).build(),
                Quest.builder("lumberjack", Branch.NATURE, Objective.of(ObjectiveType.CHOP_LOG, 96))
                        .skillPoints(2).after("timber").build(),
                Quest.builder("green_thumb", Branch.NATURE, Objective.of(ObjectiveType.HARVEST_CROP, 48))
                        .skillPoints(1).build(),
                Quest.builder("harvest_season", Branch.NATURE, Objective.of(ObjectiveType.HARVEST_CROP, 128))
                        .skillPoints(1).after("green_thumb").repeatable().build(),
                Quest.builder("matchmaker", Branch.NATURE, Objective.of(ObjectiveType.BREED_ANIMAL, 6))
                        .skillPoints(1).build(),
                Quest.builder("rancher", Branch.NATURE, Objective.of(ObjectiveType.BREED_ANIMAL, 30))
                        .skillPoints(2).after("matchmaker").build(),
                Quest.builder("gone_fishing", Branch.NATURE, Objective.of(ObjectiveType.CATCH_FISH, 10))
                        .skillPoints(2).build(),
                Quest.builder("merchant", Branch.NATURE, Objective.of(ObjectiveType.TRADE_WITH_VILLAGER, 10))
                        .skillPoints(2).build(),
                Quest.builder("well_rested", Branch.NATURE, Objective.of(ObjectiveType.SLEEP_IN_BED, 3))
                        .skillPoints(1).build()
        ));
    }
}

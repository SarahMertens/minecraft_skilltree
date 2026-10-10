package com.sarah.skilltree.presentation.watcher;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.model.ObjectiveType;
import com.sarah.skilltree.presentation.controller.QuestReporter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Turns Minecraft's own statistics into quest progress.
 *
 * <p>The game already counts how far a player walked, how many animals they
 * bred, how many fish they caught and so on (the Statistics screen). Instead
 * of detecting all of that again, this watcher reads those counters and
 * reports how much they went up.</p>
 */
public final class StatisticWatcher implements PlayerWatcher {

    /**
     * @param unit how many statistic points make one point of quest progress
     *             (distances are stored in centimetres, quests count blocks)
     */
    private record TrackedStatistic(Identifier statistic, ObjectiveType objective, int unit) {
    }

    private static final List<TrackedStatistic> TRACKED = List.of(
            new TrackedStatistic(Stats.WALK_ONE_CM, ObjectiveType.TRAVEL_DISTANCE,
                    SkillTreeConfig.CENTIMETRES_PER_BLOCK),
            new TrackedStatistic(Stats.SPRINT_ONE_CM, ObjectiveType.TRAVEL_DISTANCE,
                    SkillTreeConfig.CENTIMETRES_PER_BLOCK),
            new TrackedStatistic(Stats.CROUCH_ONE_CM, ObjectiveType.TRAVEL_DISTANCE,
                    SkillTreeConfig.CENTIMETRES_PER_BLOCK),
            new TrackedStatistic(Stats.SWIM_ONE_CM, ObjectiveType.SWIM_DISTANCE,
                    SkillTreeConfig.CENTIMETRES_PER_BLOCK),
            new TrackedStatistic(Stats.WALK_ON_WATER_ONE_CM, ObjectiveType.SWIM_DISTANCE,
                    SkillTreeConfig.CENTIMETRES_PER_BLOCK),
            new TrackedStatistic(Stats.WALK_UNDER_WATER_ONE_CM, ObjectiveType.SWIM_DISTANCE,
                    SkillTreeConfig.CENTIMETRES_PER_BLOCK),
            new TrackedStatistic(Stats.ANIMALS_BRED, ObjectiveType.BREED_ANIMAL, 1),
            new TrackedStatistic(Stats.FISH_CAUGHT, ObjectiveType.CATCH_FISH, 1),
            new TrackedStatistic(Stats.TRADED_WITH_VILLAGER, ObjectiveType.TRADE_WITH_VILLAGER, 1),
            new TrackedStatistic(Stats.ENCHANT_ITEM, ObjectiveType.ENCHANT_ITEM, 1),
            new TrackedStatistic(Stats.SLEEP_IN_BED, ObjectiveType.SLEEP_IN_BED, 1)
    );

    /** What one player's counters looked like the last time, plus progress too small to report yet. */
    private static final class Snapshot {
        private final Map<Identifier, Integer> lastValues = new HashMap<>();
        private final Map<ObjectiveType, Integer> pending = new EnumMap<>(ObjectiveType.class);
    }

    private final QuestReporter reporter;
    private final Map<UUID, Snapshot> snapshots = new HashMap<>();

    public StatisticWatcher(QuestReporter reporter) {
        this.reporter = Objects.requireNonNull(reporter, "reporter");
    }

    @Override
    public void everySecond(ServerPlayer player) {
        Snapshot snapshot = snapshots.computeIfAbsent(player.getUUID(), id -> new Snapshot());
        Map<ObjectiveType, Integer> units = new EnumMap<>(ObjectiveType.class);

        for (TrackedStatistic tracked : TRACKED) {
            int value = player.getStats().getValue(Stats.CUSTOM.get(tracked.statistic()));
            Integer previous = snapshot.lastValues.put(tracked.statistic(), value);
            // The first look only records the starting point: what a player did
            // before the mod was installed does not count.
            if (previous != null && value > previous) {
                snapshot.pending.merge(tracked.objective(), value - previous, Integer::sum);
                units.put(tracked.objective(), tracked.unit());
            }
        }

        units.forEach((objective, unit) -> {
            int pending = snapshot.pending.getOrDefault(objective, 0);
            snapshot.pending.put(objective, pending % unit);
            reporter.report(player, objective, QuestReporter.NO_SUBJECT, pending / unit);
        });
    }

    @Override
    public void forget(UUID playerId) {
        snapshots.remove(playerId);
    }
}

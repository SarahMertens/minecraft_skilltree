package com.sarah.skilltree.presentation.watcher;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.presentation.ServiceProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.animal.Animal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The Rancher skill: animals bred by the player can breed again sooner.
 *
 * <p>When two animals breed, the game sets their "age" to 6000 ticks and counts
 * it down to 0 before they can breed again. As soon as the player's
 * "animals bred" statistic goes up, this watcher looks for animals close by
 * whose countdown has only just started and shortens it.</p>
 */
public final class BreedingWatcher implements PlayerWatcher {

    /** An animal whose cooldown is at most this many ticks old was bred since the previous look. */
    private static final int JUST_BRED_WINDOW_TICKS = 2 * SkillTreeConfig.STATISTIC_POLL_INTERVAL_TICKS;

    private final ServiceProvider provider;
    private final Map<UUID, Integer> lastAnimalsBred = new HashMap<>();

    public BreedingWatcher(ServiceProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    @Override
    public void everySecond(ServerPlayer player) {
        int animalsBred = player.getStats().getValue(Stats.CUSTOM.get(Stats.ANIMALS_BRED));
        Integer previous = lastAnimalsBred.put(player.getUUID(), animalsBred);
        if (previous == null || animalsBred <= previous) {
            return;
        }
        int level = provider.services().skills().getSkillLevel(player.getUUID(), SkillTreeConfig.SKILL_BREEDING);
        if (level > 0) {
            shortenCooldowns(player, level);
        }
    }

    private void shortenCooldowns(ServerPlayer player, int level) {
        int vanillaCooldown = SkillTreeConfig.VANILLA_BREEDING_COOLDOWN_TICKS;
        double remaining = Math.max(0.0, 1.0 - level * SkillTreeConfig.BREEDING_COOLDOWN_REDUCTION_PER_LEVEL);
        int shortenedCooldown = (int) Math.round(vanillaCooldown * remaining);

        player.level().getEntitiesOfClass(Animal.class,
                        player.getBoundingBox().inflate(SkillTreeConfig.BREEDING_SEARCH_RADIUS),
                        animal -> animal.getAge() > vanillaCooldown - JUST_BRED_WINDOW_TICKS)
                .forEach(animal -> animal.setAge(shortenedCooldown));
    }

    @Override
    public void forget(UUID playerId) {
        lastAnimalsBred.remove(playerId);
    }
}

package com.sarah.skilltree.presentation.watcher;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.presentation.ServiceProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The Gourmet skill: every time the hunger bar of the player goes up, it goes
 * up a bit more.
 */
public final class EatingWatcher implements PlayerWatcher {

    private final ServiceProvider provider;
    private final Map<UUID, Integer> lastFoodLevels = new HashMap<>();

    public EatingWatcher(ServiceProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    @Override
    public void everySecond(ServerPlayer player) {
        FoodData food = player.getFoodData();
        Integer previous = lastFoodLevels.get(player.getUUID());
        int current = food.getFoodLevel();

        if (previous != null && current > previous) {
            int level = provider.services().skills()
                    .getSkillLevel(player.getUUID(), SkillTreeConfig.SKILL_EATING);
            if (level > 0) {
                current = Math.min(SkillTreeConfig.MAX_FOOD_LEVEL,
                        current + level * SkillTreeConfig.EATING_BONUS_FOOD_PER_LEVEL);
                food.setFoodLevel(current);
                // Saturation can never be higher than the food level itself.
                food.setSaturation(Math.min(current,
                        food.getSaturationLevel() + level * SkillTreeConfig.EATING_BONUS_SATURATION_PER_LEVEL));
            }
        }
        // Remember the level including the bonus, otherwise the bonus itself
        // would look like "the player ate again" one second later.
        lastFoodLevels.put(player.getUUID(), current);
    }

    @Override
    public void forget(UUID playerId) {
        lastFoodLevels.remove(playerId);
    }
}

package com.sarah.skilltree.presentation.controller;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.dto.response.RewardResponse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** Hands out the quest rewards that only the game itself can give. */
public final class RewardGranter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillTreeConfig.MOD_ID);

    private RewardGranter() {
    }

    public static void grant(ServerPlayer player, List<RewardResponse> rewards) {
        for (RewardResponse reward : rewards) {
            switch (reward.type()) {
                case SKILL_POINTS -> { /* already added to the profile by the domain */ }
                case EXPERIENCE_LEVELS -> player.giveExperienceLevels(reward.amount());
                case ITEM -> giveItem(player, reward);
            }
        }
    }

    private static void giveItem(ServerPlayer player, RewardResponse reward) {
        Identifier itemId = Identifier.tryParse(reward.itemId());
        if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
            LOGGER.warn("Quest reward refers to unknown item '{}'", reward.itemId());
            return;
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(itemId), reward.amount());
        // Puts the items in the inventory, or drops them at the player's feet if it is full.
        player.getInventory().placeItemBackInInventory(stack);
    }
}

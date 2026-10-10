package com.sarah.skilltree.presentation.message;

import com.sarah.skilltree.domain.dto.response.RewardResponse;
import com.sarah.skilltree.exception.InsufficientSkillPointsException;
import com.sarah.skilltree.exception.MaxLevelReachedException;
import com.sarah.skilltree.exception.PrerequisiteNotMetException;
import com.sarah.skilltree.exception.SkillTreeException;
import com.sarah.skilltree.exception.UnknownSkillException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

/**
 * Turns ids, rewards and exceptions into text for the player.
 *
 * <p>All text comes from the language files ({@code assets/skilltree/lang}),
 * so the mod can be translated without touching code. Server and client both
 * use this class, which keeps the wording identical everywhere.</p>
 */
public final class Messages {

    private Messages() {
    }

    public static MutableComponent skillName(String skillId) {
        return Component.translatable("skilltree.skill." + skillId);
    }

    public static MutableComponent skillDescription(String skillId) {
        return Component.translatable("skilltree.skill." + skillId + ".desc");
    }

    public static MutableComponent questName(String questId) {
        return Component.translatable("skilltree.quest." + questId);
    }

    public static MutableComponent reward(RewardResponse reward) {
        return switch (reward.type()) {
            case SKILL_POINTS -> Component.translatable("skilltree.reward.skill_points", reward.amount());
            case EXPERIENCE_LEVELS -> Component.translatable("skilltree.reward.experience_levels", reward.amount());
            case ITEM -> Component.translatable("skilltree.reward.item", reward.amount(), itemName(reward.itemId()));
        };
    }

    private static Component itemName(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            return Component.literal(itemId);
        }
        return BuiltInRegistries.ITEM.getOptional(id)
                .map(item -> item.getName())
                .orElseGet(() -> Component.literal(itemId));
    }

    /** The one place where a broken rule becomes a message for the player. */
    public static MutableComponent error(SkillTreeException exception) {
        return switch (exception) {
            case UnknownSkillException e -> Component.translatable("skilltree.error.unknown_skill", e.getSkillId());
            case MaxLevelReachedException e ->
                    Component.translatable("skilltree.error.max_level", skillName(e.getSkillId()));
            case InsufficientSkillPointsException e ->
                    Component.translatable("skilltree.error.insufficient_points", e.getRequired(), e.getAvailable());
            case PrerequisiteNotMetException e -> Component.translatable("skilltree.error.prerequisite",
                    skillName(e.getSkillId()), skillName(e.getRequiredSkillId()), e.getRequiredLevel());
            default -> Component.translatable("skilltree.error.generic");
        };
    }
}

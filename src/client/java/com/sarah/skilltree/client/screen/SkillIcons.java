package com.sarah.skilltree.client.screen;

import com.sarah.skilltree.config.SkillTreeConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

/**
 * The icon of each skill. Vanilla items are used as icons, so the mod needs no
 * textures of its own and always matches the player's resource pack.
 */
final class SkillIcons {

    private static final Item FALLBACK = Items.BOOK;

    private static final Map<String, Item> ICONS = Map.ofEntries(
            Map.entry(SkillTreeConfig.SKILL_MINING, Items.IRON_PICKAXE),
            Map.entry(SkillTreeConfig.SKILL_PROSPECTOR, Items.RAW_GOLD),
            Map.entry(SkillTreeConfig.SKILL_SMELTING, Items.BLAST_FURNACE),
            Map.entry(SkillTreeConfig.SKILL_COMBAT, Items.IRON_SWORD),
            Map.entry(SkillTreeConfig.SKILL_MOB_DROPS, Items.BONE),
            Map.entry(SkillTreeConfig.SKILL_HEARTS, Items.GOLDEN_APPLE),
            Map.entry(SkillTreeConfig.SKILL_AGILITY, Items.FEATHER),
            Map.entry(SkillTreeConfig.SKILL_ACROBATICS, Items.RABBIT_FOOT),
            Map.entry(SkillTreeConfig.SKILL_WATER, Items.HEART_OF_THE_SEA),
            Map.entry(SkillTreeConfig.SKILL_WOODCUTTING, Items.IRON_AXE),
            Map.entry(SkillTreeConfig.SKILL_EATING, Items.BREAD),
            Map.entry(SkillTreeConfig.SKILL_BREEDING, Items.WHEAT)
    );

    private SkillIcons() {
    }

    static ItemStack of(String skillId) {
        return new ItemStack(ICONS.getOrDefault(skillId, FALLBACK));
    }
}

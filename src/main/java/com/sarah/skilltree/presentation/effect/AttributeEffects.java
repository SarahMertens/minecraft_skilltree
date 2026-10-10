package com.sarah.skilltree.presentation.effect;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.service.SkillService;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

/**
 * Applies the passive skills: the ones that simply make a number of the player
 * better (speed, health, damage ...).
 *
 * <p>Minecraft already has an attribute system for exactly this, so these
 * skills need no event handling at all: the mod only sets a modifier and the
 * game does the rest. Adding a passive skill is one extra line in
 * {@link #BONUSES}.</p>
 */
public final class AttributeEffects {

    private static final List<AttributeBonus> BONUSES = List.of(
            new AttributeBonus(SkillTreeConfig.SKILL_MINING, Attributes.BLOCK_BREAK_SPEED,
                    SkillTreeConfig.MINING_SPEED_PER_LEVEL, Operation.ADD_MULTIPLIED_TOTAL),
            new AttributeBonus(SkillTreeConfig.SKILL_COMBAT, Attributes.ATTACK_DAMAGE,
                    SkillTreeConfig.COMBAT_DAMAGE_PER_LEVEL, Operation.ADD_VALUE),
            new AttributeBonus(SkillTreeConfig.SKILL_HEARTS, Attributes.MAX_HEALTH,
                    SkillTreeConfig.HEARTS_HEALTH_PER_LEVEL, Operation.ADD_VALUE),
            new AttributeBonus(SkillTreeConfig.SKILL_AGILITY, Attributes.MOVEMENT_SPEED,
                    SkillTreeConfig.AGILITY_SPEED_PER_LEVEL, Operation.ADD_MULTIPLIED_TOTAL),
            new AttributeBonus(SkillTreeConfig.SKILL_ACROBATICS, Attributes.FALL_DAMAGE_MULTIPLIER,
                    -SkillTreeConfig.ACROBATICS_FALL_DAMAGE_REDUCTION_PER_LEVEL, Operation.ADD_MULTIPLIED_TOTAL),
            new AttributeBonus(SkillTreeConfig.SKILL_ACROBATICS, Attributes.SAFE_FALL_DISTANCE,
                    SkillTreeConfig.ACROBATICS_SAFE_FALL_PER_LEVEL, Operation.ADD_VALUE),
            new AttributeBonus(SkillTreeConfig.SKILL_WATER, Attributes.OXYGEN_BONUS,
                    SkillTreeConfig.WATER_OXYGEN_PER_LEVEL, Operation.ADD_VALUE),
            new AttributeBonus(SkillTreeConfig.SKILL_WATER, Attributes.WATER_MOVEMENT_EFFICIENCY,
                    SkillTreeConfig.WATER_MOVEMENT_PER_LEVEL, Operation.ADD_VALUE)
    );

    private AttributeEffects() {
    }

    /**
     * Makes the attributes of the player match their skill levels. Safe to call
     * as often as needed: an existing modifier is replaced, never stacked.
     */
    public static void apply(ServerPlayer player, SkillService skillService) {
        for (AttributeBonus bonus : BONUSES) {
            AttributeInstance attribute = player.getAttribute(bonus.attribute());
            if (attribute == null) {
                continue;
            }
            Identifier modifierId = Identifier.fromNamespaceAndPath(SkillTreeConfig.MOD_ID, bonus.skillId());
            int level = skillService.getSkillLevel(player.getUUID(), bonus.skillId());
            if (level > 0) {
                attribute.addOrReplacePermanentModifier(
                        new AttributeModifier(modifierId, level * bonus.amountPerLevel(), bonus.operation()));
            } else {
                attribute.removeModifier(modifierId);
            }
        }
        // After a skill reset the player may have more health than the new maximum.
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}

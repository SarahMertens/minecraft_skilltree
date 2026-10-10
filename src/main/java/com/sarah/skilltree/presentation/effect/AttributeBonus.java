package com.sarah.skilltree.presentation.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * "Every level of this skill changes this player attribute by this much."
 *
 * @param amountPerLevel the change per skill level; its meaning depends on the operation
 *                       (a plain number for ADD_VALUE, a fraction such as 0.10 = +10% for the others)
 */
public record AttributeBonus(
        String skillId,
        Holder<Attribute> attribute,
        double amountPerLevel,
        AttributeModifier.Operation operation
) {
}

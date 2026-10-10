package com.sarah.skilltree.presentation.controller;

import com.sarah.skilltree.domain.dto.request.UpgradeSkillRequest;
import com.sarah.skilltree.domain.dto.response.UpgradeSkillResponse;
import com.sarah.skilltree.exception.SkillTreeException;
import com.sarah.skilltree.presentation.ServiceProvider;
import com.sarah.skilltree.presentation.effect.AttributeEffects;
import com.sarah.skilltree.presentation.message.Messages;
import com.sarah.skilltree.presentation.network.ClientSynchronizer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

/**
 * Handles "the player wants to upgrade a skill", no matter where the request
 * came from. The screen (through a network packet) and the chat command both
 * end up here, so they can never behave differently.
 */
public final class SkillController {

    private final ServiceProvider provider;
    private final ClientSynchronizer synchronizer;

    public SkillController(ServiceProvider provider, ClientSynchronizer synchronizer) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.synchronizer = Objects.requireNonNull(synchronizer, "synchronizer");
    }

    /** @return whether the upgrade succeeded */
    public boolean upgrade(ServerPlayer player, String skillId) {
        if (!provider.isReadyFor(player.getUUID())) {
            return false;
        }
        try {
            UpgradeSkillResponse response = provider.services().skills()
                    .upgradeSkill(new UpgradeSkillRequest(player.getUUID(), skillId));
            refresh(player);
            player.sendSystemMessage(Component.translatable("skilltree.message.upgraded",
                            Messages.skillName(response.skillId()).withStyle(ChatFormatting.GOLD),
                            response.newLevel(), response.maxLevel())
                    .withStyle(ChatFormatting.GREEN));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.4f);
            return true;
        } catch (SkillTreeException e) {
            player.sendSystemMessage(Messages.error(e).withStyle(ChatFormatting.RED));
            return false;
        }
    }

    /** Re-applies the skills of the player and updates their client. Call after any change. */
    public void refresh(ServerPlayer player) {
        if (!provider.isReadyFor(player.getUUID())) {
            return;
        }
        AttributeEffects.apply(player, provider.services().skills());
        synchronizer.sync(player);
    }
}

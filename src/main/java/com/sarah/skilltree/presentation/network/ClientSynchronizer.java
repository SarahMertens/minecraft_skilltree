package com.sarah.skilltree.presentation.network;

import com.sarah.skilltree.presentation.ServiceProvider;
import com.sarah.skilltree.presentation.SkillTreeServices;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;

/** Sends the current state of a player to their client. */
public final class ClientSynchronizer {

    private final ServiceProvider provider;

    public ClientSynchronizer(ServiceProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    public void sync(ServerPlayer player) {
        UUID playerId = player.getUUID();
        // A client without the mod cannot read the packet, so it is not sent.
        if (!provider.isReadyFor(playerId) || !ServerPlayNetworking.canSend(player, SkillTreeSyncPayload.TYPE)) {
            return;
        }
        SkillTreeServices services = provider.services();
        ServerPlayNetworking.send(player, new SkillTreeSyncPayload(
                services.skills().getSkillTree(playerId),
                services.quests().getQuests(playerId)));
    }
}

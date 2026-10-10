package com.sarah.skilltree.presentation.network;

import com.sarah.skilltree.presentation.controller.SkillController;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers the network packets of the mod and what the server does when it
 * receives one.
 *
 * <p>The client only ever <em>asks</em>. Whether an upgrade is allowed is
 * decided on the server by the domain, so a modified client cannot cheat.</p>
 */
public final class ServerNetworking {

    private ServerNetworking() {
    }

    /** Must run on both client and server, before any packet is sent. */
    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(SkillTreeSyncPayload.TYPE, SkillTreeSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpgradeSkillPayload.TYPE, UpgradeSkillPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestSyncPayload.TYPE, RequestSyncPayload.CODEC);
    }

    public static void registerReceivers(SkillController skillController, ClientSynchronizer synchronizer) {
        ServerPlayNetworking.registerGlobalReceiver(UpgradeSkillPayload.TYPE, (payload, context) ->
                context.server().execute(() -> skillController.upgrade(context.player(), payload.skillId())));
        ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.TYPE, (payload, context) ->
                context.server().execute(() -> synchronizer.sync(context.player())));
    }
}

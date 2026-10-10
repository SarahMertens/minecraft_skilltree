package com.sarah.skilltree.presentation.network;

import com.sarah.skilltree.config.SkillTreeConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client to server: the skill tree screen is open and wants fresh data. Carries no content. */
public record RequestSyncPayload() implements CustomPacketPayload {

    public static final Type<RequestSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SkillTreeConfig.MOD_ID, "request_sync"));

    public static final StreamCodec<FriendlyByteBuf, RequestSyncPayload> CODEC =
            StreamCodec.unit(new RequestSyncPayload());

    @Override
    public Type<RequestSyncPayload> type() {
        return TYPE;
    }
}

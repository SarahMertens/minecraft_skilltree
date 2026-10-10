package com.sarah.skilltree.presentation.network;

import com.sarah.skilltree.config.SkillTreeConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client to server: the player clicked a skill to upgrade it. */
public record UpgradeSkillPayload(String skillId) implements CustomPacketPayload {

    /** Longest skill id the server accepts; protects against oversized packets. */
    private static final int MAX_ID_LENGTH = 64;

    public static final Type<UpgradeSkillPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SkillTreeConfig.MOD_ID, "upgrade_skill"));

    public static final StreamCodec<FriendlyByteBuf, UpgradeSkillPayload> CODEC =
            CustomPacketPayload.codec(UpgradeSkillPayload::write, UpgradeSkillPayload::read);

    private void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(skillId, MAX_ID_LENGTH);
    }

    private static UpgradeSkillPayload read(FriendlyByteBuf buffer) {
        return new UpgradeSkillPayload(buffer.readUtf(MAX_ID_LENGTH));
    }

    @Override
    public Type<UpgradeSkillPayload> type() {
        return TYPE;
    }
}

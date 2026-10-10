package com.sarah.skilltree.presentation.network;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.dto.response.QuestResponse;
import com.sarah.skilltree.domain.dto.response.SkillTreeResponse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Server to client: the complete skill tree and quest log of the receiving player. */
public record SkillTreeSyncPayload(SkillTreeResponse tree, List<QuestResponse> quests) implements CustomPacketPayload {

    public static final Type<SkillTreeSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SkillTreeConfig.MOD_ID, "sync"));

    public static final StreamCodec<FriendlyByteBuf, SkillTreeSyncPayload> CODEC =
            CustomPacketPayload.codec(SkillTreeSyncPayload::write, SkillTreeSyncPayload::read);

    public SkillTreeSyncPayload {
        quests = List.copyOf(quests);
    }

    private void write(FriendlyByteBuf buffer) {
        PayloadCodecs.writeSkillTree(buffer, tree);
        buffer.writeCollection(quests, PayloadCodecs::writeQuest);
    }

    private static SkillTreeSyncPayload read(FriendlyByteBuf buffer) {
        SkillTreeResponse tree = PayloadCodecs.readSkillTree(buffer);
        return new SkillTreeSyncPayload(tree, buffer.readList(PayloadCodecs::readQuest));
    }

    @Override
    public Type<SkillTreeSyncPayload> type() {
        return TYPE;
    }
}

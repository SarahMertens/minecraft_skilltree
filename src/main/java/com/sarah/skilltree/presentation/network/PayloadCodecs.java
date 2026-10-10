package com.sarah.skilltree.presentation.network;

import com.sarah.skilltree.domain.dto.response.QuestResponse;
import com.sarah.skilltree.domain.dto.response.QuestState;
import com.sarah.skilltree.domain.dto.response.RewardResponse;
import com.sarah.skilltree.domain.dto.response.RewardType;
import com.sarah.skilltree.domain.dto.response.SkillResponse;
import com.sarah.skilltree.domain.dto.response.SkillState;
import com.sarah.skilltree.domain.dto.response.SkillTreeResponse;
import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.domain.model.ObjectiveType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Writes the response DTOs of the domain to a network buffer and reads them
 * back. Fields must be read in exactly the order they were written.
 */
final class PayloadCodecs {

    private PayloadCodecs() {
    }

    static void writeSkillTree(FriendlyByteBuf buffer, SkillTreeResponse tree) {
        buffer.writeVarInt(tree.skillPoints());
        buffer.writeCollection(tree.skills(), PayloadCodecs::writeSkill);
    }

    static SkillTreeResponse readSkillTree(FriendlyByteBuf buffer) {
        int skillPoints = buffer.readVarInt();
        return new SkillTreeResponse(skillPoints, buffer.readList(PayloadCodecs::readSkill));
    }

    private static void writeSkill(FriendlyByteBuf buffer, SkillResponse skill) {
        buffer.writeUtf(skill.id());
        buffer.writeEnum(skill.branch());
        buffer.writeVarInt(skill.level());
        buffer.writeVarInt(skill.maxLevel());
        buffer.writeVarInt(skill.costPerLevel());
        buffer.writeUtf(skill.prerequisiteSkillId());
        buffer.writeVarInt(skill.prerequisiteLevel());
        buffer.writeEnum(skill.state());
    }

    private static SkillResponse readSkill(FriendlyByteBuf buffer) {
        return new SkillResponse(
                buffer.readUtf(),
                buffer.readEnum(Branch.class),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readUtf(),
                buffer.readVarInt(),
                buffer.readEnum(SkillState.class));
    }

    static void writeQuest(FriendlyByteBuf buffer, QuestResponse quest) {
        buffer.writeUtf(quest.id());
        buffer.writeEnum(quest.branch());
        buffer.writeEnum(quest.objectiveType());
        buffer.writeUtf(quest.target());
        buffer.writeVarInt(quest.required());
        buffer.writeVarInt(quest.progress());
        buffer.writeVarInt(quest.completions());
        buffer.writeBoolean(quest.repeatable());
        buffer.writeEnum(quest.state());
        buffer.writeCollection(quest.rewards(), PayloadCodecs::writeReward);
    }

    static QuestResponse readQuest(FriendlyByteBuf buffer) {
        return new QuestResponse(
                buffer.readUtf(),
                buffer.readEnum(Branch.class),
                buffer.readEnum(ObjectiveType.class),
                buffer.readUtf(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readEnum(QuestState.class),
                buffer.readList(PayloadCodecs::readReward));
    }

    private static void writeReward(FriendlyByteBuf buffer, RewardResponse reward) {
        buffer.writeEnum(reward.type());
        buffer.writeUtf(reward.itemId());
        buffer.writeVarInt(reward.amount());
    }

    private static RewardResponse readReward(FriendlyByteBuf buffer) {
        return new RewardResponse(buffer.readEnum(RewardType.class), buffer.readUtf(), buffer.readVarInt());
    }
}

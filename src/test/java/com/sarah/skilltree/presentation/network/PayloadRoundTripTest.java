package com.sarah.skilltree.presentation.network;

import com.sarah.skilltree.domain.catalog.DefaultQuests;
import com.sarah.skilltree.domain.catalog.DefaultSkills;
import com.sarah.skilltree.domain.dto.request.QuestProgressRequest;
import com.sarah.skilltree.domain.dto.request.UpgradeSkillRequest;
import com.sarah.skilltree.domain.model.ObjectiveType;
import com.sarah.skilltree.domain.model.PlayerProfile;
import com.sarah.skilltree.domain.service.ProfileService;
import com.sarah.skilltree.domain.service.QuestService;
import com.sarah.skilltree.domain.service.SkillService;
import com.sarah.skilltree.persistence.PlayerProfileRepository;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What the server writes into a packet must come out identical on the client.
 * A field read in the wrong order would silently scramble the whole screen.
 */
class PayloadRoundTripTest {

    private static final UUID PLAYER = UUID.randomUUID();

    @Test
    void syncPayloadSurvivesTheNetwork() {
        SkillTreeSyncPayload original = realisticPayload();
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        SkillTreeSyncPayload.CODEC.encode(buffer, original);
        SkillTreeSyncPayload decoded = SkillTreeSyncPayload.CODEC.decode(buffer);

        assertEquals(original, decoded);
        assertFalse(buffer.isReadable(), "every written byte must be read again");
    }

    @Test
    void upgradePayloadSurvivesTheNetwork() {
        UpgradeSkillPayload original = new UpgradeSkillPayload("mob_drops");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        UpgradeSkillPayload.CODEC.encode(buffer, original);

        assertEquals(original, UpgradeSkillPayload.CODEC.decode(buffer));
    }

    @Test
    void requestPayloadIsEmpty() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        RequestSyncPayload.CODEC.encode(buffer, new RequestSyncPayload());

        assertEquals(0, buffer.readableBytes());
        assertEquals(new RequestSyncPayload(), RequestSyncPayload.CODEC.decode(buffer));
    }

    /** The default tree and quests of a player who is halfway: all states and reward kinds occur. */
    private SkillTreeSyncPayload realisticPayload() {
        PlayerProfileRepository repository = mock(PlayerProfileRepository.class);
        PlayerProfile profile = new PlayerProfile(PLAYER);
        profile.addSkillPoints(40);
        when(repository.findById(PLAYER)).thenReturn(Optional.of(profile));

        ProfileService profiles = new ProfileService(repository);
        SkillService skills = new SkillService(DefaultSkills.createCatalog(), profiles);
        QuestService quests = new QuestService(DefaultQuests.createCatalog(), profiles);
        for (int i = 0; i < 5; i++) {
            skills.upgradeSkill(new UpgradeSkillRequest(PLAYER, "mining"));
        }
        skills.upgradeSkill(new UpgradeSkillRequest(PLAYER, "prospector"));
        quests.recordProgress(new QuestProgressRequest(PLAYER, ObjectiveType.KILL_MONSTER, "minecraft:zombie", 60));
        quests.recordProgress(new QuestProgressRequest(PLAYER, ObjectiveType.MINE_STONE, "minecraft:stone", 7));

        return new SkillTreeSyncPayload(skills.getSkillTree(PLAYER), quests.getQuests(PLAYER));
    }
}

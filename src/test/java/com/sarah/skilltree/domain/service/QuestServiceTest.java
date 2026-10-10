package com.sarah.skilltree.domain.service;

import com.sarah.skilltree.domain.catalog.QuestCatalog;
import com.sarah.skilltree.domain.dto.request.QuestProgressRequest;
import com.sarah.skilltree.domain.dto.response.QuestProgressResponse;
import com.sarah.skilltree.domain.dto.response.QuestResponse;
import com.sarah.skilltree.domain.dto.response.QuestState;
import com.sarah.skilltree.domain.dto.response.RewardResponse;
import com.sarah.skilltree.domain.dto.response.RewardType;
import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.domain.model.ExperienceReward;
import com.sarah.skilltree.domain.model.ItemReward;
import com.sarah.skilltree.domain.model.Objective;
import com.sarah.skilltree.domain.model.ObjectiveType;
import com.sarah.skilltree.domain.model.PlayerProfile;
import com.sarah.skilltree.domain.model.Quest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    private static final UUID PLAYER = UUID.randomUUID();

    @Mock
    private ProfileService profileService;

    private QuestService questService;
    private PlayerProfile profile;

    @BeforeEach
    void setUp() {
        QuestCatalog catalog = new QuestCatalog(List.of(
                Quest.builder("hunter", Branch.COMBAT, Objective.of(ObjectiveType.KILL_MONSTER, 3))
                        .skillPoints(2).reward(new ExperienceReward(5)).build(),
                Quest.builder("slayer", Branch.COMBAT, Objective.of(ObjectiveType.KILL_MONSTER, 2))
                        .skillPoints(3).after("hunter").build(),
                Quest.builder("wither", Branch.COMBAT,
                                Objective.ofTarget(ObjectiveType.KILL_MONSTER, "minecraft:wither", 1))
                        .skillPoints(5).reward(new ItemReward("minecraft:diamond", 2)).build(),
                Quest.builder("bounty", Branch.COMBAT, Objective.of(ObjectiveType.MINE_ORE, 10))
                        .skillPoints(1).repeatable().build()));
        questService = new QuestService(catalog, profileService);
        profile = new PlayerProfile(PLAYER);
        when(profileService.getProfile(PLAYER)).thenReturn(profile);
    }

    private QuestProgressResponse kill(String entityId, int amount) {
        return questService.recordProgress(
                new QuestProgressRequest(PLAYER, ObjectiveType.KILL_MONSTER, entityId, amount));
    }

    @Test
    void progressBelowTheGoalCompletesNothing() {
        QuestProgressResponse response = kill("minecraft:zombie", 2);

        assertTrue(response.progressed());
        assertTrue(response.completed().isEmpty());
        assertEquals(0, profile.getSkillPoints());
        assertEquals(2, find("hunter").progress());
    }

    @Test
    void reachingTheGoalCompletesTheQuestAndPaysSkillPoints() {
        QuestProgressResponse response = kill("minecraft:zombie", 3);

        assertEquals(1, response.completed().size());
        assertEquals("hunter", response.completed().get(0).questId());
        assertEquals(2, profile.getSkillPoints());
        assertEquals(QuestState.COMPLETED, find("hunter").state());
    }

    @Test
    void responseListsEveryRewardSoThePresentationLayerCanHandItOut() {
        List<RewardResponse> rewards = kill("minecraft:zombie", 3).completed().get(0).rewards();

        assertEquals(List.of(
                new RewardResponse(RewardType.SKILL_POINTS, "", 2),
                new RewardResponse(RewardType.EXPERIENCE_LEVELS, "", 5)), rewards);
    }

    @Test
    void theEventThatUnlocksAQuestDoesNotCountForThatQuest() {
        kill("minecraft:zombie", 3); // completes "hunter", which unlocks "slayer"

        assertEquals(QuestState.ACTIVE, find("slayer").state());
        assertEquals(0, find("slayer").progress());
    }

    @Test
    void lockedQuestBecomesActiveAfterItsPrerequisite() {
        assertEquals(QuestState.LOCKED, find("slayer").state());

        kill("minecraft:zombie", 3);
        QuestProgressResponse response = kill("minecraft:zombie", 2);

        assertEquals(List.of("slayer"), response.completed().stream().map(c -> c.questId()).toList());
        assertEquals(5, profile.getSkillPoints());
    }

    @Test
    void targetedQuestOnlyCountsItsTargetButTheTargetAlsoCountsForGeneralQuests() {
        kill("minecraft:zombie", 1);
        assertEquals(0, find("wither").progress());

        QuestProgressResponse response = kill("minecraft:wither", 1);

        assertEquals(List.of("wither"), response.completed().stream().map(c -> c.questId()).toList());
        assertEquals(new RewardResponse(RewardType.ITEM, "minecraft:diamond", 2),
                response.completed().get(0).rewards().get(1));
        assertEquals(2, find("hunter").progress());
    }

    @Test
    void repeatableQuestCanCompleteSeveralTimesInOneStep() {
        QuestProgressResponse response = questService.recordProgress(
                new QuestProgressRequest(PLAYER, ObjectiveType.MINE_ORE, "minecraft:iron_ore", 25));

        assertEquals(2, response.completed().size());
        assertEquals(2, profile.getSkillPoints());
        QuestResponse bounty = find("bounty");
        assertEquals(5, bounty.progress());
        assertEquals(2, bounty.completions());
        assertEquals(QuestState.ACTIVE, bounty.state());
    }

    @Test
    void eventThatMatchesNoQuestChangesNothing() {
        QuestProgressResponse response = questService.recordProgress(
                new QuestProgressRequest(PLAYER, ObjectiveType.CATCH_FISH, "", 1));

        assertFalse(response.progressed());
        assertTrue(response.completed().isEmpty());
    }

    private QuestResponse find(String questId) {
        return questService.getQuests(PLAYER).stream()
                .filter(quest -> quest.id().equals(questId)).findFirst().orElseThrow();
    }
}

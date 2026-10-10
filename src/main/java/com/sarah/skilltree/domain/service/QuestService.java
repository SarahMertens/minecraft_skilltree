package com.sarah.skilltree.domain.service;

import com.sarah.skilltree.domain.catalog.QuestCatalog;
import com.sarah.skilltree.domain.dto.request.QuestProgressRequest;
import com.sarah.skilltree.domain.dto.response.CompletedQuestResponse;
import com.sarah.skilltree.domain.dto.response.QuestProgressResponse;
import com.sarah.skilltree.domain.dto.response.QuestResponse;
import com.sarah.skilltree.domain.dto.response.QuestState;
import com.sarah.skilltree.domain.dto.response.RewardResponse;
import com.sarah.skilltree.domain.dto.response.RewardType;
import com.sarah.skilltree.domain.model.ExperienceReward;
import com.sarah.skilltree.domain.model.ItemReward;
import com.sarah.skilltree.domain.model.PlayerProfile;
import com.sarah.skilltree.domain.model.Quest;
import com.sarah.skilltree.domain.model.Reward;
import com.sarah.skilltree.domain.model.SkillPointReward;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Entry point of the domain for everything about quests. */
public class QuestService {

    private final QuestCatalog questCatalog;
    private final ProfileService profileService;

    public QuestService(QuestCatalog questCatalog, ProfileService profileService) {
        this.questCatalog = Objects.requireNonNull(questCatalog, "questCatalog");
        this.profileService = Objects.requireNonNull(profileService, "profileService");
    }

    /** @return every quest with the progress and state it has for this player */
    public List<QuestResponse> getQuests(UUID playerId) {
        PlayerProfile profile = profileService.getProfile(playerId);
        return questCatalog.getAll().stream()
                .map(quest -> toResponse(quest, profile))
                .toList();
    }

    /**
     * Counts a game event for every quest it matches.
     *
     * <p>Skill point rewards are added to the profile right here. Rewards that
     * only the game can hand out (items, experience) are returned in the
     * response; giving them is the job of the presentation layer.</p>
     */
    public QuestProgressResponse recordProgress(QuestProgressRequest request) {
        PlayerProfile profile = profileService.getProfile(request.playerId());

        // Decide first which quests count, then apply the progress. Otherwise a
        // quest that gets unlocked by this very event would count it as well.
        List<Quest> activeQuests = questCatalog.findMatching(request.type(), request.subject()).stream()
                .filter(quest -> quest.isActiveFor(profile))
                .toList();

        List<CompletedQuestResponse> completed = new ArrayList<>();
        for (Quest quest : activeQuests) {
            int completions = profile.advanceQuest(quest, request.amount());
            for (int i = 0; i < completions; i++) {
                grantSkillPoints(quest, profile);
                completed.add(new CompletedQuestResponse(quest.getId(), toResponses(quest.getRewards())));
            }
        }
        return new QuestProgressResponse(!activeQuests.isEmpty(), completed);
    }

    private void grantSkillPoints(Quest quest, PlayerProfile profile) {
        for (Reward reward : quest.getRewards()) {
            if (reward instanceof SkillPointReward skillPoints) {
                profile.addSkillPoints(skillPoints.amount());
            }
        }
    }

    private QuestResponse toResponse(Quest quest, PlayerProfile profile) {
        return new QuestResponse(
                quest.getId(),
                quest.getBranch(),
                quest.getObjective().getType(),
                quest.getObjective().getTarget().orElse(""),
                quest.getObjective().getAmount(),
                profile.getQuestProgress(quest.getId()),
                profile.getQuestCompletions(quest.getId()),
                quest.isRepeatable(),
                stateOf(quest, profile),
                toResponses(quest.getRewards()));
    }

    private QuestState stateOf(Quest quest, PlayerProfile profile) {
        if (!quest.isUnlockedFor(profile)) {
            return QuestState.LOCKED;
        }
        return quest.isActiveFor(profile) ? QuestState.ACTIVE : QuestState.COMPLETED;
    }

    private List<RewardResponse> toResponses(List<Reward> rewards) {
        return rewards.stream().map(this::toResponse).toList();
    }

    private RewardResponse toResponse(Reward reward) {
        // Reward is sealed, so the compiler checks that no kind is missing here.
        return switch (reward) {
            case SkillPointReward r -> new RewardResponse(RewardType.SKILL_POINTS, "", r.amount());
            case ExperienceReward r -> new RewardResponse(RewardType.EXPERIENCE_LEVELS, "", r.amount());
            case ItemReward r -> new RewardResponse(RewardType.ITEM, r.itemId(), r.amount());
        };
    }
}

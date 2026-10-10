package com.sarah.skilltree.domain.model;

import com.sarah.skilltree.exception.InsufficientSkillPointsException;
import com.sarah.skilltree.exception.MaxLevelReachedException;
import com.sarah.skilltree.exception.PrerequisiteNotMetException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Everything the mod remembers about one player: skill points, skill levels
 * and quest progress.
 *
 * <p>The rules live here, next to the data they protect. No other class can
 * put a profile in an invalid state (negative points, a level above the
 * maximum, a skill without its prerequisite).</p>
 */
public final class PlayerProfile {

    private final UUID playerId;
    private int skillPoints;
    private final Map<String, Integer> skillLevels = new HashMap<>();
    private final Map<String, Integer> questProgress = new HashMap<>();
    private final Map<String, Integer> questCompletions = new HashMap<>();

    /** Creates an empty profile for a player who joins for the first time. */
    public PlayerProfile(UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    /** Restores a profile from storage. */
    public PlayerProfile(UUID playerId, int skillPoints, Map<String, Integer> skillLevels,
                         Map<String, Integer> questProgress, Map<String, Integer> questCompletions) {
        this(playerId);
        if (skillPoints < 0) {
            throw new IllegalArgumentException("Skill points must not be negative");
        }
        this.skillPoints = skillPoints;
        this.skillLevels.putAll(skillLevels);
        this.questProgress.putAll(questProgress);
        this.questCompletions.putAll(questCompletions);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    // ── Skill points ────────────────────────────────────────────────────────

    public int getSkillPoints() {
        return skillPoints;
    }

    public void addSkillPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add a negative amount of skill points");
        }
        skillPoints += amount;
    }

    private void spendSkillPoints(int amount) {
        if (amount > skillPoints) {
            throw new InsufficientSkillPointsException(amount, skillPoints);
        }
        skillPoints -= amount;
    }

    // ── Skills ──────────────────────────────────────────────────────────────

    public int getSkillLevel(String skillId) {
        return skillLevels.getOrDefault(skillId, 0);
    }

    public boolean meets(SkillRequirement requirement) {
        return getSkillLevel(requirement.skillId()) >= requirement.level();
    }

    /** @return whether the prerequisite of the skill (if any) is fulfilled */
    public boolean hasUnlocked(Skill skill) {
        return skill.getRequirement().map(this::meets).orElse(true);
    }

    /**
     * Raises a skill by one level and pays for it.
     *
     * @return the new level
     * @throws MaxLevelReachedException         if the skill cannot go higher
     * @throws PrerequisiteNotMetException      if the parent skill is not high enough yet
     * @throws InsufficientSkillPointsException if the player cannot afford the level
     */
    public int upgradeSkill(Skill skill) {
        int currentLevel = getSkillLevel(skill.getId());
        if (currentLevel >= skill.getMaxLevel()) {
            throw new MaxLevelReachedException(skill.getId(), skill.getMaxLevel());
        }
        skill.getRequirement().ifPresent(requirement -> {
            if (!meets(requirement)) {
                throw new PrerequisiteNotMetException(skill.getId(), requirement.skillId(), requirement.level());
            }
        });
        spendSkillPoints(skill.getCostPerLevel());
        int newLevel = currentLevel + 1;
        skillLevels.put(skill.getId(), newLevel);
        return newLevel;
    }

    /**
     * Sets a skill back to level 0 and gives the spent points back.
     *
     * @return the number of refunded skill points
     */
    public int refundSkill(Skill skill) {
        Integer level = skillLevels.remove(skill.getId());
        if (level == null) {
            return 0;
        }
        int refund = level * skill.getCostPerLevel();
        skillPoints += refund;
        return refund;
    }

    public Map<String, Integer> getSkillLevels() {
        return Collections.unmodifiableMap(skillLevels);
    }

    // ── Quests ──────────────────────────────────────────────────────────────

    public int getQuestProgress(String questId) {
        return questProgress.getOrDefault(questId, 0);
    }

    public int getQuestCompletions(String questId) {
        return questCompletions.getOrDefault(questId, 0);
    }

    public boolean hasCompletedQuest(String questId) {
        return getQuestCompletions(questId) > 0;
    }

    /**
     * Adds progress to a quest.
     *
     * <p>A normal quest stops counting once it is finished. A repeatable quest
     * starts over and keeps the surplus, so one big step can finish it more
     * than once.</p>
     *
     * @return how many times the quest was completed by this step (usually 0 or 1)
     */
    public int advanceQuest(Quest quest, int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Quest progress must be at least 1");
        }
        if (!quest.isActiveFor(this)) {
            return 0;
        }
        int required = quest.getObjective().getAmount();
        int progress = getQuestProgress(quest.getId()) + amount;
        int completions = 0;

        if (quest.isRepeatable()) {
            completions = progress / required;
            progress = progress % required;
        } else if (progress >= required) {
            completions = 1;
            progress = required;
        }

        questProgress.put(quest.getId(), progress);
        if (completions > 0) {
            questCompletions.merge(quest.getId(), completions, Integer::sum);
        }
        return completions;
    }

    public Map<String, Integer> getQuestProgress() {
        return Collections.unmodifiableMap(questProgress);
    }

    public Map<String, Integer> getQuestCompletions() {
        return Collections.unmodifiableMap(questCompletions);
    }
}

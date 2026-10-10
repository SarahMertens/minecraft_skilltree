package com.sarah.skilltree.domain.service;

import com.sarah.skilltree.domain.catalog.SkillCatalog;
import com.sarah.skilltree.domain.dto.request.GrantSkillPointsRequest;
import com.sarah.skilltree.domain.dto.request.UpgradeSkillRequest;
import com.sarah.skilltree.domain.dto.response.ResetSkillsResponse;
import com.sarah.skilltree.domain.dto.response.SkillResponse;
import com.sarah.skilltree.domain.dto.response.SkillState;
import com.sarah.skilltree.domain.dto.response.SkillTreeResponse;
import com.sarah.skilltree.domain.dto.response.UpgradeSkillResponse;
import com.sarah.skilltree.domain.model.PlayerProfile;
import com.sarah.skilltree.domain.model.Skill;
import com.sarah.skilltree.domain.model.SkillRequirement;
import com.sarah.skilltree.exception.InsufficientSkillPointsException;
import com.sarah.skilltree.exception.MaxLevelReachedException;
import com.sarah.skilltree.exception.PrerequisiteNotMetException;
import com.sarah.skilltree.exception.UnknownSkillException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Entry point of the domain for everything about skills and skill points. */
public class SkillService {

    private final SkillCatalog skillCatalog;
    private final ProfileService profileService;

    public SkillService(SkillCatalog skillCatalog, ProfileService profileService) {
        this.skillCatalog = Objects.requireNonNull(skillCatalog, "skillCatalog");
        this.profileService = Objects.requireNonNull(profileService, "profileService");
    }

    /** @return every skill with the level and state it has for this player */
    public SkillTreeResponse getSkillTree(UUID playerId) {
        PlayerProfile profile = profileService.getProfile(playerId);
        List<SkillResponse> skills = skillCatalog.getAll().stream()
                .map(skill -> toResponse(skill, profile))
                .toList();
        return new SkillTreeResponse(profile.getSkillPoints(), skills);
    }

    /**
     * Raises a skill by one level.
     *
     * @throws UnknownSkillException            if the skill does not exist
     * @throws MaxLevelReachedException         if the skill is at its maximum level
     * @throws PrerequisiteNotMetException      if the parent skill is not high enough
     * @throws InsufficientSkillPointsException if the player cannot pay for the level
     */
    public UpgradeSkillResponse upgradeSkill(UpgradeSkillRequest request) {
        Skill skill = skillCatalog.getById(request.skillId());
        PlayerProfile profile = profileService.getProfile(request.playerId());
        int newLevel = profile.upgradeSkill(skill);
        return new UpgradeSkillResponse(skill.getId(), newLevel, skill.getMaxLevel(), profile.getSkillPoints());
    }

    /** @return the level of a skill for this player, 0 if not learned (or unknown) */
    public int getSkillLevel(UUID playerId, String skillId) {
        return profileService.getProfile(playerId).getSkillLevel(skillId);
    }

    public int getSkillPoints(UUID playerId) {
        return profileService.getProfile(playerId).getSkillPoints();
    }

    /** @return the new total of skill points */
    public int grantSkillPoints(GrantSkillPointsRequest request) {
        PlayerProfile profile = profileService.getProfile(request.playerId());
        profile.addSkillPoints(request.amount());
        return profile.getSkillPoints();
    }

    /** Sets every skill back to level 0 and returns the spent points to the player. */
    public ResetSkillsResponse resetSkills(UUID playerId) {
        PlayerProfile profile = profileService.getProfile(playerId);
        int refunded = 0;
        for (Skill skill : skillCatalog.getAll()) {
            refunded += profile.refundSkill(skill);
        }
        return new ResetSkillsResponse(refunded, profile.getSkillPoints());
    }

    private SkillResponse toResponse(Skill skill, PlayerProfile profile) {
        int level = profile.getSkillLevel(skill.getId());
        Optional<SkillRequirement> requirement = skill.getRequirement();
        return new SkillResponse(
                skill.getId(),
                skill.getBranch(),
                level,
                skill.getMaxLevel(),
                skill.getCostPerLevel(),
                requirement.map(SkillRequirement::skillId).orElse(""),
                requirement.map(SkillRequirement::level).orElse(0),
                stateOf(skill, level, profile));
    }

    private SkillState stateOf(Skill skill, int level, PlayerProfile profile) {
        if (level >= skill.getMaxLevel()) {
            return SkillState.MAXED;
        }
        return profile.hasUnlocked(skill) ? SkillState.AVAILABLE : SkillState.LOCKED;
    }
}

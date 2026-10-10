package com.sarah.skilltree.domain.dto.response;

/** Result of a successful skill upgrade. */
public record UpgradeSkillResponse(String skillId, int newLevel, int maxLevel, int remainingSkillPoints) {
}

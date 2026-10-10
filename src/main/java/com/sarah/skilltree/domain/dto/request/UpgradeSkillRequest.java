package com.sarah.skilltree.domain.dto.request;

import java.util.Objects;
import java.util.UUID;

/** A player wants to raise a skill by one level. */
public record UpgradeSkillRequest(UUID playerId, String skillId) {

    public UpgradeSkillRequest {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(skillId, "skillId");
    }
}

package com.sarah.skilltree.domain.dto.request;

import java.util.Objects;
import java.util.UUID;

/** An administrator gives skill points to a player. */
public record GrantSkillPointsRequest(UUID playerId, int amount) {

    public GrantSkillPointsRequest {
        Objects.requireNonNull(playerId, "playerId");
        if (amount < 1) {
            throw new IllegalArgumentException("Amount must be at least 1");
        }
    }
}

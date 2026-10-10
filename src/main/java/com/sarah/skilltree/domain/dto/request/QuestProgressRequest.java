package com.sarah.skilltree.domain.dto.request;

import com.sarah.skilltree.domain.model.ObjectiveType;

import java.util.Objects;
import java.util.UUID;

/**
 * Something happened in the game that may count for a quest.
 *
 * @param type    what the player did
 * @param subject what it was done to, as an id (for example {@code minecraft:zombie})
 * @param amount  how often
 */
public record QuestProgressRequest(UUID playerId, ObjectiveType type, String subject, int amount) {

    public QuestProgressRequest {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(subject, "subject");
        if (amount < 1) {
            throw new IllegalArgumentException("Amount must be at least 1");
        }
    }
}

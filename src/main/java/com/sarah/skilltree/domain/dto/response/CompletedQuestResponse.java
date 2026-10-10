package com.sarah.skilltree.domain.dto.response;

import java.util.List;

/** A quest that was just finished, with the rewards the player earned. */
public record CompletedQuestResponse(String questId, List<RewardResponse> rewards) {

    public CompletedQuestResponse {
        rewards = List.copyOf(rewards);
    }
}

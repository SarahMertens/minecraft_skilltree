package com.sarah.skilltree.domain.dto.response;

import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.domain.model.ObjectiveType;

import java.util.List;

/**
 * One quest as seen by one player.
 *
 * @param target      the specific subject the objective asks for, or an empty string for "any"
 * @param completions how many times the player finished the quest (more than 1 only if repeatable)
 */
public record QuestResponse(
        String id,
        Branch branch,
        ObjectiveType objectiveType,
        String target,
        int required,
        int progress,
        int completions,
        boolean repeatable,
        QuestState state,
        List<RewardResponse> rewards
) {

    public QuestResponse {
        rewards = List.copyOf(rewards);
    }
}

package com.sarah.skilltree.domain.dto.response;

import java.util.List;

/**
 * Result of reporting progress.
 *
 * @param progressed whether any quest moved forward
 * @param completed  the quests that were finished by this step (one entry per completion)
 */
public record QuestProgressResponse(boolean progressed, List<CompletedQuestResponse> completed) {

    public QuestProgressResponse {
        completed = List.copyOf(completed);
    }
}

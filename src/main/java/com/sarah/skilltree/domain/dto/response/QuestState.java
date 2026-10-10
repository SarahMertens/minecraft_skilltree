package com.sarah.skilltree.domain.dto.response;

/** How a quest looks for one player. */
public enum QuestState {
    /** The quest before it in the chain is not finished yet. */
    LOCKED,
    /** Progress is being counted. */
    ACTIVE,
    /** Finished for good (repeatable quests never reach this state). */
    COMPLETED
}

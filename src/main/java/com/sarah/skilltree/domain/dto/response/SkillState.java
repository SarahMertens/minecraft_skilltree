package com.sarah.skilltree.domain.dto.response;

/** How a skill node looks for one player. */
public enum SkillState {
    /** The prerequisite skill is not high enough yet. */
    LOCKED,
    /** Can be upgraded (if the player has enough points). */
    AVAILABLE,
    /** At maximum level. */
    MAXED
}

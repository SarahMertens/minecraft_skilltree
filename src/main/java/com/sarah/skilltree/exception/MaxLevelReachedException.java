package com.sarah.skilltree.exception;

/** Thrown when a player tries to upgrade a skill that is already at its maximum level. */
public class MaxLevelReachedException extends SkillTreeException {

    private final String skillId;
    private final int maxLevel;

    public MaxLevelReachedException(String skillId, int maxLevel) {
        super("Skill " + skillId + " is already at its maximum level " + maxLevel);
        this.skillId = skillId;
        this.maxLevel = maxLevel;
    }

    public String getSkillId() {
        return skillId;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}

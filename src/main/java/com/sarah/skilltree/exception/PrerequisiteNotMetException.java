package com.sarah.skilltree.exception;

/** Thrown when a skill is upgraded before the skill above it in the tree is high enough. */
public class PrerequisiteNotMetException extends SkillTreeException {

    private final String skillId;
    private final String requiredSkillId;
    private final int requiredLevel;

    public PrerequisiteNotMetException(String skillId, String requiredSkillId, int requiredLevel) {
        super("Skill " + skillId + " requires " + requiredSkillId + " at level " + requiredLevel);
        this.skillId = skillId;
        this.requiredSkillId = requiredSkillId;
        this.requiredLevel = requiredLevel;
    }

    public String getSkillId() {
        return skillId;
    }

    public String getRequiredSkillId() {
        return requiredSkillId;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}

package com.sarah.skilltree.exception;

/** Thrown when a skill id does not exist in the skill catalog. */
public class UnknownSkillException extends SkillTreeException {

    private final String skillId;

    public UnknownSkillException(String skillId) {
        super("Unknown skill: " + skillId);
        this.skillId = skillId;
    }

    public String getSkillId() {
        return skillId;
    }
}

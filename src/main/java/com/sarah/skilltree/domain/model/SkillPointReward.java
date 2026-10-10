package com.sarah.skilltree.domain.model;

/** Skill points to spend in the skill tree. */
public record SkillPointReward(int amount) implements Reward {

    public SkillPointReward {
        if (amount < 1) {
            throw new IllegalArgumentException("A reward must give at least 1 skill point");
        }
    }
}

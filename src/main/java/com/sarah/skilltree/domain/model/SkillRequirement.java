package com.sarah.skilltree.domain.model;

import java.util.Objects;

/** "Skill X must be at least level N" - the link between a skill and its parent in the tree. */
public record SkillRequirement(String skillId, int level) {

    public SkillRequirement {
        Objects.requireNonNull(skillId, "skillId");
        if (level < 1) {
            throw new IllegalArgumentException("Required level must be at least 1");
        }
    }
}

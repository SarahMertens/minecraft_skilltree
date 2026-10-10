package com.sarah.skilltree.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Definition of one node in the skill tree. Immutable: the level a player has
 * reached is stored in the {@link PlayerProfile}, not here.
 */
public final class Skill {

    private final String id;
    private final Branch branch;
    private final int maxLevel;
    private final int costPerLevel;
    private final SkillRequirement requirement;

    private Skill(String id, Branch branch, int maxLevel, int costPerLevel, SkillRequirement requirement) {
        this.id = Objects.requireNonNull(id, "id");
        this.branch = Objects.requireNonNull(branch, "branch");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Skill id must not be blank");
        }
        if (maxLevel < 1) {
            throw new IllegalArgumentException("Max level must be at least 1");
        }
        if (costPerLevel < 1) {
            throw new IllegalArgumentException("Cost per level must be at least 1");
        }
        this.maxLevel = maxLevel;
        this.costPerLevel = costPerLevel;
        this.requirement = requirement;
    }

    /** Creates a root skill: the first node of a branch, without prerequisite. */
    public static Skill root(String id, Branch branch, int maxLevel, int costPerLevel) {
        return new Skill(id, branch, maxLevel, costPerLevel, null);
    }

    /** Creates a skill that only unlocks once another skill has reached a level. */
    public static Skill child(String id, Branch branch, int maxLevel, int costPerLevel,
                              SkillRequirement requirement) {
        return new Skill(id, branch, maxLevel, costPerLevel,
                Objects.requireNonNull(requirement, "requirement"));
    }

    public String getId() {
        return id;
    }

    public Branch getBranch() {
        return branch;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getCostPerLevel() {
        return costPerLevel;
    }

    public Optional<SkillRequirement> getRequirement() {
        return Optional.ofNullable(requirement);
    }

    public boolean isRoot() {
        return requirement == null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Skill skill && id.equals(skill.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Skill[" + id + "]";
    }
}

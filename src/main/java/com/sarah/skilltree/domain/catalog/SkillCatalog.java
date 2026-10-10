package com.sarah.skilltree.domain.catalog;

import com.sarah.skilltree.domain.model.Skill;
import com.sarah.skilltree.domain.model.SkillRequirement;
import com.sarah.skilltree.exception.InvalidCatalogException;
import com.sarah.skilltree.exception.UnknownSkillException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * All skills of the tree, in display order.
 *
 * <p>The catalog checks itself when it is created, so a mistake in the skill
 * definitions (duplicate id, missing or impossible prerequisite) stops the mod
 * at start-up instead of causing strange behaviour during play.</p>
 */
public final class SkillCatalog {

    private final Map<String, Skill> skillsById = new LinkedHashMap<>();

    public SkillCatalog(List<Skill> skills) {
        for (Skill skill : skills) {
            if (skillsById.putIfAbsent(skill.getId(), skill) != null) {
                throw new InvalidCatalogException("Duplicate skill id: " + skill.getId());
            }
        }
        skillsById.values().forEach(this::validateRequirement);
    }

    private void validateRequirement(Skill skill) {
        Optional<SkillRequirement> requirement = skill.getRequirement();
        if (requirement.isEmpty()) {
            return;
        }
        Skill parent = skillsById.get(requirement.get().skillId());
        if (parent == null) {
            throw new InvalidCatalogException(
                    "Skill " + skill.getId() + " requires unknown skill " + requirement.get().skillId());
        }
        if (requirement.get().level() > parent.getMaxLevel()) {
            throw new InvalidCatalogException(
                    "Skill " + skill.getId() + " requires a level that " + parent.getId() + " can never reach");
        }
        if (leadsBackTo(skill, parent)) {
            throw new InvalidCatalogException("Skill " + skill.getId() + " is part of a prerequisite cycle");
        }
    }

    /** Walks up the tree from {@code start}; finding {@code skill} again means there is a cycle. */
    private boolean leadsBackTo(Skill skill, Skill start) {
        Skill current = start;
        for (int steps = 0; current != null && steps <= skillsById.size(); steps++) {
            if (current.equals(skill)) {
                return true;
            }
            current = current.getRequirement().map(r -> skillsById.get(r.skillId())).orElse(null);
        }
        return false;
    }

    public Optional<Skill> findById(String skillId) {
        return Optional.ofNullable(skillsById.get(skillId));
    }

    /** @throws UnknownSkillException if no skill has this id */
    public Skill getById(String skillId) {
        return findById(skillId).orElseThrow(() -> new UnknownSkillException(skillId));
    }

    public Collection<Skill> getAll() {
        return Collections.unmodifiableCollection(skillsById.values());
    }
}

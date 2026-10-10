package com.sarah.skilltree.domain.catalog;

import com.sarah.skilltree.domain.model.ObjectiveType;
import com.sarah.skilltree.domain.model.Quest;
import com.sarah.skilltree.exception.InvalidCatalogException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** All quests, in display order. Validates itself on creation, like {@link SkillCatalog}. */
public final class QuestCatalog {

    private final Map<String, Quest> questsById = new LinkedHashMap<>();

    public QuestCatalog(List<Quest> quests) {
        for (Quest quest : quests) {
            if (questsById.putIfAbsent(quest.getId(), quest) != null) {
                throw new InvalidCatalogException("Duplicate quest id: " + quest.getId());
            }
        }
        for (Quest quest : questsById.values()) {
            quest.getPrerequisiteQuestId().ifPresent(prerequisiteId -> validatePrerequisite(quest, prerequisiteId));
        }
    }

    private void validatePrerequisite(Quest quest, String prerequisiteId) {
        Quest prerequisite = questsById.get(prerequisiteId);
        if (prerequisite == null) {
            throw new InvalidCatalogException(
                    "Quest " + quest.getId() + " comes after unknown quest " + prerequisiteId);
        }
        if (leadsBackTo(quest, prerequisite)) {
            throw new InvalidCatalogException("Quest " + quest.getId() + " is part of a prerequisite cycle");
        }
    }

    private boolean leadsBackTo(Quest quest, Quest start) {
        Quest current = start;
        for (int steps = 0; current != null && steps <= questsById.size(); steps++) {
            if (current.equals(quest)) {
                return true;
            }
            current = current.getPrerequisiteQuestId().map(questsById::get).orElse(null);
        }
        return false;
    }

    public Optional<Quest> findById(String questId) {
        return Optional.ofNullable(questsById.get(questId));
    }

    public Collection<Quest> getAll() {
        return Collections.unmodifiableCollection(questsById.values());
    }

    /** @return the quests whose objective counts events of this type about this subject */
    public List<Quest> findMatching(ObjectiveType type, String subject) {
        return questsById.values().stream()
                .filter(quest -> quest.getObjective().matches(type, subject))
                .toList();
    }
}

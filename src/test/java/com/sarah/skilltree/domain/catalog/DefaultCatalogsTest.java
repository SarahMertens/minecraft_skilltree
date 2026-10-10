package com.sarah.skilltree.domain.catalog;

import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.domain.model.Objective;
import com.sarah.skilltree.domain.model.ObjectiveType;
import com.sarah.skilltree.domain.model.Quest;
import com.sarah.skilltree.domain.model.Reward;
import com.sarah.skilltree.domain.model.Skill;
import com.sarah.skilltree.domain.model.SkillPointReward;
import com.sarah.skilltree.exception.InvalidCatalogException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the content that ships with the mod against mistakes while balancing. */
class DefaultCatalogsTest {

    private final SkillCatalog skills = DefaultSkills.createCatalog();
    private final QuestCatalog quests = DefaultQuests.createCatalog();

    @Test
    void defaultCatalogsAreValid() {
        assertDoesNotThrow(DefaultSkills::createCatalog);
        assertDoesNotThrow(DefaultQuests::createCatalog);
    }

    @Test
    void everyBranchHasExactlyOneRootSkill() {
        for (Branch branch : Branch.values()) {
            long roots = skills.getAll().stream()
                    .filter(skill -> skill.getBranch() == branch && skill.isRoot())
                    .count();
            assertEquals(1, roots, "branch " + branch);
        }
    }

    @Test
    void aSkillAndItsPrerequisiteAreInTheSameBranch() {
        for (Skill skill : skills.getAll()) {
            skill.getRequirement().ifPresent(requirement ->
                    assertEquals(skill.getBranch(), skills.getById(requirement.skillId()).getBranch(),
                            "skill " + skill.getId()));
        }
    }

    @Test
    void everyQuestRewardsSkillPoints() {
        for (Quest quest : quests.getAll()) {
            assertTrue(quest.getRewards().stream().anyMatch(SkillPointReward.class::isInstance),
                    "quest " + quest.getId());
        }
    }

    @Test
    void everyBranchEndsInARepeatableQuestSoPointsNeverRunOut() {
        for (Branch branch : Branch.values()) {
            assertTrue(quests.getAll().stream()
                            .anyMatch(quest -> quest.getBranch() == branch && quest.isRepeatable()),
                    "branch " + branch);
        }
    }

    @Test
    void oneTimeQuestsPayForASubstantialPartOfTheTree() {
        int treeCost = skills.getAll().stream()
                .mapToInt(skill -> skill.getMaxLevel() * skill.getCostPerLevel())
                .sum();
        int oneTimePoints = quests.getAll().stream()
                .filter(quest -> !quest.isRepeatable())
                .flatMap(quest -> quest.getRewards().stream())
                .filter(SkillPointReward.class::isInstance)
                .mapToInt(Reward::amount)
                .sum();

        assertTrue(oneTimePoints * 3 >= treeCost,
                "one-time quests give " + oneTimePoints + " of the " + treeCost + " points the tree costs");
        assertFalse(oneTimePoints >= treeCost, "repeatable quests should still matter");
    }

    @Test
    void questCatalogRejectsBrokenChains() {
        Quest orphan = Quest.builder("orphan", Branch.MINING,
                        Objective.of(ObjectiveType.MINE_ORE, 1))
                .skillPoints(1).after("missing").build();

        assertThrows(InvalidCatalogException.class, () -> new QuestCatalog(List.of(orphan)));
        assertThrows(InvalidCatalogException.class, () -> new QuestCatalog(
                List.of(quests.getAll().iterator().next(), quests.getAll().iterator().next())));
    }
}

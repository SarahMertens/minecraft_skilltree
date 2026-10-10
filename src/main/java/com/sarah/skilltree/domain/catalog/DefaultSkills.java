package com.sarah.skilltree.domain.catalog;

import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.domain.model.Skill;
import com.sarah.skilltree.domain.model.SkillRequirement;

import java.util.List;

import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_ACROBATICS;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_AGILITY;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_BREEDING;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_COMBAT;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_EATING;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_HEARTS;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_MINING;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_MOB_DROPS;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_PROSPECTOR;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_SMELTING;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_WATER;
import static com.sarah.skilltree.config.SkillTreeConfig.SKILL_WOODCUTTING;

/**
 * The skill tree that ships with the mod: four branches of three skills.
 *
 * <pre>
 *   MINING        COMBAT              EXPLORATION           NATURE
 *   Mining        Combat              Agility               Lumberjack
 *     └ Prospector  ├ Looter            ├ Acrobatics          ├ Gourmet
 *         └ Smelter └ Vitality          └ Diver               └ Rancher
 * </pre>
 */
public final class DefaultSkills {

    private DefaultSkills() {
    }

    public static SkillCatalog createCatalog() {
        return new SkillCatalog(List.of(
                // id, branch, max level, cost per level, prerequisite
                Skill.root(SKILL_MINING, Branch.MINING, 5, 2),
                Skill.child(SKILL_PROSPECTOR, Branch.MINING, 3, 2, new SkillRequirement(SKILL_MINING, 2)),
                Skill.child(SKILL_SMELTING, Branch.MINING, 3, 2, new SkillRequirement(SKILL_PROSPECTOR, 1)),

                Skill.root(SKILL_COMBAT, Branch.COMBAT, 5, 2),
                Skill.child(SKILL_MOB_DROPS, Branch.COMBAT, 4, 2, new SkillRequirement(SKILL_COMBAT, 2)),
                Skill.child(SKILL_HEARTS, Branch.COMBAT, 5, 3, new SkillRequirement(SKILL_COMBAT, 3)),

                Skill.root(SKILL_AGILITY, Branch.EXPLORATION, 5, 2),
                Skill.child(SKILL_ACROBATICS, Branch.EXPLORATION, 4, 2, new SkillRequirement(SKILL_AGILITY, 2)),
                Skill.child(SKILL_WATER, Branch.EXPLORATION, 3, 2, new SkillRequirement(SKILL_AGILITY, 3)),

                Skill.root(SKILL_WOODCUTTING, Branch.NATURE, 3, 2),
                Skill.child(SKILL_EATING, Branch.NATURE, 3, 1, new SkillRequirement(SKILL_WOODCUTTING, 1)),
                Skill.child(SKILL_BREEDING, Branch.NATURE, 3, 1, new SkillRequirement(SKILL_WOODCUTTING, 2))
        ));
    }
}

package com.sarah.skilltree.client.screen;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.model.Branch;

/** The accent colour of each branch, shared by the skill tree and the quest log. */
final class BranchColors {

    private BranchColors() {
    }

    static int of(Branch branch) {
        return switch (branch) {
            case MINING -> SkillTreeConfig.COLOR_BRANCH_MINING;
            case COMBAT -> SkillTreeConfig.COLOR_BRANCH_COMBAT;
            case EXPLORATION -> SkillTreeConfig.COLOR_BRANCH_EXPLORATION;
            case NATURE -> SkillTreeConfig.COLOR_BRANCH_NATURE;
        };
    }
}

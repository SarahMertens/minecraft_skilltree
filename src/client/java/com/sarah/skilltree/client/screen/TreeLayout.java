package com.sarah.skilltree.client.screen;

import com.sarah.skilltree.domain.dto.response.SkillResponse;
import com.sarah.skilltree.domain.model.Branch;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Works out where every skill is drawn, purely from "who is the parent of
 * whom". The positions are in grid units, not pixels:
 *
 * <ul>
 *   <li>{@code row} - how deep the skill is in its branch (0 = root)</li>
 *   <li>{@code column} - horizontal position inside its branch; a parent sits
 *       exactly in the middle above its children</li>
 * </ul>
 *
 * <p>Because nothing is hard-coded, a skill added to the catalog shows up in
 * the right place automatically.</p>
 */
final class TreeLayout {

    /** Grid position of one skill. */
    record Position(double column, int row) {
    }

    private final Map<String, Position> positions = new HashMap<>();
    private final Map<Branch, Integer> branchWidths = new EnumMap<>(Branch.class);
    private final Map<String, List<SkillResponse>> childrenByParent = new HashMap<>();
    private int rowCount;

    TreeLayout(List<SkillResponse> skills) {
        for (SkillResponse skill : skills) {
            if (skill.hasPrerequisite()) {
                childrenByParent.computeIfAbsent(skill.prerequisiteSkillId(), id -> new ArrayList<>()).add(skill);
            }
        }
        Map<Branch, Integer> nextFreeColumn = new EnumMap<>(Branch.class);
        for (SkillResponse skill : skills) {
            if (!skill.hasPrerequisite()) {
                place(skill, 0, nextFreeColumn);
            }
        }
        branchWidths.putAll(nextFreeColumn);
    }

    /**
     * Places a skill and everything below it. A skill without children takes
     * the next free column of its branch; a parent is centred over its children.
     *
     * @return the column the skill ended up in
     */
    private double place(SkillResponse skill, int row, Map<Branch, Integer> nextFreeColumn) {
        rowCount = Math.max(rowCount, row + 1);
        List<SkillResponse> children = childrenByParent.getOrDefault(skill.id(), List.of());
        double column;
        if (children.isEmpty()) {
            column = nextFreeColumn.merge(skill.branch(), 1, Integer::sum) - 1;
        } else {
            double first = 0;
            double last = 0;
            for (int i = 0; i < children.size(); i++) {
                double childColumn = place(children.get(i), row + 1, nextFreeColumn);
                if (i == 0) {
                    first = childColumn;
                }
                last = childColumn;
            }
            column = (first + last) / 2.0;
        }
        positions.put(skill.id(), new Position(column, row));
        return column;
    }

    /** @return the position of the skill, or {@code null} if it is not part of the tree */
    Position positionOf(String skillId) {
        return positions.get(skillId);
    }

    /** @return how many columns the branch needs (at least 1) */
    int widthOf(Branch branch) {
        return Math.max(1, branchWidths.getOrDefault(branch, 1));
    }

    int rowCount() {
        return rowCount;
    }
}

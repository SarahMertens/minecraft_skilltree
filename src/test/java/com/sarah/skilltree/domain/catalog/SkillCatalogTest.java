package com.sarah.skilltree.domain.catalog;

import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.domain.model.Skill;
import com.sarah.skilltree.domain.model.SkillRequirement;
import com.sarah.skilltree.exception.InvalidCatalogException;
import com.sarah.skilltree.exception.UnknownSkillException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillCatalogTest {

    private static Skill root(String id) {
        return Skill.root(id, Branch.MINING, 3, 1);
    }

    private static Skill child(String id, String parentId, int parentLevel) {
        return Skill.child(id, Branch.MINING, 3, 1, new SkillRequirement(parentId, parentLevel));
    }

    @Test
    void keepsSkillsInTheOrderTheyWereGiven() {
        SkillCatalog catalog = new SkillCatalog(List.of(root("a"), child("b", "a", 1), child("c", "b", 2)));

        assertEquals(List.of("a", "b", "c"), catalog.getAll().stream().map(Skill::getId).toList());
    }

    @Test
    void findsASkillById() {
        SkillCatalog catalog = new SkillCatalog(List.of(root("a")));

        assertEquals("a", catalog.getById("a").getId());
        assertTrue(catalog.findById("missing").isEmpty());
    }

    @Test
    void unknownIdThrowsWithTheIdInTheException() {
        SkillCatalog catalog = new SkillCatalog(List.of(root("a")));

        UnknownSkillException exception = assertThrows(UnknownSkillException.class, () -> catalog.getById("nope"));

        assertEquals("nope", exception.getSkillId());
    }

    @Test
    void rejectsDuplicateIds() {
        assertThrows(InvalidCatalogException.class, () -> new SkillCatalog(List.of(root("a"), root("a"))));
    }

    @Test
    void rejectsAPrerequisiteThatDoesNotExist() {
        assertThrows(InvalidCatalogException.class, () -> new SkillCatalog(List.of(child("b", "ghost", 1))));
    }

    @Test
    void rejectsAPrerequisiteLevelThatCanNeverBeReached() {
        assertThrows(InvalidCatalogException.class,
                () -> new SkillCatalog(List.of(root("a"), child("b", "a", 4))));
    }

    @Test
    void rejectsSkillsThatRequireEachOther() {
        assertThrows(InvalidCatalogException.class,
                () -> new SkillCatalog(List.of(child("a", "b", 1), child("b", "a", 1))));
    }
}

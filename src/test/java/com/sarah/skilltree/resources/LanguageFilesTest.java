package com.sarah.skilltree.resources;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sarah.skilltree.domain.catalog.DefaultQuests;
import com.sarah.skilltree.domain.catalog.DefaultSkills;
import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.domain.model.ObjectiveType;
import com.sarah.skilltree.domain.model.Quest;
import com.sarah.skilltree.domain.model.Skill;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A skill or quest without a translation shows up in the game as a raw key
 * such as {@code skilltree.skill.mining}. These tests catch that at build time.
 */
class LanguageFilesTest {

    private static final String LANGUAGE_FOLDER = "/assets/skilltree/lang/";

    private Map<String, String> load(String language) throws IOException {
        InputStream stream = getClass().getResourceAsStream(LANGUAGE_FOLDER + language + ".json");
        assertNotNull(stream, "language file " + language + ".json is missing");
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, new TypeToken<Map<String, String>>() { }.getType());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"en_us", "nl_nl"})
    void everySkillQuestBranchAndObjectiveIsTranslated(String language) throws IOException {
        Map<String, String> translations = load(language);
        List<String> expectedKeys = new ArrayList<>();

        for (Skill skill : DefaultSkills.createCatalog().getAll()) {
            expectedKeys.add("skilltree.skill." + skill.getId());
            expectedKeys.add("skilltree.skill." + skill.getId() + ".desc");
        }
        for (Quest quest : DefaultQuests.createCatalog().getAll()) {
            expectedKeys.add("skilltree.quest." + quest.getId());
            if (quest.getObjective().getTarget().isPresent()) {
                expectedKeys.add("skilltree.quest." + quest.getId() + ".desc");
            }
        }
        for (Branch branch : Branch.values()) {
            expectedKeys.add("skilltree.branch." + branch.name().toLowerCase(Locale.ROOT));
        }
        for (ObjectiveType type : ObjectiveType.values()) {
            expectedKeys.add("skilltree.objective." + type.name().toLowerCase(Locale.ROOT));
        }

        List<String> missing = expectedKeys.stream().filter(key -> !translations.containsKey(key)).toList();
        assertTrue(missing.isEmpty(), language + " misses: " + missing);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nl_nl"})
    void translationHasTheSameKeysAndPlaceholdersAsEnglish(String language) throws IOException {
        Map<String, String> english = load("en_us");
        Map<String, String> translation = load(language);

        assertEquals(english.keySet(), translation.keySet());
        english.forEach((key, text) -> assertEquals(countPlaceholders(text), countPlaceholders(translation.get(key)),
                "number of %s in " + key));
    }

    private int countPlaceholders(String text) {
        return text.split("%s", -1).length - 1;
    }
}

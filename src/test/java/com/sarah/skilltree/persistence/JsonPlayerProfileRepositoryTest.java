package com.sarah.skilltree.persistence;

import com.sarah.skilltree.domain.model.PlayerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonPlayerProfileRepositoryTest {

    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @TempDir
    Path directory;

    private JsonPlayerProfileRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonPlayerProfileRepository(directory);
    }

    private Path fileOf(UUID playerId) {
        return directory.resolve(playerId + ".json");
    }

    @Test
    void unknownPlayerHasNoProfile() {
        assertTrue(repository.findById(PLAYER).isEmpty());
    }

    @Test
    void savedProfileComesBackIdentical() {
        PlayerProfile profile = new PlayerProfile(PLAYER, 7,
                Map.of("mining", 3, "combat", 1),
                Map.of("ore_hunter", 12),
                Map.of("first_dig", 1, "motherlode", 4));

        repository.save(profile);
        PlayerProfile loaded = repository.findById(PLAYER).orElseThrow();

        assertEquals(PLAYER, loaded.getPlayerId());
        assertEquals(7, loaded.getSkillPoints());
        assertEquals(profile.getSkillLevels(), loaded.getSkillLevels());
        assertEquals(profile.getQuestProgress(), loaded.getQuestProgress());
        assertEquals(profile.getQuestCompletions(), loaded.getQuestCompletions());
    }

    @Test
    void savingAgainReplacesTheOldVersion() {
        PlayerProfile profile = new PlayerProfile(PLAYER);
        repository.save(profile);

        profile.addSkillPoints(3);
        repository.save(profile);

        assertEquals(3, repository.findById(PLAYER).orElseThrow().getSkillPoints());
    }

    @Test
    void createsTheDirectoryWhenItDoesNotExistYet() {
        JsonPlayerProfileRepository nested = new JsonPlayerProfileRepository(directory.resolve("world/skilltree"));

        nested.save(new PlayerProfile(PLAYER));

        assertTrue(nested.findById(PLAYER).isPresent());
    }

    @Test
    void leavesNoTemporaryFilesBehind() throws IOException {
        repository.save(new PlayerProfile(PLAYER));

        try (Stream<Path> files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void everyPlayerHasTheirOwnFile() {
        UUID other = UUID.fromString("00000000-0000-0000-0000-000000000002");
        PlayerProfile rich = new PlayerProfile(PLAYER);
        rich.addSkillPoints(50);

        repository.save(rich);
        repository.save(new PlayerProfile(other));

        assertEquals(50, repository.findById(PLAYER).orElseThrow().getSkillPoints());
        assertEquals(0, repository.findById(other).orElseThrow().getSkillPoints());
    }

    @Test
    void damagedFileIsSetAsideInsteadOfCrashingOrBeingOverwritten() throws IOException {
        Files.writeString(fileOf(PLAYER), "{ this is not json");

        Optional<PlayerProfile> loaded = repository.findById(PLAYER);

        assertTrue(loaded.isEmpty());
        assertFalse(Files.exists(fileOf(PLAYER)));
        assertEquals("{ this is not json", Files.readString(directory.resolve(PLAYER + ".json.corrupt")));
    }

    @Test
    void emptyFileCountsAsDamaged() throws IOException {
        Files.writeString(fileOf(PLAYER), "");

        assertTrue(repository.findById(PLAYER).isEmpty());
        assertTrue(Files.exists(directory.resolve(PLAYER + ".json.corrupt")));
    }

    @Test
    void fileWithMissingSectionsStillLoads() throws IOException {
        Files.writeString(fileOf(PLAYER), "{ \"version\": 1, \"skillPoints\": 4 }");

        PlayerProfile loaded = repository.findById(PLAYER).orElseThrow();

        assertEquals(4, loaded.getSkillPoints());
        assertTrue(loaded.getSkillLevels().isEmpty());
    }

    @Test
    void negativeSkillPointsCountAsDamaged() throws IOException {
        Files.writeString(fileOf(PLAYER), "{ \"skillPoints\": -5 }");

        assertTrue(repository.findById(PLAYER).isEmpty());
    }
}

package com.sarah.skilltree.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.model.PlayerProfile;
import com.sarah.skilltree.exception.ProfileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores every profile as {@code <player uuid>.json} in one directory.
 *
 * <p>JSON was chosen over Minecraft's NBT format because it is readable by
 * humans and needs no running game, so this class can be unit tested with a
 * temporary folder.</p>
 */
public final class JsonPlayerProfileRepository implements PlayerProfileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillTreeConfig.MOD_ID);
    private static final String CORRUPT_SUFFIX = ".corrupt";
    private static final String TEMPORARY_SUFFIX = ".tmp";

    private final Path directory;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonPlayerProfileRepository(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
    }

    @Override
    public Optional<PlayerProfile> findById(UUID playerId) {
        Path file = fileOf(playerId);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            ProfileDocument document = gson.fromJson(json, ProfileDocument.class);
            if (document == null) {
                throw new JsonParseException("File is empty");
            }
            return Optional.of(document.toProfile(playerId));
        } catch (JsonParseException | IllegalArgumentException e) {
            // A damaged file must not crash the server or be overwritten silently:
            // keep it under another name so it can be inspected, and start fresh.
            setAsideCorruptFile(file, e);
            return Optional.empty();
        } catch (IOException e) {
            throw new ProfileStorageException("Could not read profile of player " + playerId, e);
        }
    }

    @Override
    public void save(PlayerProfile profile) {
        Path file = fileOf(profile.getPlayerId());
        Path temporaryFile = file.resolveSibling(file.getFileName() + TEMPORARY_SUFFIX);
        try {
            Files.createDirectories(directory);
            // Write to a temporary file first and then swap it in. If the server
            // crashes halfway, the previous file is still complete.
            Files.writeString(temporaryFile, gson.toJson(ProfileDocument.from(profile)), StandardCharsets.UTF_8);
            Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ProfileStorageException("Could not save profile of player " + profile.getPlayerId(), e);
        }
    }

    private Path fileOf(UUID playerId) {
        return directory.resolve(playerId + SkillTreeConfig.STORAGE_FILE_EXTENSION);
    }

    private void setAsideCorruptFile(Path file, Exception cause) {
        Path target = file.resolveSibling(file.getFileName() + CORRUPT_SUFFIX);
        try {
            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.warn("Profile {} is damaged and was moved to {}: {}", file.getFileName(), target.getFileName(),
                    cause.getMessage());
        } catch (IOException e) {
            throw new ProfileStorageException("Could not move damaged profile " + file, e);
        }
    }

    /**
     * The shape of the JSON file. Kept separate from {@link PlayerProfile} so the
     * file format does not change by accident when the domain class changes.
     */
    private static final class ProfileDocument {

        private int version;
        private int skillPoints;
        private Map<String, Integer> skillLevels;
        private Map<String, Integer> questProgress;
        private Map<String, Integer> questCompletions;

        static ProfileDocument from(PlayerProfile profile) {
            ProfileDocument document = new ProfileDocument();
            document.version = SkillTreeConfig.STORAGE_FORMAT_VERSION;
            document.skillPoints = profile.getSkillPoints();
            document.skillLevels = new HashMap<>(profile.getSkillLevels());
            document.questProgress = new HashMap<>(profile.getQuestProgress());
            document.questCompletions = new HashMap<>(profile.getQuestCompletions());
            return document;
        }

        PlayerProfile toProfile(UUID playerId) {
            return new PlayerProfile(playerId, skillPoints,
                    withoutNulls(skillLevels), withoutNulls(questProgress), withoutNulls(questCompletions));
        }

        /** A hand-edited file may miss a section or contain {@code null} values. */
        private static Map<String, Integer> withoutNulls(Map<String, Integer> map) {
            Map<String, Integer> result = new HashMap<>();
            if (map != null) {
                map.forEach((key, value) -> {
                    if (key != null && value != null) {
                        result.put(key, value);
                    }
                });
            }
            return result;
        }
    }
}

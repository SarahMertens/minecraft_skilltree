package com.sarah.skilltree.domain.service;

import com.sarah.skilltree.domain.model.PlayerProfile;
import com.sarah.skilltree.exception.ProfileStorageException;
import com.sarah.skilltree.persistence.PlayerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    private static final UUID PLAYER = UUID.randomUUID();
    private static final UUID OTHER_PLAYER = UUID.randomUUID();

    @Mock
    private PlayerProfileRepository repository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void newPlayerGetsAnEmptyProfile() {
        when(repository.findById(PLAYER)).thenReturn(Optional.empty());

        PlayerProfile profile = profileService.getProfile(PLAYER);

        assertEquals(PLAYER, profile.getPlayerId());
        assertEquals(0, profile.getSkillPoints());
    }

    @Test
    void storedProfileIsUsed() {
        PlayerProfile stored = new PlayerProfile(PLAYER, 7, Map.of("mining", 2), Map.of(), Map.of());
        when(repository.findById(PLAYER)).thenReturn(Optional.of(stored));

        assertSame(stored, profileService.getProfile(PLAYER));
    }

    @Test
    void repositoryIsReadOnlyOnceWhileThePlayerIsOnline() {
        when(repository.findById(PLAYER)).thenReturn(Optional.empty());

        profileService.load(PLAYER);
        PlayerProfile first = profileService.getProfile(PLAYER);
        PlayerProfile second = profileService.getProfile(PLAYER);

        assertSame(first, second);
        verify(repository, times(1)).findById(PLAYER);
    }

    @Test
    void unloadSavesTheProfileAndForgetsIt() {
        when(repository.findById(PLAYER)).thenReturn(Optional.empty());
        PlayerProfile profile = profileService.getProfile(PLAYER);

        profileService.unload(PLAYER);

        verify(repository).save(profile);
        assertFalse(profileService.isLoaded(PLAYER));
    }

    @Test
    void unloadOfAPlayerThatWasNeverLoadedDoesNothing() {
        profileService.unload(PLAYER);

        verify(repository, never()).save(any());
    }

    @Test
    void profileStaysInMemoryWhenSavingFails() {
        when(repository.findById(PLAYER)).thenReturn(Optional.empty());
        PlayerProfile profile = profileService.getProfile(PLAYER);
        doThrow(new ProfileStorageException("disk full", new IOException())).when(repository).save(profile);

        assertThrows(ProfileStorageException.class, () -> profileService.unload(PLAYER));

        assertTrue(profileService.isLoaded(PLAYER), "progress must not be thrown away when it could not be saved");
    }

    @Test
    void saveAllKeepsGoingWhenOneProfileFails() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        PlayerProfile failing = profileService.getProfile(PLAYER);
        PlayerProfile working = profileService.getProfile(OTHER_PLAYER);
        doAnswer(invocation -> {
            if (invocation.getArgument(0) == failing) {
                throw new ProfileStorageException("disk full", new IOException());
            }
            return null;
        }).when(repository).save(any());

        assertThrows(ProfileStorageException.class, () -> profileService.saveAll());

        verify(repository).save(working);
        assertTrue(profileService.isLoaded(PLAYER));
        assertTrue(profileService.isLoaded(OTHER_PLAYER));
    }

    @Test
    void readErrorIsPassedOnAndNothingIsCached() {
        when(repository.findById(PLAYER)).thenThrow(new ProfileStorageException("unreadable", new IOException()));

        assertThrows(ProfileStorageException.class, () -> profileService.load(PLAYER));

        assertFalse(profileService.isLoaded(PLAYER));
    }
}

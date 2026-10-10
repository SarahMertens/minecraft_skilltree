package com.sarah.skilltree.exception;

/** Thrown by the persistence layer when a player profile cannot be read or written. */
public class ProfileStorageException extends SkillTreeException {

    public ProfileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

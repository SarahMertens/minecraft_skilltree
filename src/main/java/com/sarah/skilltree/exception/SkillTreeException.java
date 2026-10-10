package com.sarah.skilltree.exception;

/**
 * Base class for every error the mod can raise on purpose.
 *
 * <p>All exceptions are unchecked: they signal a broken rule (for example
 * "not enough skill points"), and the presentation layer translates them into
 * a message for the player in one central place.</p>
 */
public abstract class SkillTreeException extends RuntimeException {

    protected SkillTreeException(String message) {
        super(message);
    }

    protected SkillTreeException(String message, Throwable cause) {
        super(message, cause);
    }
}

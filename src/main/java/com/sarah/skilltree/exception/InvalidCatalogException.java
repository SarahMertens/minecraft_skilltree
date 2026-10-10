package com.sarah.skilltree.exception;

/**
 * Thrown at start-up when the skill or quest definitions contradict each other,
 * for example a duplicate id or a prerequisite that does not exist.
 */
public class InvalidCatalogException extends SkillTreeException {

    public InvalidCatalogException(String message) {
        super(message);
    }
}

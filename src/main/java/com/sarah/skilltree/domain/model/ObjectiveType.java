package com.sarah.skilltree.domain.model;

/**
 * The kinds of things a quest can ask for. The presentation layer translates
 * game events into one of these types; the domain never sees Minecraft classes.
 */
public enum ObjectiveType {
    MINE_STONE,
    MINE_ORE,
    CHOP_LOG,
    HARVEST_CROP,
    KILL_MONSTER,
    TRAVEL_DISTANCE,
    SWIM_DISTANCE,
    BREED_ANIMAL,
    CATCH_FISH,
    TRADE_WITH_VILLAGER,
    ENCHANT_ITEM,
    SLEEP_IN_BED,
    ENTER_DIMENSION
}

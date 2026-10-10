package com.sarah.skilltree.config;

/**
 * Single place for every constant of the mod.
 *
 * <p>Balancing a skill (how strong it is, how much it costs) or renaming an id
 * only ever requires a change in this class.</p>
 */
public final class SkillTreeConfig {

    private SkillTreeConfig() {
        // constants only
    }

    // ── General ─────────────────────────────────────────────────────────────
    public static final String MOD_ID = "skilltree";
    public static final String MOD_NAME = "Skill Tree";

    // ── Persistence ─────────────────────────────────────────────────────────
    /** Folder inside the world save that holds one JSON file per player. */
    public static final String STORAGE_DIRECTORY = "skilltree";
    public static final String STORAGE_FILE_EXTENSION = ".json";
    public static final int STORAGE_FORMAT_VERSION = 1;

    // ── Commands ────────────────────────────────────────────────────────────
    public static final String COMMAND_ROOT = "skilltree";
    public static final int MAX_ADMIN_POINT_GRANT = 1000;

    // ── Skill ids ───────────────────────────────────────────────────────────
    public static final String SKILL_MINING = "mining";
    public static final String SKILL_PROSPECTOR = "prospector";
    public static final String SKILL_SMELTING = "smelting";
    public static final String SKILL_COMBAT = "combat";
    public static final String SKILL_MOB_DROPS = "mob_drops";
    public static final String SKILL_HEARTS = "hearts";
    public static final String SKILL_AGILITY = "agility";
    public static final String SKILL_ACROBATICS = "acrobatics";
    public static final String SKILL_WATER = "water";
    public static final String SKILL_WOODCUTTING = "woodcutting";
    public static final String SKILL_EATING = "eating";
    public static final String SKILL_BREEDING = "breeding";

    // ── Skill effects (value per skill level) ───────────────────────────────
    /** Mining: +10% block break speed per level. */
    public static final double MINING_SPEED_PER_LEVEL = 0.10;
    /** Prospector: 15% chance per level that an ore drops twice. */
    public static final double PROSPECTOR_CHANCE_PER_LEVEL = 0.15;
    /** Smelter: chance per level that an ore drop comes out already smelted. */
    public static final double SMELTING_CHANCE_PER_LEVEL = 1.0 / 3.0;
    /** Combat: +1 attack damage (half a heart) per level. */
    public static final double COMBAT_DAMAGE_PER_LEVEL = 1.0;
    /** Looter: 15% chance per level that a mob drops its loot twice. */
    public static final double MOB_DROPS_CHANCE_PER_LEVEL = 0.15;
    /** Looter: bonus experience points per level for every kill. */
    public static final int MOB_DROPS_BONUS_XP_PER_LEVEL = 2;
    /** Vitality: +2 health (one heart) per level. */
    public static final double HEARTS_HEALTH_PER_LEVEL = 2.0;
    /** Agility: +5% movement speed per level. */
    public static final double AGILITY_SPEED_PER_LEVEL = 0.05;
    /** Acrobatics: -20% fall damage per level. */
    public static final double ACROBATICS_FALL_DAMAGE_REDUCTION_PER_LEVEL = 0.20;
    /** Acrobatics: +1 block of safe fall distance per level. */
    public static final double ACROBATICS_SAFE_FALL_PER_LEVEL = 1.0;
    /** Diver: +1 oxygen bonus per level (same scale as the Respiration enchantment). */
    public static final double WATER_OXYGEN_PER_LEVEL = 1.0;
    /** Diver: +25% water movement efficiency per level. */
    public static final double WATER_MOVEMENT_PER_LEVEL = 0.25;
    /** Lumberjack: maximum number of extra logs felled at level 1, 2 and 3. */
    public static final int[] WOODCUTTING_MAX_LOGS_BY_LEVEL = {16, 48, 128};
    /** Gourmet: bonus hunger points per level whenever the player eats. */
    public static final int EATING_BONUS_FOOD_PER_LEVEL = 1;
    /** Gourmet: bonus saturation per level whenever the player eats. */
    public static final float EATING_BONUS_SATURATION_PER_LEVEL = 0.5f;
    /** Rancher: -25% breeding cooldown per level. */
    public static final double BREEDING_COOLDOWN_REDUCTION_PER_LEVEL = 0.25;

    // ── Game integration ────────────────────────────────────────────────────
    /** Vanilla cooldown (in ticks) before an animal can breed again. */
    public static final int VANILLA_BREEDING_COOLDOWN_TICKS = 6000;
    /** Radius (in blocks) around the player in which freshly bred animals are searched. */
    public static final double BREEDING_SEARCH_RADIUS = 16.0;
    /** How often (in ticks) statistics are polled for quest progress. 20 ticks = 1 second. */
    public static final int STATISTIC_POLL_INTERVAL_TICKS = 20;
    /** Vanilla stores distances in centimetres; quests count whole blocks. */
    public static final int CENTIMETRES_PER_BLOCK = 100;
    public static final int MAX_FOOD_LEVEL = 20;

    // ── Client: key binding ─────────────────────────────────────────────────
    public static final String KEY_CATEGORY = "main";
    public static final String KEY_OPEN_SCREEN = "key.skilltree.open";
    /** How often (in ticks) the open screen asks the server for fresh data. */
    public static final int SCREEN_REFRESH_INTERVAL_TICKS = 20;

    // ── Client: screen layout (in GUI pixels) ───────────────────────────────
    public static final int SCREEN_MAX_WIDTH = 400;
    public static final int SCREEN_MAX_HEIGHT = 230;
    public static final int SCREEN_MARGIN = 10;
    public static final int SCREEN_HEADER_HEIGHT = 30;
    public static final int SCREEN_PADDING = 8;
    public static final int TAB_BUTTON_WIDTH = 60;
    public static final int TAB_BUTTON_HEIGHT = 16;
    public static final int TAB_UNDERLINE_HEIGHT = 2;
    public static final int NODE_SIZE = 24;
    public static final int NODE_ROW_HEIGHT = 52;
    public static final int NODE_MAX_SPACING = 56;
    public static final int QUEST_ROW_HEIGHT = 30;
    public static final int QUEST_BAR_WIDTH = 100;
    public static final int QUEST_BAR_HEIGHT = 5;
    public static final int QUEST_SCROLL_STEP = 15;
    public static final int TOOLTIP_MAX_WIDTH = 190;

    // ── Client: colours (ARGB, the first two hex digits are the opacity) ────
    public static final int COLOR_PANEL = 0xE0101018;
    public static final int COLOR_PANEL_BORDER = 0xFF4A4A5E;
    public static final int COLOR_HEADER = 0xFF1C1C2A;
    public static final int COLOR_TEXT = 0xFFFFFFFF;
    public static final int COLOR_TEXT_MUTED = 0xFFA0A0B0;
    public static final int COLOR_TEXT_DISABLED = 0xFF606070;
    public static final int COLOR_POINTS = 0xFFFFD75E;
    public static final int COLOR_NODE_BACKGROUND = 0xFF24243A;
    public static final int COLOR_NODE_HOVER = 0xFF34345A;
    public static final int COLOR_LOCKED = 0xFF55555F;
    public static final int COLOR_AVAILABLE = 0xFFB8B8C8;
    public static final int COLOR_AFFORDABLE = 0xFFFFD75E;
    public static final int COLOR_MAXED = 0xFF5EE07A;
    public static final int COLOR_BAR_BACKGROUND = 0xFF0C0C14;
    public static final int COLOR_BAR_FILL = 0xFF5EA8FF;
    public static final int COLOR_BRANCH_MINING = 0xFF7FD4E8;
    public static final int COLOR_BRANCH_COMBAT = 0xFFFF7A6B;
    public static final int COLOR_BRANCH_EXPLORATION = 0xFFB89CFF;
    public static final int COLOR_BRANCH_NATURE = 0xFF8FDC6A;
}

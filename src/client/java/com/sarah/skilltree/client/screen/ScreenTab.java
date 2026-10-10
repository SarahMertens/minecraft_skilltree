package com.sarah.skilltree.client.screen;

import com.sarah.skilltree.presentation.network.SkillTreeSyncPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * One page of the skill tree screen. The screen draws the frame and the tab
 * buttons, and leaves everything inside the frame to the active tab.
 */
interface ScreenTab {

    /** Draws the tab inside {@code area} with the latest data from the server. */
    void render(GuiGraphics graphics, Font font, Area area, SkillTreeSyncPayload data, int mouseX, int mouseY);

    /** @return whether the click was used by this tab */
    default boolean mouseClicked(Area area, SkillTreeSyncPayload data, double mouseX, double mouseY) {
        return false;
    }

    /** @return whether the scroll was used by this tab */
    default boolean mouseScrolled(Area area, SkillTreeSyncPayload data, double amount) {
        return false;
    }
}

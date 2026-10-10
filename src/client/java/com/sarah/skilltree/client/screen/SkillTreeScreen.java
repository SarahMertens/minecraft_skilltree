package com.sarah.skilltree.client.screen;

import com.sarah.skilltree.client.ClientState;
import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.presentation.network.RequestSyncPayload;
import com.sarah.skilltree.presentation.network.SkillTreeSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * The screen that opens with the skill tree key (K by default).
 *
 * <p>The screen itself only draws the frame, the header and the two tab
 * buttons. The content is drawn by the active {@link ScreenTab}. It keeps no
 * game data: every frame it shows whatever {@link ClientState} holds, so a
 * new snapshot from the server appears immediately.</p>
 */
public class SkillTreeScreen extends Screen {

    private static final int LEFT_MOUSE_BUTTON = 0;

    private final ClientState state;
    private final KeyMapping openScreenKey;
    private final ScreenTab skillTab = new SkillTreeTab();
    private final ScreenTab questTab = new QuestLogTab();

    private ScreenTab activeTab = skillTab;
    private Button skillTabButton;
    private Button questTabButton;
    private Area panel;
    private Area content;
    private int ticksOpen;

    public SkillTreeScreen(ClientState state, KeyMapping openScreenKey) {
        super(Component.translatable("skilltree.screen.title"));
        this.state = Objects.requireNonNull(state, "state");
        this.openScreenKey = Objects.requireNonNull(openScreenKey, "openScreenKey");
    }

    /** Called when the screen opens and every time the window is resized. */
    @Override
    protected void init() {
        int margin = SkillTreeConfig.SCREEN_MARGIN;
        int padding = SkillTreeConfig.SCREEN_PADDING;
        int header = SkillTreeConfig.SCREEN_HEADER_HEIGHT;
        int panelWidth = Math.min(SkillTreeConfig.SCREEN_MAX_WIDTH, width - 2 * margin);
        int panelHeight = Math.min(SkillTreeConfig.SCREEN_MAX_HEIGHT, height - 2 * margin);

        panel = new Area((width - panelWidth) / 2, (height - panelHeight) / 2, panelWidth, panelHeight);
        content = new Area(panel.x() + padding, panel.y() + header + padding,
                panelWidth - 2 * padding, panelHeight - header - 2 * padding);

        int buttonWidth = SkillTreeConfig.TAB_BUTTON_WIDTH;
        int buttonHeight = SkillTreeConfig.TAB_BUTTON_HEIGHT;
        int buttonY = panel.y() + (header - buttonHeight) / 2;
        skillTabButton = addRenderableWidget(Button.builder(
                        Component.translatable("skilltree.screen.tab.skills"), button -> select(skillTab))
                .bounds(panel.centerX() - buttonWidth - 2, buttonY, buttonWidth, buttonHeight).build());
        questTabButton = addRenderableWidget(Button.builder(
                        Component.translatable("skilltree.screen.tab.quests"), button -> select(questTab))
                .bounds(panel.centerX() + 2, buttonY, buttonWidth, buttonHeight).build());

        select(activeTab);
        requestFreshData();
    }

    private void select(ScreenTab tab) {
        activeTab = tab;
        // The button of the page that is already open is greyed out.
        skillTabButton.active = tab != skillTab;
        questTabButton.active = tab != questTab;
    }

    @Override
    public void tick() {
        ticksOpen++;
        if (ticksOpen % SkillTreeConfig.SCREEN_REFRESH_INTERVAL_TICKS == 0) {
            requestFreshData();
        }
    }

    private void requestFreshData() {
        if (ClientPlayNetworking.canSend(RequestSyncPayload.TYPE)) {
            ClientPlayNetworking.send(new RequestSyncPayload());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int header = SkillTreeConfig.SCREEN_HEADER_HEIGHT;
        int padding = SkillTreeConfig.SCREEN_PADDING;
        int textY = panel.y() + (header - font.lineHeight) / 2 + 1;

        graphics.fill(panel.x(), panel.y(), panel.right(), panel.bottom(), SkillTreeConfig.COLOR_PANEL);
        graphics.fill(panel.x(), panel.y(), panel.right(), panel.y() + header, SkillTreeConfig.COLOR_HEADER);
        graphics.renderOutline(panel.x(), panel.y(), panel.width(), panel.height(), SkillTreeConfig.COLOR_PANEL_BORDER);
        graphics.drawString(font, title, panel.x() + padding, textY, SkillTreeConfig.COLOR_TEXT);

        super.render(graphics, mouseX, mouseY, partialTick); // draws the tab buttons
        Button activeButton = activeTab == skillTab ? skillTabButton : questTabButton;
        graphics.fill(activeButton.getX(), activeButton.getY() + activeButton.getHeight() + 1,
                activeButton.getX() + activeButton.getWidth(),
                activeButton.getY() + activeButton.getHeight() + 1 + SkillTreeConfig.TAB_UNDERLINE_HEIGHT,
                SkillTreeConfig.COLOR_POINTS);

        Optional<SkillTreeSyncPayload> data = state.latest();
        if (data.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("skilltree.screen.no_data"),
                    content.centerX(), content.y() + content.height() / 2, SkillTreeConfig.COLOR_TEXT_MUTED);
            return;
        }
        Component points = Component.translatable("skilltree.screen.points", data.get().tree().skillPoints());
        graphics.drawString(font, points, panel.right() - padding - font.width(points), textY,
                SkillTreeConfig.COLOR_POINTS);
        activeTab.render(graphics, font, content, data.get(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true; // a tab button was clicked
        }
        Optional<SkillTreeSyncPayload> data = state.latest();
        if (event.button() == LEFT_MOUSE_BUTTON && data.isPresent() && content.contains(event.x(), event.y())) {
            return activeTab.mouseClicked(content, data.get(), event.x(), event.y());
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Optional<SkillTreeSyncPayload> data = state.latest();
        if (data.isPresent() && activeTab.mouseScrolled(content, data.get(), verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // The key that opens the screen also closes it, like the inventory key.
        if (openScreenKey.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    /** The game keeps running behind the screen; this is not a pause menu. */
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

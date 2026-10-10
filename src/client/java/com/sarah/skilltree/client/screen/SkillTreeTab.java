package com.sarah.skilltree.client.screen;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.dto.response.SkillResponse;
import com.sarah.skilltree.domain.dto.response.SkillState;
import com.sarah.skilltree.domain.dto.response.SkillTreeResponse;
import com.sarah.skilltree.domain.model.Branch;
import com.sarah.skilltree.presentation.message.Messages;
import com.sarah.skilltree.presentation.network.SkillTreeSyncPayload;
import com.sarah.skilltree.presentation.network.UpgradeSkillPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The "Skills" page: the four branches side by side, every skill a square
 * with an icon, connected to its parent by a line. Clicking a square asks the
 * server to upgrade that skill.
 */
final class SkillTreeTab implements ScreenTab {

    private static final int BRANCH_TITLE_HEIGHT = 16;
    private static final int HINT_HEIGHT = 12;
    private static final int ICON_SIZE = 16;
    private static final int LEVEL_TEXT_GAP = 3;

    @Override
    public void render(GuiGraphics graphics, Font font, Area area, SkillTreeSyncPayload data, int mouseX, int mouseY) {
        SkillTreeResponse tree = data.tree();
        Map<String, Area> nodes = layoutNodes(area, tree);
        Map<String, SkillResponse> skillsById = new HashMap<>();
        tree.skills().forEach(skill -> skillsById.put(skill.id(), skill));

        drawBranchTitles(graphics, font, area);
        graphics.drawCenteredString(font, Component.translatable("skilltree.screen.skills.hint"),
                area.centerX(), area.bottom() - font.lineHeight, SkillTreeConfig.COLOR_TEXT_DISABLED);
        for (SkillResponse skill : tree.skills()) {
            drawConnection(graphics, skill, skillsById, nodes);
        }
        SkillResponse hovered = null;
        for (SkillResponse skill : tree.skills()) {
            Area node = nodes.get(skill.id());
            boolean isHovered = node.contains(mouseX, mouseY);
            drawNode(graphics, font, skill, node, isHovered, tree.skillPoints());
            if (isHovered) {
                hovered = skill;
            }
        }
        if (hovered != null) {
            graphics.setTooltipForNextFrame(font, tooltipOf(font, hovered, tree.skillPoints()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(Area area, SkillTreeSyncPayload data, double mouseX, double mouseY) {
        SkillTreeResponse tree = data.tree();
        Map<String, Area> nodes = layoutNodes(area, tree);
        for (SkillResponse skill : tree.skills()) {
            if (nodes.get(skill.id()).contains(mouseX, mouseY)) {
                if (canUpgrade(skill, tree.skillPoints())) {
                    // Only a request: the server checks the rules again and answers with new data.
                    ClientPlayNetworking.send(new UpgradeSkillPayload(skill.id()));
                    Minecraft.getInstance().getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
                return true;
            }
        }
        return false;
    }

    // ── Layout ──────────────────────────────────────────────────────────────

    /** Converts the grid positions of {@link TreeLayout} into pixel rectangles inside {@code area}. */
    private Map<String, Area> layoutNodes(Area area, SkillTreeResponse tree) {
        TreeLayout layout = new TreeLayout(tree.skills());
        int nodeSize = SkillTreeConfig.NODE_SIZE;
        int branchWidth = area.width() / Branch.values().length;
        int availableHeight = area.height() - BRANCH_TITLE_HEIGHT - HINT_HEIGHT;
        int rowHeight = Math.min(SkillTreeConfig.NODE_ROW_HEIGHT, availableHeight / Math.max(1, layout.rowCount()));

        Map<String, Area> nodes = new HashMap<>();
        for (SkillResponse skill : tree.skills()) {
            TreeLayout.Position position = layout.positionOf(skill.id());
            int columns = layout.widthOf(skill.branch());
            int spacing = Math.min(SkillTreeConfig.NODE_MAX_SPACING, branchWidth / columns);
            int branchCenter = area.x() + skill.branch().ordinal() * branchWidth + branchWidth / 2;
            double offsetFromCenter = position.column() - (columns - 1) / 2.0;

            int centerX = branchCenter + (int) Math.round(offsetFromCenter * spacing);
            int top = area.y() + BRANCH_TITLE_HEIGHT + position.row() * rowHeight;
            nodes.put(skill.id(), new Area(centerX - nodeSize / 2, top, nodeSize, nodeSize));
        }
        return nodes;
    }

    // ── Drawing ─────────────────────────────────────────────────────────────

    private void drawBranchTitles(GuiGraphics graphics, Font font, Area area) {
        int branchWidth = area.width() / Branch.values().length;
        for (Branch branch : Branch.values()) {
            int centerX = area.x() + branch.ordinal() * branchWidth + branchWidth / 2;
            graphics.drawCenteredString(font,
                    Component.translatable("skilltree.branch." + branch.name().toLowerCase(Locale.ROOT)),
                    centerX, area.y() + 2, BranchColors.of(branch));
        }
    }

    /** Draws the line from a skill up to its parent: down from the parent, sideways, down into the skill. */
    private void drawConnection(GuiGraphics graphics, SkillResponse skill, Map<String, SkillResponse> skillsById,
                                Map<String, Area> nodes) {
        SkillResponse parent = skillsById.get(skill.prerequisiteSkillId());
        if (parent == null) {
            return;
        }
        Area from = nodes.get(parent.id());
        Area to = nodes.get(skill.id());
        boolean unlocked = parent.level() >= skill.prerequisiteLevel();
        int color = unlocked ? BranchColors.of(skill.branch()) : SkillTreeConfig.COLOR_LOCKED;

        int fromX = from.centerX();
        int toX = to.centerX();
        int bendY = to.y() - 6;
        graphics.fill(fromX, from.bottom(), fromX + 1, bendY + 1, color);
        graphics.fill(Math.min(fromX, toX), bendY, Math.max(fromX, toX) + 1, bendY + 1, color);
        graphics.fill(toX, bendY, toX + 1, to.y(), color);
    }

    private void drawNode(GuiGraphics graphics, Font font, SkillResponse skill, Area node, boolean hovered,
                          int skillPoints) {
        int accent = accentOf(skill, skillPoints);
        int background = hovered ? SkillTreeConfig.COLOR_NODE_HOVER : SkillTreeConfig.COLOR_NODE_BACKGROUND;
        int iconOffset = (node.width() - ICON_SIZE) / 2;

        graphics.fill(node.x(), node.y(), node.right(), node.bottom(), background);
        graphics.renderOutline(node.x(), node.y(), node.width(), node.height(), accent);
        graphics.renderItem(SkillIcons.of(skill.id()), node.x() + iconOffset, node.y() + iconOffset);
        graphics.drawCenteredString(font, skill.level() + "/" + skill.maxLevel(),
                node.centerX(), node.bottom() + LEVEL_TEXT_GAP, accent);
    }

    private int accentOf(SkillResponse skill, int skillPoints) {
        return switch (skill.state()) {
            case LOCKED -> SkillTreeConfig.COLOR_LOCKED;
            case MAXED -> SkillTreeConfig.COLOR_MAXED;
            case AVAILABLE -> canUpgrade(skill, skillPoints)
                    ? SkillTreeConfig.COLOR_AFFORDABLE
                    : SkillTreeConfig.COLOR_AVAILABLE;
        };
    }

    private boolean canUpgrade(SkillResponse skill, int skillPoints) {
        return skill.state() == SkillState.AVAILABLE && skillPoints >= skill.costPerLevel();
    }

    // ── Tooltip ─────────────────────────────────────────────────────────────

    private List<FormattedCharSequence> tooltipOf(Font font, SkillResponse skill, int skillPoints) {
        List<Component> lines = new ArrayList<>();
        lines.add(Messages.skillName(skill.id()).withStyle(ChatFormatting.BOLD));
        lines.add(Component.translatable("skilltree.screen.skill.level", skill.level(), skill.maxLevel())
                .withStyle(ChatFormatting.GRAY));
        lines.add(Messages.skillDescription(skill.id()));
        lines.add(Component.empty());

        switch (skill.state()) {
            case MAXED -> lines.add(Component.translatable("skilltree.screen.skill.maxed")
                    .withStyle(ChatFormatting.GREEN));
            case LOCKED -> lines.add(Component.translatable("skilltree.screen.skill.requires",
                            Messages.skillName(skill.prerequisiteSkillId()), skill.prerequisiteLevel())
                    .withStyle(ChatFormatting.RED));
            case AVAILABLE -> {
                boolean affordable = canUpgrade(skill, skillPoints);
                lines.add(Component.translatable("skilltree.screen.skill.cost", skill.costPerLevel())
                        .withStyle(affordable ? ChatFormatting.GOLD : ChatFormatting.RED));
                if (affordable) {
                    lines.add(Component.translatable("skilltree.screen.skill.click")
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }

        // Tooltips do not wrap by themselves, so long lines are split here.
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : lines) {
            if (line.getString().isEmpty()) {
                wrapped.add(FormattedCharSequence.EMPTY);
            } else {
                wrapped.addAll(font.split(line, SkillTreeConfig.TOOLTIP_MAX_WIDTH));
            }
        }
        return wrapped;
    }
}

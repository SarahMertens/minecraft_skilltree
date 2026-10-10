package com.sarah.skilltree.client.screen;

import com.sarah.skilltree.config.SkillTreeConfig;
import com.sarah.skilltree.domain.dto.response.QuestResponse;
import com.sarah.skilltree.domain.dto.response.QuestState;
import com.sarah.skilltree.domain.dto.response.RewardResponse;
import com.sarah.skilltree.presentation.message.Messages;
import com.sarah.skilltree.presentation.network.SkillTreeSyncPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * The "Quests" page: a scrollable list with one row per quest, showing what
 * to do, how far the player is and what the reward will be.
 */
final class QuestLogTab implements ScreenTab {

    private static final int STRIPE_WIDTH = 2;
    private static final int TEXT_INSET = 8;
    private static final int FIRST_LINE_OFFSET = 4;
    private static final int SECOND_LINE_OFFSET = 16;
    private static final int SCROLLBAR_WIDTH = 2;
    private static final int SCROLLBAR_GAP = 4;

    private int scroll;

    @Override
    public void render(GuiGraphics graphics, Font font, Area area, SkillTreeSyncPayload data, int mouseX, int mouseY) {
        List<QuestResponse> quests = sorted(data.quests());
        int rowHeight = SkillTreeConfig.QUEST_ROW_HEIGHT;
        scroll = clampScroll(scroll, quests.size(), area);

        // Rows that are scrolled half out of view must not be drawn over the frame.
        graphics.enableScissor(area.x(), area.y(), area.right(), area.bottom());
        for (int i = 0; i < quests.size(); i++) {
            int rowY = area.y() + i * rowHeight - scroll;
            if (rowY + rowHeight > area.y() && rowY < area.bottom()) {
                Area row = new Area(area.x(), rowY, area.width() - SCROLLBAR_WIDTH - SCROLLBAR_GAP, rowHeight - 2);
                drawRow(graphics, font, row, quests.get(i));
            }
        }
        graphics.disableScissor();
        drawScrollbar(graphics, area, quests.size());
    }

    @Override
    public boolean mouseScrolled(Area area, SkillTreeSyncPayload data, double amount) {
        scroll = clampScroll(scroll - (int) Math.round(amount * SkillTreeConfig.QUEST_SCROLL_STEP),
                data.quests().size(), area);
        return true;
    }

    /** Quests the player is working on first, then the locked ones, finished ones at the bottom. */
    private List<QuestResponse> sorted(List<QuestResponse> quests) {
        return quests.stream()
                .sorted(Comparator.comparingInt(quest -> switch (quest.state()) {
                    case ACTIVE -> 0;
                    case LOCKED -> 1;
                    case COMPLETED -> 2;
                }))
                .toList();
    }

    private int clampScroll(int value, int questCount, Area area) {
        int maxScroll = Math.max(0, questCount * SkillTreeConfig.QUEST_ROW_HEIGHT - area.height());
        return Math.max(0, Math.min(value, maxScroll));
    }

    // ── Drawing ─────────────────────────────────────────────────────────────

    private void drawRow(GuiGraphics graphics, Font font, Area row, QuestResponse quest) {
        boolean locked = quest.state() == QuestState.LOCKED;
        int branchColor = locked ? SkillTreeConfig.COLOR_LOCKED : BranchColors.of(quest.branch());
        int textX = row.x() + TEXT_INSET;

        graphics.fill(row.x(), row.y(), row.right(), row.bottom(), SkillTreeConfig.COLOR_NODE_BACKGROUND);
        graphics.fill(row.x(), row.y(), row.x() + STRIPE_WIDTH, row.bottom(), branchColor);

        graphics.drawString(font, titleOf(quest), textX, row.y() + FIRST_LINE_OFFSET, titleColorOf(quest));
        graphics.drawString(font, descriptionOf(quest), textX, row.y() + SECOND_LINE_OFFSET,
                locked ? SkillTreeConfig.COLOR_TEXT_DISABLED : SkillTreeConfig.COLOR_TEXT_MUTED);

        Component rewards = rewardsOf(quest.rewards());
        graphics.drawString(font, rewards, row.right() - TEXT_INSET - font.width(rewards),
                row.y() + FIRST_LINE_OFFSET, locked ? SkillTreeConfig.COLOR_TEXT_DISABLED : SkillTreeConfig.COLOR_POINTS);

        if (quest.state() == QuestState.ACTIVE) {
            drawProgress(graphics, font, row, quest);
        }
    }

    private void drawProgress(GuiGraphics graphics, Font font, Area row, QuestResponse quest) {
        int barWidth = SkillTreeConfig.QUEST_BAR_WIDTH;
        int barX = row.right() - TEXT_INSET - barWidth;
        int barY = row.y() + SECOND_LINE_OFFSET + 2;
        int filled = (int) ((long) barWidth * Math.min(quest.progress(), quest.required()) / quest.required());

        graphics.fill(barX, barY, barX + barWidth, barY + SkillTreeConfig.QUEST_BAR_HEIGHT,
                SkillTreeConfig.COLOR_BAR_BACKGROUND);
        graphics.fill(barX, barY, barX + filled, barY + SkillTreeConfig.QUEST_BAR_HEIGHT,
                SkillTreeConfig.COLOR_BAR_FILL);

        String numbers = quest.progress() + "/" + quest.required();
        graphics.drawString(font, numbers, barX - 4 - font.width(numbers), row.y() + SECOND_LINE_OFFSET,
                SkillTreeConfig.COLOR_TEXT);
    }

    private void drawScrollbar(GuiGraphics graphics, Area area, int questCount) {
        int contentHeight = questCount * SkillTreeConfig.QUEST_ROW_HEIGHT;
        if (contentHeight <= area.height()) {
            return;
        }
        int barX = area.right() - SCROLLBAR_WIDTH;
        int thumbHeight = Math.max(10, area.height() * area.height() / contentHeight);
        int thumbY = area.y() + (int) ((long) (area.height() - thumbHeight) * scroll / (contentHeight - area.height()));

        graphics.fill(barX, area.y(), area.right(), area.bottom(), SkillTreeConfig.COLOR_BAR_BACKGROUND);
        graphics.fill(barX, thumbY, area.right(), thumbY + thumbHeight, SkillTreeConfig.COLOR_AVAILABLE);
    }

    // ── Text ────────────────────────────────────────────────────────────────

    private Component titleOf(QuestResponse quest) {
        MutableComponent title = Messages.questName(quest.id());
        if (quest.repeatable()) {
            title.append(" ").append(Component.translatable("skilltree.screen.quest.repeatable", quest.completions()));
        }
        return title;
    }

    private int titleColorOf(QuestResponse quest) {
        return switch (quest.state()) {
            case ACTIVE -> SkillTreeConfig.COLOR_TEXT;
            case LOCKED -> SkillTreeConfig.COLOR_TEXT_DISABLED;
            case COMPLETED -> SkillTreeConfig.COLOR_MAXED;
        };
    }

    private Component descriptionOf(QuestResponse quest) {
        return switch (quest.state()) {
            case LOCKED -> Component.translatable("skilltree.screen.quest.locked");
            case COMPLETED -> Component.translatable("skilltree.screen.quest.completed");
            case ACTIVE -> quest.target().isEmpty()
                    // "Mine 16 ores": one sentence per kind of objective, with the amount filled in
                    ? Component.translatable(
                            "skilltree.objective." + quest.objectiveType().name().toLowerCase(Locale.ROOT),
                            quest.required())
                    // quests about one specific target have their own sentence
                    : Component.translatable("skilltree.quest." + quest.id() + ".desc");
        };
    }

    private Component rewardsOf(List<RewardResponse> rewards) {
        MutableComponent text = Component.empty();
        for (int i = 0; i < rewards.size(); i++) {
            if (i > 0) {
                text.append("  ");
            }
            text.append(shortReward(rewards.get(i)));
        }
        return text;
    }

    /** A compact form of {@link Messages#reward}, because a quest row has little room. */
    private Component shortReward(RewardResponse reward) {
        return switch (reward.type()) {
            case SKILL_POINTS -> Component.translatable("skilltree.reward.short.skill_points", reward.amount());
            case EXPERIENCE_LEVELS ->
                    Component.translatable("skilltree.reward.short.experience_levels", reward.amount());
            case ITEM -> Messages.reward(reward);
        };
    }
}

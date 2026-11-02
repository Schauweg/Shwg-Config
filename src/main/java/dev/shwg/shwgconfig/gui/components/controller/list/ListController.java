package dev.shwg.shwgconfig.gui.components.controller.list;

import dev.shwg.shwgconfig.api.entries.ListEntry;
import dev.shwg.shwgconfig.api.entries.ListOptions;
import dev.shwg.shwgconfig.api.values.ListValue;
import dev.shwg.shwgconfig.gui.components.screens.GenericScreen;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.components.widget.button.GenericAbstractButton;
import dev.shwg.shwgconfig.gui.components.widget.button.GenericButton;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.layout.GenericLayout;
import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.gui.layout.UnhandledKeyListener;
import dev.shwg.shwgconfig.render.GenericCursors;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.GenericIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Editable list for a {@link ListValue}: one {@link ListRow} per element, each with move up /
 * move down buttons on the left and a remove button on the right, plus an "Add" button below
 * the last row.
 *
 * <pre>
 *  ▲  [ row from the mod ]  ✕
 *  ▼
 *  ▲  [ row from the mod ]  ✕
 *  ▼
 *  [            Add             ]
 * </pre>
 *
 * <p>Saving: the list is the committed element of every row, in order, skipping rows that
 * were never completed. It is saved whenever a row commits, a row moves or a row is removed.</p>
 *
 * <p>Keyboard: Shift + Up / Down moves the row that has focus, if the focused widget
 * didn't use the key itself (e.g. an open suggestion list uses the arrow keys).</p>
 *
 * <p>Selection: the row the user last worked in (any of its widgets got focus), or the first
 * row at the start. It follows its row when rows move, and is reported to a
 * {@link ListSelectionListener} set with {@link ListOptions#onSelectionChanged}.</p>
 */
public final class ListController<E> implements RenderableElement, GenericLayout, ListRowOwner<E>, UnhandledKeyListener {

    // Fixed sizes, independent of the row.
    public static final int ARROW_WIDTH = 13;
    public static final int ARROW_HEIGHT = 10;
    public static final int REMOVE_SIZE = 12;
    public static final int SPACING = 4;          // between arrows, row and remove button
    public static final int ROW_SPACING = 2;      // between two lines
    public static final int MIN_LINE_HEIGHT = 2 * ARROW_HEIGHT;
    public static final int ADD_BUTTON_HEIGHT = 20;

    private static final int SELECTED_FOCUSED = 0x80FFFFFF;
    private static final int SELECTED_UNFOCUSED = 0xFF808080;
    private static final int HOVERED_BACKGROUND = 0x2AC8C8C8;

    /** One line: the row and its three buttons. */
    private final class Line {
        final ListRow<E> row;
        final IconButton up, down, remove;
        int y, height;

        boolean hasFocus() {
            if (up.isFocused() || down.isFocused() || remove.isFocused()) return true;
            boolean[] focused = {false};
            row.visitWidgets(w -> focused[0] |= w.isFocused());
            return focused[0];
        }

        boolean contains(GenericAbstractWidget widget) {
            if (widget == up || widget == down || widget == remove) return true;
            boolean[] found = {false};
            row.visitWidgets(w -> found[0] |= (w == widget));
            return found[0];
        }

        Line(ListRow<E> row) {
            this.row = row;
            this.up = new IconButton(ARROW_WIDTH, ARROW_HEIGHT, Component.translatable("shwgconfig.list.move_up"),
                    () -> move(this, -1), ListController::paintUp);
            this.down = new IconButton(ARROW_WIDTH, ARROW_HEIGHT, Component.translatable("shwgconfig.list.move_down"),
                    () -> move(this, 1), ListController::paintDown);
            this.remove = new IconButton(REMOVE_SIZE, REMOVE_SIZE, Component.translatable("shwgconfig.list.remove"),
                    () -> remove(this), ListController::paintRemove);
        }
    }

    private final ListValue<E> value;
    private final Function<E, ListRow<E>> rowFactory;
    private final int maxSize;
    private final @Nullable Supplier<Boolean> enabledCondition;
    private final @Nullable ListSelectionListener<E> selectionListener;
    private final List<Line> lines = new ArrayList<>();
    private final GenericButton addButton;
    private final int rowWidth;

    private @Nullable Line selected;
    private boolean selectionNotified = false;
    private @Nullable ListRow<E> notifiedRow;
    private @Nullable E notifiedValue;

    private int x, y, width, height;
    private boolean visible = true;

    @SuppressWarnings("unchecked") // the listener's type is the list's element type, see ListOptions#onSelectionChanged
    public ListController(ListEntry<E> entry) {
        this.value = entry.getValue();
        this.rowFactory = entry.getRowFactory();
        ListOptions options = entry.getOptions();
        this.maxSize = options.getMaxSize();
        this.enabledCondition = options.getEnabledCondition();
        this.selectionListener = (ListSelectionListener<E>) options.getSelectionListener();

        int widestRow = measureRowWidth(rowFactory);
        for (E element : value.getValue()) {
            Line line = new Line(createRow(element));
            lines.add(line);
            widestRow = Math.max(widestRow, line.row.getWidth());
        }
        this.rowWidth = widestRow;
        this.width = widthForRow(rowWidth);

        Component addText = options.getAddButtonText() != null
                ? options.getAddButtonText()
                : Component.translatable("shwgconfig.list.add");
        this.addButton = GenericButton.builder(addText, b -> addRow())
                .size(width, ADD_BUTTON_HEIGHT)
                .build();

        this.selected = lines.isEmpty() ? null : lines.get(0);
        layoutChildren();
        notifySelection();
    }

    /**
     * The width a list for {@code entry} will have, measured from one empty row - used to size
     * the category column before the list is built. Rows of one list are expected to be equally wide.
     */
    public static <E> int preferredWidth(ListEntry<E> entry) {
        return widthForRow(measureRowWidth(entry.getRowFactory()));
    }

    private static int widthForRow(int rowWidth) {
        return ARROW_WIDTH + SPACING + rowWidth + SPACING + REMOVE_SIZE;
    }

    /** Width of an empty row, so the list has its width even with no rows. */
    private static <E> int measureRowWidth(Function<E, ListRow<E>> rowFactory) {
        return rowFactory.apply(null).getWidth();
    }

    private ListRow<E> createRow(@Nullable E element) {
        ListRow<E> row = rowFactory.apply(element);
        row.attach(this);
        return row;
    }

    // Row actions

    private void addRow() {
        if (!canAdd()) return;
        Line line = new Line(createRow(null));
        lines.add(line);
        selected = line;
        layoutChildren();
        notifySelection();
        requestScreenLayout();
    }

    private void move(Line line, int direction) {
        int from = lines.indexOf(line);
        int to = from + direction;
        if (from < 0 || to < 0 || to >= lines.size()) return;
        lines.remove(from);
        lines.add(to, line);
        selected = line; // the selection moves with its row
        layoutChildren();
        save();
        notifySelection();
    }

    private void remove(Line line) {
        int index = lines.indexOf(line);
        if (index < 0) return;
        line.remove.setFocused(false);
        line.row.visitWidgets(widget -> widget.setFocused(false));
        lines.remove(index);
        if (selected == line) {
            // select the row that took its place, or the new last row
            selected = lines.isEmpty() ? null : lines.get(Math.min(index, lines.size() - 1));
        }
        layoutChildren();
        save();
        notifySelection();
        requestScreenLayout();
    }

    private boolean canAdd() {
        if (!isEnabled()) return false;
        if (maxSize > 0 && lines.size() >= maxSize) return false;
        for (Line line : lines) {
            if (!line.row.isComplete()) return false;
        }
        return true;
    }

    private boolean isEnabled() {
        return enabledCondition == null || enabledCondition.get();
    }

    private boolean hasFocus() {
        if (addButton.isFocused()) return true;
        for (Line line : lines) {
            if (line.hasFocus()) return true;
        }
        return false;
    }

    @Override
    public boolean onUnhandledKeyPressed(GenericKeyEvent keyEvent, GenericAbstractWidget focused) {
        if (!keyEvent.hasShiftDown() || !(keyEvent.isUp() || keyEvent.isDown()) || !isEnabled()) {
            return false;
        }
        for (Line line : lines) {
            if (line.contains(focused)) {
                move(line, keyEvent.isUp() ? -1 : 1);
                return true;
            }
        }
        return false;
    }

    private void save() {
        List<E> elements = new ArrayList<>();
        for (Line line : lines) {
            E element = line.row.getValue();
            if (element != null) elements.add(element);
        }
        value.setValue(elements);
    }

    @Override
    public void rowChanged(ListRow<E> row) {
        save();
        notifySelection(); // the selected row may have a new element
    }

    @Override
    public List<E> valuesExcept(ListRow<E> row) {
        List<E> elements = new ArrayList<>();
        for (Line line : lines) {
            if (line.row == row) continue;
            E element = line.row.getValue();
            if (element != null) elements.add(element);
        }
        return elements;
    }

    private void updateSelectionFromFocus() {
        for (Line line : lines) {
            if (line != selected && line.hasFocus()) {
                selected = line;
                notifySelection();
                return;
            }
        }
    }

    private void notifySelection() {
        if (selectionListener == null) return;
        ListRow<E> row = selected == null ? null : selected.row;
        E element = row == null ? null : row.getValue();
        if (selectionNotified && row == notifiedRow && Objects.equals(element, notifiedValue)) return;
        selectionNotified = true;
        notifiedRow = row;
        notifiedValue = element;
        selectionListener.onSelectionChanged(element, row);
    }

    private static void requestScreenLayout() {
        if (Minecraft.getInstance().screen instanceof GenericScreen screen) {
            screen.requestLayout();
        }
    }

    private void layoutChildren() {
        int currentY = y;
        int rowX = x + ARROW_WIDTH + SPACING;
        int removeX = rowX + rowWidth + SPACING;

        for (Line line : lines) {
            // A line is as tall as its row, but at least as tall as the two arrows.
            int lineHeight = Math.max(MIN_LINE_HEIGHT, line.row.getHeight());
            line.y = currentY;
            line.height = lineHeight;

            int arrowsY = currentY + (lineHeight - 2 * ARROW_HEIGHT) / 2;
            line.up.setX(x);
            line.up.setY(arrowsY);
            line.down.setX(x);
            line.down.setY(arrowsY + ARROW_HEIGHT);

            line.row.setX(rowX);
            line.row.setY(currentY + (lineHeight - line.row.getHeight()) / 2);

            line.remove.setX(removeX);
            line.remove.setY(currentY + (lineHeight - REMOVE_SIZE) / 2);

            currentY += lineHeight + ROW_SPACING;
        }

        addButton.setX(x);
        addButton.setY(currentY);
        height = currentY + ADD_BUTTON_HEIGHT - y;
    }

    @Override
    public void arrange() {
        layoutChildren();
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        updateSelectionFromFocus();
        boolean enabled = isEnabled();
        boolean showSelection = hasFocus() && lines.size() > 1;

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            
            if (line == selected && showSelection) {
                int color = line.hasFocus() ? SELECTED_FOCUSED : SELECTED_UNFOCUSED;
                graphics.outline(x - 2, line.y - 1, width + 4, line.height + 2, color);
            } else if (isMouseOverLine(line, mouseX, mouseY)) {
                graphics.fillWH(x - 2, line.y - 1, width + 4, line.height + 2, HOVERED_BACKGROUND);
            }
            
            line.up.visible = enabled && i > 0;
            line.down.visible = enabled && i < lines.size() - 1;
            line.remove.visible = enabled;
            line.row.visitWidgets(widget -> widget.active = enabled);

            line.up.render(graphics, mouseX, mouseY, partialTicks);
            line.down.render(graphics, mouseX, mouseY, partialTicks);
            line.row.renderChildren(graphics, mouseX, mouseY, partialTicks);
            line.remove.render(graphics, mouseX, mouseY, partialTicks);
        }

        addButton.active = canAdd();
        addButton.render(graphics, mouseX, mouseY, partialTicks);
    }

    private boolean isMouseOverLine(Line line, int mouseX, int mouseY) {
        return mouseX >= x - 2 && mouseX < x + width + 2
                && mouseY >= line.y - 1 && mouseY < line.y + line.height + 1;
    }

    private static void paintUp(GenericGraphics graphics, int x, int y, int width, int height, boolean highlighted) {
        GenericIcons.drawArrowUp(graphics,
                x + (width - GenericIcons.SMALL_ARROW_GLYPH_WIDTH) / 2,
                y + (height - GenericIcons.SMALL_ARROW_GLYPH_HEIGHT) / 2, highlighted);
    }

    private static void paintDown(GenericGraphics graphics, int x, int y, int width, int height, boolean highlighted) {
        GenericIcons.drawArrowDown(graphics,
                x + (width - GenericIcons.SMALL_ARROW_GLYPH_WIDTH) / 2,
                y + (height - GenericIcons.SMALL_ARROW_GLYPH_HEIGHT) / 2, highlighted);
    }

    private static void paintRemove(GenericGraphics graphics, int x, int y, int width, int height, boolean highlighted) {
        if (highlighted) {
            graphics.fillWH(x, y, width, height, 0x33FFFFFF); // the cross has no highlighted sprite
        }
        GenericIcons.drawCross(graphics,
                x + (width - GenericIcons.CROSS_WIDTH) / 2,
                y + (height - GenericIcons.CROSS_HEIGHT) / 2);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        for (Line line : lines) {
            consumer.accept(line.up);
            consumer.accept(line.down);
            consumer.accept(line.row);
            consumer.accept(line.remove);
        }
        consumer.accept(addButton);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setX(int x) {
        this.x = x;
        layoutChildren();
    }

    @Override
    public void setY(int y) {
        this.y = y;
        layoutChildren();
    }

    @Override
    public void setWidth(int width) {
    }

    @Override
    public void setHeight(int height) {
    }

    @Override
    public int getContentWidth() {
        return width;
    }

    @Override
    public int getContentHeight() {
        return height;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        for (Line line : lines) {
            line.up.setVisible(visible);
            line.down.setVisible(visible);
            line.remove.setVisible(visible);
            line.row.visitWidgets(widget -> widget.setVisible(visible));
        }
        addButton.setVisible(visible);
    }


    private static final class IconButton extends GenericAbstractButton {

        @FunctionalInterface
        interface IconPainter {
            void paint(GenericGraphics graphics, int x, int y, int width, int height, boolean highlighted);
        }

        private final Runnable action;
        private final IconPainter painter;

        IconButton(int width, int height, Component narration, Runnable action, IconPainter painter) {
            super(0, 0, width, height, narration);
            this.action = action;
            this.painter = painter;
        }

        @Override
        protected void onPress() {
            action.run();
        }

        @Override
        public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
            painter.paint(graphics, getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused());
            if (isHovered) {
                graphics.requestCursor(GenericCursors.POINTING_HAND);
            }
        }
    }
}

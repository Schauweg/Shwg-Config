package dev.shwg.testmod.config;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import dev.shwg.shwgconfig.util.DisplayItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryWidget extends GenericAbstractWidget {

    private static final GenericTexture CONTAINER_TEXTURE = GenericTexture.vanillaTexture(
            "textures/gui/container/generic_54.png", 256, 256);

    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 222;
    private static final int BORDER_WIDTH = 7;
    private static final int BORDER_WIDTH_TOP = 17;
    private static final int SLOT_SIZE = 18;
    private static final int SPLITTER_HEIGHT = 14;
    private static final int SPLITTER_TEXTURE_V = 125;
    private static final int HOVER_COLOR = 0x80FFFFFF;

    private final int columns;
    private final int rows;
    private final List<WidgetSlot> slots = new ArrayList<>();
    private final Component title;
    private final Font font = Minecraft.getInstance().font;

    private WidgetSlot focusedSlot;
    private ItemStack mouseStack = ItemStack.EMPTY;

    private final DragState rightDrag = new DragState(true);
    private final DragState leftDrag = new DragState(false);

    public InventoryWidget(int x, int y, int rows, int columns, Component title, Map<Integer, ItemStack> items) {
        super(x, y, columns * SLOT_SIZE + 2 * BORDER_WIDTH, rows * SLOT_SIZE + BORDER_WIDTH_TOP + BORDER_WIDTH + SPLITTER_HEIGHT, title);
        this.title = title;
        this.rows = rows;
        this.columns = columns;

        SimpleContainer container = new SimpleContainer(columns * rows);
        for (int index = 0; index < columns * rows; index++) {
            slots.add(new WidgetSlot(container, index, getSlotX(index), getSlotY(index)));
        }

        items.forEach(container::setItem);
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderFrame(graphics);
        graphics.drawString(font, title, getX() + 8, getY() + 6, 0xFF404040, false);
        renderSlots(graphics, mouseX, mouseY);

        if (!mouseStack.isEmpty()) {
            graphics.renderDecoratedItem(font, mouseStack, mouseX - 8, mouseY - 8);
        }
    }

    private void renderFrame(GenericGraphics graphics) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        int innerW = w - 2 * BORDER_WIDTH;


        graphics.blit(CONTAINER_TEXTURE, x, y, 0, 0, BORDER_WIDTH, h - BORDER_WIDTH, BORDER_WIDTH, h - BORDER_WIDTH); //left border
        graphics.blit(CONTAINER_TEXTURE, x, y + h - BORDER_WIDTH, 0, TEXTURE_HEIGHT - BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH); //bottom-left corner
        graphics.blit(CONTAINER_TEXTURE, x + BORDER_WIDTH, y, BORDER_WIDTH, 0, innerW, BORDER_WIDTH_TOP, innerW, BORDER_WIDTH_TOP); // top border
        graphics.blit(CONTAINER_TEXTURE, x + w - BORDER_WIDTH, y, TEXTURE_WIDTH - BORDER_WIDTH, 0, BORDER_WIDTH, h - BORDER_WIDTH, BORDER_WIDTH, h - BORDER_WIDTH); //right border
        graphics.blit(CONTAINER_TEXTURE, x + w - BORDER_WIDTH, y + h - BORDER_WIDTH, TEXTURE_WIDTH - BORDER_WIDTH, TEXTURE_HEIGHT - BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH); //bottom-right corner
        graphics.blit(CONTAINER_TEXTURE, x + BORDER_WIDTH, y + h - BORDER_WIDTH, BORDER_WIDTH, TEXTURE_HEIGHT - BORDER_WIDTH, innerW, BORDER_WIDTH, innerW, BORDER_WIDTH); //bottom border
        graphics.blit(CONTAINER_TEXTURE, x + BORDER_WIDTH, y + BORDER_WIDTH_TOP + (rows - 1) * SLOT_SIZE, BORDER_WIDTH, SPLITTER_TEXTURE_V, innerW, SPLITTER_HEIGHT, innerW, SPLITTER_HEIGHT); //splitter (above the hotbar row)
    }

    private void renderSlots(GenericGraphics graphics, int mouseX, int mouseY) {
        focusedSlot = null;

        for (int i = 0; i < slots.size(); i++) {
            WidgetSlot slot = slots.get(i);
            graphics.blit(CONTAINER_TEXTURE, getSlotX(i) - 1, getSlotY(i) - 1, BORDER_WIDTH, BORDER_WIDTH_TOP, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
            graphics.renderDecoratedItem(font, slot.getItem(), slot.x, slot.y);

            if (isPointOverSlot(slot, mouseX, mouseY)) {
                focusedSlot = slot;
                graphics.fillWH(slot.x, slot.y, SLOT_SIZE - 2, SLOT_SIZE - 2, HOVER_COLOR);
            }
        }
    }

    @Override
    protected void updateGenericWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        // intentionally silent
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        if (focusedSlot != null) {
            // Number keys 1-9: swap the hovered slot with the matching hotbar slot
            int hotbarIndex = keyEvent.getKey().getValue() - InputConstants.KEY_1;
            if (hotbarIndex >= 0 && hotbarIndex < Math.min(9, columns)) {
                WidgetSlot hotbarSlot = slots.get(hotbarStart() + hotbarIndex);
                if (hotbarSlot != focusedSlot) {
                    swapSlots(focusedSlot, hotbarSlot);
                    return true;
                }
            }
        }
        return super.onKeyPressed(keyEvent);
    }

    @Override
    protected boolean onMouseClicked(GenericMouseButtonEvent event) {
        WidgetSlot target = getSlotAt(event.getMouseX(), event.getMouseY());

        if (target == null) {
            endDrag(rightDrag, false);
            endDrag(leftDrag, true);
            return false;
        }

        // Pressing the other button cancels the drag that is in progress
        if (event.isLeftClick()) {
            endDrag(rightDrag, false);
        }
        if (event.isRightClick()) {
            endDrag(leftDrag, true);
        }

        if (event.isLeftClick() && event.isDoubleClick() && !mouseStack.isEmpty()) {
            collectMatchingItemsToCursor();
            return true;
        }

        if (event.isLeftClick() && event.isShiftDown() && !target.getItem().isEmpty()) {
            quickMove(target);
            return true;
        }

        if (event.isRightClick()) {
            handleRightClick(target);
            return true;
        }

        if (event.isLeftClick()) {
            handleLeftClick(target);
            return true;
        }

        return super.onMouseClicked(event);
    }

    @Override
    protected boolean onMouseDragged(GenericMouseButtonEvent event, double deltaX, double deltaY) {
        DragState drag = rightDrag.active ? rightDrag : leftDrag.active ? leftDrag : null;
        if (drag != null) {
            dragOver(drag, getSlotAt(event.getMouseX(), event.getMouseY()));
            return true;
        }
        return super.onMouseDragged(event, deltaX, deltaY);
    }

    @Override
    protected boolean onMouseReleased(GenericMouseButtonEvent event) {
        if (leftDrag.active) {
            if (leftDrag.slots.size() > 1) {
                // Real drag: keep the distribution
                endDrag(leftDrag, false);
            } else {
                // Plain click: undo the preview and run the normal click logic instead
                WidgetSlot clicked = leftDrag.slots.isEmpty()
                        ? getSlotAt(event.getMouseX(), event.getMouseY())
                        : leftDrag.slots.get(0);
                endDrag(leftDrag, true);
                if (clicked != null) {
                    clickSlotWithCursor(clicked);
                }
            }
            return true;
        }

        if (rightDrag.active) {
            endDrag(rightDrag, false);
            return true;
        }

        return super.onMouseReleased(event);
    }

    private void handleLeftClick(WidgetSlot slot) {
        if (mouseStack.isEmpty()) {
            ItemStack slotStack = slot.getItem();
            if (!slotStack.isEmpty()) {
                mouseStack = slotStack.copy();
                slot.set(ItemStack.EMPTY);
            }
            return;
        }

        startDrag(leftDrag);
        tryAddSlotToDrag(leftDrag, slot);
    }

    private void handleRightClick(WidgetSlot slot) {
        ItemStack slotStack = slot.getItem();

        if (mouseStack.isEmpty()) {
            // Pick up half the stack and be ready to drag-distribute it right away
            if (slotStack.isEmpty()) return;

            ItemStack remaining = slotStack.copy();
            mouseStack = remaining.split((slotStack.getCount() + 1) / 2);
            slot.set(remaining);
            startDrag(rightDrag);
            rightDrag.suppressedSlot = slot;
            return;
        }

        // Holding items: the drag only stays armed if the first slot accepted an item
        startDrag(rightDrag);
        if (!tryAddSlotToDrag(rightDrag, slot)) {
            endDrag(rightDrag, false);

            // Different item in the slot: swap, like vanilla
            if (!slotStack.isEmpty() && !ItemStack.isSameItem(mouseStack, slotStack)) {
                clickSlotWithCursor(slot);
            }
        }
    }

    private void clickSlotWithCursor(WidgetSlot slot) {
        ItemStack slotStack = slot.getItem();

        if (slotStack.isEmpty()) {
            slot.set(mouseStack.copy());
            mouseStack = ItemStack.EMPTY;
            return;
        }

        if (ItemStack.isSameItem(mouseStack, slotStack)) {
            int max = maxStackFor(slot, slotStack);
            int total = slotStack.getCount() + mouseStack.getCount();

            ItemStack merged = slotStack.copy();
            merged.setCount(Math.min(total, max));
            slot.set(merged);

            if (total <= max) {
                mouseStack = ItemStack.EMPTY;
            } else {
                mouseStack.setCount(total - max);
            }
            return;
        }

        ItemStack previous = slotStack.copy();
        slot.set(mouseStack.copy());
        mouseStack = previous;
    }

    private void collectMatchingItemsToCursor() {
        if (mouseStack.isEmpty()) return;

        int maxCount = mouseStack.getMaxStackSize();
        if (mouseStack.getCount() >= maxCount) return;

        List<WidgetSlot> matching = new ArrayList<>();
        for (WidgetSlot slot : slots) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && ItemStack.isSameItem(mouseStack, stack)) {
                matching.add(slot);
            }
        }

        // Smallest stacks first
        matching.sort(Comparator.comparingInt(slot -> slot.getItem().getCount()));

        for (WidgetSlot slot : matching) {
            if (mouseStack.getCount() >= maxCount) break;

            ItemStack remaining = slot.getItem().copy();
            int amount = Math.min(maxCount - mouseStack.getCount(), remaining.getCount());

            mouseStack.grow(amount);
            remaining.shrink(amount);
            slot.set(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        }
    }

    private void swapSlots(WidgetSlot first, WidgetSlot second) {
        ItemStack firstStack = first.getItem().copy();
        ItemStack secondStack = second.getItem().copy();
        first.set(secondStack);
        second.set(firstStack);
    }

    private void startDrag(DragState drag) {
        drag.reset();
        drag.active = true;
        drag.startStack = mouseStack.copy();
    }

    private void endDrag(DragState drag, boolean revert) {
        if (drag.active && revert) {
            for (WidgetSlot slot : drag.slots) {
                slot.set(drag.originalStacks.get(slot).copy());
            }
            mouseStack = drag.startStack.copy();
        }
        drag.reset();
    }

    private void dragOver(DragState drag, WidgetSlot hovered) {
        if (hovered == drag.suppressedSlot) return;

        drag.suppressedSlot = null; // the pointer left the pickup slot, it is a normal target again
        if (hovered != null) {
            tryAddSlotToDrag(drag, hovered);
        }
    }

    private boolean tryAddSlotToDrag(DragState drag, WidgetSlot slot) {
        if (!drag.active || drag.startStack.isEmpty()) return false;
        if (drag.slots.contains(slot)) return false;
        // Never include more slots than there are items to hand out
        if (drag.slots.size() >= drag.startStack.getCount()) return false;

        ItemStack current = slot.getItem();
        if (!current.isEmpty() && !ItemStack.isSameItem(drag.startStack, current)) return false;
        if (current.getCount() >= maxStackFor(slot, drag.startStack)) return false;

        drag.slots.add(slot);
        drag.originalStacks.put(slot, current.copy());
        redistribute(drag);
        return true;
    }

    private void redistribute(DragState drag) {
        int perSlot = drag.oneItemPerSlot
                ? 1
                : drag.startStack.getCount() / drag.slots.size();
        int used = 0;

        for (WidgetSlot slot : drag.slots) {
            ItemStack original = drag.originalStacks.get(slot);
            int add = Math.min(perSlot, maxStackFor(slot, drag.startStack) - original.getCount());

            if (add > 0) {
                ItemStack updated = drag.startStack.copy();
                updated.setCount(original.getCount() + add);
                slot.set(updated);
                used += add;
            } else {
                slot.set(original.copy());
            }
        }

        mouseStack = drag.startStack.copy();
        mouseStack.shrink(used);
    }

    private void quickMove(WidgetSlot source) {
        ItemStack remaining = source.getItem().copy();

        if (isSlotInHotbar(source.getContainerSlot())) {
            insertIntoRange(remaining, 0, hotbarStart(), false);
        } else {
            insertIntoRange(remaining, hotbarStart(), slots.size(), true);
        }

        source.set(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    }

    private void insertIntoRange(ItemStack stack, int start, int end, boolean reverse) {
        int count = end - start;

        for (int pass = 0; pass < 2; pass++) {
            boolean intoEmpty = pass == 1;
            for (int n = 0; n < count && !stack.isEmpty(); n++) {
                WidgetSlot slot = slots.get(reverse ? end - 1 - n : start + n);
                insertIntoSlot(stack, slot, intoEmpty);
            }
        }
    }

    private void insertIntoSlot(ItemStack source, WidgetSlot slot, boolean intoEmpty) {
        ItemStack target = slot.getItem();

        if (intoEmpty) {
            if (!target.isEmpty()) return;
        } else if (target.isEmpty() || !ItemStack.isSameItem(source, target)) {
            return;
        }

        int amount = Math.min(source.getCount(), maxStackFor(slot, source) - target.getCount());
        if (amount <= 0) return;

        ItemStack updated = source.copy();
        updated.setCount(target.getCount() + amount);
        slot.set(updated);
        source.shrink(amount);
    }

    private WidgetSlot getSlotAt(double mouseX, double mouseY) {
        for (WidgetSlot slot : slots) {
            if (isPointOverSlot(slot, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private boolean isPointOverSlot(WidgetSlot slot, double mouseX, double mouseY) {
        return mouseX >= slot.x && mouseX < slot.x + SLOT_SIZE
                && mouseY >= slot.y && mouseY < slot.y + SLOT_SIZE;
    }

    private int getSlotX(int index) {
        return getX() + 1 + BORDER_WIDTH + (index % columns) * SLOT_SIZE;
    }

    private int getSlotY(int index) {
        int row = index / columns;
        return getY() + 1 + BORDER_WIDTH_TOP + row * SLOT_SIZE + (isSlotInHotbar(index) ? SPLITTER_HEIGHT : 0);
    }

    private int hotbarStart() {
        return (rows - 1) * columns;
    }

    private boolean isSlotInHotbar(int index) {
        return index >= hotbarStart();
    }

    private static int maxStackFor(Slot slot, ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), slot.getMaxStackSize(stack));
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i).x = getSlotX(i);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i).y = getSlotY(i);
        }
    }

    private static final class DragState {

        private final boolean oneItemPerSlot;
        private final List<WidgetSlot> slots = new ArrayList<>();
        private final Map<WidgetSlot, ItemStack> originalStacks = new HashMap<>();
        private ItemStack startStack = ItemStack.EMPTY;
        private boolean active;

        private WidgetSlot suppressedSlot;

        private DragState(boolean oneItemPerSlot) {
            this.oneItemPerSlot = oneItemPerSlot;
        }

        private void reset() {
            active = false;
            slots.clear();
            originalStacks.clear();
            startStack = ItemStack.EMPTY;
            suppressedSlot = null;
        }
    }

    // shadows Slot's final x/y, only correct when accessed through the WidgetSlot type
    public static class WidgetSlot extends Slot {
        public int x;
        public int y;

        public WidgetSlot(final Container container, final int slot, final int x, final int y) {
            super(container, slot, x, y);
            this.x = x;
            this.y = y;
        }
    }

    public static Builder builder(Component title) {
        return new Builder(title);
    }

    public static final class Builder extends GenericAbstractWidget.Builder<InventoryWidget, Builder> {

        private int rows = 3;
        private int columns = 3;
        private final Map<Integer, ItemStack> items = new HashMap<>();

        public Builder(Component title) {
            super(title);
        }

        @Override
        public Builder size(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
            return self();
        }

        @Override
        public Builder bounds(int x, int y, int rows, int columns) {
            this.x = x;
            this.y = y;
            this.rows = rows;
            this.columns = columns;
            return self();
        }

        @Override
        public Builder width(int columns) {
            this.columns = columns;
            return self();
        }

        @Override
        public Builder height(int rows) {
            this.rows = rows;
            return self();
        }

        public Builder rows(int rows) {
            this.rows = rows;
            return self();
        }

        public Builder columns(int columns) {
            this.columns = columns;
            return self();
        }

        public Builder addItem(Item item, int count, int slot) {
            items.put(slot, DisplayItemStack.getStackSimple(item, count));
            return self();
        }

        public Builder addItem(Item item, int slot) {
            items.put(slot, DisplayItemStack.getStackSimple(item, 1));
            return self();
        }

        @Override
        public InventoryWidget build() {
            int maxSlots = rows * columns - 1;
            items.forEach((slot, stack) -> {
                if (slot > maxSlots) {
                    throw new IllegalArgumentException("Can't put items into slot: " + slot + " highest slot is: " + maxSlots);
                }
            });
            return new  InventoryWidget(x, y, rows, columns, message, items);
        }
    }
}

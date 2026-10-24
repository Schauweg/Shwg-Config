package dev.shwg.testmod.config.widgets;

import dev.shwg.shwgconfig.gui.components.controller.list.ListSelectionListener;
import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Test preview for the item list: shows the selected row's item next to that row.
 * The item is drawn at the selected row's height instead of this widget's own position.
 */
public class ItemListPreview implements RenderableElement, ListSelectionListener<String> {

    private static final int SIZE = 16;

    private @Nullable Item item;
    private @Nullable LayoutElement row;

    private int x, y;
    private boolean visible = true;

    @Override
    public void onSelectionChanged(@Nullable String value, @Nullable LayoutElement row) {
        Identifier id = value == null ? null : RegistryHelper.tryBuild(value);
        this.item = id == null ? null : RegistryHelper.getItem(id);
        this.row = row;
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        // Read the row's position every frame - it moves when scrolling or when rows change.
        int drawY = row == null ? y : row.getY() + (row.getHeight() - SIZE) / 2;
        graphics.outline(x - 1, drawY - 1, SIZE + 2, SIZE + 2, 0xFF505050);
        if (item != null) {
            graphics.renderItem(item, x, drawY);
        }
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
        return SIZE;
    }

    @Override
    public int getHeight() {
        return SIZE;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void setWidth(int width) {
    }

    @Override
    public void setHeight(int height) {
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}

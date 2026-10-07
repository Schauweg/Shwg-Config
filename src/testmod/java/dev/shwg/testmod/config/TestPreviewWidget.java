package dev.shwg.testmod.config;

import dev.shwg.shwgconfig.gui.components.widget.slider.NumberSlider;
import dev.shwg.shwgconfig.gui.layout.GenericLayout;
import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.util.DisplayItemStack;
import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class TestPreviewWidget implements GenericLayout, RenderableElement {

    private int x;
    private int y;
    private int width;
    private int height;
    private boolean visible = true;

    private final ItemStack itemStack = DisplayItemStack.builder(Items.DIAMOND_PICKAXE).withDamage(RegistryHelper.DIAMOND_TOOL_MAX_DAMAGE).build();
    private final NumberSlider<Integer> damageSlider;

    public TestPreviewWidget() {
        this.x = 0;
        this.y = 0;
        this.width = 50;
        this.height = 38;

        this.damageSlider = NumberSlider.intSlider(Component.empty(), 0, itemStack.getMaxDamage(), itemStack.getMaxDamage())
                .applyValue(integer -> {
                    itemStack.setDamageValue(itemStack.getMaxDamage() - integer);
                })
                .size(width, 20)
                .build();

        this.damageSlider.setVisible(true);
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int centerX = getX() + getWidth() / 2;
        graphics.renderDecoratedItem(font, itemStack, centerX - 8, getY());
        damageSlider.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        damageSlider.setVisible(visible);
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
        this.damageSlider.setX(x);
    }

    @Override
    public void setY(int y) {
        this.y = y;
        this.damageSlider.setY(y + 20);
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        consumer.accept(damageSlider);
    }

    @Override
    public int getContentHeight() {
        return height;
    }

    @Override
    public int getContentWidth() {
        return width;
    }
}

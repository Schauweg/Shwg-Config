package dev.shwg.shwgconfig.gui.components.widget.button;

import dev.shwg.shwgconfig.render.GenericCursors;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.gui.deferred.GenericTooltip;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GenericButton extends GenericAbstractButton {

    private final PressAction pressAction;

    public GenericButton(int x, int y, int width, int height, Component message, @NotNull PressAction pressAction, GenericTooltip tooltip, GenericTexture button, GenericTexture buttonHighlighted, GenericTexture buttonDisabled) {
        super(x, y, width, height, message, button, buttonHighlighted, buttonDisabled);
        this.setTooltip(tooltip);
        this.pressAction = pressAction;
    }

    public GenericButton(int x, int y, int width, int height, Component message, @NotNull PressAction pressAction, GenericTooltip tooltip) {
        this(x, y, width, height, message, pressAction, tooltip, BUTTON, BUTTON_HIGHLIGHTED, BUTTON_DISABLED);
    }

    @Override
    protected void onPress() {
        pressAction.onPress(this);
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawTexture(currentButtonTexture(), this.getX(), this.getY(), this.width, this.height);
        Font font = Minecraft.getInstance().font;
        graphics.drawCenteredString(font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, 0xFFFFFFFF);
        if (this.isHovered) {
            renderDeferredTooltip(graphics, mouseX, mouseY);
            graphics.requestCursor(GenericCursors.POINTING_HAND);
        }
    }

    public static Builder builder(Component message, @NotNull PressAction pressAction) {
        return new Builder(message, pressAction);
    }

    public static class Builder extends GenericAbstractButton.Builder<GenericButton, GenericButton.Builder> {

        private final PressAction pressAction;

        public Builder(Component message, @NotNull PressAction pressAction) {
            super(message);
            this.pressAction = pressAction;
        }

        public GenericButton build() {
            return new GenericButton(x, y, width, height, message, pressAction, tooltip, button, buttonHighlighted, buttonDisabled);
        }
    }

    @FunctionalInterface
    public interface PressAction {
        void onPress(GenericButton button);
    }

}

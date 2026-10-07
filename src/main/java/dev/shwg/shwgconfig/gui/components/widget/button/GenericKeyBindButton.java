package dev.shwg.shwgconfig.gui.components.widget.button;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.render.GenericCursors;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GenericKeyBindButton extends GenericAbstractButton{

    private final KeyMapping keyMapping;
    private boolean listening;

    public GenericKeyBindButton(int x, int y, int width, int height, KeyMapping keyMapping, Component message) {
        super(x, y, width, height, message);

        this.keyMapping = keyMapping;
        this.listening = false;
    }

    @Override
    protected void onPress() {
        listening = true;
        setMessage(Component.literal("> ").append(Component.literal("...")).append(" <").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        if (listening) {
            bindButton(keyEvent.getKey());
            return true;
        }
        return super.onKeyPressed(keyEvent);
    }

    @Override
    protected boolean onMouseClicked(GenericMouseButtonEvent mouseButtonEvent) {
        if (listening) {
            bindButton(mouseButtonEvent.getKey());
            return true;
        }
        return super.onMouseClicked(mouseButtonEvent);
    }

    public void bindButton(InputConstants.Key key) {
        keyMapping.setKey(key);
        listening = false;
        setMessage(keyMapping.getTranslatedKeyMessage());
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

    @Override
    protected GenericTexture currentButtonTexture() {
        if (listening) { return buttonHighlighted; }
        return super.currentButtonTexture();
    }

    public KeyMapping getKeyMapping() {
        return keyMapping;
    }

    public boolean isListening() {
        return listening;
    }

    public static class Builder extends GenericAbstractButton.Builder<GenericKeyBindButton, GenericKeyBindButton.Builder> {

        private final KeyMapping mapping;

        public Builder(Component message, @NotNull KeyMapping mapping) {
            super(message);
            this.mapping = mapping;
        }

        public GenericKeyBindButton build() {
            return new GenericKeyBindButton(x, y, width, height, mapping, message);
        }
    }
}

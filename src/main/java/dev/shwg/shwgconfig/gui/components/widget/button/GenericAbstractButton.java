package dev.shwg.shwgconfig.gui.components.widget.button;

import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import dev.shwg.shwgconfig.render.textures.NineSlicedSprite;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public abstract class GenericAbstractButton extends GenericAbstractWidget {

    public static final NineSlicedSprite BUTTON = NineSlicedSprite.constantBorder("minecraft", "textures/gui/widgets.png",
            0, 66, 200, 20, 256, 256,
            "widget/button", 3);

    public static final NineSlicedSprite BUTTON_HIGHLIGHTED = NineSlicedSprite.constantBorder("minecraft", "textures/gui/widgets.png",
            0, 86, 200, 20, 256, 256,
            "widget/button_highlighted", 3);

    public static final NineSlicedSprite BUTTON_DISABLED = NineSlicedSprite.constantBorder("minecraft", "textures/gui/widgets.png",
            0, 46, 200, 20, 256, 256,
            "widget/button_disabled", 3);

    protected final GenericTexture button;
    protected final GenericTexture buttonHighlighted;
    protected final GenericTexture buttonDisabled;

    protected abstract void onPress();

    public GenericAbstractButton(int x, int y, int width, int height, Component message) {
        this(x, y, width, height, message, BUTTON, BUTTON_HIGHLIGHTED, BUTTON_DISABLED);
    }

    public GenericAbstractButton(int x, int y, int width, int height, Component message, GenericTexture button, GenericTexture buttonHighlighted, GenericTexture buttonDisabled) {
        super(x, y, width, height, message);
        this.button = button;
        this.buttonHighlighted = buttonHighlighted;
        this.buttonDisabled = buttonDisabled;
    }

    @Override
    protected void onClick(GenericMouseButtonEvent event) {
        if (this.active && this.visible) {
            this.onPress();
        }
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        if (this.active && this.visible && keyEvent.isSelection()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
        }
        return false;
    }

    protected GenericTexture currentButtonTexture() {
        if (!this.isActive()) {
            return buttonDisabled;
        } else if (this.isHoveredOrFocused()) {
            return this.buttonHighlighted;
        } else {
            return this.button;
        }
    }

    public abstract static class Builder<T extends GenericAbstractButton, B extends Builder<T, B>> extends GenericAbstractWidget.Builder<T, B> {

        protected GenericTexture button = GenericAbstractButton.BUTTON;
        protected GenericTexture buttonHighlighted = GenericAbstractButton.BUTTON_HIGHLIGHTED;
        protected GenericTexture buttonDisabled = GenericAbstractButton.BUTTON_DISABLED;

        public Builder(Component message) {
            super(message);
        }

        public B textures(GenericTexture button, GenericTexture buttonHighlighted, GenericTexture buttonDisabled) {
            this.button = button;
            this.buttonHighlighted = buttonHighlighted;
            this.buttonDisabled = buttonDisabled;
            return self();
        }

    }
}

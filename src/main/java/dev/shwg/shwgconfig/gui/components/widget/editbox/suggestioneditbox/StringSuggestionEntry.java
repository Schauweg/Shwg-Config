package dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox;

import dev.shwg.shwgconfig.gui.deferred.overlay.ListWidgetOverlay;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class StringSuggestionEntry extends SuggestionEntry {

    private final String text;

    public StringSuggestionEntry(ListWidgetOverlay<?> owner, String text) {
        super(owner);
        this.text = text;
    }

    @Override
    protected void renderContent(GenericGraphics graphics, int mouseX, int mouseY, float partialTick, int contentWidth) {
        boolean hovered = isHovered(mouseX, mouseY);
        if (isSelected()) {
            graphics.fillWH(getX(), getY(), getWidth(), getHeight(), 0xFF5865F2);
        } else if (hovered) {
            graphics.fillWH(getX(), getY(), getWidth(), getHeight(), 0x33FFFFFF);
        }
        int textColor = isSelected() ? 0xFFFFFF00 : 0xFFFFFFFF;
        graphics.drawScrollingString(Minecraft.getInstance().font, Component.literal(text),
                getContentX(), getY(), contentWidth, getHeight(), isSelected() || hovered, textColor);
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @Override
    public Component displayComponent() {
        return Component.literal(text);
    }

}

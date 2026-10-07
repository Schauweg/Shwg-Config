package dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox;

import dev.shwg.shwgconfig.gui.deferred.overlay.ListWidgetOverlay;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class RegistrySuggestionEntry extends SuggestionEntry {

    private final Identifier id;
    private final Function<Identifier, Component> displayComponent;
    private final @Nullable Function<Identifier, ItemStack> iconProvider;

    public RegistrySuggestionEntry(ListWidgetOverlay<?> owner, Identifier identifier,
                                   Function<Identifier, Component> displayComponent) {
        this(owner, identifier, displayComponent, null);
    }

    public RegistrySuggestionEntry(ListWidgetOverlay<?> owner, Identifier identifier,
                                   Function<Identifier, Component> displayComponent,
                                   @Nullable Function<Identifier, ItemStack> iconProvider) {
        super(owner);
        this.id = identifier;
        this.displayComponent = displayComponent;
        this.iconProvider = iconProvider;
    }

    @Override
    public String saveValue() {
        return id.toString();
    }

    @Override
    public Component displayComponent() {
        return displayComponent.apply(id);
    }

    @Override
    protected boolean hasIcon() {
        return iconProvider != null;
    }

    @Override
    protected void renderIcon(GenericGraphics graphics, int x, int y, int size, float partialTick) {
        if (iconProvider != null) {
            graphics.renderScaledItem(iconProvider.apply(id), x, y, size);
        }
    }

    @Override
    protected void renderContent(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks, int contentWidth) {
        boolean hovered = isHovered(mouseX, mouseY);
        if (isSelected()) {
            graphics.fillWH(getX(), getY(), getWidth(), getHeight(), 0xFF5865F2);
        } else if (hovered) {
            graphics.fillWH(getX(), getY(), getWidth(), getHeight(), 0x33FFFFFF);
        }
        int textColor = isSelected() ? 0xFFFFFF00 : 0xFFFFFFFF;
        graphics.drawScrollingString(Minecraft.getInstance().font, displayComponent(),
                getContentX(), getY(), contentWidth, getHeight(), isSelected() || hovered, textColor);
    }

    @Override
    public int getHeight() {
        return 12;
    }
}

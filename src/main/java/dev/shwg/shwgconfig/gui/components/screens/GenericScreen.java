package dev.shwg.shwgconfig.gui.components.screens;

import dev.shwg.shwgconfig.gui.components.elements.StringElement;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.GraphicsAccessor;
import dev.shwg.shwgconfig.gui.deferred.overlay.OverlayDispatch;
import dev.shwg.shwgconfig.gui.deferred.overlay.WidgetOverlay;
import dev.shwg.shwgconfig.gui.input.events.GenericCharacterEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.gui.layout.GenericLayout;
import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.input.LastInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.ArrayList;
import java.util.List;

/*? if >= 1.21.9 { */
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
/*?} */

/*? if >= 1.20 { */
import net.minecraft.client.gui.GuiGraphicsExtractor;
/*?} else {*/
//import com.mojang.blaze3d.vertex.PoseStack;
/*?} */

/*? if < 1.19.4 { */
/*import dev.shwg.shwgconfig.gui.navigation.Rect;
import dev.shwg.shwgconfig.gui.navigation.SpatialDirection;
import dev.shwg.shwgconfig.gui.navigation.SpatialNavigation;
import com.mojang.blaze3d.platform.InputConstants;
*//*?} */

/**
 * Base class for screens with proper overlay handling.
 * <br>
 * {@link WidgetOverlay} Overlays always get input priority over normal widgets.
 */
public class GenericScreen extends Screen {

    /*? if < 1.19.4 { */
        //public static final int TAB_BYPASS_KEYCODE = -999999;
    /*? } */

    protected final List<LayoutElement> elements = new ArrayList<>();
    private final Screen parent;
    private boolean closeOnEsc = true;

    public GenericScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    /*? if >= 26 { */
    public final void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
     /*? } else if >= 1.20 { */
    //public final void render(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
     /*? } else { */
    //public final void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        /*? } */
        GenericGraphics g = ((GraphicsAccessor) graphics).shwgConfigs$getGraphics();
        updateOverlayBoundaries();
        render(g, mouseX, mouseY, partialTicks);
        renderDeferred(g, mouseX, mouseY, partialTicks);
    }


    public void add(LayoutElement element) {
        elements.add(element);
    }

    public void remove(LayoutElement element) {
        elements.remove(element);
    }

    public void addAndRegister(LayoutElement element) {
        add(element);
        registerElement(element);
    }

    public void removeAndDeregister(LayoutElement element) {
        remove(element);
        if (element instanceof GenericAbstractWidget widget) {
            this.removeWidget(widget);
        } else if (element instanceof GenericLayout layout) {
            layout.visitWidgets(this::removeWidget);
        }
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        this.elements.clear();
    }

    protected void registerElements() {
        for (LayoutElement element : elements) {
            registerElement(element);
        }
    }

    private void registerElement(LayoutElement element) {
        if (element instanceof GenericAbstractWidget widget && !children().contains(widget)) {
            this.addWidget(widget);
        } else if (element instanceof GenericLayout layout) {
            layout.visitWidgets(this::addWidget);
        }
    }

    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        //? if < 1.21.6
        //renderBackground(graphics, mouseX, mouseY, partialTicks);

        boolean blocked = OverlayDispatch.isMouseBlocked(this.children(), mouseX, mouseY);
        for (LayoutElement element : elements) {
            renderElement(element, graphics, blocked ? -1 : mouseX, blocked ? -1 : mouseY, partialTicks);
        }
    }

    private static void renderElement(LayoutElement element, GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (element instanceof RenderableElement renderable) {
            renderable.render(graphics, mouseX, mouseY, partialTicks);
        } else if (element instanceof GenericLayout layout) {
            layout.renderChildren(graphics, mouseX, mouseY, partialTicks);
        }
    }

    public RenderableElement label(Font font, Component component, int x, int y, int color) {
        return new StringElement(font, component, x, y, color);
    }

    public void renderDeferred(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.renderDeferred(mouseX, mouseY, partialTicks);
    }

    public void renderBackground(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        /*?  if >= 26 { */
        super.extractBackground(graphics.getGraphics(), mouseX, mouseY, partialTicks);
         /*? } else if >= 1.20.2 { */
        //super.renderBackground(graphics.getGraphics(), mouseX, mouseY, partialTicks);
         /*? } else { */
        //super.renderBackground(graphics.getGraphics());
        /*? } */
    }

    public void updateOverlayBoundaries() {
        WidgetOverlay.topBoundary = 0;
        WidgetOverlay.bottomBoundary = height;
    }

    // --- mouse click

    protected boolean onMouseClicked(GenericMouseButtonEvent mouseButtonEvent) {
        return false;
    }

    @Override
    /*? if >= 1.21.9 { */
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseButtonEvent, doubleClick);
    /*?} else {*/
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GenericMouseButtonEvent.beginClick(mouseX, mouseY, button);
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseX, mouseY, button, true);
    *//*?}*/

        LastInput.input = LastInput.mouse(e.getButton());

        if (OverlayDispatch.mouseClicked(this.children(), e)) return true;
        if (onMouseClicked(e)) return true;

        /*? if >= 1.21.9 { */
        boolean handled = super.mouseClicked(mouseButtonEvent, doubleClick);
         /*?} else {*/
        //boolean handled = super.mouseClicked(mouseX, mouseY, button);
        /*?}*/

        // Vanilla only starts "dragging" for the primary button. Do it for the others too,
        // so drag and release events reach the widget that took the press.
        if (handled && !e.isLeftClick()) {
            GuiEventListener pressed = getChildAt(e.getMouseX(), e.getMouseY()).orElse(null);
            if (pressed != null && pressed == getFocused()) {
                setDragging(true);
            }
        }

        if (!handled) {
            OverlayDispatch.closeOverlays(children());
        }

        return handled;
    }

    // --- mouse release

    protected boolean onMouseReleased(GenericMouseButtonEvent mouseButtonEvent) {
        return false;
    }

    @Override
    /*? if >= 1.21.9 { */
    public boolean mouseReleased(@NotNull MouseButtonEvent mouseButtonEvent) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseButtonEvent, false);
    /*?} else {*/
    /*public boolean mouseReleased(double mouseX, double mouseY, int button) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseX, mouseY, button, false);
        *//*?}*/

        if (OverlayDispatch.mouseReleased(this.children(), e)) return true;
        if (onMouseReleased(e)) return true;

        //manually resetting dragging status
        if (this.isDragging() && this.getFocused() != null) {
            GuiEventListener focused = this.getFocused();
            this.setDragging(false);
            /*? if >= 1.21.9 { */
            return focused.mouseReleased(mouseButtonEvent);
            /*?} else {*/
            //return focused.mouseReleased(mouseX, mouseY, button);
            /*?}*/
        }

        /*? if >= 1.21.9 { */
        return super.mouseReleased(mouseButtonEvent);
         /*?} else {*/
        //return super.mouseReleased(mouseX, mouseY, button);
        /*?}*/
    }

    // --- mouse drag

    protected boolean onMouseDragged(GenericMouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        return false;
    }

    @Override
    /*? if >= 1.21.9 { */
    public boolean mouseDragged(@NotNull MouseButtonEvent event, double deltaX, double deltaY) {
         GenericMouseButtonEvent e = new GenericMouseButtonEvent(event, false);
    /*?} else {*/
    /*public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseX, mouseY, button, false);
        *//*?}*/

        if (OverlayDispatch.mouseDragged(this.children(), e, deltaX, deltaY)) return true;
        if (onMouseDragged(e, deltaX, deltaY)) return true;

        // Workaround to make drag work with other buttons than left mouse button, calling mouseDragged directly of the focused widget
        if (isDragging() && !e.isLeftClick() && getFocused() != null) {
            /*? if >= 1.21.9 { */
            return getFocused().mouseDragged(event, deltaX, deltaY);
            /*?} else {*/
            //return getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            /*?}*/
        }

        /*? if >= 1.21.9 { */
        return super.mouseDragged(event, deltaX, deltaY);
         /*?} else {*/
        //return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        /*?}*/
    }

    // --- mouse scroll

    protected boolean onMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        return false;
    }

    @Override
    /*? if >= 1.20.2 { */
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        double effectiveVertical = vertical;
    /*?} else {*/
    /*public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        double horizontal = 0;
        double effectiveVertical = vertical;
        if (Screen.hasShiftDown()) {
            horizontal = vertical;
            effectiveVertical = 0;
        }
        *//*?}*/

        if (OverlayDispatch.mouseScrolled(this.children(), mouseX, mouseY, horizontal, effectiveVertical)) return true;
        if (onMouseScrolled(mouseX, mouseY, horizontal, effectiveVertical)) return true;

        /*? if >= 1.20.2 { */
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
         /*?} else {*/
        //return super.mouseScrolled(mouseX, mouseY, vertical);
        /*?}*/
    }

    // --- Key press

    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        return false;
    }

    @Override
    /*? if >= 1.19.4 { */
    /*? if >= 1.21.9 { */
    public boolean keyPressed(@NotNull KeyEvent keyEvent) {
        GenericKeyEvent e = new GenericKeyEvent(keyEvent);
    /*?} else {*/
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        GenericKeyEvent e = new GenericKeyEvent(keyCode, scanCode, modifiers);
    *//*?}*/

        LastInput.input = LastInput.keyboard(e.key());

        if (OverlayDispatch.keyPressed(this.children(), e)) return true;
        if (onKeyPressed(e)) return true;

        closeOnEsc = false;
        /*? if >= 1.21.9 { */
        boolean handled = super.keyPressed(keyEvent);
         /*?} else {*/
        //boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        /*?}*/
        closeOnEsc = true;
        if (handled) {
            return true;
        }

        //hack to get be able to handle escape presses in widgets but still be able to close screen with esc key
        if (e.isEscape()) {
            /*? if >= 1.21.9 { */
            return super.keyPressed(keyEvent);
            /*?} else {*/
            //return super.keyPressed(keyCode, scanCode, modifiers);
             /*?}*/
        }

        return false;
    }

    /*?} else {*/
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        GenericKeyEvent e = new GenericKeyEvent(keyCode, scanCode, modifiers);

        LastInput.input = LastInput.keyboard(e.key());

        if (OverlayDispatch.keyPressed(this.children(), e)) return true;
        if (onKeyPressed(e)) return true;

        //another hacky fix because in versions < 1.19.4 Minecraft does the tab navigation before widget handling.
        closeOnEsc = false;
        boolean handled = super.keyPressed(keyCode == InputConstants.KEY_TAB ? TAB_BYPASS_KEYCODE : keyCode, scanCode, modifiers);
        closeOnEsc = true;
        if (handled) {
            return true;
        }

        if (e.isEscape() || e.isTab()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        SpatialDirection direction = SpatialNavigation.direction(e);
        if (direction == null) return false;
        return SpatialNavigation.navigate(this, Rect.of(0, 0, this.width, this.height), direction);
    }
    *//*?}*/

    // --- Key release

    protected boolean onKeyReleased(GenericKeyEvent keyEvent) {
        return false;
    }

    @Override
    /*? if >= 1.21.9 { */
    public boolean keyReleased(@NotNull KeyEvent keyEvent) {
        GenericKeyEvent e = new GenericKeyEvent(keyEvent);
    /*?} else {*/
    /*public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        GenericKeyEvent e = new GenericKeyEvent(keyCode, scanCode, modifiers);
        *//*?}*/

        if (OverlayDispatch.keyReleased(this.children(), e)) return true;
        if (onKeyReleased(e)) return true;

        /*? if >= 1.21.9 { */
        return super.keyReleased(keyEvent);
         /*?} else {*/
        //return super.keyReleased(keyCode, scanCode, modifiers);
        /*?}*/
    }

    protected boolean onCharTyped(GenericCharacterEvent keyEvent) {
        return false;
    }

    @Override
    /*? if >= 1.21.9 { */
    public boolean charTyped(@NotNull CharacterEvent characterEvent) {
        GenericCharacterEvent e = new GenericCharacterEvent(characterEvent);
    /*?} else {*/
    /*public boolean charTyped(char character, int modifiers) {
        GenericCharacterEvent e = new GenericCharacterEvent(character, modifiers);
        *//*?}*/

        if (OverlayDispatch.charTyped(this.children(), e)) return true;
        if (onCharTyped(e)) return true;

        /*? if >= 1.21.9 { */
        return super.charTyped(characterEvent);
         /*?} else {*/
        //return super.charTyped(character, modifiers);
        /*?}*/
    }

    @Override
    public void onClose() {
        setScreen(parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return closeOnEsc;
    }

    public void setScreen(Screen screen) {
        /*? if >= 26.2 { */
        Minecraft.getInstance().setScreenAndShow(screen);
         /*? } else { */
        //Minecraft.getInstance().gui.setScreen(screen);
        /*? } */
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        GuiEventListener oldFocused = this.getFocused();
        if (oldFocused == guiEventListener) return;

        if (oldFocused != null) {
            setFocus(oldFocused, false);
        }
        if (guiEventListener != null) {
            setFocus(guiEventListener, true);
        }
        super.setFocused(guiEventListener);
    }

    private static void setFocus(GuiEventListener listener, boolean focused) {
        /*? if >= 1.19.4 { */
        listener.setFocused(focused);
        /*? } else { */
        /*if (listener instanceof GenericAbstractWidget widget) {
            widget.setFocused(focused);
        }
        *//*? } */
    }

    /*? if < 1.21.11 { */
    /*@Override
    public final void resize(@NotNull Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        resize(width, height);
    }

    public void resize(int width, int height) {
    }
    *//*? }  */
}

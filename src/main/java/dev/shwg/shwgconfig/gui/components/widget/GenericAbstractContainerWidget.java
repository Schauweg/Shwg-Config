package dev.shwg.shwgconfig.gui.components.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.gui.components.screens.GenericScreen;
import dev.shwg.shwgconfig.gui.deferred.overlay.OverlayDispatch;
import dev.shwg.shwgconfig.gui.input.events.GenericCharacterEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*? if >= 1.21.9 { */
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
/*? } */

/*? if < 1.20.2 {*/
//import net.minecraft.client.gui.screens.Screen;
/*? } */

/*? if >= 1.19.4 { */
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
/*? } */

/**
 * Base for widgets that host and delegate to child {@link GuiEventListener}s
 * (scrollable panels, tab bars, anything implementing {@link ContainerEventHandler}).
 * <br>
 * Handles focus bookkeeping, the {@code isDragging}/{@code setDragging} contract, and
 * the full Stonecutter-conditioned input dispatch (Overlay -> subclass hook -> children).
 * Subclasses reuse the {@code onMouseClicked}/{@code onMouseReleased}/{@code onMouseDragged}/
 * {@code onMouseScrolled} hooks inherited from {@link GenericAbstractWidget} to intercept
 * input at the container level instead of the leaf level - override only the ones you need,
 * default behavior is a no-op exactly like a leaf widget's.
 */
public abstract class GenericAbstractContainerWidget extends GenericAbstractWidget implements ContainerEventHandler {

    private GuiEventListener focused;
    private boolean draggingFocusedChild = false;

    protected GenericAbstractContainerWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public abstract @NotNull List<? extends GuiEventListener> children();

    // --- focus bookkeeping ---

    @Override
    public @Nullable GuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        if (this.focused != null && this.focused.equals(guiEventListener)) {
            return;
        }
        if (this.focused != null) {
            setFocus(this.focused, false);
        }
        if (guiEventListener != null) {
            setFocus(guiEventListener, true);
        }
        this.focused = guiEventListener;
        onChildFocusChanged(guiEventListener);
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

    /**
     * Fires whenever the currently-focused child changes, including to/from null.
     * No-op by default. Override to react to a child gaining focus - e.g. scrolling it
     * into view, or (for a tab bar) treating focus as an implicit selection.
     */
    protected void onChildFocusChanged(@Nullable GuiEventListener newFocused) {
    }

    @Override
    protected void onFocusLost() {
        setFocused(null);
    }

    @Override
    public boolean isDragging() {
        return draggingFocusedChild;
    }

    @Override
    public void setDragging(boolean dragging) {
        draggingFocusedChild = dragging;
    }

    @Override
    /*? if >= 1.19.4 { */
    public ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
        return ContainerEventHandler.super.nextFocusPath(event);
    }
    /*?} else {*/
    /*public boolean changeFocus(boolean forward) {
        return ContainerEventHandler.super.changeFocus(forward);
    }
    *//*?}*/

    // --- mouse click ---

    @Override
    /*? if >= 1.21.9 { */
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseButtonEvent, doubleClick);
    /*?} else {*/
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseX, mouseY, button, true);
    *//*?}*/

        if (OverlayDispatch.mouseClicked(this.children(), e)) return true;
        if (!isHovered) {
            return false;
        }
        if (onMouseClicked(e)) return true;

        /*? if >= 1.21.9 { */
        boolean handled = ContainerEventHandler.super.mouseClicked(mouseButtonEvent, doubleClick);
         /*?} else {*/
        //boolean handled = ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
        /*?}*/

        // ContainerEventHandler only starts "dragging" for the primary button.
        // Do it for the others too, so drag and release events reach the child that took the press.
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

    @Override
    /*? if >= 1.21.9 { */
    public boolean mouseReleased(@NotNull MouseButtonEvent mouseButtonEvent) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseButtonEvent, false);
    /*?} else {*/
    /*public boolean mouseReleased(double mouseX, double mouseY, int button) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseX, mouseY, button, false);
    *//*?}*/

        if (onMouseReleased(e)) return true;
        if (OverlayDispatch.mouseReleased(this.children(), e)) return true;

        // manually set dragging to false again for non left click dragging
        if (isDragging() && !e.isLeftClick() && getFocused() != null) {
            GuiEventListener focusedChild = getFocused();
            setDragging(false);
            /*? if >= 1.21.9 { */
            return focusedChild.mouseReleased(mouseButtonEvent);
            /*?} else {*/
            //return focusedChild.mouseReleased(mouseX, mouseY, button);
            /*?}*/
        }

        /*? if >= 1.21.9 { */
        return ContainerEventHandler.super.mouseReleased(mouseButtonEvent);
         /*?} else {*/
        //return ContainerEventHandler.super.mouseReleased(mouseX, mouseY, button);
        /*?}*/
    }

    // --- mouse drag ---

    @Override
    /*? if >= 1.21.9 { */
    public boolean mouseDragged(@NotNull MouseButtonEvent event, double deltaX, double deltaY) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(event, false);
    /*?} else {*/
    /*public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        GenericMouseButtonEvent e = new GenericMouseButtonEvent(mouseX, mouseY, button, false);
        *//*?}*/

        if (onMouseDragged(e, deltaX, deltaY)) return true;
        if (OverlayDispatch.mouseDragged(this.children(), e, deltaX, deltaY)) return true;

        //manually calling mouseDragged on focused widget when mouse button is not left button
        if (isDragging() && !e.isLeftClick() && getFocused() != null) {
            /*? if >= 1.21.9 { */
            return getFocused().mouseDragged(event, deltaX, deltaY);
            /*?} else {*/
            //return getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            /*?}*/
        }

        /*? if >= 1.21.9 { */
        return ContainerEventHandler.super.mouseDragged(event, deltaX, deltaY);
         /*?} else {*/
        //return ContainerEventHandler.super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        /*?}*/
    }

    // --- mouse scroll ---
    // effectiveVertical/horizontal (shift-swapped) exist ONLY for OverlayDispatch, which is
    // custom code free of vanilla's version constraints. Children delegation and the self-scroll
    // hook both get the raw, un-swapped vertical - pre-1.20.2 there is no vanilla concept of a
    // horizontal delta to hand them at all.

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

        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        /*? if >= 1.20.2 { */
        if (ContainerEventHandler.super.mouseScrolled(mouseX, mouseY, horizontal, vertical)) {
            return true; // a child (e.g. a nested list) handled it itself
        }
        return onMouseScrolled(mouseX, mouseY, horizontal, vertical);
        /*?} else {*/
        /*if (ContainerEventHandler.super.mouseScrolled(mouseX, mouseY, vertical)) {
            return true; // a child (e.g. a nested list) handled it itself
        }
        return onMouseScrolled(mouseX, mouseY, 0, vertical);
        *//*?}*/
    }

    // --- key / char passthrough ---
    // No container-level hook yet (per your call to defer this) - plain OverlayDispatch + delegation.

    @Override
    /*? if >= 1.21.9 { */
    public boolean keyPressed(@NotNull KeyEvent keyEvent) {
        GenericKeyEvent e = new GenericKeyEvent(keyEvent);
    /*?} else {*/
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        /^? if < 1.19.4 { ^/
        /^//reverse the hack again
        if (keyCode == GenericScreen.TAB_BYPASS_KEYCODE) {
            keyCode = InputConstants.KEY_TAB;
        }
        ^//^? } ^/

        GenericKeyEvent e = new GenericKeyEvent(keyCode, scanCode, modifiers);
    *//*?}*/

        if (OverlayDispatch.keyPressed(this.children(), e)) return true;

        /*? if >= 1.21.9 { */
        return ContainerEventHandler.super.keyPressed(keyEvent);
         /*?} else {*/
        //return ContainerEventHandler.super.keyPressed(keyCode, scanCode, modifiers);
        /*?}*/
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

        /*? if >= 1.21.9 { */
        return ContainerEventHandler.super.keyReleased(keyEvent);
         /*?} else {*/
        //return ContainerEventHandler.super.keyReleased(keyCode, scanCode, modifiers);
        /*?}*/
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

        /*? if >= 1.21.9 { */
        return ContainerEventHandler.super.charTyped(characterEvent);
         /*?} else {*/
        //return ContainerEventHandler.super.charTyped(character, modifiers);
        /*?}*/
    }
}

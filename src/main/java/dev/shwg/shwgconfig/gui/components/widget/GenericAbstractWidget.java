package dev.shwg.shwgconfig.gui.components.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.render.GraphicsAccessor;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.gui.deferred.GenericTooltip;
import dev.shwg.shwgconfig.gui.input.events.GenericCharacterEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

/*? if >= 1.21.9 { */
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonInfo;
/*?} */

/*? if < 1.20.2 { */
//import net.minecraft.client.gui.screens.Screen;
/*?} */

/*? if >= 1.20 { */
import net.minecraft.client.gui.GuiGraphicsExtractor;
/*?} else {*/
//import com.mojang.blaze3d.vertex.PoseStack;
/*?} */

/*? if < 1.19.4 { */
/*import dev.shwg.shwgconfig.gui.components.screens.GenericScreen;
import com.mojang.blaze3d.platform.InputConstants;
*//*?} */

/*? if < 1.19.3 { */
//import net.minecraft.client.gui.narration.NarratedElementType;
/*?} */

public abstract class GenericAbstractWidget extends AbstractWidget implements RenderableElement {

    @Nullable
    private GenericTooltip tooltip;
    private Consumer<Integer> onXChanged;
    private Consumer<Integer> onYChanged;
    private Consumer<Integer> onWidthChanged;
    private Consumer<Integer> onHeightChanged;

    public GenericAbstractWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        setFocused(false);
    }

    @Override
    /*? if >= 26 { */
    public final void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    /*? } else if >= 1.20 { */
    //public final void renderWidget(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    /*? } else if >= 1.19.4 { */
    //public final void renderWidget(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTick) {
    /*? } else { */
    //public final void renderButton(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTick) {
    /*? }*/
        GenericGraphics g = ((GraphicsAccessor) graphics).shwgConfigs$getGraphics();
        renderWidget(g, mouseX, mouseY, partialTick);
    }

    public final void render(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        /*? if >= 26 { */
        super.extractRenderState(graphics.getGraphics(), mouseX, mouseY, partialTick);
        /*? } else { */
        //super.render(graphics.getGraphics(), mouseX, mouseY, partialTick);
        /*? }*/
    }

    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {

    }

    public void renderTooltip(GenericGraphics graphics, int mouseX, int mouseY) {
        if (tooltip != null && !tooltip.isEmpty()) {
            graphics.renderTooltip(tooltip, mouseX, mouseY);
        }
    }

    public void renderDeferredTooltip(GenericGraphics graphics, int mouseX, int mouseY) {
        if (tooltip != null && !tooltip.isEmpty()) {
            graphics.addDeferred(tooltip);
        }
    }

    public void setTooltip(@Nullable GenericTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Nullable
    public GenericTooltip getTTip() {
        return tooltip;
    }

    // --- mouse inputs ---

    protected boolean onMouseClicked(GenericMouseButtonEvent mouseButtonEvent) {
        return false;
    }

    protected boolean onMouseReleased(GenericMouseButtonEvent mouseButtonEvent) {
        return false;
    }

    protected boolean onMouseDragged(GenericMouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        return false;
    }

    protected boolean onMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        return false;
    }

    protected void onClick(GenericMouseButtonEvent event) { }
    protected void onRelease(GenericMouseButtonEvent event) { }
    protected void onDrag(GenericMouseButtonEvent event, double deltaX, double deltaY) { }

    @Override
    /*? if >= 1.21.9 { */
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y()) && onMouseClicked(new GenericMouseButtonEvent(mouseButtonEvent, doubleClick))) return true;
        return super.mouseClicked(mouseButtonEvent, doubleClick);
    }
    /*?} else {*/
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && onMouseClicked(new GenericMouseButtonEvent(mouseX, mouseY, button, true))) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    *//*?}*/

    @Override
    /*? if >= 1.21.9 { */
    public boolean mouseReleased(@NotNull MouseButtonEvent event) {
        if (onMouseReleased(new GenericMouseButtonEvent(event, false))) return true;
        return super.mouseReleased(event);
    }
    /*?} else { */
    /*public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (onMouseReleased(new GenericMouseButtonEvent(mouseX, mouseY, button, false))) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    *//*?}*/

    @Override
    /*? if >= 1.21.9 { */
    public boolean mouseDragged(@NotNull MouseButtonEvent event, double deltaX, double deltaY) {
        if (onMouseDragged(new GenericMouseButtonEvent(event, false), deltaX, deltaY)) return true;
        return super.mouseDragged(event, deltaX, deltaY);
    }
    /*?} else {*/
    /*public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (onMouseDragged(new GenericMouseButtonEvent(mouseX, mouseY, button, false), deltaX, deltaY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    *//*?}*/


    @Override
    /*? if >= 1.20.2 { */
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (onMouseScrolled(mouseX, mouseY, horizontal, vertical)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }
    /*?} else {*/
    /*public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        if (Screen.hasShiftDown()) {
            if (onMouseScrolled(mouseX, mouseY, vertical, 0)) return true;
        } else {
            if (onMouseScrolled(mouseX, mouseY, 0, vertical)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, vertical);
    }
    *//*?}*/

    @Override
    /*? if >= 1.21.9 { */
    public void onClick(@NotNull MouseButtonEvent event, boolean doubleClick) {
        onClick(new GenericMouseButtonEvent(event, doubleClick));
    }
    /*? } else { */
    /*public void onClick(double mouseX, double mouseY) {
        onClick(new GenericMouseButtonEvent(mouseX, mouseY));
    }
    *//*? } */

    @Override
    /*? if >= 1.21.9 { */
    public void onRelease(@NotNull MouseButtonEvent event) {
        onRelease(new GenericMouseButtonEvent(event, false));
    }
    /*? } else { */
    /*public void onRelease(double mouseX, double mouseY) {
        onRelease(new GenericMouseButtonEvent(mouseX, mouseY));
    }
    *//*? } */

    @Override
    /*? if >= 1.21.9 { */
    public void onDrag(@NotNull MouseButtonEvent event, double deltaX, double deltaY) {
        onDrag(new GenericMouseButtonEvent(event, false), deltaX, deltaY);
    }
    /*? } else { */
    /*public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        onDrag(new GenericMouseButtonEvent(mouseX, mouseY),  deltaX, deltaY);
    }
    *//*? } */

    // --- key inputs ---
    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        return false;
    }

    protected boolean onKeyReleased(GenericKeyEvent keyEvent) {
        return false;
    }

    protected boolean onCharTyped(GenericCharacterEvent characterEvent) {
        return false;
    }

    @Override
    /*? if >= 1.21.9 { */
    public boolean keyPressed(@NotNull KeyEvent keyEvent) {
        if (onKeyPressed(new GenericKeyEvent(keyEvent))) return true;
        return super.keyPressed(keyEvent);
    }
    /*?} else {*/
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        /^? if < 1.19.4 { ^/
        /^//reverse the hack again
        if (keyCode == GenericScreen.TAB_BYPASS_KEYCODE) {
            keyCode = InputConstants.KEY_TAB;
        }
        ^//^? } ^/

        if (onKeyPressed(new GenericKeyEvent(keyCode, scanCode, modifiers))) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *//*?}*/

    @Override
    /*? if >= 1.21.9 { */
    public boolean keyReleased(@NotNull KeyEvent event) {
        if (onKeyReleased(new GenericKeyEvent(event))) return true;
        return super.keyReleased(event);
    }
    /*?} else {*/
    /*public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (onKeyReleased(new GenericKeyEvent(keyCode, scanCode, modifiers))) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    *//*?}*/

    @Override
    /*? if >= 1.21.9 { */
    public boolean charTyped(@NotNull CharacterEvent event) {
        if (onCharTyped(new GenericCharacterEvent(event))) return true;
        return super.charTyped(event);
    }
    /*?} else {*/
    /*public boolean charTyped(char character, int modifiers) {
        if (onCharTyped(new GenericCharacterEvent(character, modifiers))) return true;
        return super.charTyped(character, modifiers);
    }
    *//*?}*/

    /*? < 1.21.9 */
    //@Override
    protected boolean isValidClickButton(int button) {
        return button == InputConstants.MOUSE_BUTTON_LEFT;
    }

    /*? if >= 1.21.9 { */
    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return isValidClickButton(buttonInfo.button());
    }
    /*? } */


    @Override
    public void setFocused(final boolean focused) {
        boolean wasFocused = this.isFocused();
        super.setFocused(focused);
        if (wasFocused != focused) {
            onFocusChanged(focused);
        }
    }

    protected void onFocusChanged(boolean focused) {
        if (focused) {
            onFocusGained();
        } else {
            onFocusLost();
        }
    }

    protected void onFocusGained() { }
    protected void onFocusLost() { }

    protected void updateGenericWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        defaultGenericButtonNarration(narrationElementOutput);
    }

    private void defaultGenericButtonNarration(@NotNull NarrationElementOutput output) {
        /*? if >= 1.19.3 { */
        this.defaultButtonNarrationText(output);
        /*? } else { */
        //output.add(NarratedElementType.TITLE, this.getMessage());
        /*? }*/
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    /*? if >= 1.19.3 { */
    protected final void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        updateGenericWidgetNarration(narrationElementOutput);
    }
    /*? } else { */
    /*public final void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        updateGenericWidgetNarration(narrationElementOutput);
    }
    *//*? }*/


    @Override
    public int getX() {
        /*? if >= 1.19.3 { */
        return super.getX();
        /*? } else { */
        //return x;
        /*? }*/
    }

    @Override
    public void setX(int x) {
        /*? if >= 1.19.3 { */
        super.setX(x);
        /*? } else { */
        //this.x = x;
        /*? }*/
        if (onXChanged != null) {
            onXChanged.accept(x);
        }
    }

    @Override
    public int getY() {
        /*? if >= 1.19.3 { */
        return super.getY();
        /*? } else { */
        //return y;
        /*? }*/
    }

    @Override
    public void setY(int y) {
        /*? if >= 1.19.3 { */
        super.setY(y);
        /*? } else { */
        //this.y = y;
        /*? }*/
        if (onYChanged != null){
            onYChanged.accept(y);
        }
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        if (onHeightChanged != null) {
            onHeightChanged.accept(width);
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setHeight(int height){
        this.height = height;
        if (onHeightChanged != null) {
            onHeightChanged.accept(height);
        }
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setOnXChanged(Consumer<Integer> onXChanged) {
        this.onXChanged = onXChanged;
    }

    public void setOnYChanged(Consumer<Integer> onYChanged) {
        this.onYChanged = onYChanged;
    }

    public void setOnWidthChanged(Consumer<Integer> onWidthChanged) {
        this.onWidthChanged = onWidthChanged;
    }

    public void setOnHeightChanged(Consumer<Integer> onHeightChanged) {
        this.onHeightChanged = onHeightChanged;
    }

    /*? if < 1.19.4 { */
    /*public boolean isHovered() {
        return this.isHovered;
    }

    @Override
    public boolean changeFocus(boolean forward) {
        if (this.active && this.visible) {
            this.setFocused(!this.isFocused());
            return this.isFocused();
        }
        return false;
    }
    *//*? } */

    public abstract static class Builder<T extends GenericAbstractWidget, B extends Builder<T, B>> {
        protected final Component message;

        protected int x = 0;
        protected int y = 0;
        protected int width = 150;
        protected int height = 20;

        protected GenericTooltip tooltip;

        public Builder(Component message) {
            this.message = message;
        }

        @SuppressWarnings("unchecked")
        protected final B self() {
            return (B) this;
        }

        public B pos(int x, int y) {
            this.x = x;
            this.y = y;
            return self();
        }

        public B width(int width) {
            this.width = width;
            return self();
        }

        public B height(int height) {
            this.height = height;
            return self();
        }

        public B size(int width, int height) {
            this.width = width;
            this.height = height;
            return self();
        }

        public B bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public B tooltip(Component line) {
            this.tooltip = GenericTooltip.of(line);
            return self();
        }

        public B tooltip(GenericTooltip tooltip) {
            this.tooltip = tooltip;
            return self();
        }

        public abstract T build();
    }
    

}

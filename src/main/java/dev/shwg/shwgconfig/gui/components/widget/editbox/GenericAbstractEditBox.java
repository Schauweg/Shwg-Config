package dev.shwg.shwgconfig.gui.components.widget.editbox;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.gui.input.events.GenericCharacterEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/*? if >= 26 { */
import dev.shwg.shwgconfig.mixin.EditBoxAccessor;
import net.minecraft.util.StringUtil;
/*? } */

/*? if >= 1.21.9 { */
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
/*? } */

/*? if >= 1.19.4 { */
import net.minecraft.client.gui.components.AbstractWidget;
/*? } */

/*? if < 1.19.3 { */
//import com.mojang.blaze3d.vertex.PoseStack;
/*? } */
public abstract class GenericAbstractEditBox extends GenericAbstractWidget {

    protected static final int TEXT_PADDING = 4;
    protected static final int TEXT_HEIGHT = 8;

    protected final CustomEditBox editBox;
    protected final Font font;

    public GenericAbstractEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);

        /*? if >= 1.20.2 { */
        this.editBox = new CustomEditBox(font, x, y, width, height, message);
         /*? } else { */
        //this.editBox = new CustomEditBox(font, x + 1, y + 1, width - 2, height - 2, message);
        /*? } */
        this.setFocused(false);
        this.font = font;
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        /*? if >= 26 { */
        editBox.extractRenderState(graphics.getGraphics(),  mouseX, mouseY, partialTick);
        /*? } else if >= 1.19.4 { */
        //editBox.render(graphics.getGraphics(), mouseX, mouseY, partialTick);
        /*? } else { */
        //editBox.render(graphics.getGraphics(), mouseX, mouseY, partialTick);
        /*? }*/
    }

    protected final CustomEditBox getEditBox() {
        return editBox;
    }

    public void setValue(String value) {
        editBox.setValue(value);
    }

    public String getValue() {
        return editBox.getValue();
    }

    public void setSuggestion(@Nullable String suggestion) {
        editBox.setSuggestion(suggestion);
    }

    public void setHint(Component hint) {
        editBox.setHint(hint);
    }

    public void setMaxLength(int maxLength) {
        editBox.setMaxLength(maxLength);
    }

    public void setEditable(boolean editable) {
        editBox.setEditable(editable);
    }

    public void setResponder(Consumer<String> responder) {
        editBox.setResponder(responder);
    }

    public void setFilter(Predicate<String> filter) {
        editBox.setFilter(filter);
    }

    public boolean isVisible() {
        return editBox.isVisible();
    }

    public void setVisible(boolean visible) {
        editBox.setVisible(visible);
    }

    public int getCursorPosition() {
        return editBox.getCursorPosition();
    }

    public void setCursorPosition(int cursorPosition) {
        editBox.setCursorPosition(cursorPosition);
    }

    public void moveCursor(int offset, boolean shiftDown) {
        editBox.moveCursor(offset, shiftDown);
    }

    public void moveCursorTo(int offset, boolean shiftDown) {
        editBox.moveCursorTo(offset, shiftDown);
    }

    public void setHighlightPos(int index) {
        editBox.setHighlightPos(index);
    }

    public String getHighlighted(){
        return editBox.getHighlighted();
    }

    public void setBordered(boolean bordered) {
        editBox.setBordered(bordered);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        /*? if >= 1.20.2 { */
        editBox.setX(x);
         /*? } else { */
        //editBox.setX(x + 1);
        /*? } */
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        /*? if >= 1.20.2 { */
        editBox.setY(y);
         /*? } else { */
        //editBox.setY(y + 1);
        /*? } */
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        /*? if >= 1.20.2 { */
        editBox.setWidth(width);
        /*? } else { */
        //editBox.setWidth(width - 2);
        /*? } */
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        /*? if >= 1.20.2 { */
        editBox.setHeight(height);
        /*? } else { */
        //editBox.setHeight(height - 2);
        /*? } */
    }

    @Override
    public boolean isHovered() {
        return editBox.isHovered();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (editBox != null) {
            editBox.setFocused(focused);
        }
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        /*? if >= 1.21.9 { */
        return editBox.keyPressed(keyEvent.getKeyEvent());
         /*? } else { */
        //return editBox.keyPressed(keyEvent.key(), keyEvent.getScanCode(), keyEvent.getModifiers());
        /*? } */
    }

    @Override
    protected boolean onCharTyped(GenericCharacterEvent characterEvent) {
        /*? if >= 26 { */
        return editBox.charTyped(new CharacterEvent(characterEvent.getCodepoint()));
        /*? } else if >= 1.21.9 { */
        //return editBox.charTyped(new CharacterEvent(characterEvent.getCodepoint(), characterEvent.getModifier()));
        /*? } else { */
        //return editBox.charTyped(characterEvent.getCodepointAsChar(), characterEvent.getModifier());
        /*? } */
    }

    @Override
    protected boolean onMouseClicked(GenericMouseButtonEvent mouseButtonEvent) {
        /*? if >= 1.21.9 { */
        return editBox.mouseClicked(new MouseButtonEvent(mouseButtonEvent.getMouseX(), mouseButtonEvent.getMouseY(), new MouseButtonInfo(mouseButtonEvent.getButton(), mouseButtonEvent.getModifiers())), mouseButtonEvent.isDoubleClick());
         /*? } else { */
        //return editBox.mouseClicked(mouseButtonEvent.getMouseX(), mouseButtonEvent.getMouseY(), mouseButtonEvent.getButton());
        /*? } */
    }

    @Override
    protected boolean onMouseDragged(GenericMouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        /*? if >= 1.21.9 { */
        return editBox.mouseDragged(new MouseButtonEvent(mouseButtonEvent.getMouseX(), mouseButtonEvent.getMouseY(), new  MouseButtonInfo(mouseButtonEvent.getButton(), mouseButtonEvent.getModifiers())), deltaX, deltaY);
        /*? } else { */
        //return editBox.mouseDragged(mouseButtonEvent.getMouseX(), mouseButtonEvent.getMouseY(), mouseButtonEvent.getButton(), deltaX, deltaY);
         /*? } */
    }

    /*? if >= 1.19.4 { */
    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(editBox);
    }
    /*? } */

    @SuppressWarnings("InnerClassMayBeStatic")
    protected class CustomEditBox extends EditBox {

        public CustomEditBox(Font font, int x, int y, int width, int height, Component message) {
            super(font, x, y, width, height, message);

            /*? if < 1.19.3 { */
            /*this.font = font;
            this.border = true;
            this.editable = true;
            *//*? } */
        }

        /*? if >= 26 { */
        private Predicate<String> filter = Objects::nonNull;

        public void setFilter(Predicate<String> filter) {
            this.filter = filter == null ? Objects::nonNull : filter;
        }

        @Override
        public void setValue(@NotNull String value) {
            if (this.filter.test(value)) {
                super.setValue(value);
            }
        }

        @Override
        public void insertText(@NotNull String input) {
            int cursorPos = this.getCursorPosition();
            int highlightPos = ((EditBoxAccessor) this).shwgconfig$getHighlightPos();

            int start = Math.min(cursorPos, highlightPos);
            int end = Math.max(cursorPos, highlightPos);

            int maxInsertionLength = ((EditBoxAccessor) this).shwgconfig$getMaxLength() - this.getValue().length() - (start - end);

            if (maxInsertionLength > 0) {
                String text = StringUtil.filterText(input);

                if (maxInsertionLength < text.length()) {
                    if (Character.isHighSurrogate(text.charAt(maxInsertionLength - 1))) {
                        maxInsertionLength--;
                    }

                    text = text.substring(0, maxInsertionLength);
                }

                String candidate = new StringBuilder(this.getValue())
                        .replace(start, end, text)
                        .toString();

                if (this.filter.test(candidate)) {
                    super.insertText(input);
                }
            }
        }
        /*? } */

        /*? if < 1.20.2 { */
        /*public void moveCursor(int offset, boolean shiftDown) {
            super.moveCursor(offset);
        }

        public void moveCursorTo(int position,  boolean shiftDown) {
            super.moveCursorTo(position);
        }
        *//*? } */


        /*? if < 1.20.6 { */
        /*public void setHeight(int height) {
            this.height = height;
        }
        *//*? } */

        /*? if < 1.19.3 { */
        /*private final Font font;
        private Component hint;
        private boolean border;
        private boolean editable;

        public void setY(int y) {
            this.y = y;
        }

        public void setHint(Component hint) {
            this.hint = hint;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        @Override
        public void setBordered(boolean border) {
            super.setBordered(border);
            this.border = border;
        }

        @Override
        public void setEditable(boolean editable) {
            super.setEditable(editable);
            this.editable = editable;
        }

        @Override
        public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            super.render(poseStack, mouseX, mouseY, partialTick);
            if (this.hint != null && getValue().isEmpty() && !this.isFocused()) {
                int x = this.border ? getX() + TEXT_PADDING : getX();
                int y = this.border ? getY() + (this.height - TEXT_HEIGHT) / 2 : getY();
                int color = editable ? 14737632 : 7368816;

                this.font.drawShadow(poseStack, this.hint, (float) x, (float) y, color);
            }
        }
        *//*? } */

        /*? if < 1.19.4 { */
        /*public boolean isHovered() {
            return this.isHovered;
        }

        @Override //Override to make it public
        public void setFocused(boolean focused) {
            super.setFocused(focused);
        }
        *//*? } */
    }

    public abstract static class Builder<T extends GenericAbstractEditBox, B extends Builder<T, B>> extends GenericAbstractWidget.Builder<T, B> {

        protected final Font font;

        protected Component hint = null;
        protected boolean bordered = true;
        protected boolean editable = true;
        protected int maxLength = 32;
        protected Consumer<String> responder;
        protected Predicate<String> filter = Objects::nonNull;

        protected Builder(Font font, Component message) {
            super(message);
            this.font = font;
        }

        public B maxLength(int maxLength) {
            this.maxLength = maxLength;
            return self();
        }

        public B hint(Component hint) {
            this.hint = hint;
            return self();
        }

        public B hint(String hint) {
            this.hint = Component.literal(hint);
            return self();
        }

        public B bordered(boolean border) {
            this.bordered = border;
            return self();
        }

        public B editable(boolean editable) {
            this.editable = editable;
            return self();
        }

        public B filter(Predicate<String> filter) {
            this.filter = filter;
            return self();
        }

        public B responder(Consumer<String> responder) {
            this.responder = responder;
            return self();
        }
    }


}

package dev.shwg.shwgconfig.gui.components.widget.editbox;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class GenericEditBox extends GenericAbstractEditBox {

    public GenericEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    public static Builder builder(Font font, Component message) {
        return new Builder(font, message);
    }

    public static final class Builder extends GenericAbstractEditBox.Builder<GenericEditBox, Builder> {
        private Builder(Font font, Component message) {
            super(font, message);
        }

        public GenericEditBox build() {
            GenericEditBox editBox = new GenericEditBox(font, x, y, width, height, message);
            editBox.setMaxLength(maxLength);
            if (hint != null) {
                editBox.setHint(hint);
            }
            editBox.setBordered(bordered);
            editBox.setEditable(editable);
            editBox.setFilter(filter);
            editBox.setResponder(responder);
            return editBox;
        }
    }
}

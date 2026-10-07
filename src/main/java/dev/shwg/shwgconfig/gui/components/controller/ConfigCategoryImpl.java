package dev.shwg.shwgconfig.gui.components.controller;

import dev.shwg.shwgconfig.api.entries.CategoryEntry;
import dev.shwg.shwgconfig.api.entries.ControllerEntry;
import dev.shwg.shwgconfig.api.entries.CustomWidgetEntry;
import dev.shwg.shwgconfig.api.model.ConfigCategory;
import dev.shwg.shwgconfig.gui.layout.*;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.gui.Font;

import java.util.List;
import java.util.function.Consumer;

public class ConfigCategoryImpl extends GenericAbstractLayout implements RenderableElement {

    public static final int TOP_BOTTOM_SPACING = 1;
    public static final int CONTROLLER_PREVIEW_SPACING = 9;

    private final Font font;

    private final GenericVerticalLayout controllerContainer;
    private final RenderableElement previewWidget;

    private final int controllerWidth;
    private final boolean centerPreviewWithControls;

    private final ConfigCategory configCategory;
    private boolean visible = true;

    /**
     *
     * @param sharedControllerWidth  width of the vertical layout housing the controllers, calculated from all controllers for equal spacing
     * @param font                   Font used for all kinds of rendering
     * @param configCategory         ConfigCategory used to build the actual category
     */
    public ConfigCategoryImpl(int sharedControllerWidth, Font font, ConfigCategory configCategory) {
        super(0, 0, 0, 0);
        this.font = font;
        this.previewWidget = configCategory.getPreviewWidget();
        this.configCategory = configCategory;
        this.centerPreviewWithControls = configCategory.centerPreviewWithControls();

        int controllerWidth = configCategory.useOwnControllerWidth()
                ? calculateEntryWidth(font, configCategory.getEntries())
                : sharedControllerWidth;

        this.controllerWidth = controllerWidth;
        this.controllerContainer = new GenericVerticalLayout(0, 0, controllerWidth, 0, 1, 0, Alignment.Horizontal.CENTER);
        populateEntries(configCategory.getEntries(), controllerWidth, font);
        arrange();
        setWidth(calculateWidth());
        setHeight(calculateHeight());
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            if (configCategory.getTitle() != null) {
                graphics.drawCenteredString(font, configCategory.getTitle(), this.getX() + this.getWidth() / 2, this.getY() + TOP_BOTTOM_SPACING, -1);
            }

            if (previewWidget != null) {
                previewWidget.render(graphics, mouseX, mouseY, partialTicks);
                graphics.vLine(this.getX() + controllerContainer.getWidth() + CONTROLLER_PREVIEW_SPACING / 2, getY() + getTitleHeight(), getBottom() - TOP_BOTTOM_SPACING, 0xFFAAAAAA);
            }
            controllerContainer.renderChildren(graphics, mouseX, mouseY, partialTicks);
        }
    }

    private void populateEntries(List<CategoryEntry> entries, int rowWidth, Font font) {
        for (CategoryEntry entry : entries) {
            if (entry instanceof ControllerEntry<?> controllerEntry) {
                SimpleController controller = new SimpleController(controllerEntry, rowWidth, SimpleController.DEFAULT_CONTROLLER_HEIGHT, font);
                controllerContainer.add(controller);
            } else if (entry instanceof CustomWidgetEntry customEntry) {
                controllerContainer.add(customEntry.getWidget());
            }
        }
    }

    protected void updateY() {
        if (this.previewWidget == null) {
            this.controllerContainer.setY(this.getY() + getTitleHeight());
        } else {
            int controllerHeight = this.controllerContainer.getHeight();
            int previewHeight = this.previewWidget.getHeight();

            //vertically center smaller container to the larger one
            if (controllerHeight > previewHeight) {
                this.controllerContainer.setY(this.getY() + getTitleHeight());
                int heightDifference = controllerHeight - previewHeight;
                this.previewWidget.setY(this.getY() + getTitleHeight() + heightDifference / 2);
            } else if (previewHeight > controllerHeight) {
                this.previewWidget.setY(this.getY() + getTitleHeight());
                int heightDifference = previewHeight - controllerHeight;
                this.controllerContainer.setY(this.getY() + getTitleHeight() + heightDifference / 2);
            } else {
                this.controllerContainer.setY(this.getY() + getTitleHeight());
                this.previewWidget.setY(this.getY() + getTitleHeight());
            }
        }
    }

    private int getTitleHeight() {
        return this.configCategory.getTitle() != null ? this.font.lineHeight + (2 * TOP_BOTTOM_SPACING) : TOP_BOTTOM_SPACING;
    }

    private int calculateWidth() {
        if (previewWidget != null && centerPreviewWithControls) {
            return controllerWidth + CONTROLLER_PREVIEW_SPACING + previewWidget.getWidth();
        }
        return controllerWidth;
    }

    private int calculateHeight() {
        int height = 0;
        height += getTitleHeight();
        controllerContainer.arrange();
        if (this.previewWidget == null) {
            height += this.controllerContainer.getHeight();
        } else {
            height += Math.max(controllerContainer.getHeight(), previewWidget.getHeight());
        }
        height += TOP_BOTTOM_SPACING;
        return height;
    }

    public static int calculateEntryWidth(Font font, List<CategoryEntry> entries) {
        int max = 0;
        for (CategoryEntry entry : entries) {
            if (entry instanceof ControllerEntry<?> controllerEntry) {
                max = Math.max(max, font.width(controllerEntry.getLabel())
                        + SimpleController.LABEL_CONTROLLER_SPACING
                        + SimpleController.DEFAULT_CONTROLLER_WIDTH);
            } else if (entry instanceof CustomWidgetEntry customEntry) {
                max = Math.max(max, customEntry.getWidget().getWidth());
            }
        }
        return max;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        consumer.accept(controllerContainer);
        if (previewWidget != null) {
            consumer.accept(previewWidget);
        }
    }

    @Override
    public void arrange() {
        super.arrange();
        updateY();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        controllerContainer.setX(x);
        if (previewWidget != null) {
            previewWidget.setX(x + controllerWidth + CONTROLLER_PREVIEW_SPACING);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
    }

    @Override
    public int getContentHeight() {
        return getHeight();
    }

    @Override
    public int getContentWidth() {
        return getWidth();
    }


    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        controllerContainer.visitChildren(element -> {
            if (element instanceof RenderableElement renderableElement) {
                renderableElement.setVisible(visible);
            }
        });
        if (previewWidget != null) {
            previewWidget.setVisible(visible);
        }
    }
}

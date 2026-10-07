package dev.shwg.shwgconfig.gui.layout;

import dev.shwg.shwgconfig.gui.components.screens.GenericScreen;
import dev.shwg.shwgconfig.render.GenericGraphics;

import java.util.function.Consumer;

public class GenericHeaderFooterLayout implements GenericLayout, RenderableElement {

    private final GenericScreen screen;
    private final GenericVerticalLayout header;
    private final GenericHorizontalLayout footer;
    private final GenericFrameLayout footerWrapper;

    public GenericHeaderFooterLayout(GenericScreen screen) {
        this(screen, 2, 2, 2, 2);
    }

    public GenericHeaderFooterLayout(GenericScreen screen, int headerSpacing, int headerPadding, int footerSpacing, int footerPadding) {
        this.screen = screen;
        this.header = new GenericVerticalLayout(0, 0, screen.width, 0, headerSpacing, headerPadding, Alignment.Horizontal.CENTER);
        this.footer = new GenericHorizontalLayout(0, 0, 0, 0, footerSpacing, footerPadding, Alignment.Vertical.CENTER);
        this.footerWrapper = new GenericFrameLayout(0, 0, screen.width, 0, footer, Alignment.Horizontal.CENTER, Alignment.Vertical.CENTER);
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(0, 0, screen.width, header.getHeight(), 0x4d000000);
        graphics.fill(0, footerWrapper.getY(), screen.width, screen.height, 0x4d000000);

        header.renderChildren(graphics, mouseX, mouseY, partialTicks);

        footer.renderChildren(graphics, mouseX, mouseY, partialTicks);

        graphics.hLine(0, screen.width, header.getBottom(), 0xAAFFFFFF);
        graphics.hLine(0, screen.width, footerWrapper.getY(), 0xAAFFFFFF);
    }

    public GenericVerticalLayout header() {
        return header;
    }

    public GenericHorizontalLayout footer() {
        return footer;
    }

    public int getHeaderHeight() {
        return header.getHeight();
    }

    public int getFooterHeight() {
        return footerWrapper.getHeight();
    }

    public void arrange() {
        header.setWidth(screen.width);
        header.arrange();
        header.setX(0);
        header.setY(0);

        int footerHeight = naturalFooterHeight();
        footer.setHeight(footerHeight);
        footerWrapper.setWidth(screen.width);
        footerWrapper.setHeight(footerHeight);
        footerWrapper.setX(0);
        footerWrapper.setY(screen.height - footerHeight);
        footerWrapper.arrange();
    }

    private int naturalFooterHeight() {
        int max = 0;
        for (LayoutElement child : footer.getChildren()) {
            max = Math.max(max, child.getHeight());
        }
        return max + footer.padding * 2;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        header.visitChildren(consumer);
        footer.visitChildren(consumer);
    }

    @Override
    public int getContentHeight() {
        return this.screen.height - this.getHeaderHeight() - this.getFooterHeight();
    }

    @Override
    public int getContentWidth() {
        return screen.width;
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getWidth() {
        return screen.width;
    }

    @Override
    public int getHeight() {
        return screen.height;
    }

    @Override
    public void setX(int x) {

    }

    @Override
    public void setY(int y) {

    }

    @Override
    public void setWidth(int width) {

    }

    @Override
    public void setHeight(int height) {

    }

    @Override
    public void setVisible(boolean visible) {

    }
}

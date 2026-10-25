package dev.shwg.shwgconfig.gui.components.screens;

import com.google.gson.JsonObject;
import dev.shwg.shwgconfig.api.ConfigManager;
import dev.shwg.shwgconfig.api.entries.CategoryEntry;
import dev.shwg.shwgconfig.api.entries.ControllerEntry;
import dev.shwg.shwgconfig.api.entries.CustomWidgetEntry;
import dev.shwg.shwgconfig.api.model.ConfigCategory;
import dev.shwg.shwgconfig.api.model.Tab;
import dev.shwg.shwgconfig.gui.components.controller.ConfigCategoryImpl;
import dev.shwg.shwgconfig.gui.components.controller.SimpleController;
import dev.shwg.shwgconfig.gui.components.elements.HorizontalLineElement;
import dev.shwg.shwgconfig.gui.components.tabs.ITab;
import dev.shwg.shwgconfig.gui.components.tabs.SimpleTab;
import dev.shwg.shwgconfig.gui.components.tabs.TabManager;
import dev.shwg.shwgconfig.gui.components.widget.GenericScrollableWidget;
import dev.shwg.shwgconfig.gui.components.widget.GenericTabNavigationBar;
import dev.shwg.shwgconfig.gui.components.widget.button.GenericButton;
import dev.shwg.shwgconfig.gui.components.widget.button.TabButton;
import dev.shwg.shwgconfig.gui.deferred.overlay.WidgetOverlay;
import dev.shwg.shwgconfig.gui.layout.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShwgConfigScreen extends GenericScreen {

    private final List<Tab> tabs;
    private final ConfigManager manager;
    private JsonObject configBackup;

    private final GenericHeaderFooterLayout headerFooterLayout = new GenericHeaderFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addAndRegister, this::removeAndDeregister);
    private final List<SimpleTab> builtTabs = new ArrayList<>();
    private final List<GenericScrollableWidget> tabScrollables = new ArrayList<>(); // same order as builtTabs
    private int layoutWidth = -1; // screen width the tab layouts were last built for

    /** Ticks to wait after the last width change before rebuilding (dragging the window border resizes many times). */
    private static final int REBUILD_DELAY_TICKS = 2;
    private int rebuildCountdown = 0; // 0 = no rebuild pending
    private GenericTabNavigationBar navigationBar;
    private boolean firstRun = true;

    public ShwgConfigScreen(Component title, List<Tab> tabs, @Nullable Screen parent, ConfigManager manager) {
        super(title, parent);
        this.tabs = tabs;
        this.manager = manager;
    }

    @Override
    protected void init() {
        if (firstRun) {
            configBackup = manager.config().toJson();

            for (Tab tab : tabs) {
                BuiltTab built = buildTabLayout(tab);
                builtTabs.add(new SimpleTab(tab.getTitle(), built.layout()));
                tabScrollables.add(built.scrollable());
            }
            layoutWidth = width;

            LayoutElement titleElement = label(font, title, 0, 0, -1);

            if (builtTabs.size() > 1) {
                if (!title.equals(Component.empty())) {
                    headerFooterLayout.header().add(titleElement);
                }
                navigationBar = buildNavigationBar();
                headerFooterLayout.header().add(navigationBar);
            } else if (!title.equals(Component.empty())) {
                GenericFrameLayout titleFrame = new GenericFrameLayout(
                        0, 0, titleElement.getWidth(), 16,
                        titleElement, Alignment.Horizontal.CENTER, Alignment.Vertical.CENTER);
                headerFooterLayout.header().add(titleFrame);
            }

            headerFooterLayout.footer().add(GenericButton.builder(CommonComponents.GUI_CANCEL, b -> onCancel()).build());
            headerFooterLayout.footer().add(GenericButton.builder(CommonComponents.GUI_DONE, b -> onClose()).build());

            headerFooterLayout.arrange();
            tabManager.setCurrentTab(builtTabs.get(0), false);
            firstRun = false;
        } else {
            // Widths (controller column, label wrapping, dividers) are baked in when a layout is
            // built, so a different screen width (resize, fullscreen, GUI scale) needs new layouts.
            // Not rebuilt right away: while the window border is dragged this runs for every
            // step, so the rebuild waits until the width stopped changing (see tick()).
            // Until then the old layout is only re-arranged at the new size.
            if (width != layoutWidth) {
                rebuildCountdown = REBUILD_DELAY_TICKS;
            }
            if (tabManager.getCurrentTab() != null) {
                tabManager.getCurrentTab().getLayout().visitChildren(this::add);
            }
        }

        add(headerFooterLayout);
        registerElements();
        arrange();
        focusOnFirstOpen(tabScrollables.get(builtTabs.indexOf(tabManager.getCurrentTab())));
    }

    @Override
    public void tick() {
        super.tick();
        if (rebuildCountdown > 0 && --rebuildCountdown == 0) {
            ITab current = tabManager.getCurrentTab();
            if (current != null) current.visitChildren(this::removeAndDeregister);
            rebuildTabLayouts();
            if (current != null) current.visitChildren(this::addAndRegister);
            arrange();
        }
    }

    /**
     * Builds every tab's layout again for the current screen width. Keeps the tabs themselves
     * (so the navigation bar and the current tab stay valid) and each tab's scroll position.
     * Like vanilla on resize, focus and unsaved text in widgets are not kept.
     */
    private void rebuildTabLayouts() {
        for (int i = 0; i < tabs.size(); i++) {
            double scrollOffset = tabScrollables.get(i).getScrollOffset();
            BuiltTab built = buildTabLayout(tabs.get(i));
            built.scrollable().setScrollOffset(scrollOffset); // clamped by the next arrange()
            builtTabs.get(i).setLayout(built.layout());
            tabScrollables.set(i, built.scrollable());
        }
        layoutWidth = width;
    }

    @Override
    protected void arrange() {
        if (navigationBar != null) {
            navigationBar.arrangeButtons(headerFooterLayout.getWidth(), navigationBar.getHeight());
        }
        headerFooterLayout.arrange();
        if (tabManager.getCurrentTab() != null) {
            tabManager.getCurrentTab().adjustLayout(0, headerFooterLayout.header().getBottom(), width, headerFooterLayout.getContentHeight());
            tabManager.getCurrentTab().getLayout().arrange();
        }
        tabManager.setTabArea(0, headerFooterLayout.header().getBottom(), width, headerFooterLayout.getContentHeight());
    }

    @Override
    public void updateOverlayBoundaries() {
        WidgetOverlay.topBoundary = headerFooterLayout.header().getBottom();
        WidgetOverlay.bottomBoundary = headerFooterLayout.footer().getY();
    }

    private record BuiltTab(GenericLayout layout, GenericScrollableWidget scrollable) {}

    private BuiltTab buildTabLayout(Tab tab) {
        int rawControllerWidth = calculateControllerWidth(font, tab);
        int maxPreviewWidth = calculateMaxPreviewWidth(tab);

        int scrollbarAllowance = 20;
        int availableWidth = width - scrollbarAllowance * 2 - maxPreviewWidth * 2 - (maxPreviewWidth > 0 ? ConfigCategoryImpl.CONTROLLER_PREVIEW_SPACING : 0);

        // At least the minimum width, but never more than the window offers - on a very narrow
        // window the available width wins (clamping would return the minimum and overflow).
        int controllerWidth = Math.min(Math.max(rawControllerWidth, SimpleController.DEFAULT_CONTROLLER_WIDTH + 40), availableWidth);

        int dividerWidth = controllerWidth + (maxPreviewWidth == 0 ? 0 : maxPreviewWidth + ConfigCategoryImpl.CONTROLLER_PREVIEW_SPACING);
        dividerWidth = Math.min(width, (int) (dividerWidth * 1.2f));

        GenericScrollableWidget scrollable = GenericScrollableWidget.builder()
                .bounds(0, 0, width, height)
                .padding(0)
                .build();

        List<ConfigCategory> categories = tab.getCategories();
        for (int i = 0; i < categories.size(); i++) {
            ConfigCategoryImpl categoryImpl = new ConfigCategoryImpl(controllerWidth, availableWidth, font, categories.get(i));
            scrollable.add(categoryImpl);

            if (i < categories.size() - 1) {
                scrollable.add(new HorizontalLineElement(dividerWidth, 12, 1, 0xFFAAAAAA));
            }
        }

        GenericVerticalLayout layout = new GenericVerticalLayout(0, 0, 0, 0, 2, 2, Alignment.Horizontal.CENTER, scrollable.getScrollbarWidth()) {
            @Override
            public void arrange() {
                scrollable.setWidth(getWidth());
                scrollable.setHeight(this.getHeight() - getPadding());
                super.arrange();
                scrollable.arrange();
            }
        };
        layout.add(scrollable);
        return new BuiltTab(layout, scrollable);
    }

    private GenericTabNavigationBar buildNavigationBar() {
        GenericTabNavigationBar.Builder builder = GenericTabNavigationBar.builder(tabManager);
        builtTabs.forEach(t -> builder.addTab(TabButton.builder(tabManager, t).build()));
        builder.width(this.width).stretchWidth(true);
        return builder.build();
    }

    private static int calculateControllerWidth(Font font, Tab tab) {
        int max = 0;
        for (ConfigCategory category : tab.getCategories()) {
            if (!category.useOwnControllerWidth()) {
                max = Math.max(max, ConfigCategoryImpl.calculateEntryWidth(font, category.getEntries()));
            }
        }
        return max;
    }

    private static int calculateMaxPreviewWidth(Tab tab) {
        int max = 0;
        for (ConfigCategory category : tab.getCategories()) {
            RenderableElement preview = category.getPreviewWidget();
            if (preview != null) {
                max = Math.max(max, preview.getWidth());
            }
        }
        return max;
    }

    private void onCancel() {
        manager.config().fromJson(configBackup);
        manager.save();
        onClose();
    }
}

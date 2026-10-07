/*? if < 1.20 { */
/*package dev.shwg.shwgconfig.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;

public class GuiComponentAccessor extends GuiComponent {

    /^? if < 1.19.4 { ^/
    /^private static GuiComponentAccessor INSTANCE;

    private GuiComponentAccessor() {}

    public static GuiComponentAccessor INSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new GuiComponentAccessor();
        }
        return INSTANCE;
    }
    ^//^? } ^/


    public static void gradient(PoseStack poseStack, int x0, int y0, int x1, int y1, int colorTop, int colorBottom) {
        GuiComponent.fillGradient(poseStack, x0, y0, x1, y1, colorTop, colorBottom, 1);
    }

    public static void shLine(PoseStack poseStack, int x0, int x1, int y, int color) {
        /^? if >= 1.19.4 { ^/
        GuiComponent.hLine(poseStack, x0, x1, y, color);
        /^? } else {^/
        //INSTANCE().hLine(poseStack, x0, x1, y, color);
        /^? } ^/


    }

    public static void svLine(PoseStack poseStack, int x, int y0, int y1, int color) {
        /^? if >= 1.19.4 { ^/
        GuiComponent.vLine(poseStack, x, y0, y1, color);
        /^? } else {^/
        //INSTANCE().vLine(poseStack, x, y0, y1, color);
        /^? } ^/
    }

}
*//*? } */

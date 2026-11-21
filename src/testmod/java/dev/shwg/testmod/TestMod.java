package dev.shwg.testmod;

import dev.shwg.shwgconfig.api.hud.HudOptions;
import dev.shwg.shwgconfig.api.hud.HudRenderDispatcher;
import dev.shwg.testmod.test.AutoTest;
import dev.shwg.testmod.test.render.RenderTestScreen;
import net.minecraft.client.Minecraft;
import dev.shwg.shwgconfig.api.ConfigManager;
import dev.shwg.shwgconfig.api.ClientTickDispatcher;
import dev.shwg.testmod.config.TestConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;

/*? if fabric {*/
import net.fabricmc.api.ModInitializer;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
/^? if 1.20.6 || >= 1.21.6  { ^/
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
/^? } ^/
*//*?}*/

/*? if neoforge {*/
/*import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.IEventBus;
*//*?}*/

/*? if forgeLike { */
/*import dev.shwg.shwgconfig.ShwgConfig;
@Mod(TestMod.MOD_ID)
*//*?}*/
/*~ if fabric '{' -> 'implements ModInitializer {' */
public class TestMod implements ModInitializer {

    public static final String MOD_ID = "shwgconfigtestmod";
    public static final TestConfig TEST_CONFIG = new TestConfig();
    public static ConfigManager configManager;
    public static int Z_OFFSET = 0;

    /*? if forge {*/
    /*/^~ if 1.20.6 || >= 1.21.6 '()' -> '(FMLJavaModLoadingContext context)' ^/
    public TestMod(FMLJavaModLoadingContext context) {
        init();
        /^~ if 1.20.6 || >= 1.21.6 'null' -> 'context' ^/
        ShwgConfig.forgeInit(configManager, context);
    }
    *//*?}*/

    /*? if neoforge {*/
    /*public TestMod(IEventBus modEventBus, ModContainer modContainer) {
        init();
        ShwgConfig.neoForgeInit(configManager, modEventBus, modContainer);
    }
    *//*?}*/

    /*? if fabric {*/
    @Override
    public void onInitialize() {
        init();
    }
    /*?}*/

    private void init() {
        configManager = ConfigManager.of(TEST_CONFIG, "shwgconfig-testmod.json");
        configManager.load();

        AutoTest.start();

        ClientTickDispatcher.registerGUI(keyEvent -> {
            if (keyEvent.matches(TEST_CONFIG.keybindValue)) {
                Minecraft.getInstance().setScreen(configManager.getScreen(Minecraft.getInstance().screen));
            }
            if (keyEvent.matches(TEST_CONFIG.openTestScreen)) {
                Minecraft.getInstance().setScreen(new RenderTestScreen(Minecraft.getInstance().screen));
            }
        });

        ClientTickDispatcher.registerGUI(keyEvent -> {
            if (keyEvent.isUp() || keyEvent.isDown()) {
                int offset = !keyEvent.hasShiftDown() ? 1 : 10;
                if (keyEvent.isDown()) {
                    Z_OFFSET -= offset;
                } else {
                    Z_OFFSET += offset;
                }
                System.out.println("z offset: " + Z_OFFSET);
            }
        });

        ClientTickDispatcher.registerWorld(() -> {
            if (TEST_CONFIG.openTestScreen.consumeClick()) {
                Screen screen = Minecraft.getInstance().screen;
                if (!(screen instanceof RenderTestScreen)) {
                    Minecraft.getInstance().setScreen(new RenderTestScreen(screen));
                }
            }
        });

        HudRenderDispatcher.register(MOD_ID, "huddebughide",
                context -> {
                    Font font = Minecraft.getInstance().font;
                    context.graphics().drawString(font, "Hud Test hide with debug screen", 5, 5, -1);
                },
                HudOptions.create().hideWithDebugScreen()
        );

        HudRenderDispatcher.register(MOD_ID, "hudalways", context -> {
                    Font font = Minecraft.getInstance().font;
                    context.graphics().drawString(font, "Hud Test show always", 5, context.screenHeight() - font.lineHeight - 5, -1);
                }
        );


    }
}

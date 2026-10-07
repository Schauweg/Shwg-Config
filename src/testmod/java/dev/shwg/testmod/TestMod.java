package dev.shwg.testmod;

import net.minecraft.client.Minecraft;
import dev.shwg.shwgconfig.api.ConfigManager;
import dev.shwg.shwgconfig.api.ClientTickDispatcher;
import dev.shwg.testmod.config.TestConfig;

/*? if fabric {*/
//import net.fabricmc.api.ModInitializer;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
/^? if 1.20.6 || >= 1.21.6  { ^/
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
/^? } ^/
*//*?}*/

/*? if neoforge {*/
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.IEventBus;
/*?}*/

/*? if forgeLike{ */
import dev.shwg.shwgconfig.ShwgConfig;
@Mod(TestMod.MOD_ID)
/*?}*/
/*~ if fabric '{' -> 'implements ModInitializer {' */
public class TestMod {

    public static final String MOD_ID = "shwgconfigtestmod";
    public static final TestConfig TEST_CONFIG = new TestConfig();
    public static ConfigManager configManager;

    /*? if forge {*/
    /*/^~ if 1.20.6 || >= 1.21.6 '()' -> '(FMLJavaModLoadingContext context)' ^/
    public TestMod(FMLJavaModLoadingContext context) {
        init();
        /^~ if 1.20.6 || >= 1.21.6 'null' -> 'context' ^/
        ShwgConfig.forgeInit(configManager, context);
    }
    *//*?}*/

    /*? if neoforge {*/
    public TestMod(IEventBus modEventBus, ModContainer modContainer) {
        init();
        ShwgConfig.neoForgeInit(configManager, modEventBus, modContainer);
    }
    /*?}*/

    /*? if fabric {*/
    /*@Override
    public void onInitialize() {
        init();
    }
    *//*?}*/

    private void init(){
        configManager = ConfigManager.of(TEST_CONFIG, "shwgconfig-testmod.json");
        configManager.load();

        ClientTickDispatcher.registerGUI(keyEvent ->  {
            if (keyEvent.matches(TEST_CONFIG.keybindValue)) {
                Minecraft.getInstance().gui.setScreen(configManager.getScreen(Minecraft.getInstance().gui.screen()));
            }
        });
    }
}

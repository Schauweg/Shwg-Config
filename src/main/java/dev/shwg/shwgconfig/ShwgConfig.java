package dev.shwg.shwgconfig;

import dev.shwg.shwgconfig.api.ClientTickDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*? if fabric {*/
//import net.fabricmc.api.ModInitializer;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.ConfigScreenHandler;
/^? if >= 1.21.6  { ^/
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
/^? } ^/
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
*//*?}*/

/*? if neoforge {*/
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
/*?}*/

/*? if forgeLike {*/
import dev.shwg.shwgconfig.api.ConfigManager;
import dev.shwg.shwgconfig.api.KeybindRegistrar;
@Mod(ShwgConfig.MOD_ID)
/*?}*/
/*~ if fabric '{' -> 'implements ModInitializer {' */
public class ShwgConfig {

    public static final String MOD_ID = "shwgconfig";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean initialized = false;

    /*? if forge {*/
    /*public static void forgeInit(ConfigManager configManager, FMLJavaModLoadingContext context) {
        /^~ if 1.20.6 || >= 1.21.6 'ModLoadingContext.get()' -> 'context'^/
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> configManager.getScreen(parent))
        );

        /^? if >= 1.21.9  { ^/
        RegisterKeyMappingsEvent.BUS.addListener(KeybindRegistrar::registerAll);
        /^? } else if >= 1.21.6 { ^/
        //RegisterKeyMappingsEvent.getBus(context.getModBusGroup()).addListener(KeybindRegistrar::registerAll);
        /^? } else if 1.20.6 { ^/
        //context.getModEventBus().addListener(KeybindRegistrar::registerAll);
        /^? } else { ^/
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(KeybindRegistrar::registerAll);
        /^? } ^/
    }

    public ShwgConfig() {
        initializePlatform();
    }
    *//*?}*/

    /*? if neoforge {*/
    public static void neoForgeInit(ConfigManager configManager, IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (client, parent) -> configManager.getScreen(parent));
        modEventBus.addListener(KeybindRegistrar::registerAll);
    }

    public ShwgConfig(IEventBus modEventBus, ModContainer modContainer) {
        initializePlatform();
    }
    /*?}*/

    /*? if fabric {*/
    /*@Override
    public void onInitialize() {
        initializePlatform();
    }
    *//*?}*/

    private static void initializePlatform() {
        if (initialized) {
            return;
        }

        initialized = true;
        ClientTickDispatcher.init();

        LOGGER.info("ShwgConfig initialized");
    }
}

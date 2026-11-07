package dev.shwg.shwgconfig.util;

/*? if fabric {*/
import net.fabricmc.loader.api.FabricLoader;
/*?} else if neoforge {*/
//import net.neoforged.fml.ModList;
/*?} else if forge {*/
//import net.minecraftforge.fml.ModList;
/*?}*/

public final class ModLoaderHelper {

    private ModLoaderHelper() {
    }

    public static boolean isModLoaded(String modId) {
        /*? if fabric {*/
        return FabricLoader.getInstance().isModLoaded(modId);
        /*?} else if neoforge {*/
        //return ModList.get().isLoaded(modId);
        /*?} else if forge {*/
        /*/^~ if >= 26 '.get().isLoaded(' -> '.isLoaded(' ^/
        return ModList.get().isLoaded(modId);
        *//*?}*/
    }
}

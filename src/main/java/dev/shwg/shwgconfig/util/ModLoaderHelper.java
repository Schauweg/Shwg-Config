package dev.shwg.shwgconfig.util;

/*? if fabric {*/
import net.fabricmc.loader.api.FabricLoader;
/*?} else if neoforge {*/
/*import net.neoforged.fml.ModList;
/^? if >= 1.21.9 ^/
import net.neoforged.fml.loading.FMLLoader;
/^? else ^/
//import net.neoforged.fml.loading.LoadingModList;
*//*?} else if forge {*/
/*import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
*//*?}*/

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

    public static boolean isModPresentEarly(String modId) {
        /*? if fabric {*/
        return FabricLoader.getInstance().isModLoaded(modId);
        /*?} else if neoforge {*/
        /*/^? if >= 1.21.9 ^/
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
        /^? else ^/
        //return LoadingModList.get().getModFileById(modId) != null;
        *//*?} else if forge {*/
        /*/^~ if >= 26 '.get().getModFileById(' -> '.getModFileById(' ^/
        return LoadingModList.get().getModFileById(modId) != null;
         *//*?}*/
    }
}

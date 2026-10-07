package dev.shwg.shwgconfig.util;

/*? if fabric {*/
//import net.fabricmc.loader.api.FabricLoader;
/*?} else if neoforge {*/
import net.neoforged.fml.loading.FMLPaths;
/*?} else if forge {*/
//import net.minecraftforge.fml.loading.FMLPaths;
/*? }*/

import java.nio.file.Path;

public class ConfigPathResolver {

    public static Path getConfigDir(String dir) {
        /*? if fabric {*/
        //return FabricLoader.getInstance().getConfigDir().resolve(dir);
        /*?} else if neoforge {*/
        return FMLPaths.CONFIGDIR.get().resolve(dir);
         /*?} else if forge {*/
        //return FMLPaths.CONFIGDIR.get().resolve(dir);
         /*? }*/
    }
}

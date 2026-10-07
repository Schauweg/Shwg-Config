package dev.shwg.shwgconfig.util;

import com.mojang.blaze3d.platform.InputConstants;

/*? if < 26.3 {*/
import net.minecraft.client.Minecraft;
/*? } */

public class InputUtil {
    
    public static boolean isKeyDown(int key){
        /*? if >= 26.3 {*/
        //return InputConstants.isKeyDown(key);
        /*? } else if >= 1.21.9 { */
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key);
        /*? } else { */
        //return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
        /*? } */
    }
}

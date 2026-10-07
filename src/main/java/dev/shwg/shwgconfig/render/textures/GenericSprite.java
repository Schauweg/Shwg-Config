package dev.shwg.shwgconfig.render.textures;

import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/*? if >= 1.21.9 { */
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
/*? } else if >= 1.20.2 { */
//import net.minecraft.client.gui.GuiSpriteManager;
 /*? } */

/*? if >= 1.20.2 { */
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
/*? } else { */
/*import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
*//*? } */

/**
 * Base class for GUI sprites located in {@code textures/gui/sprites/}.
 *
 * <p>This class automatically reads the sprite's scaling behavior from its {@code .mcmeta} file
 * (if present) to determine whether it should be stretched, tiled, or nine-sliced.</p>
 *
 * <p><strong>Important:</strong> {@link #fromMcMeta(String, String)} and related methods
 * only work if a {@code .mcmeta} file exists for the sprite.</p>
 *
 */
/*? if >= 1.20.2 { */
@SuppressWarnings({"DeconstructionCanBeUsed", "resource"})
/*? } */
public class GenericSprite extends GenericTexture {

    public GenericSprite(String namespace, String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String spritePath) {
        super(namespace, atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, spritePath);
    }

    public static GenericSprite changedVanillaTexture(String atlasPath, int atlasX, int atlasY, int regionWidth, int regionHeight, int atlasWidth, int atlasHeight, String spritePath) {
        return new GenericSprite("minecraft", atlasPath, atlasX, atlasY, regionWidth, regionHeight, atlasWidth, atlasHeight, spritePath);
    }

    /**
     * Creates a sprite and automatically determines its scaling type by reading the
     * associated {@code .mcmeta} file.
     *
     * <p><strong>Note:</strong> This method requires a {@code .mcmeta} file to exist.
     * If no {@code .mcmeta} is found, an exception will be thrown in newer versions,
     * or it will fall back to stretched behavior in older versions.
     * </p>
     *
     * @param namespace the namespace (usually "minecraft")
     * @param path      the path to the sprite (without "textures/gui/sprites/" prefix and without ".png")
     * @return the appropriate sprite type (NineSlicedSprite, TiledSprite, or StretchedSprite)
     * @throws IllegalArgumentException if the sprite or its metadata cannot be loaded
     */
    public static GenericSprite fromMcMeta(String namespace, String path) {
        /*? if >= 1.20.2 { */
        Identifier rl = RegistryHelper.build(namespace, path);
        TextureAtlasSprite textureAtlasSprite = manager().getSprite(rl);
        int width = textureAtlasSprite.contents().width();
        int height = textureAtlasSprite.contents().height();

        /*? if >= 1.21.9 { */
        GuiSpriteScaling scaling = textureAtlasSprite.contents().getAdditionalMetadata(GuiMetadataSection.TYPE).orElse(GuiMetadataSection.DEFAULT).scaling();
        /*? } else { */
        //GuiSpriteScaling scaling = manager().getSpriteScaling(textureAtlasSprite);
        /*? } */
        if (scaling instanceof GuiSpriteScaling.Stretch) {
            return new StretchedSprite(namespace, path, 0, 0, width, height, width, height, path);
        } else if (scaling instanceof GuiSpriteScaling.Tile tile) {
            return new TiledSprite(namespace, path, 0, 0, tile.width(), tile.height(), width, height, path);
        } else if (scaling instanceof GuiSpriteScaling.NineSlice  nineSlice) {
            return new NineSlicedSprite(namespace, path, 0, 0, nineSlice.width(), nineSlice.height(), width, height, path,
                    nineSlice.border().left(),
                    nineSlice.border().top(),
                    nineSlice.border().right(),
                    nineSlice.border().bottom(),
                    //? if >= 1.21.4 {
                    nineSlice.stretchInner()
                    //? } else
                    //false
            );
        } else {
            return null;
        }
        /*? } else {*/
        /*path = "textures/gui/sprites/" + path + ".png";
        Resource mcMeta = getMcMetaResource(namespace, path);
        if (mcMeta == null) {
            return buildStretch(namespace, path);
        }
        JsonObject scaling = getScaling(mcMeta);
        String scalingType = scaling.get("type").getAsString();
        return switch (scalingType) {
            case "stretch" -> buildStretch(namespace, path, scaling);
            case "tile" -> buildTiled(namespace, path, scaling);
            case "nine_slice" -> buildNineSlice(namespace, path, scaling);
            default -> buildStretch(namespace, path, scaling);
        };
        *//*? } */
    }

    /*? if >= 1.20.2 { */
    protected static TiledSprite buildTiledSprite(String namespace, String path) {
        Identifier rl = RegistryHelper.build(namespace, path);
        TextureAtlasSprite textureAtlasSprite = manager().getSprite(rl);

        if (scaling(textureAtlasSprite) instanceof GuiSpriteScaling.Tile tile) {
            return new TiledSprite(namespace, path, 0, 0, tile.width(), tile.height(), tile.width(), tile.height(), path);
        } else {
            throw new IllegalArgumentException("The sprite is not a tile type " + namespace + ":" + path);
        }
    }

    protected static StretchedSprite buildStretch(String namespace, String path) {
        Identifier rl = RegistryHelper.build(namespace, path);
        TextureAtlasSprite textureAtlasSprite = manager().getSprite(rl);

        if (scaling(textureAtlasSprite) instanceof GuiSpriteScaling.Stretch) {
            return new StretchedSprite(namespace, path, 0, 0, 0, 0, 0, 0, path);
        } else {
            throw new IllegalArgumentException("The sprite is not a stretch type (defined as stretch or not defined at all) " + namespace + ":" + path);
        }
    }

    protected static NineSlicedSprite buildNineSlice(String namespace, String path) {
        Identifier rl = RegistryHelper.build(namespace, path);
        TextureAtlasSprite textureAtlasSprite = manager().getSprite(rl);

        if (scaling(textureAtlasSprite) instanceof GuiSpriteScaling.NineSlice nineSlice) {
            return new NineSlicedSprite(namespace, path, 0, 0, nineSlice.width(), nineSlice.height(), nineSlice.width(), nineSlice.height(), path,
                    nineSlice.border().left(),
                    nineSlice.border().top(),
                    nineSlice.border().right(),
                    nineSlice.border().bottom(),
                    /*? if >= 1.21.4 { */
                    nineSlice.stretchInner()
                    /*? } else { */
                    //false
                    /*? } */
            );
        } else {
            throw new IllegalArgumentException("The sprite is not a nice_slice type " + namespace + ":" + path);
        }
    }
    /*? } */


    /*? if >= 1.21.9 { */
    private static TextureAtlas manager() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
    }

    private static GuiSpriteScaling scaling(TextureAtlasSprite textureAtlasSprite) {
        return textureAtlasSprite.contents().getAdditionalMetadata(GuiMetadataSection.TYPE).orElse(GuiMetadataSection.DEFAULT).scaling();
    }
    /*? } else if >= 1.20.2 { */
    /*private static GuiSpriteManager manager() {
        return Minecraft.getInstance().getGuiSprites();
    }

    private static GuiSpriteScaling scaling(TextureAtlasSprite textureAtlasSprite) {
        return manager().getSpriteScaling(textureAtlasSprite);
    }
    *//*? } else {*/
    /*@Nullable
    protected static Resource getMcMetaResource(String namespace, String path) {
        try {
            Identifier metaLocation = RegistryHelper.build(namespace, path + ".mcmeta");
            return Minecraft.getInstance().getResourceManager().getResourceOrThrow(metaLocation);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    protected static JsonObject getScaling(Resource resource) {
        try {
            InputStreamReader reader = new InputStreamReader(resource.open());
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("gui") && root.get("gui").isJsonObject()) {
                JsonObject gui = root.getAsJsonObject("gui");
                if (gui.has("scaling") && gui.get("scaling").isJsonObject()) {
                    return gui.getAsJsonObject("scaling");
                } else {
                    throw new IllegalArgumentException("Missing 'scaling' json object in " + resource);
                }
            } else  {
                throw new IllegalArgumentException("Missing 'gui' json object in " + resource);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error when reading resource " + resource);
        }
    }

    protected static int[] getDimensions(String namespace, String path) {
        Identifier rl = RegistryHelper.build(namespace, path);
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        try {
            NativeImage texture = NativeImage.read(manager.open(rl));
            return new int[]{texture.getWidth(), texture.getHeight()};
        } catch (IOException e) {
            throw new IllegalArgumentException("Error trying to load file for resource " + namespace + ":" + path);
        }
    }

    protected static StretchedSprite buildStretch(String namespace, String path, JsonObject scaling) {
        if (scaling.has("type") && !scaling.get("type").getAsString().equals("stretch")) {
            throw new IllegalArgumentException("Scaling type must be 'stretch' or not defined in " + namespace + ":" + path);
        }

        int[] dimensions = getDimensions(namespace,  path);
        return new StretchedSprite(namespace, path, 0, 0, dimensions[0], dimensions[1], dimensions[0], dimensions[1], path);
    }

    private static GenericSprite buildStretch(String namespace, String path) {
        int[] dimensions = getDimensions(namespace,  path);
        return new StretchedSprite(namespace, path, 0, 0, dimensions[0], dimensions[1], dimensions[0], dimensions[1], path);
    }

    protected static TiledSprite buildTiled(String namespace, String path, JsonObject scaling) {
        if (!scaling.has("type") || !scaling.get("type").getAsString().equals("tile")) {
            throw new IllegalArgumentException("Scaling type must be 'tile' " + namespace + ":" + path);
        }

        int width = scaling.get("width").getAsInt();
        int height = scaling.get("height").getAsInt();
        return new TiledSprite(namespace, path, 0, 0, width , height, width, height, path);
    }

    protected static NineSlicedSprite buildNineSlice(String namespace, String path, JsonObject scaling) {
        if (!scaling.has("type") || !scaling.get("type").getAsString().equals("nine_slice")){
            throw new IllegalArgumentException("Scaling type must be 'nine_sliced' in " + namespace + ":" + path);
        }

        int width = scaling.get("width").getAsInt();
        int height = scaling.get("height").getAsInt();

        boolean stretchInner = false;
        if (scaling.get("stretch_inner") != null) {
            stretchInner = scaling.get("stretch_inner").getAsBoolean();
        }

        JsonElement borderElem = scaling.get("border");
        if (borderElem.isJsonPrimitive()) {
            int border = borderElem.getAsInt();
            return new NineSlicedSprite(namespace, path, 0, 0, width, height, width, height, path, border, border, border, border, stretchInner);
        } else if (borderElem.isJsonObject()) {
            JsonObject border = borderElem.getAsJsonObject();
            int left = border.get("left").getAsInt();
            int top = border.get("top").getAsInt();
            int right = border.get("right").getAsInt();
            int bottom = border.get("bottom").getAsInt();
            return new NineSlicedSprite(namespace, path, 0, 0, width, height, width, height, path, left, top, right, bottom, stretchInner);
        } else  {
            throw new IllegalArgumentException("Missing 'border' json object in " + namespace + ":" + path);
        }
    }
    *//*? } */
}

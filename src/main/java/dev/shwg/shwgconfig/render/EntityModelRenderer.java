package dev.shwg.shwgconfig.render;

/*? if <1.21.2 {*/
/*import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.shwg.shwgconfig.mixin.EntityRenderDispatcherAccessor;
import dev.shwg.shwgconfig.mixin.LivingEntityRendererInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import dev.shwg.shwgconfig.util.RegistryHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
*//*?}*/

/*? if >=1.19.3 && <1.21.2 {*/
//import org.joml.Quaternionf;
/*?} else if <1.19.3 {*/
/*import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
*//*?}*/

/*? if >=1.19.4 && <1.20.6 {*/
//import org.joml.Matrix4f;
/*?}*/

/*? if <1.20 {*/
//import com.mojang.blaze3d.systems.RenderSystem;
/*?}*/

final class EntityModelRenderer {

    private EntityModelRenderer() {
    }

    /*? if <1.21.2 {*/
    /*private static final Map<EntityType<?>, Identifier> DEFAULT_TEXTURES = Map.of(
            EntityType.GHAST, texture("ghast/ghast.png"),
            EntityType.HORSE, texture("horse/horse_white.png"),
            EntityType.TROPICAL_FISH, texture("fish/tropical_a.png"),
            EntityType.AXOLOTL, texture("axolotl/axolotl_lucy.png"),
            EntityType.WOLF, texture("wolf/wolf.png"),
            EntityType.CAT, texture("cat/jellie.png"),
            EntityType.OCELOT, texture("cat/ocelot.png"),
            EntityType.RABBIT, texture("rabbit/brown.png"),
            EntityType.BEE, texture("bee/bee.png")
    );

    private static final Map<EntityType<?>, Entity> WORLD_ENTITIES = new HashMap<>();
    @Nullable
    private static Level worldEntitiesLevel;

    @Nullable
    static LivingEntity worldEntity(EntityType<?> type) {
        Level level = Minecraft.getInstance().level;
        if (level != worldEntitiesLevel) {
            WORLD_ENTITIES.clear();
            worldEntitiesLevel = level;
        }
        if (level == null) return null;
        Entity entity = WORLD_ENTITIES.computeIfAbsent(type, t -> t.create(level));
        return entity instanceof LivingEntity living ? living : null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void drawModel(GenericGraphics g, EntityType<?> type, int x0, int y0, int x1, int y1, float yaw, float pitch) {
        Map<EntityType<?>, ?> renderers = ((EntityRenderDispatcherAccessor) Minecraft.getInstance().getEntityRenderDispatcher()).shwgconfig$getRenderers();
        if (!(renderers.get(type) instanceof LivingEntityRenderer<?, ?> renderer)) {
            drawSpawnEgg(g, type, x0, y0, x1, y1);
            return;
        }

        Identifier texture = DEFAULT_TEXTURES.get(type);
        if (texture == null) {
            try {
                texture = ((EntityRenderer) renderer).getTextureLocation(null);
            } catch (RuntimeException e) {
                texture = null;
            }
        }
        if (texture == null) {
            drawSpawnEgg(g, type, x0, y0, x1, y1);
            return;
        }

        EntityModel model = renderer.getModel();
        model.young = false;
        model.riding = false;
        model.attackTime = 0.0F;

        ModelBounds bounds = ModelBounds.hitbox(type.getWidth(), type.getHeight()).union(ModelBounds.of(renderer, null));
        ModelBounds.ScreenBounds screen = bounds.project(yaw, pitch);
        float scale = GenericGraphics.fitScale(x1 - x0, y1 - y0, screen.width(), screen.height());
        g.enableScissor(x0, y0, x1, y1);
        Lighting.setupForEntityInInventory();
        drawInBox(g, renderer, model, texture, (x0 + x1) / 2.0F, (y0 + y1) / 2.0F, scale, -screen.centerX(), -screen.centerY(), yaw, pitch);
        Lighting.setupFor3DItems();
        g.disableScissor();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void renderModel(LivingEntityRenderer<?, ?> renderer, EntityModel<?> model, Identifier texture,
                                    PoseStack pose, MultiBufferSource buffers, float yaw) {
        pose.pushPose();
        pose.mulPose(yawRotation(yaw));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.pushPose();
        try {
            ((LivingEntityRendererInvoker) renderer).shwgconfig$scale(null, pose, 1.0F);
        } catch (RuntimeException e) {
            pose.popPose();
            pose.pushPose();
        }
        pose.translate(0.0, -1.501, 0.0);
        renderToBuffer(model, pose, buffers.getBuffer(model.renderType(texture)));
        pose.popPose();
        pose.popPose();
    }

    private static Identifier texture(String path) {
        return RegistryHelper.build("minecraft", "textures/entity/" + path);
    }

    private static void drawSpawnEgg(GenericGraphics g, EntityType<?> type, int x0, int y0, int x1, int y1) {
        SpawnEggItem egg = SpawnEggItem.byId(type);
        if (egg == null) return;
        int size = Math.min(x1 - x0, y1 - y0);
        g.renderScaledItem(new ItemStack(egg), x0 + (x1 - x0 - size) / 2, y0 + (y1 - y0 - size) / 2, size);
    }
    *//*?}*/

    /*? if >=1.20 && <1.21.2 {*/
    /*private static void drawInBox(GenericGraphics g, LivingEntityRenderer<?, ?> renderer, EntityModel<?> model, Identifier texture,
                                  float centerX, float centerY, float scale, float offsetX, float offsetY, float yaw, float pitch) {
        var gui = g.getGraphics();
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 50.0);
        scaleGui(pose, scale);
        pose.translate(offsetX, offsetY, 0.0);
        pose.mulPose(guiRotation(pitch));
        renderModel(renderer, model, texture, pose, gui.bufferSource(), yaw);
        gui.flush();
        pose.popPose();
    }
    *//*?} else if >=1.19.4 && <1.20 {*/
    /*private static void drawInBox(GenericGraphics g, LivingEntityRenderer<?, ?> renderer, EntityModel<?> model, Identifier texture,
                                  float centerX, float centerY, float scale, float offsetX, float offsetY, float yaw, float pitch) {
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.translate(0.0, 0.0, 1000.0);
        RenderSystem.applyModelViewMatrix();
        PoseStack pose = g.getGraphics();
        pose.pushPose();
        pose.translate(centerX, centerY, -950.0);
        scaleGui(pose, scale);
        pose.translate(offsetX, offsetY, 0.0);
        pose.mulPose(guiRotation(pitch));
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        renderModel(renderer, model, texture, pose, buffers, yaw);
        buffers.endBatch();
        pose.popPose();
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }
    *//*?} else if <1.19.4 {*/
    /*private static void drawInBox(GenericGraphics g, LivingEntityRenderer<?, ?> renderer, EntityModel<?> model, Identifier texture,
                                  float centerX, float centerY, float scale, float offsetX, float offsetY, float yaw, float pitch) {
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.translate(centerX, centerY, 1050.0);
        modelView.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack pose = new PoseStack();
        pose.translate(0.0, 0.0, 1000.0);
        pose.scale(scale, scale, scale);
        pose.translate(offsetX, offsetY, 0.0);
        pose.mulPose(guiRotation(pitch));
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        renderModel(renderer, model, texture, pose, buffers, yaw);
        buffers.endBatch();
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }
    *//*?}*/

    /*? if >=1.20.6 && <1.21.2 {*/
    /*private static void scaleGui(PoseStack pose, float scale) {
        pose.scale(scale, scale, -scale);
    }
    *//*?} else if >=1.19.4 && <1.20.6 {*/
    /*private static void scaleGui(PoseStack pose, float scale) {
        pose.mulPoseMatrix(new Matrix4f().scaling(scale, scale, -scale));
    }
    *//*?}*/

    /*? if >=1.21 && <1.21.2 {*/
    /*static void renderToBuffer(EntityModel<?> model, PoseStack pose, VertexConsumer buffer) {
        model.renderToBuffer(pose, buffer, 15728880, OverlayTexture.NO_OVERLAY, -1);
    }
    *//*?} else if <1.21 {*/
    /*static void renderToBuffer(EntityModel<?> model, PoseStack pose, VertexConsumer buffer) {
        model.renderToBuffer(pose, buffer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    *//*?}*/

    /*? if >=1.19.3 && <1.21.2 {*/
    /*private static Quaternionf guiRotation(float pitch) {
        return new Quaternionf().rotateZ((float) Math.PI).mul(new Quaternionf().rotateX((float) Math.toRadians(-pitch)));
    }

    private static Quaternionf yawRotation(float yaw) {
        return new Quaternionf().rotateY((float) Math.toRadians(-yaw));
    }
    *//*?} else if <1.19.3 {*/
    /*private static Quaternion guiRotation(float pitch) {
        Quaternion rotation = Vector3f.ZP.rotationDegrees(180.0F);
        rotation.mul(Vector3f.XP.rotationDegrees(-pitch));
        return rotation;
    }

    private static Quaternion yawRotation(float yaw) {
        return Vector3f.YP.rotationDegrees(-yaw);
    }
    *//*?}*/
}

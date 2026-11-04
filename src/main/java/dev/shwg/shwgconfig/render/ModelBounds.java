package dev.shwg.shwgconfig.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.shwg.shwgconfig.mixin.LivingEntityRendererInvoker;
import dev.shwg.shwgconfig.mixin.ModelPartAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;

/*? if >=1.21.2 {*/
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
/*?} else {*/
/*import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
*//*?}*/

/*? if >=1.19.3 {*/
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
/*?} else {*/
/*import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
*//*?}*/

record ModelBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

    static final ModelBounds EMPTY = new ModelBounds(
            Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

    static ModelBounds hitbox(float width, float height) {
        float half = width / 2.0F;
        return new ModelBounds(-half, 0.0F, -half, half, height, half);
    }

    float width() {
        return maxX - minX;
    }

    float height() {
        return maxY - minY;
    }

    float depth() {
        return maxZ - minZ;
    }

    boolean isEmpty() {
        return maxX < minX || maxY < minY || maxZ < minZ;
    }

    ModelBounds union(ModelBounds other) {
        if (other.isEmpty()) return this;
        if (isEmpty()) return other;
        return new ModelBounds(
                Math.min(minX, other.minX), Math.min(minY, other.minY), Math.min(minZ, other.minZ),
                Math.max(maxX, other.maxX), Math.max(maxY, other.maxY), Math.max(maxZ, other.maxZ));
    }

    /*? if >=1.19.3 {*/
    ScreenBounds project(float yaw, float pitch) {
        if (isEmpty()) return new ScreenBounds(0.0F, 0.0F, 0.0F, 0.0F);
        Quaternionf view = new Quaternionf().rotateZ((float) Math.PI)
                .mul(new Quaternionf().rotateX((float) Math.toRadians(-pitch)))
                .mul(new Quaternionf().rotateY((float) Math.toRadians(-yaw)));
        ScreenBuilder screen = new ScreenBuilder();
        Vector3f corner = new Vector3f();
        for (int i = 0; i < 8; i++) {
            corner.set(cornerX(i), cornerY(i), cornerZ(i)).rotate(view);
            screen.include(corner.x(), corner.y());
        }
        return screen.build();
    }
    /*?} else {*/
    /*ScreenBounds project(float yaw, float pitch) {
        if (isEmpty()) return new ScreenBounds(0.0F, 0.0F, 0.0F, 0.0F);
        Quaternion view = Vector3f.ZP.rotationDegrees(180.0F);
        view.mul(Vector3f.XP.rotationDegrees(-pitch));
        view.mul(Vector3f.YP.rotationDegrees(-yaw));
        ScreenBuilder screen = new ScreenBuilder();
        for (int i = 0; i < 8; i++) {
            Vector3f corner = new Vector3f(cornerX(i), cornerY(i), cornerZ(i));
            corner.transform(view);
            screen.include(corner.x(), corner.y());
        }
        return screen.build();
    }
    *//*?}*/

    private float cornerX(int i) {
        return (i & 1) == 0 ? minX : maxX;
    }

    private float cornerY(int i) {
        return (i & 2) == 0 ? minY : maxY;
    }

    private float cornerZ(int i) {
        return (i & 4) == 0 ? minZ : maxZ;
    }

    record ScreenBounds(float minX, float minY, float maxX, float maxY) {

        float width() {
            return maxX - minX;
        }

        float height() {
            return maxY - minY;
        }

        float centerX() {
            return (minX + maxX) / 2.0F;
        }

        float centerY() {
            return (minY + maxY) / 2.0F;
        }
    }

    private static final class ScreenBuilder {
        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;

        void include(float x, float y) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        ScreenBounds build() {
            return new ScreenBounds(minX, minY, maxX, maxY);
        }
    }

    /*? if >=1.21.2 {*/
    @SuppressWarnings({"unchecked", "rawtypes"})
    static ModelBounds of(LivingEntityRenderer<?, ?, ?> renderer, LivingEntityRenderState state) {
        try {
            EntityModel model = renderer.getModel();
            model.setupAnim(state);
            PoseStack pose = new PoseStack();
            ((LivingEntityRendererInvoker) renderer).shwgconfig$scale(state, pose);
            pose.scale(-1.0F, -1.0F, 1.0F);
            pose.translate(0.0F, -1.501F, 0.0F);
            return of(model.root(), pose);
        } catch (RuntimeException e) {
            return EMPTY;
        }
    }
    /*?} else {*/
    /*@SuppressWarnings({"unchecked", "rawtypes"})
    static ModelBounds of(LivingEntityRenderer<?, ?> renderer, @Nullable LivingEntity entity) {
        EntityModel model = renderer.getModel();
        PoseStack pose = new PoseStack();
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.pushPose();
        try {
            ((LivingEntityRendererInvoker) renderer).shwgconfig$scale(entity, pose, 1.0F);
        } catch (RuntimeException e) {
            pose.popPose();
            pose.pushPose();
        }
        pose.translate(0.0, -1.501, 0.0);
        try {
            if (entity != null) {
                model.young = entity.isBaby();
                model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount, 0.0F, 0.0F);
            }
            Builder builder = new Builder();
            EntityModelRenderer.renderToBuffer(model, pose, new MeasuringConsumer(builder));
            return builder.build();
        } catch (RuntimeException e) {
            return EMPTY;
        }
    }
    *//*?}*/

    static ModelBounds of(ModelPart root, PoseStack pose) {
        Builder builder = new Builder();
        visit(root, pose, builder);
        return builder.build();
    }

    private static void visit(ModelPart part, PoseStack pose, Builder builder) {
        if (!part.visible) return;
        ModelPartAccessor accessor = (ModelPartAccessor) (Object) part;
        pose.pushPose();
        part.translateAndRotate(pose);
        if (!part.skipDraw) {
            Matrix4f matrix = pose.last().pose();
            for (ModelPart.Cube cube : accessor.shwgconfig$getCubes()) {
                builder.addCube(matrix, cube);
            }
        }
        for (ModelPart child : accessor.shwgconfig$getChildren().values()) {
            visit(child, pose, builder);
        }
        pose.popPose();
    }

    /*? if >=1.21 && <1.21.2 {*/
    /*private record MeasuringConsumer(Builder builder) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            builder.include(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }
    }
    *//*?} else if <1.21 {*/
    /*private record MeasuringConsumer(Builder builder) implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            builder.include((float) x, (float) y, (float) z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public void endVertex() {
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
        }

        @Override
        public void unsetDefaultColor() {
        }
    }
    *//*?}*/

    private static final class Builder {
        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float minZ = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private float maxZ = Float.NEGATIVE_INFINITY;

        void addCube(Matrix4f matrix, ModelPart.Cube cube) {
            for (int i = 0; i < 8; i++) {
                float x = ((i & 1) == 0 ? cube.minX : cube.maxX) / 16.0F;
                float y = ((i & 2) == 0 ? cube.minY : cube.maxY) / 16.0F;
                float z = ((i & 4) == 0 ? cube.minZ : cube.maxZ) / 16.0F;
                addPoint(matrix, x, y, z);
            }
        }

        private void addPoint(Matrix4f matrix, float x, float y, float z) {
            /*? if >=1.19.3 {*/
            Vector3f point = matrix.transformPosition(x, y, z, new Vector3f());
            include(point.x(), point.y(), point.z());
            /*?} else {*/
            /*Vector4f point = new Vector4f(x, y, z, 1.0F);
            point.transform(matrix);
            include(point.x(), point.y(), point.z());
            *//*?}*/
        }

        void include(float x, float y, float z) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        ModelBounds build() {
            if (minX > maxX) return EMPTY;
            return new ModelBounds(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}

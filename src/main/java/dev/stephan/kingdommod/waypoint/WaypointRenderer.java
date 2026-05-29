package dev.stephan.kingdommod.waypoint;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.stephan.kingdommod.KingdomMod;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class WaypointRenderer {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static final List<Waypoint> followWaypoints = new ArrayList<>();

    public static void init(){WorldRenderEvents.END.register(context -> WaypointRenderer.renderInWorld(client.getRenderTickCounter().getLastFrameDuration(), context.matrixStack(), context.camera()));}

    public static void renderWaypoint(MatrixStack matrices, Camera camera, double distance, boolean isPointedAt, String name, double baseX, double baseY, double baseZ, GameProfile ign) {
        name = name + " (" + (int) distance + "m)";
        double maxDistance = client.options.getViewDistance().getValue() * 16.0 * 0.99;
        double adjustedDistance = distance;

        if (distance > maxDistance) {
            baseX = baseX / distance * maxDistance;
            baseY = baseY / distance * maxDistance;
            baseZ = baseZ / distance * maxDistance;
            adjustedDistance = maxDistance;
        }

        float scale = ((float) adjustedDistance * 0.1F + 1.0F) * 0.0266F * (KingdomMod.config.FollowWaypointScale / 100f);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        matrices.translate(baseX, baseY, baseZ);
        matrices.multiply(camera.getRotation());
        matrices.translate(0,0,0.06f);

        matrices.scale(scale, -scale, scale);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        float fade = distance > 5.0 ? 1.0F : (float) distance / 5.0F;
        float width = 9F;
        float height = 9F;
        float r = 1;
        float g = 1;
        float b = 1;
        float opacity = 1;

        Identifier textureId = client.getSkinProvider().getSkinTextures(ign).texture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        RenderSystem.setShaderTexture(0, textureId);

        float u1 = 8f / 64f;
        float u2 = 16f / 64f;
        float v1 = 8f / 64f;
        float v2 = 16f / 64f;

        buffer.vertex(positionMatrix, -width, -height, 0).color(r, g, b, opacity).texture(u1, v1);
        buffer.vertex(positionMatrix, -width, height, 0).color(r, g, b, opacity).texture(u1, v2);
        buffer.vertex(positionMatrix, width, height, 0).color(r, g, b, opacity).texture(u2, v2);
        buffer.vertex(positionMatrix, width, -height, 0).color(r, g, b, opacity).texture(u2, v1);

        float hu1 = 40f / 64f;
        float hu2 = 48f / 64f;
        float hv1 = 8f / 64f;
        float hv2 = 16f / 64f;

        buffer.vertex(positionMatrix, -width, -height, 0).color(r, g, b, opacity).texture(hu1, hv1);
        buffer.vertex(positionMatrix, -width, height, 0).color(r, g, b, opacity).texture(hu1, hv2);
        buffer.vertex(positionMatrix, width, height, 0).color(r, g, b, opacity).texture(hu2, hv2);
        buffer.vertex(positionMatrix, width, -height , 0).color(r, g, b, opacity).texture(hu2, hv1);



        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();

        TextRenderer textRenderer = client.textRenderer;
        positionMatrix = matrices.peek().getPositionMatrix();
        VertexConsumerProvider.Immediate v = client.getBufferBuilders().getEntityVertexConsumers();
        TextRenderer fontRenderer = client.textRenderer;

        if (isPointedAt && fontRenderer != null) {
            byte elevateBy = -19;
            RenderSystem.enablePolygonOffset();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.polygonOffset(1.0F, 11.0F);

            RenderSystem.polygonOffset(1.0F, 9.0F);

            RenderSystem.disablePolygonOffset();
            RenderSystem.depthMask(false);
            int textColor = (int) (255.0F * fade) << 24 | 13421772;
            RenderSystem.disableDepthTest();
            textRenderer.draw(name, (-textRenderer.getWidth(name) / 2f), elevateBy, textColor, false, positionMatrix, v, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, 15728880);
            v.draw();
            RenderSystem.enableDepthTest();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
    }


    public static void renderInWorld(float partialTicks, MatrixStack matrixStack, Camera camera){

        Vec3d camPos = camera.getPos();
        Entity focused = camera.getFocusedEntity();

        synchronized (followWaypoints) {
            for (Waypoint pt : followWaypoints) {
                double x = pt.getX() + 0.5;
                double y = pt.getY() + 0.5;
                double z = pt.getZ() + 0.5;

                double dx = x - camPos.x;
                double dy = y - camPos.y;
                double dz = z - camPos.z;

                double distanceSq = camPos.squaredDistanceTo(x, y, z);

                boolean isPointedAt = isPointedAt(pt.pos(), focused, partialTicks);
                String label = pt.getPlayerName();
                GameProfile gameProfile = pt.player();

                renderWaypoint(matrixStack, camera, Math.sqrt(distanceSq), isPointedAt, label, dx, dy, dz, gameProfile);
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    }

    private static boolean isPointedAt(BlockPos waypoint, Entity cameraEntity, float partialTicks) {
        Vec3d cameraPos = cameraEntity.getCameraPosVec(partialTicks);
        Vec3d toTarget = new Vec3d(waypoint.getX() + 0.5 - cameraPos.x, waypoint.getY() + 1.0 - cameraPos.y, waypoint.getZ() + 0.5 - cameraPos.z).normalize();

        Vec3d lookVec = cameraEntity.getRotationVec(partialTicks).normalize();

        double dot = lookVec.dotProduct(toTarget);

        double angleThreshold = Math.cos(Math.toRadians(10));

        return dot >= angleThreshold;
    }
}

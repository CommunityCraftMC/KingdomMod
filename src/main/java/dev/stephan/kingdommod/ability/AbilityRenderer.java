package dev.stephan.kingdommod.ability;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

public class AbilityRenderer {

    public static void drawRing(MatrixStack matrices, float screenX, float screenY, float radius, float thickness, int color, float percentFilled) {
        float a = (float) (color >> 24 & 255) / 255f;
        float r = (float) (color >> 16 & 255) / 255f;
        float g = (float) (color >> 8 & 255) / 255f;
        float b = (float) (color & 255) / 255f;

        matrices.push();
        matrices.translate(screenX, screenY, 0);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int segments = 90;
        float filledDegrees = 360f * percentFilled;

        for (int i = 0; i < segments; i++) {
            float angleStart = -filledDegrees * i / segments - 90f;
            float angleEnd = -filledDegrees * (i + 1) / segments - 90f;

            double radStart = Math.toRadians(angleStart);
            double radEnd = Math.toRadians(angleEnd);

            float x1 = (float) Math.cos(radStart) * radius;
            float y1 = (float) Math.sin(radStart) * radius;
            float x2 = (float) Math.cos(radEnd) * radius;
            float y2 = (float) Math.sin(radEnd) * radius;

            float x3 = (float) Math.cos(radEnd) * (radius - thickness);
            float y3 = (float) Math.sin(radEnd) * (radius - thickness);
            float x4 = (float) Math.cos(radStart) * (radius - thickness);
            float y4 = (float) Math.sin(radStart) * (radius - thickness);

            buffer.vertex(matrix, x1, y1, 0f).color(r, g, b, a);
            buffer.vertex(matrix, x2, y2, 0f).color(r, g, b, a);
            buffer.vertex(matrix, x3, y3, 0f).color(r, g, b, a);
            buffer.vertex(matrix, x4, y4, 0f).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    public static void drawCooldownCircle(MatrixStack matrices, float screenX, float screenY, float radius, int color, int percentRemaining) {
        float a = (float) (color >> 24 & 255) / 255f;
        float r = (float) (color >> 16 & 255) / 255f;
        float g = (float) (color >> 8 & 255) / 255f;
        float b = (float) (color & 255) / 255f;

        MinecraftClient client = MinecraftClient.getInstance();

        matrices.push();
        matrices.translate(screenX, screenY, 0);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        Tessellator tess = Tessellator.getInstance();

        BufferBuilder buffer = tess.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, 0f, 0f, 0f).color(r, g, b, a);
        for (int i = 0; i <= 360; i++) {
            double rad = Math.toRadians(i);
            float x = (float) (Math.cos(rad) * radius);
            float y = (float) (-Math.sin(rad) * radius);
            buffer.vertex(matrix, x, y, 0f).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

            TextRenderer font = client.textRenderer;
            String text = String.valueOf(percentRemaining);
            int textWidth = font.getWidth(text);
            font.draw(text, -textWidth / 2f, 5, 0xFF000000, false, matrix, client.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880);

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();

            matrices.pop();
        }

    public static void drawAbilityCooldown(DrawContext context, Ability ability, ItemStack stack, int backgroundColor, int ringColor, int progressColor, float circleRadius, float ringRadius, float ringWidth, int percent) {
        if (ability.cooldownTicks <= 0) return;

        AbilityRenderer.drawCooldownCircle(context.getMatrices(), ability.hudX, ability.hudY, circleRadius, backgroundColor, percent);
        AbilityRenderer.drawRing(context.getMatrices(), ability.hudX, ability.hudY, ringRadius, ringWidth, ringColor, 1f);
        AbilityRenderer.drawRing(context.getMatrices(), ability.hudX, ability.hudY, ringRadius, ringWidth, progressColor, percent / (ability.maxCooldownTicks / 20f));
        context.drawItem(stack, ability.hudX - 8, ability.hudY - 12);
    }

}

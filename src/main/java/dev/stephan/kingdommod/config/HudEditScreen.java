package dev.stephan.kingdommod.config;

import com.mojang.blaze3d.systems.RenderSystem;


import dev.stephan.kingdommod.KingdomMod;
import dev.stephan.kingdommod.ability.Ability;
import dev.stephan.kingdommod.ability.AbilityManager;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.awt.*;

public class HudEditScreen extends Screen {

    private static final int CIRCLE_RADIUS = 16;
    private Ability draggingAbility = null;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditScreen() {
        super(Text.of("Edit HUD"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        MatrixStack matrices = context.getMatrices();

        int lightGray = new Color(220, 220, 220, 120).getRGB();
        int darkGray  = new Color(80, 80, 80, 120).getRGB();

        for (Ability ability : AbilityManager.ABILITIES) {
            int x = ability.hudX;
            int y = ability.hudY;

            drawCooldownCircle(matrices, x, y, CIRCLE_RADIUS, darkGray);
            drawCooldownCircle(matrices, x, y, CIRCLE_RADIUS - 2, lightGray);

            context.drawItem(ability.hudItem, x - 8, y - 12);

            String previewText = String.valueOf(ability.maxCooldownTicks / 20);
            int textWidth = textRenderer.getWidth(previewText);
            context.drawText(
                    textRenderer,
                    previewText,
                    x - textWidth / 2,
                    y - textRenderer.fontHeight / 2 + 9,
                    Color.BLACK.getRGB(),
                    false
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        for (Ability ability : AbilityManager.ABILITIES) {
            if (isMouseOverCircle(mouseX, mouseY, ability.hudX, ability.hudY)) {
                draggingAbility = ability;
                dragOffsetX = (int) (mouseX - ability.hudX);
                dragOffsetY = (int) (mouseY - ability.hudY);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingAbility != null) {

            KingdomMod.config.setAbilityHud(
                    draggingAbility.id,
                    draggingAbility.hudX,
                    draggingAbility.hudY
            );

            AutoConfig.getConfigHolder(KingdomModConfig.class).save();
        }

        draggingAbility = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }



    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingAbility == null) return super.mouseDragged(mouseX, mouseY, button, dx, dy);

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        int newX = (int) (mouseX - dragOffsetX);
        int newY = (int) (mouseY - dragOffsetY);

        newX = Math.max(CIRCLE_RADIUS, Math.min(newX, screenW - CIRCLE_RADIUS));
        newY = Math.max(CIRCLE_RADIUS, Math.min(newY, screenH - CIRCLE_RADIUS));

        draggingAbility.hudX = newX;
        draggingAbility.hudY = newY;

        return true;
    }

    private boolean isMouseOverCircle(double mouseX, double mouseY, int x, int y) {
        double dx = mouseX - x;
        double dy = mouseY - y;
        return dx * dx + dy * dy <= CIRCLE_RADIUS * CIRCLE_RADIUS;
    }

    private void drawCooldownCircle(MatrixStack matrices, float screenX, float screenY, float radius, int color) {
        float a = (color >> 24 & 255) / 255f;
        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color & 255) / 255f;

        matrices.push();
        matrices.translate(screenX, screenY, 0);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.disableDepthTest();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, 0f, 0f, 0f).color(r, g, b, a);
        for (int i = 0; i <= 360; i++) {
            double rad = Math.toRadians(i);
            buffer.vertex(
                    matrix,
                    (float) Math.sin(rad) * radius,
                    (float) Math.cos(rad) * radius,
                    0f
            ).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        matrices.pop();
    }
}

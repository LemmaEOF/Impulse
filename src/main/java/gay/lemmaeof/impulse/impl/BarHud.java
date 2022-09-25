package gay.lemmaeof.impulse.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import gay.lemmaeof.impulse.ImpulseConfig;
import gay.lemmaeof.impulse.api.ResourceBar;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BarHud implements HudRenderCallback {

	private static final Identifier BAR_TEX = new Identifier("impulse", "textures/gui/bars.png");
	private static final int FULL_BAR_WIDTH = 98;
	private final MinecraftClient client = MinecraftClient.getInstance();

	public static final Object2FloatMap<ResourceBar> bars = new Object2FloatArrayMap<>();

	@Override
	public void onHudRender(MatrixStack matrices, float tickDelta) {
		if (client.options.hudHidden || client.player == null) return;
		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		//coords for each bar
		int left = ImpulseConfig.barsX;
		int top = ImpulseConfig.barsY;

		for (ResourceBar bar : bars.keySet()) {

			float alpha = 1f;

			if (!bar.isBarVisible(client.player)) {
				float fadeTime = bars.getFloat(bar);
				if (fadeTime >= bar.getBarFadeoutTime(client.player) || fadeTime < 0) continue;
				alpha = 1 - (fadeTime / bar.getBarFadeoutTime(client.player));
				bars.put(bar, fadeTime + (tickDelta / 20f));
			} else {
				if (bars.getFloat(bar) != 0) bars.put(bar, 0f);
			}

			int rowsUsed = drawBar(matrices, left, top, bar, alpha);

			// Increment
			top += (16 + (6 * (rowsUsed - 1)));
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}

		RenderSystem.disableBlend();
		RenderSystem.disableTexture();
	}

	private int drawBar(MatrixStack matrices, int left, int top, ResourceBar bar, float alpha) {
		int color = bar.getBarColor(client.player);

		//draw icon
		Identifier icon = bar.getIconId();
		RenderSystem.setShaderTexture(0, icon);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		blit(left, top, 13, 13);

		left += 14;

		//draw bar
		float r = (color >> 16 & 255) / 255f;
		float g = (color >> 8 & 255) / 255f;
		float b = (color & 255) / 255f;
		RenderSystem.setShaderTexture(0, BAR_TEX);

		int rows = 1;

		float totalFill = bar.getCurrentBarFill(client.player);
		int boxes = bar.getTotalSegments(client.player) - 1;

		if (!ImpulseConfig.bigResourceBars && bar.getBarStyle(client.player) == ResourceBar.BarStyle.SEGMENTED) {

			rows += (Math.min(boxes, 35) / 12);

			int fullBoxes = (int) totalFill;
			float currentBarFill = totalFill - fullBoxes;
			if (fullBoxes > boxes) {
				fullBoxes = boxes;
				if (currentBarFill == 0f) currentBarFill = 1f;
			}

			boolean needsPlus = boxes > 36;
			boolean plusOn = fullBoxes > 36;

			int barWidth = fullBoxes == boxes? FULL_BAR_WIDTH : (int) (bar.getTopBarPercentage(client.player) * FULL_BAR_WIDTH);
			if (barWidth < 1) barWidth = 1; //Never display a bar with length 0

			int fgWidth = (int) (currentBarFill * FULL_BAR_WIDTH);
			if (fgWidth > barWidth) fgWidth = barWidth; //Never display a bar fuller than the current max bar width
			if (currentBarFill > 0 && fgWidth <= 0) fgWidth = 1; //Never display an empty bar for *some* resource

			//bar BG: left edge, middle, right edge
			blit(left, top, 1, 7, 0, 0);
			blit(left + 1, top, barWidth, 7, 1, 0);
			blit(left + barWidth + 1, top, 1, 7, 99, 0);

			if (boxes > 0) {
				int boxesLeft = boxes;
				int newTop = top + 6;
				for (int i = 0; i < rows; i++) {
					int toDraw = 12;
					if (boxesLeft > 12) {
						boxesLeft -= 12;
					} else {
						toDraw = boxesLeft;
					}
					//first box
					blit(left, newTop, 9, 7, 0, 7);
					int newLeft = left + 8;
					//the rest of the boxes
					for (int j = 1; j < toDraw; j++) {
						if (j == toDraw - 1) {
							blit(newLeft, newTop, 9, 7, 18, 7);
						} else {
							blit(newLeft, newTop, 9, 7, 9, 7);
						}
						newLeft += 8;
					}
					if (needsPlus) {
						if (i < 2) {
							blit(newLeft, newTop, 4, 7, 27, 7);
						} else {
							blit(newLeft, newTop, 7, 7, 31, 7);
						}
					}
					newTop += 6;
				}
			}

			if (!ImpulseConfig.disableResourceColors) RenderSystem.setShaderColor(r, g, b, alpha);
			//bar FG: left edge, middle, right edge
			blit(left, top, 1, 7, 0, 16);
			blit(left + 1, top, fgWidth, 7, 1, 16);
			blit(left + fgWidth + 1, top, 1, 7, 99, 16);
			if (fullBoxes > 0) {
				int boxesLeft = fullBoxes;
				int newTop = top + 6;
				for (int i = 0; i < rows; i++) {
					int toDraw = 12;
					if (boxesLeft > 12) {
						boxesLeft -= 12;
					} else {
						toDraw = boxesLeft;
						i = rows;
					}

					//first box
					blit(left, newTop, 9, 7, 0, 23);
					int newLeft = left + 8;
					//the rest of the boxes
					for (int j = 1; j < toDraw; j++) {
						if (j == toDraw - 1) {
							blit(newLeft, newTop, 9, 7, 18, 23);
						} else {
							blit(newLeft, newTop, 9, 7, 9, 23);
						}
						newLeft += 8;
					}
					if (plusOn) {
						if (i < 2) {
							blit(newLeft, newTop, 4, 7, 27, 23);
						} else {
							blit(newLeft, newTop, 7, 7, 31, 23);
						}
					}
					newTop += 6;
				}
			}
		} else {
			//bar BG: left edge, middle, right edge
			blit(left, top, 1, 13, 0, 32);
			blit(left + 1, top, 98, 9, 1, 32);
			blit(left + 99, top, 1, 9, 99, 32);

			if (!ImpulseConfig.disableResourceColors) RenderSystem.setShaderColor(r, g, b, alpha);
			int fgWidth = (int) ((totalFill / (float) boxes) * FULL_BAR_WIDTH);
			if (totalFill > 0 && fgWidth <= 0) fgWidth = 1; //never display an empty bar for *some* resource
			//bar FG: left edge, middle, right edge
			blit(left, top, 1, 13, 0, 48);
			blit(left + 1, top, fgWidth, 13, 1, 48);
			blit(left + fgWidth + 1, top, 1, 13, 99, 48);
		}

		if (bar.isVarValueVisible(client.player)) {
			MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, "" + bar.getBarValue(client.player), left + 102, top + 2, 0xFFFFFF);
		}

		return rows;
	}

	private static void blit(int x, int y, int width, int height) {
		blit(x, y, width, height, 0f, 0f, 1f, 1f);
	}

	private static void blit(int x, int y, int width, int height, int u, int v) {
		blit(x, y, width, height, texUV(u), texUV(v), texUV(u+width), texUV(v+height));
	}

	private static void blit(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
		innerBlit(x, y, x+width, y+height, 0d, u1, v1, u2, v2);
	}

	private static void innerBlit(double x1, double y1, double x2, double y2, double z, float u1, float v1, float u2, float v2) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBufferBuilder();
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		buffer.vertex(x1, y2, z).uv(u1, v2).next();
		buffer.vertex(x2, y2, z).uv(u2, v2).next();
		buffer.vertex(x2, y1, z).uv(u2, v1).next();
		buffer.vertex(x1, y1, z).uv(u1, v1).next();
		BufferRenderer.drawWithShader(buffer.end());
	}

	private static float texUV(int orig) {
		return ((float)orig) / 256f;
	}
}

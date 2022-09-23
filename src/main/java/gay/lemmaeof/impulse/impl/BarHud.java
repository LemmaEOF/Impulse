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
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BarHud extends DrawableHelper implements HudRenderCallback {

	private static final Identifier BAR_TEX = new Identifier("impulse", "textures/gui/bars.png");
	//TODO: might be a good idea to embiggen bars, these are kinda dinky especially on small gui scales
	private static final int FULL_BAR_WIDTH = 62;
	private final MinecraftClient client = MinecraftClient.getInstance();
	private static final float SCALE_FACTOR = 1.5f;

	public static final Object2FloatMap<ResourceBar> bars = new Object2FloatArrayMap<>();

	@Override
	public void onHudRender(MatrixStack matrices, float tickDelta) {
		if (client.options.hudHidden || client.player == null) return;
		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		RenderSystem.enableAlphaTest();

		//coords for each bar
		int left = ImpulseConfig.barsX;
		int top = ImpulseConfig.barsY;

		for (ResourceBar bar : bars.keySet()) {

			float alpha = 1f;

			if (!bar.isBarVisible(client.player)) {
				float fadeTime = bars.getFloat(bar);
				if (fadeTime >= bar.getBarFadeoutTime(client.player) || fadeTime == -1f) continue;
				alpha = 1 - (fadeTime / bar.getBarFadeoutTime(client.player));
				bars.put(bar, fadeTime + (tickDelta / 20f));
			} else {
				if (bars.getFloat(bar) != 0) bars.put(bar, 0f);
			}

			int rowsUsed = drawBar(matrices, left, top, bar, alpha);

			// Increment
			top += (12 + (4 * (rowsUsed - 1)));
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}

//		RenderSystem.disableAlphaTest();
		RenderSystem.disableBlend();
		RenderSystem.disableTexture();
	}

	private int drawBar(MatrixStack matrices, int left, int top, ResourceBar bar, float alpha) {
		int color = bar.getBarColor(client.player);

		//draw icon
		Identifier icon = bar.getIconId();
		RenderSystem.setShaderTexture(0, icon);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		blit(left, top, 9, 9);

		left += 10;

		//draw bar
		float r = (color >> 16 & 255) / 255f;
		float g = (color >> 8 & 255) / 255f;
		float b = (color & 255) / 255f;
		RenderSystem.setShaderTexture(0, BAR_TEX);

		int rows = 1;

		float totalFill = bar.getCurrentBarFill(client.player);
		int boxes = bar.getTotalSegments(client.player);

		if (!ImpulseConfig.bigResourceBars && bar.getBarStyle(client.player) == ResourceBar.BarStyle.SEGMENTED) {

			rows += (Math.min(boxes, 35) / 12);

			int fullBoxes = (int) totalFill;
			float currentBarFill = totalFill - fullBoxes;
			if (fullBoxes > boxes) fullBoxes = boxes;

			boolean needsPlus = boxes > 36;
			boolean plusOn = fullBoxes > 36;

			int barWidth = (int) (bar.getTopBarPercentage(client.player) * FULL_BAR_WIDTH);
			if (barWidth < 1) barWidth = 1; //Never display a bar with length 0

			int fgWidth = (int) (currentBarFill * FULL_BAR_WIDTH);
			if (currentBarFill > 0 && fgWidth <= 0) fgWidth = 1; //never display an empty bar for *some* health

			//bar BG: left edge, middle, right edge
			blit(left, top, 1, 5, texUV(0), texUV(0), texUV(1), texUV(5));
			blit(left + 1, top, barWidth, 5, texUV(1), texUV(0), texUV(barWidth + 1), texUV(5));
			blit(left + barWidth + 1, top, 1, 5, texUV(63), texUV(0), texUV(64), texUV(5));

			if (boxes > 0) {
				int boxesLeft = boxes;
				int newTop = top + 4;
				for (int i = 0; i < rows; i++) {
					int toDraw = 12;
					if (boxesLeft > 12) {
						boxesLeft -= 12;
					} else {
						toDraw = boxesLeft;
					}
					//first box
					blit(left, newTop, 6, 5, texUV(0), texUV(5), texUV(6), texUV(10));
					int newLeft = left + 5;
					//the rest of the boxes
					for (int j = 1; j < toDraw; j++) {
						blit(newLeft, newTop, 6, 5, texUV(6), texUV(5), texUV(12), texUV(10));
						newLeft += 5;
					}
					if (needsPlus) {
						if (i < 2) {
							blit(newLeft, newTop, 3, 5, texUV(19), texUV(5), texUV(22), texUV(10));
						} else {
							blit(newLeft, newTop, 5, 5, texUV(22), texUV(5), texUV(27), texUV(10));
						}
					}
					newTop += 4;
				}
			}

			if (!ImpulseConfig.disableResourceColors) RenderSystem.setShaderColor(r, g, b, alpha);
			//bar FG: left edge, middle, right edge
			blit(left, top, 1, 5, texUV(0), texUV(10), texUV(1), texUV(15));
			blit(left + 1, top, fgWidth, 5, texUV(1), texUV(10), texUV(fgWidth + 1), texUV(15));
			blit(left + fgWidth + 1, top, 1, 5, texUV(63), texUV(10), texUV(64), texUV(15));
			if (fullBoxes > 0) {
				int boxesLeft = fullBoxes;
				int newTop = top + 4;
				for (int i = 0; i < rows; i++) {
					int toDraw = 12;
					if (boxesLeft > 12) {
						boxesLeft -= 12;
					} else {
						toDraw = boxesLeft;
						i = rows;
					}

					//first box
					blit(left, newTop, 6, 5, texUV(0), texUV(15), texUV(6), texUV(20));
					int newLeft = left + 5;
					//the rest of the boxes
					for (int j = 1; j < toDraw; j++) {
						blit(newLeft, newTop, 6, 5, texUV(6), texUV(15), texUV(12), texUV(20));
						newLeft += 5;
					}
					if (plusOn) {
						if (i < 2) {
							blit(newLeft, newTop, 3, 5, texUV(19), texUV(15), texUV(22), texUV(20));
						} else {
							blit(newLeft, newTop, 5, 5, texUV(22), texUV(15), texUV(27), texUV(20));
						}
					}
					newTop += 4;
				}
			}
		} else {
//			rows = 2;
			//bar BG: left edge, middle, right edge
			blit(left, top, 1, 9, texUV(0), texUV(20), texUV(1), texUV(29));
			blit(left + 1, top, 62, 9, texUV(1), texUV(20), texUV(63), texUV(29));
			blit(left + 63, top, 1, 9, texUV(63), texUV(20), texUV(64), texUV(29));

			if (!ImpulseConfig.disableResourceColors) RenderSystem.setShaderColor(r, g, b, alpha);
			int fgWidth = (int) ((totalFill / (float) boxes) * FULL_BAR_WIDTH);
			if (totalFill > 0 && fgWidth <= 0) fgWidth = 1; //never display an empty bar for *some* resource
			//bar FG: left edge, middle, right edge
			blit(left, top, 1, 9, texUV(0), texUV(29), texUV(1), texUV(38));
			blit(left + 1, top, fgWidth, 9, texUV(1), texUV(29), texUV(fgWidth + 1), texUV(38));
			blit(left + fgWidth + 1, top, 1, 9, texUV(63), texUV(29), texUV(64), texUV(38));
		}

		if (bar.isVarValueVisible(client.player)) {
			MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, "" + bar.getBarValue(client.player), left + 66, top, 0xFFFFFF);
		}

		return rows;
	}

	private static void blit(int x, int y, int width, int height) {
		blit(x, y, width, height, 0f, 0f, 1f, 1f);
	}

	private static void blit(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
		innerBlit(x, y, x+width, y+height, 0d, u1, v1, u2, v2);
	}

	private static void innerBlit(double x1, double y1, double x2, double y2, double z, float u1, float v1, float u2, float v2) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBufferBuilder();
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		buffer.vertex(x1*SCALE_FACTOR, y2*SCALE_FACTOR, z).uv(u1, v2).next();
		buffer.vertex(x2*SCALE_FACTOR, y2*SCALE_FACTOR, z).uv(u2, v2).next();
		buffer.vertex(x2*SCALE_FACTOR, y1*SCALE_FACTOR, z).uv(u2, v1).next();
		buffer.vertex(x1*SCALE_FACTOR, y1*SCALE_FACTOR, z).uv(u1, v1).next();
		BufferRenderer.drawWithShader(buffer.end());
	}

	private static float texUV(int orig) {
		return ((float)orig) / 256f;
	}
}

package gay.lemmaeof.impulse;

import gay.lemmaeof.impulse.api.ResourceBar;
import gay.lemmaeof.impulse.api.ResourceBars;
import gay.lemmaeof.impulse.impl.BarHud;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class ImpulseClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		HudRenderCallback.EVENT.register(new BarHud());
		/*ResourceBars.addBar(new ResourceBar() { //for testing, TODO spin out into a testmod later
			@Override
			public Identifier getIconId() {
				return new Identifier("minecraft", "textures/particle/bubble.png");
			}

			@Override
			public float getCurrentBarFill(ClientPlayerEntity player) {
				return player.experienceLevel + player.experienceProgress;
			}

			@Override
			public float getTopBarPercentage(ClientPlayerEntity player) {
				return 1;
			}

			@Override
			public int getTotalSegments(ClientPlayerEntity player) {
				return 40;
			}

			@Override
			public int getBarColor(ClientPlayerEntity player) {
				return 0x80FF20;
			}

			@Override
			public boolean isBarVisible(ClientPlayerEntity player) {
				return player.experienceProgress > 0 || player.experienceLevel > 0;
			}

			@Override
			public int getBarValue(ClientPlayerEntity player) {
				return player.totalExperience;
			}

			@Override
			public float getBarFadeoutTime(ClientPlayerEntity player) {
				return 0.5f;
			}

			@Override
			public BarStyle getBarStyle(ClientPlayerEntity player) {
				return BarStyle.SEGMENTED;
			}
		});*/
	}
}

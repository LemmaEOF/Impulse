package gay.lemmaeof.impulse.api;

import gay.lemmaeof.impulse.impl.BarHud;

public class ResourceBars {
	public static void addBar(ResourceBar bar) {
		BarHud.bars.put(bar, -1f);
	}
}

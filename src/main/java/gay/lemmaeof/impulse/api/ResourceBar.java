package gay.lemmaeof.impulse.api;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

public interface ResourceBar {
	/**
	 * @return The resource ID of the icon to use for this bar.
	 */
	Identifier getIconId();

	/**
	 * @param player The player seeing this bar.
	 * @return How many bar segments are filled - 1.0 equals 1 full bar segment.
	 */
	float getCurrentBarFill(ClientPlayerEntity player);

	/**
	 * @param player The player seeing this bar.
	 * @return The length the bar should be when all boxes are full, in case it's not enough resources for another full bar.
	 */
	float getTopBarPercentage(ClientPlayerEntity player);

	/**
	 * @param player The player seeing this bar.
	 * @return How many total segments the bar should have, including empty segments and the final bar above the boxes.
	 */
	int getTotalSegments(ClientPlayerEntity player);

	/**
	 * @param player The player seeing this bar.
	 * @return What color the bar should display as.
	 */
	int getBarColor(ClientPlayerEntity player);

	/**
	 * @param player The player seeing this bar.
	 * @return Whether the bar should be visible. Bar will fade out once this is set false.
	 */
	boolean isBarVisible(ClientPlayerEntity player);

	/**
	 * @param player The player seeing this bar.
	 * @return The numerical value to display to the right of the bar.
	 */
	int getBarValue(ClientPlayerEntity player);

	/**
	 * @param player The player seeing this bar.
	 * @return Whether the numerical value for the bar should be visible.
	 */
	default boolean isVarValueVisible(ClientPlayerEntity player) {
		return player.isSneaking();
	}

	/**
	 * @param player The player seeing this bar.
	 * @return How long this bar should take to fade out, in seconds.
	 */
	float getBarFadeoutTime(ClientPlayerEntity player);

	/**
	 * @param player The player seeing this bar.
	 * @return The style of bar to use. This can be overridden by the player using accessibility config.
	 */
	BarStyle getBarStyle(ClientPlayerEntity player);

	enum BarStyle {
		/**
		 * A bar with multiple segments, similar to the Kingdom Hearts enemy health bar style.
		 */
		SEGMENTED,
		/**
		 * A single, unsegmented bar.
		 */
		SINGLE
	}
}

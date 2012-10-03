package es.odracirnumira.npuzzle.util;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Class with utility functions regarding the UI.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class UIUtilities {
	/**
	 * Forces a ListView to have a height that matches that of all of its children, even if the
	 * ListView is within a ScrollView.
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		int desiredWidth = MeasureSpec
				.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	/**
	 * Forces a ListView to have a height that matches that of the first <code>numItems</code>
	 * items, even if the ListView is within a ScrollView. <code>numItems</code> can be greater than
	 * the number of elements in the list.
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView, int numItems) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		int desiredWidth = MeasureSpec
				.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
		for (int i = 0; i < listAdapter.getCount() && i < numItems; i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	/**
	 * This method convets dp unit to equivalent device specific value in pixels.
	 * 
	 * <b>Note</b>: this method was borrowed from StackOverflow.
	 * 
	 * @param dp
	 *            A value in dp(Device independent pixels) unit. Which we need to convert into
	 *            pixels
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent Pixels equivalent to dp according to device
	 */
	public static float convertDpToPixel(float dp, Context context) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources()
				.getDisplayMetrics());
	}

	/**
	 * This method converts device specific pixels to device independent pixels.
	 * 
	 * <b>Note</b>: this method was borrowed from StackOverflow.
	 * 
	 * @param px
	 *            A value in px (pixels) unit. Which we need to convert into db
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent db equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources()
				.getDisplayMetrics());
	}

	/**
	 * Given a measure specifications value (as in {@link View#measure(int, int)}, this method
	 * returns a String representation of its mode.
	 * 
	 * @param measureSpec
	 *            the measure specifications.
	 * @return a String representation of the specifications' mode.
	 */
	public static String measureSpecificationsToMode(int measureSpec) {
		int mode = MeasureSpec.getMode(measureSpec);

		switch (mode) {
			case MeasureSpec.AT_MOST:
				return "AT_MOST";
			case MeasureSpec.EXACTLY:
				return "EXACTLY";
			case MeasureSpec.UNSPECIFIED:
				return "UNSPECIFIED";
		}

		throw new IllegalArgumentException("Invalid measure specifications");
	}

	/**
	 * Given an amount of time in seconds, this method returns the same amount of time but in hours,
	 * minutes and seconds. The result is an array whose first element is the number of hours, the
	 * second element is the number of minutes, and the third element is the number of seconds.
	 * <p>
	 * For instance, 121 seconds would translate into 2 minutes and 1 second.
	 * 
	 * @param seconds
	 *            the number of seconds to convert.
	 * @return the hours, minutes and seconds of the specified amount of time.
	 */
	public static long[] timeToHourMinSec(long seconds) {
		long[] result = new long[3];

		// Hours
		result[0] = seconds / 3600;

		// Minutes
		result[1] = (seconds % 3600) / 60;

		// Seconds
		result[2] = (seconds % 3600) % 60;

		return result;
	}

	/**
	 * Given an amount of time in seconds, this method returns a String representation of the same
	 * amount of time but in hours, minutes and seconds, with format "HH:MM:SS".
	 * <p>
	 * For instance, 121 seconds would translate into the String "02:01"
	 * 
	 * @param seconds
	 *            the number of seconds to convert.
	 * @return a String representation of <code>seconds</code> with the format "HH:MM:SS".
	 */
	public static String timeToHourMinSecChrono(long seconds) {
		long[] hms = timeToHourMinSec(seconds);

		StringBuilder result = new StringBuilder();

		if (hms[0] != 0) {
			result.append(String.format("%02d", hms[0])).append(':');
		}

		if (hms[0] != 0 || hms[1] != 0) {
			result.append(String.format("%02d", hms[1])).append(':');
		}

		result.append(String.format("%02d", hms[2]));

		return result.toString();
	}

	private UIUtilities() {
	}
}

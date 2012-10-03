package es.odracirnumira.npuzzle.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class that manages the application's settings. It is recommended that all the settings be
 * accessed from this class.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NPuzzleSettings {
	/**
	 * The application-level {@link SharedPreferences} object used to manage the settings. It is
	 * initialized from the context that is passed at construction time.
	 */
	private SharedPreferences globalSharedPreferences;

	/**
	 * Key for the preference that stores the location of the images that are used for the N
	 * puzzles. This preference can have the value {@link #IMAGES_LOCATION_GALLERY} or it can be an
	 * absolute path to a directory in the file system.
	 */
	public static final String NPUZZLE_IMAGES_LOCATION = "nPuzzleImagesLocation";

	/**
	 * Value that the {@link #NPUZZLE_IMAGES_LOCATION} preference can have. If such preference has
	 * this value, it means that the images used for the puzzles are retrieved from the gallery of
	 * images.
	 */
	public static final String IMAGES_LOCATION_GALLERY = "Gallery";

	/**
	 * Value that the {@link #NPUZZLE_IMAGES_LOCATION} preference can have. If such preference has
	 * this value, it means that no image from the file system is displayed by the puzzle. Instead,
	 * a default image is displayed: a board with numbered tiles.
	 */
	public static final String IMAGES_LOCATION_NO_IMAGE = "NoImage";

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            the context to access the settings.
	 */
	public NPuzzleSettings(Context context) {
		this.globalSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Returns the directory that contains the images used for the N puzzles. This can be
	 * {@link #IMAGES_LOCATION_GALLERY}, in which case images are retrieved from the device's images
	 * gallery, {@link #IMAGES_LOCATION_NO_IMAGE}, in which case no image will be used for the
	 * puzzle, or an absolute path, in which case images are retrieved from that very specific
	 * directory.
	 * <p>
	 * This setting defaults to {@link #IMAGES_LOCATION_GALLERY} in case it has not been defined.
	 * 
	 * @return the location for puzzle images.
	 */
	public String getNPuzzleImagesLocation() {
		return this.globalSharedPreferences.getString(NPUZZLE_IMAGES_LOCATION,
				IMAGES_LOCATION_GALLERY);
	}

	/**
	 * Sets the directory that contains the images used for the N puzzles. This can be
	 * {@link #IMAGES_LOCATION_GALLERY}, in which case images are retrieved from the device's images
	 * gallery, {@link #IMAGES_LOCATION_NO_IMAGE}, in which case no image will be used for the
	 * puzzle, or an absolute path, in which case images are retrieved from that very specific
	 * directory.
	 * 
	 * @param location
	 *            the location to use.
	 */
	public void setNPuzzleImagesLocation(String location) {
		if (location == null) {
			throw new IllegalArgumentException("null location");
		}

		this.globalSharedPreferences.edit().putString(NPUZZLE_IMAGES_LOCATION, location).apply();
	}
}

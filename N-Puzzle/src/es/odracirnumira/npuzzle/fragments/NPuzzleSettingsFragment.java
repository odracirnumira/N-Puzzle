package es.odracirnumira.npuzzle.fragments;

import java.util.ArrayList;

import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.activities.FileChooserActivity;
import es.odracirnumira.npuzzle.activities.NPuzzleSettingsActivity;
import es.odracirnumira.npuzzle.fragments.dialogs.ImagesLocationDialogFragment;
import es.odracirnumira.npuzzle.fragments.dialogs.ImagesLocationDialogFragment.ISelectImagesLocationListener;
import es.odracirnumira.npuzzle.settings.NPuzzleSettings;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * The fragment that lets the user manage the preferences of the application. This is used by
 * {@link NPuzzleSettingsActivity}.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NPuzzleSettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener, ISelectImagesLocationListener {
	/**
	 * Request code used for selecting the images directory.
	 */
	private static final int REQUEST_CODE_SELECT_IMAGES_DIRECTORY = 0;

	/**
	 * Object used to manage the images location setting.
	 */
	private NPuzzleSettings settings;

	/**
	 * Preference used to set the location of the images used by the puzzles.
	 */
	private Preference imagesLocationPreference;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.settings);
		this.settings = new NPuzzleSettings(getActivity());

		/*
		 * The image location settings is implemented the old way. We display a pop up dialog with
		 * two options:
		 * 
		 * - Images from gallery.
		 * 
		 * - Images from specific directory.
		 */
		this.imagesLocationPreference = findPreference(NPuzzleSettings.NPUZZLE_IMAGES_LOCATION);
		this.imagesLocationPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				showImagesLocationDialog();
				return true;
			}
		});
	}

	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		this.setInitialPreferencesSummaries();
	}

	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
				this);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CODE_SELECT_IMAGES_DIRECTORY:
				/*
				 * Set the selected directory as the location for puzzle images.
				 */
				if (resultCode == FileChooserActivity.RESULT_OK) {
					ArrayList<String> directory = data
							.getStringArrayListExtra(FileChooserActivity.KEY_RESULT_SELECTED_FILES);

					settings.setNPuzzleImagesLocation(directory.get(0));
				}

				return;
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		/*
		 * Update summary values for preferences.
		 */
		if (key.equals(NPuzzleSettings.NPUZZLE_IMAGES_LOCATION)) {
			String directory = settings.getNPuzzleImagesLocation();
			if (directory.equals(NPuzzleSettings.IMAGES_LOCATION_NO_IMAGE)) {
				imagesLocationPreference
						.setSummary(getString(R.string.no_image_selected_for_puzzle_images));
			} else if (directory.equals(NPuzzleSettings.IMAGES_LOCATION_GALLERY)) {
				imagesLocationPreference
						.setSummary(getString(R.string.gallery_selected_for_puzzle_images));
			} else {
				imagesLocationPreference.setSummary(getString(
						R.string.custom_directory_selected_for_puzzle_images, directory));
			}
		}
	}

	/**
	 * Sets the summaries for all the preferences.
	 */
	private void setInitialPreferencesSummaries() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		onSharedPreferenceChanged(sp, NPuzzleSettings.NPUZZLE_IMAGES_LOCATION);
	}

	/**
	 * Shows the images location dialog.
	 */
	private void showImagesLocationDialog() {
		FragmentManager manager = getFragmentManager();
		ImagesLocationDialogFragment dialog = new ImagesLocationDialogFragment();
		dialog.setTargetFragment(this, 0);
		dialog.show(manager, "imagesLocationDialog");
	}

	/**
	 * Called when a location for puzzle images has been selected.
	 */
	public void locationSelected(String location) {
		if (location != null) {
			settings.setNPuzzleImagesLocation(location);
		}
	}
}

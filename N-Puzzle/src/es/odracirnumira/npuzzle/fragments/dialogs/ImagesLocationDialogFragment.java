package es.odracirnumira.npuzzle.fragments.dialogs;

import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.activities.FileChooserActivity;
import es.odracirnumira.npuzzle.fragments.NPuzzleSettingsFragment;
import es.odracirnumira.npuzzle.settings.NPuzzleSettings;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * DialogFragment that shows a set of possible locations for the images to be used for puzzles. This
 * fragment is intended to be used only from other fragments. In particular, the target fragment of
 * this fragment must implement the {@link ImagesLocationDialogFragment.ISelectImagesLocationListener}
 * interface, so the target fragment gets reported when a location for the images has been selected.
 * <p>
 * This fragment is used from the {@link NPuzzleSettingsFragment}.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ImagesLocationDialogFragment extends DialogFragment {
	/**
	 * Request code used for selecting the images directory.
	 */
	private static final int REQUEST_CODE_SELECT_IMAGES_DIRECTORY = 0;

	/**
	 * The settings object used to manage settings.
	 */
	private NPuzzleSettings settings;

	/**
	 * Interface that the target fragment for this fragment must implement. This is used to notify
	 * the target fragment when an image location has been selected.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface ISelectImagesLocationListener {
		/**
		 * Called when a new location has been selected.
		 * 
		 * @param location
		 *            the new selected location. May be
		 *            {@link NPuzzleSettings#IMAGES_LOCATION_GALLERY},
		 *            {@link NPuzzleSettings#IMAGES_LOCATION_NO_IMAGE}, an absolute path, or a null
		 *            value (to indicate that no location was selected).
		 */
		public void locationSelected(String location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.settings = new NPuzzleSettings(getActivity());

		/*
		 * Check that there is a target fragment and that it implements the appropriate interface.
		 */
		if (!(this.getTargetFragment() instanceof ISelectImagesLocationListener)) {
			throw new ClassCastException("The target fragment must implement ISelectImagesLocation");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		CharSequence[] choices = getResources().getStringArray(R.array.imagesLocationOptions);

		final String currentChoice = settings.getNPuzzleImagesLocation();

		int currentChoiceIndex;

		if (currentChoice.equals(NPuzzleSettings.IMAGES_LOCATION_NO_IMAGE)) {
			currentChoiceIndex = 0;
		} else if (currentChoice.equals(NPuzzleSettings.IMAGES_LOCATION_GALLERY)) {
			currentChoiceIndex = 1;
		} else {
			currentChoiceIndex = 2;
			choices[2] = choices[2] + " (" + currentChoice + ")";
		}

		return new AlertDialog.Builder(getActivity())
				.setSingleChoiceItems(choices, currentChoiceIndex, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								// No image option has been selected
								// If the gallery has been selected
								((ISelectImagesLocationListener) getTargetFragment())
										.locationSelected(NPuzzleSettings.IMAGES_LOCATION_NO_IMAGE);
								dismiss();
								break;
							case 1:
								// If the gallery has been selected
								((ISelectImagesLocationListener) getTargetFragment())
										.locationSelected(NPuzzleSettings.IMAGES_LOCATION_GALLERY);
								dismiss();
								break;
							case 2:
								// Select a specific directory
								Intent intent = new Intent(getActivity(), FileChooserActivity.class);
								intent.putExtra(FileChooserActivity.INPUT_SELECTION_MODE,
										FileChooserActivity.MODE_SELECTION_SINGLE);
								intent.putExtra(FileChooserActivity.INPUT_TYPE_MODE,
										FileChooserActivity.MODE_TYPE_DIRECTORY);

								/*
								 * If currently there is a selected directory, use it as the initial
								 * directory to display.
								 */
								if (!currentChoice.equals(NPuzzleSettings.IMAGES_LOCATION_GALLERY)
										&& !currentChoice
												.equals(NPuzzleSettings.IMAGES_LOCATION_NO_IMAGE)) {
									intent.putExtra(FileChooserActivity.INPUT_INTIAL_DIRECTORY,
											currentChoice);
								}

								startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGES_DIRECTORY);

								break;
						}
					}
				}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				}).setTitle(R.string.select_location_puzzle_images).create();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onActivityResult(int, int, android.content.Intent)
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CODE_SELECT_IMAGES_DIRECTORY:
				/*
				 * Whether a directory has been selected or not from the file chooser, report the
				 * target fragment.
				 */
				if (resultCode == FileChooserActivity.RESULT_OK) {
					((ISelectImagesLocationListener) getTargetFragment()).locationSelected(data
							.getStringArrayListExtra(FileChooserActivity.KEY_RESULT_SELECTED_FILES)
							.get(0));
				} else {
					((ISelectImagesLocationListener) getTargetFragment()).locationSelected(null);
				}

				dismiss();

				break;
		}
	}
}

package es.odracirnumira.npuzzle.fragments.dialogs;

import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.view.CustomDifficultyView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * DialogFragment used to start a new custom N puzzle game. The user is allowed to select the puzzle
 * size. When the size is selected, the {@link INewCustomGameRequestListener#newCustomGame(int)}
 * method is called to report the hosting activity that a new custom game has been requested. Thus,
 * the hosting activity must implement the {@code INewCustomGameRequestListener} interface.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NewCustomGameDialogFragment extends DialogFragment {
	/**
	 * Interface that the hosting activity of this fragment must implement. This interface is used
	 * to notify the activity when the user requests that a new game be started.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface INewCustomGameRequestListener {
		/**
		 * Called when the user requests to start a new game. The {@code gameSize} variable is the
		 * size of the puzzle (the number of tiles of each side of the puzzle. For instance, a value
		 * of 4 means that a 4x4 puzzle is going to be created).
		 * 
		 * @param gameSize
		 *            the size of new game puzzle.
		 */
		public void newCustomGame(int gameSize);
	}

	/**
	 * The view used to select the difficulty level.
	 */
	private CustomDifficultyView difficultyView;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof INewCustomGameRequestListener)) {
			throw new ClassCastException("The hosting activity must implement "
					+ INewCustomGameRequestListener.class.getCanonicalName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			super.onCreate(savedInstanceState.getBundle("superState"));
		} else {
			super.onCreate(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		this.difficultyView = new CustomDifficultyView(getActivity());

		// TODO: make the max difficulty value configurable
		this.difficultyView.setDifficultyRange(2, 60);

		if (savedInstanceState != null) {
			this.difficultyView.setSelectedDifficulty(savedInstanceState
					.getInt("selectedDifficulty"));
		}

		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.puzzle_size)
				.setView(difficultyView)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((INewCustomGameRequestListener) getActivity())
								.newCustomGame(difficultyView.getSelectedDifficulty());
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
	 */
	public void onSaveInstanceState(Bundle outState) {
		Bundle superState = new Bundle();
		super.onSaveInstanceState(superState);
		outState.putBundle("superState", superState);

		outState.putInt("selectedDifficulty", this.difficultyView.getSelectedDifficulty());
	}

}

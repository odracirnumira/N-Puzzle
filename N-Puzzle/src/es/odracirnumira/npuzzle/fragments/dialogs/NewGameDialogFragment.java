package es.odracirnumira.npuzzle.fragments.dialogs;

import es.odracirnumira.npuzzle.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * DialogFragment used to display a list of options to start a new N puzzle game. When an option is
 * selected, the {@link INewGameRequestListener#newGame(int)} method of the hosting activity is called, so
 * the activity can start the new game. Therefore, the hosting activity must implement the
 * {@code INewGameRequestListener} interface.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NewGameDialogFragment extends DialogFragment {
	/**
	 * Interface that the hosting activity of this fragment must implement. This interface is used
	 * to notify the activity when a new game option is selected.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface INewGameRequestListener {
		/**
		 * Called when the user selects a new game option. The {@code gameSize} variable is the size
		 * of the puzzle (the number of tiles of each side of the puzzle. For instance, a value of 4
		 * means that a 4x4 puzzle is going to be created).
		 * <p>
		 * If the user selected to start a custom game, {@code gameSize} will be -1.
		 * 
		 * @param gameSize
		 *            the size of new game puzzle, or -1 if a custom game is requested.
		 */
		public void newGame(int gameSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		/*
		 * Check that the activity implements the required interfaces.
		 */

		if (!(activity instanceof INewGameRequestListener)) {
			throw new ClassCastException("The hosting activity must implement " + INewGameRequestListener.class.getCanonicalName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.new_game_difficulty)
				.setItems(R.array.gameDifficulties, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						int puzzleSize = 0;
						switch (which) {
							case 0:
								// Easy
								puzzleSize = 3;
								break;
							case 1:
								// Normal
								puzzleSize = 4;
								break;
							case 2:
								// Hard
								puzzleSize = 5;
								break;
							case 3:
								/*
								 * Custom difficulty level.
								 */
								puzzleSize = -1;
								break;
						}

						/*
						 * If the user selected an option, report the calling activity.
						 */
						if (puzzleSize != 0) {
							((INewGameRequestListener) getActivity()).newGame(puzzleSize);
						}
					}
				}).create();
	}
}

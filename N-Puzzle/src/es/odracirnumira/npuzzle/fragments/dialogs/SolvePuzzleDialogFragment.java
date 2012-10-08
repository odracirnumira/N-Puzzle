package es.odracirnumira.npuzzle.fragments.dialogs;

import es.odracirnumira.npuzzle.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * DialogFragment used to solve a game. When an option (yes or no) is selected, the
 * {@link ISolvePuzzleListener#solve(boolean)} method of the hosting activity is called. Thus, the
 * hosting activity must implement this interface.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class SolvePuzzleDialogFragment extends DialogFragment {
	/**
	 * Interface that the hosting activity must implement in order to use this fragment. This
	 * interface defines a method that is called when an option about solving a game is chosen.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface ISolvePuzzleListener {
		/**
		 * Called when the user has decided whether to solve the current game or not.
		 * 
		 * @param solve
		 *            true if the user selected to solve the game, and false otherwise.
		 */
		public void solve(boolean solve);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof ISolvePuzzleListener)) {
			throw new ClassCastException("The hosting activity must implement "
					+ ISolvePuzzleListener.class.getCanonicalName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.solve_game_question)
				.setMessage(R.string.solve_game_confirmation)
				.setPositiveButton(android.R.string.yes, new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((ISolvePuzzleListener) getActivity()).solve(true);
					}
				}).setNegativeButton(android.R.string.no, new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((ISolvePuzzleListener) getActivity()).solve(false);
					}
				}).create();
	}
}

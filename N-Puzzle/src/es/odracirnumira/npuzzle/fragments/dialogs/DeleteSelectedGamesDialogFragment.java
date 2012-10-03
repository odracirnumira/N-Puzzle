package es.odracirnumira.npuzzle.fragments.dialogs;

import es.odracirnumira.npuzzle.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * DialogFragment that asks the user if he wants to delete the set of games that the hosting
 * activity currently has in its selection. The hosting activity must implement the
 * {@link IDeleteGameListener}, whose method is called when the used selects whether to delete or
 * not delete the selected games. The dialog needs to be passed as an argument (
 * {@link DialogFragment#setArguments(Bundle)}) an integer that represents the number of selected
 * games, under the key {@link #INPUT_NUMBER_SELECTED_GAMES}.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class DeleteSelectedGamesDialogFragment extends DialogFragment {
	/**
	 * Key for the input argument that specifies the number of selected games.
	 */
	public static final String INPUT_NUMBER_SELECTED_GAMES = "NumSelectedGames";

	/**
	 * Interface that the hosting activity of this fragment must implement. This interface is used
	 * to notify the activity when the user decides to delete a set of selected games.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface IDeleteGameListener {
		/**
		 * Called when the user decides whether to delete the set of selected games or not.
		 * 
		 * @param delete
		 *            true if the user decided to delete the selected games, and false otherwise.
		 */
		public void deleteSelectedGames(boolean delete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof IDeleteGameListener)) {
			throw new ClassCastException("The hosting activity must implement "
					+ IDeleteGameListener.class.getCanonicalName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int numSelectedGames = getArguments().getInt(INPUT_NUMBER_SELECTED_GAMES);

		return new AlertDialog.Builder(getActivity())
				.setMessage(
						getResources().getQuantityString(R.plurals.delete_selected_games,
								numSelectedGames))
				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((IDeleteGameListener) getActivity()).deleteSelectedGames(false);
					}
				}).setPositiveButton(android.R.string.ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((IDeleteGameListener) getActivity()).deleteSelectedGames(true);
					}
				}).show();
	}
}

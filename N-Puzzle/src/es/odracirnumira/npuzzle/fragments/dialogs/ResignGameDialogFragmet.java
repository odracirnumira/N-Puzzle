package es.odracirnumira.npuzzle.fragments.dialogs;

import es.odracirnumira.npuzzle.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * DialogFragment used to resign from a game. When an option (yes or no) is selected, the
 * {@link IResignGameListener#resign(boolean)} method of the hosting activity is called. Thus, the
 * hosting activity must implement this interface.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ResignGameDialogFragmet extends DialogFragment {
	/**
	 * Interface that the hosting activity must implement in order to use this fragment. This
	 * interface defines a method that is called when an option about resigning from a game is
	 * chosen.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface IResignGameListener {
		/**
		 * Called when the user has decided whether to resign from the game or not.
		 * 
		 * @param resigned
		 *            true if the user has decided to resign from the game, and false otherwise.
		 */
		public void resign(boolean resigned);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof IResignGameListener)) {
			throw new ClassCastException("The hosting activity must implement "
					+ IResignGameListener.class.getCanonicalName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.resign_game_question)
				.setMessage(R.string.resign_game_confirmation)
				.setPositiveButton(android.R.string.yes, new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((IResignGameListener) getActivity()).resign(true);
					}
				}).setNegativeButton(android.R.string.no, new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((IResignGameListener) getActivity()).resign(false);
					}
				}).create();
	}
}

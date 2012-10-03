package es.odracirnumira.npuzzle.fragments.dialogs;

import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.model.NPuzzleGameStatistics;
import es.odracirnumira.npuzzle.util.UIUtilities;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Dialog used when an N puzzle game is solved. It presents the user with two options: either stay
 * in the current game even if it has already been solved, or just leave the solved game. The dialog
 * also shows the game statistics ({@link NPuzzleGameStatistics}), which must be passed as an
 * argument ( {@link Fragment#setArguments(Bundle)}) under the key {@link #INPUT_GAME_STATISTICS}.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class GameFinishedDialogFragment extends DialogFragment {
	/**
	 * Key for the input {@link NPuzzleGameStatistics} that the fragment must have.
	 */
	public static final String INPUT_GAME_STATISTICS = "gameStatistics";

	/**
	 * Interface that must implement the hosting activity of this fragment. This interface defines a
	 * method that is called when the user decides whether to stay in the current game after the
	 * game finishes or not.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface IGameFinishedListener {
		/**
		 * Called when the game ends and the user decides whether to stay in it or not.
		 * 
		 * @param stayInGame
		 *            true if the user decided to stay in the current game, and false to leave it.
		 */
		public void stayInGame(boolean stayInGame);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setCancelable(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof IGameFinishedListener)) {
			throw new ClassCastException("The hosting activity must implement "
					+ IGameFinishedListener.class.getCanonicalName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		/*
		 * Fill the statistics.
		 */
		View contentView = getActivity().getLayoutInflater().inflate(R.layout.game_finished_dialog,
				null);

		NPuzzleGameStatistics statistics = (NPuzzleGameStatistics) getArguments().get(
				INPUT_GAME_STATISTICS);

		((TextView) contentView.findViewById(R.id.elapsedTimeTextView)).setText(UIUtilities
				.timeToHourMinSecChrono(statistics.elapsedTime / 1000));
		((TextView) contentView.findViewById(R.id.numMovesTextView)).setText(Integer
				.toString(statistics.numMoves));

		return new AlertDialog.Builder(getActivity()).setTitle(R.string.puzzle_solved)
				.setView(contentView)
				.setPositiveButton(android.R.string.yes, new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((IGameFinishedListener) getActivity()).stayInGame(true);
					}
				}).setNegativeButton(android.R.string.no, new Dialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((IGameFinishedListener) getActivity()).stayInGame(false);
					}
				}).create();
	}
}

package es.odracirnumira.npuzzle.tasks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.widget.Toast;
import es.odracirnumira.npuzzle.NPuzzleApplication;
import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.contentproviders.NPuzzleContract;
import es.odracirnumira.npuzzle.model.NPuzzle;
import es.odracirnumira.npuzzle.model.NPuzzleGame;

/**
 * Task that saves a game ({@link NPuzzleGame}) into the content provider. A new entry is
 * created in the database for the game. If there is any error saving the game, a toast message
 * is shown.
 * <p>
 * This task sets a new value for the game's ID ({@link NPuzzleGame#gameID}): if the game is
 * properly saved, it will be a value > 0. Otherwise, it will be -1. The result of the task is
 * the ID of the game (-1 if it could not be saved).
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class SaveGameTask extends AsyncTask<NPuzzleGame, Void, Long> {
	protected Long doInBackground(NPuzzleGame... params) {
		NPuzzleGame game = params[0];

		/*
		 * Try to save the game.
		 */
		ContentValues values = new ContentValues();
		values.put(NPuzzleContract.Games.INITIAL_STATE, game.initialState);
		values.put(NPuzzleContract.Games.ELAPSED_TIME, game.elapsedTime);
		values.put(NPuzzleContract.Games.MOVES, NPuzzle.sequenceOfIntegersToString(game.moves));
		values.put(NPuzzleContract.Games.START_TIME, game.startTime);
		values.put(NPuzzleContract.Games.IMAGE_PATH, game.puzzleImagePath);
		values.put(NPuzzleContract.Games.LAST_PLAYED_TIME, game.lastPlayedTime);
		values.put(NPuzzleContract.Games.IMAGE_ROTATION, game.imageRotation);

		try {
			game.gameID = ContentUris.parseId(NPuzzleApplication.getApplication()
					.getContentResolver().insert(NPuzzleContract.Games.CONTENT_URI, values));
		} catch (Exception e) {
			game.gameID = -1L;
		}

		return game.gameID;
	}

	protected void onPostExecute(Long result) {
		if (result == -1) {
			Toast.makeText(NPuzzleApplication.getApplication(), R.string.could_not_save_game,
					Toast.LENGTH_SHORT).show();
		}
	}
}

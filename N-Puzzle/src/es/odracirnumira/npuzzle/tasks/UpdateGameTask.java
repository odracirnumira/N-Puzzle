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
 * Task that updates in the content provider a game. The input {@link NPuzzleGame} should have a
 * valid ID. If it has {@link NPuzzleGame#gameID} -1, this task will try to save the game
 * instead. If there is any error updating the game, a toast message is shown.
 * <p>
 * If the game had an ID and it could be updated, the result of this task is a Boolean with
 * value true. If the game had an ID and it could not be updated, the result is a Boolean with
 * value false. If the game did not have an ID, the result is null.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class UpdateGameTask extends AsyncTask<NPuzzleGame, Void, Object> {
	private NPuzzleGame game;

	protected Object doInBackground(NPuzzleGame... params) {
		this.game = params[0];

		if (game.gameID != -1) {
			// Update
			ContentValues values = new ContentValues();
			values.put(NPuzzleContract.Games.INITIAL_STATE, game.initialState);
			values.put(NPuzzleContract.Games.ELAPSED_TIME, game.elapsedTime);
			values.put(NPuzzleContract.Games.MOVES,
					NPuzzle.sequenceOfIntegersToString(game.moves));
			values.put(NPuzzleContract.Games.START_TIME, game.startTime);
			values.put(NPuzzleContract.Games.IMAGE_PATH, game.puzzleImagePath);
			values.put(NPuzzleContract.Games.LAST_PLAYED_TIME, game.lastPlayedTime);
			values.put(NPuzzleContract.Games.IMAGE_ROTATION, game.imageRotation);

			return NPuzzleApplication.getApplication().getContentResolver().update(
					ContentUris.withAppendedId(NPuzzleContract.Games.CONTENT_URI, game.gameID),
					values, null, null) > 0;
		} else {
			// Save. Signal save by returning null.
			return null;
		}
	}

	protected void onPostExecute(Object result) {
		if (result instanceof Boolean) {
			boolean updated = (Boolean) result;

			if (!updated) {
				Toast.makeText(NPuzzleApplication.getApplication(),
						R.string.could_not_update_game, Toast.LENGTH_SHORT).show();
			}
		} else {
			new SaveGameTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, game);
		}
	}
}
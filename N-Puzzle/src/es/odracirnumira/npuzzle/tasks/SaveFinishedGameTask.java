package es.odracirnumira.npuzzle.tasks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.widget.Toast;
import es.odracirnumira.npuzzle.NPuzzleApplication;
import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.contentproviders.NPuzzleContract;
import es.odracirnumira.npuzzle.model.FinishedNPuzzleGame;
import es.odracirnumira.npuzzle.model.NPuzzle;

public class SaveFinishedGameTask extends AsyncTask<FinishedNPuzzleGame, Void, Long> {
	protected Long doInBackground(FinishedNPuzzleGame... params) {
		FinishedNPuzzleGame game = params[0];

		/*
		 * Try to save the game.
		 */
		ContentValues values = new ContentValues();
		values.put(NPuzzleContract.FinishedGames.INITIAL_STATE, game.initialState);
		values.put(NPuzzleContract.FinishedGames.ELAPSED_TIME, game.elapsedTime);
		values.put(NPuzzleContract.FinishedGames.MOVES, NPuzzle.sequenceOfIntegersToString(game.moves));
		values.put(NPuzzleContract.FinishedGames.START_TIME, game.startTime);
		values.put(NPuzzleContract.FinishedGames.IMAGE_PATH, game.puzzleImagePath);
		values.put(NPuzzleContract.FinishedGames.FINISHED_TIME, game.finishedTime);
		values.put(NPuzzleContract.FinishedGames.IMAGE_ROTATION, game.imageRotation);

		try {
			game.gameID = ContentUris.parseId(NPuzzleApplication.getApplication()
					.getContentResolver().insert(NPuzzleContract.FinishedGames.CONTENT_URI, values));
		} catch (Exception e) {
			game.gameID = -1L;
		}

		return game.gameID;
	}

	protected void onPostExecute(Long result) {
		if (result == -1) {
			Toast.makeText(NPuzzleApplication.getApplication(), R.string.could_not_save_finished_game,
					Toast.LENGTH_SHORT).show();
		}
	}
}

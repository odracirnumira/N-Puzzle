package es.odracirnumira.npuzzle;

import java.util.ArrayList;

import es.odracirnumira.npuzzle.contentproviders.NPuzzleContract;
import es.odracirnumira.npuzzle.model.NPuzzle;
import es.odracirnumira.npuzzle.util.ImageUtilities;
import android.app.Application;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

/**
 * The application class.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NPuzzleApplication extends Application {
	/**
	 * The only instance of this class.
	 */
	private static volatile NPuzzleApplication instance;

	/**
	 * Returns the only instance of this class.
	 */
	public static NPuzzleApplication getApplication() {
		return instance;
	}

	public NPuzzleApplication() {
		instance = this;
	}

	public void onCreate() {
//		// Test code for inserting games
//		int numInsertions = 1000;
//		ContentValues[] values = new ContentValues[numInsertions];
//
//		for (int i = 0; i < numInsertions; i++) {
//			ContentValues singleValues = new ContentValues();
//			singleValues.put(NPuzzleContract.Games.INITIAL_STATE, NPuzzle.newNPuzzleFromSideSize(4)
//					.toString());
//			singleValues.put(NPuzzleContract.Games.ELAPSED_TIME, 0);
//			singleValues.put(NPuzzleContract.Games.MOVES, "");
//			singleValues.put(NPuzzleContract.Games.START_TIME, System.currentTimeMillis());
//			singleValues.putNull(NPuzzleContract.Games.IMAGE_PATH);
//			singleValues.put(NPuzzleContract.Games.LAST_PLAYED_TIME,
//					System.currentTimeMillis());
//
//			values[i] = singleValues;
//		}
//
//		Log.i("NPuzzle", "Inserting");
//
//		getContentResolver().bulkInsert(NPuzzleContract.Games.CONTENT_URI, values);
	}
}

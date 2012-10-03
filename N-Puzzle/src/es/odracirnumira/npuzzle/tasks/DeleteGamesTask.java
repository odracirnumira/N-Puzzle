package es.odracirnumira.npuzzle.tasks;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import es.odracirnumira.npuzzle.NPuzzleApplication;
import es.odracirnumira.npuzzle.contentproviders.NPuzzleContract;

/**
 * Task that deletes a set of games. This uses the ContentProvider specified by
 * {@link NPuzzleContract}.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class DeleteGamesTask extends AsyncTask<long[], Void, Void> {
	protected Void doInBackground(long[]... params) {
		ContentResolver contentResolver = NPuzzleApplication.getApplication().getContentResolver();
		ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<ContentProviderOperation>();

		for (long id : params[0]) {
			deleteOperations.add(ContentProviderOperation.newDelete(
					ContentUris.withAppendedId(NPuzzleContract.Games.CONTENT_URI, id)).build());
		}

		try {
			contentResolver.applyBatch(NPuzzleContract.AUTHORITY, deleteOperations);
		} catch (RemoteException e) {
			Log.e("NPuzzle", "Error deleting games", e);
		} catch (OperationApplicationException e) {
			Log.e("NPuzzle", "Error deleting games", e);
		}

		return null;
	}
}
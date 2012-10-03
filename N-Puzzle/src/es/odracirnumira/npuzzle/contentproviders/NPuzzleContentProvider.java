package es.odracirnumira.npuzzle.contentproviders;

import java.util.ArrayList;

import es.odracirnumira.npuzzle.contentproviders.NPuzzleContract.FinishedGames;
import es.odracirnumira.npuzzle.contentproviders.NPuzzleContract.Games;
import es.odracirnumira.npuzzle.database.NPuzzleDatabase;
import es.odracirnumira.npuzzle.database.NPuzzleDatabase.TableFinishedGame;
import es.odracirnumira.npuzzle.database.NPuzzleDatabase.TableGame;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

/**
 * Content provider that manages the data of the application. This implements the contract from
 * {@link NPuzzleContract}.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NPuzzleContentProvider extends ContentProvider {
	/**
	 * The database helper used to open connections to the database.
	 */
	private SQLiteOpenHelper openHelper;

	/**
	 * The URI matcher of this content provider.
	 */
	private UriMatcher uriMatcher;

	private static final int NO_MATCH = -1;
	private static final int GAMES_CODE = 0;
	private static final int GAMES_INSTANCE_CODE = 1;
	private static final int FINISHED_GAMES_CODE = 2;
	private static final int FINISHED_GAMES_INSTANCE_CODE = 3;

	public boolean onCreate() {
		/*
		 * Initialize database helper and URI matcher.
		 */
		this.openHelper = new NPuzzleDatabase.OpenDBHelper(getContext());

		this.uriMatcher = new UriMatcher(NO_MATCH);
		this.uriMatcher.addURI(NPuzzleContract.AUTHORITY, Games.PATH, GAMES_CODE);
		this.uriMatcher.addURI(NPuzzleContract.AUTHORITY, Games.PATH + "/#", GAMES_INSTANCE_CODE);
		this.uriMatcher.addURI(NPuzzleContract.AUTHORITY, FinishedGames.PATH, FINISHED_GAMES_CODE);
		this.uriMatcher.addURI(NPuzzleContract.AUTHORITY, FinishedGames.PATH + "/#",
				FINISHED_GAMES_INSTANCE_CODE);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[],
	 * java.lang.String, java.lang.String[], java.lang.String)
	 */
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		String tableName;

		switch (this.uriMatcher.match(uri)) {
			case GAMES_CODE:
				tableName = NPuzzleDatabase.TableGame.NAME;
				break;
			case GAMES_INSTANCE_CODE:
				/*
				 * If the incoming URI has an ID, append it to the WHERE clause for the database
				 * query.
				 */
				tableName = NPuzzleDatabase.TableGame.NAME;
				selection = DatabaseUtils.concatenateWhere(selection, TableGame.ID + "=?");
				selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
						new String[] { uri.getLastPathSegment() });
				break;
			case FINISHED_GAMES_CODE:
				tableName = NPuzzleDatabase.TableFinishedGame.NAME;
				break;
			case FINISHED_GAMES_INSTANCE_CODE:
				/*
				 * If the incoming URI has an ID, append it to the WHERE clause for the database
				 * query.
				 */
				tableName = NPuzzleDatabase.TableFinishedGame.NAME;
				selection = DatabaseUtils.concatenateWhere(selection, TableFinishedGame.ID + "=?");
				selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
						new String[] { uri.getLastPathSegment() });
				break;
			default:
				throw new IllegalArgumentException("Invalid URI " + uri);
		}

		SQLiteDatabase database = this.openHelper.getReadableDatabase();

		Cursor c = database.query(tableName, projection, selection, selectionArgs, null, null,
				sortOrder);

		c.setNotificationUri(this.getContext().getContentResolver(), uri);

		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[],
	 * java.lang.String, java.lang.String[], java.lang.String)
	 */
	public String getType(Uri uri) {
		switch (this.uriMatcher.match(uri)) {
			case GAMES_CODE:
				return Games.CONTENT_TYPE;
			case GAMES_INSTANCE_CODE:
				return Games.CONTENT_ITEM_TIPE;
			case FINISHED_GAMES_CODE:
				return FinishedGames.CONTENT_TYPE;
			case FINISHED_GAMES_INSTANCE_CODE:
				return FinishedGames.CONTENT_ITEM_TIPE;
			default:
				return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[],
	 * java.lang.String, java.lang.String[], java.lang.String)
	 */
	public Uri insert(Uri uri, ContentValues initialValues) {
		switch (this.uriMatcher.match(uri)) {
			case GAMES_CODE: {
				ContentValues values = initialValues != null ? new ContentValues(initialValues)
						: new ContentValues();

				// If the user passed a null values object, create a default game
				if (values.size() == 0) {
					long currentTime = System.currentTimeMillis();
					values.put(TableGame.START_TIME, currentTime);
					values.put(TableGame.MOVES, "");
					values.put(TableGame.INITIAL_STATE, "0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
					values.put(TableGame.ELAPSED_TIME, 0);
					values.putNull(TableGame.IMAGE_PATH);
					values.put(TableGame.LAST_PLAYED_TIME, currentTime);
					values.put(TableGame.IMAGE_ROTATION, 0);
				}

				SQLiteDatabase database = this.openHelper.getWritableDatabase();

				long id = database.insert(NPuzzleDatabase.TableGame.NAME, null, values);

				if (id >= 0) {
					Uri result = ContentUris.withAppendedId(Games.CONTENT_URI, id);
					getContext().getContentResolver().notifyChange(result, null);
					return result;
				} else {
					throw new SQLException("Failed to insert the game into " + uri);
				}
			}

			case FINISHED_GAMES_CODE: {
				ContentValues values = initialValues != null ? new ContentValues(initialValues)
						: new ContentValues();

				// If the user passed a null values object, create a default game
				if (values.size() == 0) {
					long currentTime = System.currentTimeMillis();
					values.put(TableFinishedGame.START_TIME, currentTime);
					values.put(TableFinishedGame.MOVES, "");
					values.put(TableFinishedGame.INITIAL_STATE,
							"0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
					values.put(TableFinishedGame.ELAPSED_TIME, 0);
					values.putNull(TableFinishedGame.IMAGE_PATH);
					values.put(TableFinishedGame.FINISHED_TIME, currentTime);
					values.put(TableFinishedGame.IMAGE_ROTATION, 0);
				}

				SQLiteDatabase database = this.openHelper.getWritableDatabase();

				long id = database.insert(NPuzzleDatabase.TableFinishedGame.NAME, null, values);

				if (id >= 0) {
					Uri result = ContentUris.withAppendedId(FinishedGames.CONTENT_URI, id);
					getContext().getContentResolver().notifyChange(result, null);
					return result;
				} else {
					throw new SQLException("Failed to insert the game into " + uri);
				}
			}

			default:
				throw new IllegalArgumentException("Invalid URI " + uri);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String,
	 * java.lang.String[])
	 */
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase database = this.openHelper.getWritableDatabase();

		int numDeleted;

		switch (this.uriMatcher.match(uri)) {
			case GAMES_CODE:
				numDeleted = database.delete(TableGame.NAME, selection, selectionArgs);
				break;
			case GAMES_INSTANCE_CODE:
				/*
				 * If the incoming URI has an ID, modify the where clause so it has the ID.
				 */
				selection = DatabaseUtils.concatenateWhere(selection, TableGame.ID + "=?");
				selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
						new String[] { uri.getLastPathSegment() });
				numDeleted = database.delete(TableGame.NAME, selection, selectionArgs);
				break;
			case FINISHED_GAMES_CODE:
				numDeleted = database.delete(TableFinishedGame.NAME, selection, selectionArgs);
				break;
			case FINISHED_GAMES_INSTANCE_CODE:
				/*
				 * If the incoming URL has an ID, modify the where clause so it includes the ID.
				 */
				selection = DatabaseUtils.concatenateWhere(selection, TableFinishedGame.ID + "=?");
				selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
						new String[] { uri.getLastPathSegment() });
				numDeleted = database.delete(TableFinishedGame.NAME, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Invalid URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return numDeleted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues,
	 * java.lang.String, java.lang.String[])
	 */
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase database = this.openHelper.getWritableDatabase();
		int count;

		switch (this.uriMatcher.match(uri)) {
			case GAMES_CODE:
				count = database.update(TableGame.NAME, values, selection, selectionArgs);
				break;
			case GAMES_INSTANCE_CODE:
				/*
				 * If the URI has an ID, append it to the WHERE clause.
				 */
				selection = DatabaseUtils.concatenateWhere(selection, TableGame.ID + "=?");
				selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
						new String[] { uri.getLastPathSegment() });
				count = database.update(TableGame.NAME, values, selection, selectionArgs);
				break;
			case FINISHED_GAMES_CODE:
				count = database.update(TableFinishedGame.NAME, values, selection, selectionArgs);
				break;
			case FINISHED_GAMES_INSTANCE_CODE:
				/*
				 * If the URI has an ID, append it to the WHERE clause.
				 */
				selection = DatabaseUtils.concatenateWhere(selection, TableFinishedGame.ID + "=?");
				selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
						new String[] { uri.getLastPathSegment() });
				count = database.update(TableFinishedGame.NAME, values, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Invalid URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	/**
	 * Adds transaction capabilities to the set of operations, that is, the set of operations are
	 * run as an indivisible unit. If any of the operation fails, the transaction fails, and an
	 * {@link OperationApplicationException} is thrown.
	 * 
	 * @see android.content.ContentProvider#applyBatch(java.util.ArrayList)
	 */
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		SQLiteDatabase database = openHelper.getWritableDatabase();
		database.beginTransaction();

		try {
			ContentProviderResult[] result = super.applyBatch(operations);
			database.setTransactionSuccessful();
			return result;
		} finally {
			database.endTransaction();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#bulkInsert(android.net.Uri,
	 * android.content.ContentValues[])
	 */
	public int bulkInsert(Uri uri, ContentValues[] values) {
		switch (this.uriMatcher.match(uri)) {
			case GAMES_CODE: {
				SQLiteDatabase database = this.openHelper.getWritableDatabase();
				database.beginTransaction();

				try {
					/*
					 * Use a compiled SQL statement for performance reasons.
					 */
					SQLiteStatement insertStatement = database.compileStatement("insert into "
							+ NPuzzleDatabase.TableGame.NAME + "("
							+ NPuzzleDatabase.TableGame.INITIAL_STATE + ","
							+ NPuzzleDatabase.TableGame.MOVES + ","
							+ NPuzzleDatabase.TableGame.START_TIME + ","
							+ NPuzzleDatabase.TableGame.IMAGE_PATH + ","
							+ NPuzzleDatabase.TableGame.ELAPSED_TIME + ","
							+ NPuzzleDatabase.TableGame.LAST_PLAYED_TIME
							+ NPuzzleDatabase.TableGame.IMAGE_ROTATION + ") values(?,?,?,?,?,?,?)");

					for (ContentValues originalValues : values) {
						ContentValues singleValues = originalValues;

						// If the user passed a null values object, create a default game
						if (singleValues.size() == 0) {
							// Copy so the original is not modified
							singleValues = new ContentValues(originalValues);

							long currentTime = System.currentTimeMillis();
							singleValues.put(TableGame.START_TIME, currentTime);
							singleValues.put(TableGame.MOVES, "");
							singleValues.put(TableGame.INITIAL_STATE,
									"0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
							singleValues.put(TableGame.ELAPSED_TIME, 0);
							singleValues.putNull(TableGame.IMAGE_PATH);
							singleValues.put(TableGame.LAST_PLAYED_TIME, currentTime);
							singleValues.put(TableGame.IMAGE_ROTATION, 0);
						}

						/*
						 * Bind insert statement for the current ContentValues
						 */
						insertStatement.bindString(1,
								singleValues.getAsString(NPuzzleDatabase.TableGame.INITIAL_STATE));
						insertStatement.bindString(2,
								singleValues.getAsString(NPuzzleDatabase.TableGame.MOVES));
						insertStatement.bindLong(3,
								singleValues.getAsLong(NPuzzleDatabase.TableGame.START_TIME));
						String imagePath = singleValues
								.getAsString(NPuzzleDatabase.TableGame.IMAGE_PATH);

						if (imagePath != null) {
							insertStatement.bindString(4, imagePath);
						} else {
							insertStatement.bindNull(4);
						}

						insertStatement.bindLong(5,
								singleValues.getAsLong(NPuzzleDatabase.TableGame.ELAPSED_TIME));

						insertStatement.bindLong(6,
								singleValues.getAsLong(NPuzzleDatabase.TableGame.LAST_PLAYED_TIME));

						insertStatement
								.bindLong(7, singleValues
										.getAsInteger(NPuzzleDatabase.TableGame.IMAGE_ROTATION));

						/*
						 * Insert.
						 */
						if (insertStatement.executeInsert() == -1) {
							throw new SQLException("Failed to insert the game into " + uri);
						}
					}

					database.setTransactionSuccessful();
					return values.length;
				} finally {
					database.endTransaction();
				}
			}

			case FINISHED_GAMES_CODE: {
				SQLiteDatabase database = this.openHelper.getWritableDatabase();
				database.beginTransaction();

				try {
					/*
					 * Use a compiled SQL statement for performance reasons.
					 */
					SQLiteStatement insertStatement = database.compileStatement("insert into "
							+ NPuzzleDatabase.TableFinishedGame.NAME + "("
							+ NPuzzleDatabase.TableFinishedGame.INITIAL_STATE + ","
							+ NPuzzleDatabase.TableFinishedGame.MOVES + ","
							+ NPuzzleDatabase.TableFinishedGame.START_TIME + ","
							+ NPuzzleDatabase.TableFinishedGame.IMAGE_PATH + ","
							+ NPuzzleDatabase.TableFinishedGame.ELAPSED_TIME + ","
							+ NPuzzleDatabase.TableFinishedGame.FINISHED_TIME
							+ NPuzzleDatabase.TableFinishedGame.IMAGE_ROTATION
							+ ") values(?,?,?,?,?,?,?)");

					for (ContentValues originalValues : values) {
						ContentValues singleValues = originalValues;

						// If the user passed a null values object, create a default game
						if (singleValues.size() == 0) {
							// Copy so the original is not modified
							singleValues = new ContentValues(originalValues);

							long currentTime = System.currentTimeMillis();
							singleValues.put(TableFinishedGame.START_TIME, currentTime);
							singleValues.put(TableFinishedGame.MOVES, "");
							singleValues.put(TableFinishedGame.INITIAL_STATE,
									"0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
							singleValues.put(TableFinishedGame.ELAPSED_TIME, 0);
							singleValues.putNull(TableFinishedGame.IMAGE_PATH);
							singleValues.put(TableFinishedGame.FINISHED_TIME, currentTime);
							singleValues.put(TableFinishedGame.IMAGE_ROTATION, 0);
						}

						/*
						 * Bind insert statement for the current ContentValues
						 */
						insertStatement.bindString(1, singleValues
								.getAsString(NPuzzleDatabase.TableFinishedGame.INITIAL_STATE));
						insertStatement.bindString(2,
								singleValues.getAsString(NPuzzleDatabase.TableFinishedGame.MOVES));
						insertStatement.bindLong(3, singleValues
								.getAsLong(NPuzzleDatabase.TableFinishedGame.START_TIME));
						String imagePath = singleValues
								.getAsString(NPuzzleDatabase.TableGame.IMAGE_PATH);

						if (imagePath != null) {
							insertStatement.bindString(4, imagePath);
						} else {
							insertStatement.bindNull(4);
						}

						insertStatement.bindLong(5, singleValues
								.getAsLong(NPuzzleDatabase.TableFinishedGame.ELAPSED_TIME));

						insertStatement.bindLong(6, singleValues
								.getAsLong(NPuzzleDatabase.TableFinishedGame.FINISHED_TIME));

						insertStatement.bindLong(7, singleValues
								.getAsLong(NPuzzleDatabase.TableFinishedGame.IMAGE_ROTATION));

						/*
						 * Insert.
						 */
						if (insertStatement.executeInsert() == -1) {
							throw new SQLException("Failed to insert the finished game into " + uri);
						}
					}

					database.setTransactionSuccessful();
				} finally {
					database.endTransaction();
				}
			}

			default:
				throw new IllegalArgumentException("Invalid URI " + uri);
		}
	}
}

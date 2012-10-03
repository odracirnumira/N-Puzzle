package es.odracirnumira.npuzzle.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class that manages the database side of the application. It defines all the tables as well as the
 * database helper to access them.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public final class NPuzzleDatabase {
	/**
	 * Name of the database.
	 */
	public static final String DB_NAME = "NPuzzleDatabase";

	/**
	 * Current version of the database.
	 */
	public static final int VERSION = 1;

	/**
	 * Generic columns that all N puzzle games share. This columns are used by all tables that must
	 * store a game's information.
	 * <p>
	 * IMPORTANT: the GenericGameColumns class is synchronized with the columns in the
	 * NPuzzleContract class. If you change the column values in this class, the column must change
	 * in NPuzzleContract too.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	protected interface GenericGameColumns {
		/**
		 * The ID of the game (row).
		 * <p>
		 * Type: INTEGER
		 */
		public static final String ID = "_id";

		/**
		 * The initial state of the N puzzle. It is a string consisting of a sequence of
		 * non-negative numbers, separated by blank spaces (one blank space for every couple of
		 * numbers). The first number is the tile that is placed at position 0; the second number is
		 * the tile that is placed at position 1, and so on.
		 * <p>
		 * Type: STRING
		 */
		public static final String INITIAL_STATE = "InitialState";

		/**
		 * The list of moves that have been played so far in the game. If the game is finished, this
		 * is the complete list of moves. This is a string consisting of a sequence of non-negative
		 * numbers, separated by blank spaces (one blank space for every couple of numbers). The
		 * first number is the tile that was moved first; the second number is the second tile that
		 * was moved, and so on.
		 * <p>
		 * Type: STRING
		 */
		public static final String MOVES = "Moves";

		/**
		 * The date when the game started. This is a Unix time in milliseconds, represented as an
		 * integer.
		 * <p>
		 * Type: INTEGER
		 */
		public static final String START_TIME = "StartTime";

		/**
		 * The time elapsed since the game started (in milliseconds), represented as an integer.
		 * This is the time that the player has been actively playing the game. For instance, if the
		 * player leaves the game, the time elapsed since the moment he leaves the game until he
		 * starts playing it again does not count.
		 * <p>
		 * Type: INTEGER
		 */
		public static final String ELAPSED_TIME = "ElapsedTime";

		/**
		 * The path of the image that the puzzle was displaying. This is a string, and may be null
		 * if no image was available.
		 * <p>
		 * Type: STRING
		 */
		public static final String IMAGE_PATH = "ImagePath";

		/**
		 * The rotation of the puzzle's image. This is a value in degrees that can be 0, 90, 180,
		 * and 270.
		 * <p>
		 * Type: INTEGER
		 */
		public static final String IMAGE_ROTATION = "ImageRotation";
	}

	/**
	 * Table for a game that has not finished.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class TableGame implements GenericGameColumns {
		/**
		 * The name of the table.
		 */
		public static final String NAME = "Game";

		/**
		 * The time when the game was last played. This is a Unix time in milliseconds, represented
		 * as an integer.
		 * <p>
		 * Type: INTEGER
		 */
		public static final String LAST_PLAYED_TIME = "LastPlayedTime";

		public static final String CREATE = "CREATE TABLE " + NAME + "(" + ID
				+ " INTEGER PRIMARY KEY NOT NULL, " + INITIAL_STATE + " TEXT NOT NULL, " + MOVES
				+ " TEXT NOT NULL, " + START_TIME + " INTEGER NOT NULL, " + ELAPSED_TIME
				+ " INTEGER NOT NULL, " + LAST_PLAYED_TIME + " INTEGER NOT NULL, " + IMAGE_ROTATION
				+ " INTEGER NOT NULL, " + IMAGE_PATH + " TEXT)";

		public static final String DROP = "DROP TABLE " + NAME;
	}

	/**
	 * Table for a game that has finished.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class TableFinishedGame implements GenericGameColumns {
		/**
		 * The name of the table.
		 */
		public static final String NAME = "FinishedGame";

		/**
		 * The time when the game finished. This is a Unix time in milliseconds, represented as an
		 * integer.
		 * <p>
		 * Type: INTEGER
		 */
		public static final String FINISHED_TIME = "FinishedTime";

		public static final String CREATE = "CREATE TABLE " + NAME + "(" + ID
				+ " INTEGER PRIMARY KEY NOT NULL, " + INITIAL_STATE + " TEXT NOT NULL, " + MOVES
				+ " TEXT NOT NULL, " + START_TIME + " INTEGER NOT NULL, " + ELAPSED_TIME
				+ " INTEGER NOT NULL, " + FINISHED_TIME + " INTEGER NOT NULL, " + IMAGE_ROTATION
				+ " INTEGER NOT NULL, " + IMAGE_PATH + " TEXT)";

		public static final String DROP = "DROP TABLE " + NAME;
	}

	/**
	 * A {@link SQLiteOpenHelper} that gives access to the application's database.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class OpenDBHelper extends SQLiteOpenHelper {
		public OpenDBHelper(Context context) {
			super(context, DB_NAME, null, VERSION);
		}

		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TableGame.CREATE);
			db.execSQL(TableFinishedGame.CREATE);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	private NPuzzleDatabase() {
	}
}

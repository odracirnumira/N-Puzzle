package es.odracirnumira.npuzzle.contentproviders;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract between the NPuzzle provider and other applications. This class contains the
 * definitions for the supported URIs and data columns. It explains what can be accomplished by
 * using the content provider and how to use it.
 * <p>
 * This class is intended to be distributed among clients who want to access this content provider.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public final class NPuzzleContract {
	/*
	 * IMPORTANT: the GenericGameColumns class is synchronized with the columns in the
	 * NPuzzleDatabase class. If you change the column values in this class, the column must change
	 * in NPuzzleDatabase too.
	 */
	/**
	 * The authority used in URIs to access the NPuzzle content provider.
	 */
	public static final String AUTHORITY = "es.odracirnumira.npuzzle.contentproviders.provider";

	/**
	 * The base URI for the content provider. Any table managed by this provider is placed in a path
	 * under this base URI.
	 */
	public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * Generic columns that all N puzzle games share.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	protected interface GenericGameColumns {
		/**
		 * The initial state of the N puzzle. It is a string consisting of a sequence of
		 * non-negative numbers, separated by blank spaces (one blank space for every couple of
		 * numbers). The first number is the position of the first tile, the second number is the
		 * position of the second tile, and so on.
		 * <p>
		 * Type: STRING
		 */
		public static final String INITIAL_STATE = "InitialState";

		/**
		 * The list of moves that have been played so far in the game. If the game is finished, this
		 * is the complete list of moves. This is a string consisting of a sequence of non-negative
		 * numbers, separated by blank spaces (one blank space for every couple of numbers). The
		 * first number is the tile that was moved first; the second number is the second tile that
		 * was moved, and so on. The list may be empty if no moves have been performed.
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
		 * The time elapsed since the game started (in milliseconds), represented as an integer. This is
		 * the time that the player has been actively playing the game. For instance, if the player
		 * leaves the game, the time elapsed since the moment he leaves the game until he starts
		 * playing it again does not count.
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
		public static final String IMAGE_ROTATION="ImageRotation";
	}

	/**
	 * This table represents the active N puzzle games, that is, games that have not finished yet.
	 * Each row represents a game. All fields are required when inserting a new game, except for
	 * {@link GenericGameColumns#IMAGE_PATH}, which may be null.
	 * <p>
	 * This table allows to append an ID at the end of the {@link #CONTENT_URI} in query, update and
	 * delete operations, to handle individual instances instead of groups of them.
	 * <p>
	 * You can insert a new game without specifying any values for the columns (null ContentValues).
	 * In that case, a default solved game (a 15 puzzle) without any moves is created. The start
	 * time is set to the current time, and the elapsed time, to 0. Note however that if you do
	 * specify a ContentValues object, you must provide all the values.
	 * <p>
	 * The {@link ContentResolver#applyBatch(String, java.util.ArrayList)} function behaves in a
	 * transactional way, that is, all the operations are run as an indivisible unit. If any of the
	 * operations fails, the transaction fails.
	 * <p>
	 * Insertions via {@link ContentResolver#bulkInsert(Uri, android.content.ContentValues[])} are
	 * atomic.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class Games implements BaseColumns, GenericGameColumns {
		/**
		 * The time when the game was last played. This is a Unix time in milliseconds, represented as an
		 * integer.
		 * <p>
		 * Type: INTEGER
		 */
		public static final String LAST_PLAYED_TIME = "LastPlayedTime";

		/**
		 * Specific path under the {@link NPuzzleContract#BASE_URI} where unfinished games are
		 * stored.
		 */
		static final String PATH = "games";

		/**
		 * The content URI for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

		/**
		 * MIME type for {@link #CONTENT_URI}.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.es.odracirnumira.npuzzle.game";

		/**
		 * MIME type for a single item (row) in {@link #CONTENT_URI}.
		 */
		public static final String CONTENT_ITEM_TIPE = "vnd.android.cursor.item/vnd.es.odracirnumira.npuzzle.game";
	}

	/**
	 * This table represents the finished N puzzle games, that is, games that have finished already.
	 * Each row represents a game. All fields are required when inserting a new finished game,
	 * except for {@link GenericGameColumns#IMAGE_PATH}, which may be null.
	 * <p>
	 * This table allows to append an ID at the end of the {@link #CONTENT_URI} in query, update and
	 * delete operations, to handle individual instances instead of groups of them. *
	 * <p>
	 * You can insert a new game without specifying any values for the columns (null ContentValues).
	 * In that case, a default solved game (a 15 puzzle) without any moves is created. The start
	 * time is set to the current time, and the elapsed time, to 0. Note however that if you do
	 * specify a ContentValues object, you must provide all the values.
	 * <p>
	 * The {@link ContentResolver#applyBatch(String, java.util.ArrayList)} function behaves in a
	 * transactional way, that is, all the operations are run as an indivisible unit. If any of the
	 * operations fails, the transaction fails.
	 * <p>
	 * Insertions via {@link ContentResolver#bulkInsert(Uri, android.content.ContentValues[])} are
	 * atomic.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class FinishedGames implements BaseColumns, GenericGameColumns {
		/**
		 * The time when the game finished. This is a Unix time in milliseconds, represented as an
		 * integer.
		 * <p>
		 * Type: INTEGER
		 */
		public static final String FINISHED_TIME = "FinishedTime";

		/**
		 * Specific path under the {@link NPuzzleContract#BASE_URI} where finished games are stored.
		 */
		static final String PATH = "finished_games";

		/**
		 * The content URI for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

		/**
		 * MIME type for {@link #CONTENT_URI}.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.es.odracirnumira.npuzzle.finished_game";

		/**
		 * MIME type for a single item (row) in {@link #CONTENT_URI}.
		 */
		public static final String CONTENT_ITEM_TIPE = "vnd.android.cursor.item/vnd.es.odracirnumira.npuzzle.finished_game";
	}

	private NPuzzleContract() {

	}
}

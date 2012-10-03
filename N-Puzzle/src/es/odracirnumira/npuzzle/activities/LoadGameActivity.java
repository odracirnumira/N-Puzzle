package es.odracirnumira.npuzzle.activities;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.contentproviders.NPuzzleContract;
import es.odracirnumira.npuzzle.fragments.dialogs.DeleteSelectedGamesDialogFragment;
import es.odracirnumira.npuzzle.fragments.dialogs.DeleteSelectedGamesDialogFragment.IDeleteGameListener;
import es.odracirnumira.npuzzle.model.NPuzzle;
import es.odracirnumira.npuzzle.tasks.DeleteGamesTask;
import es.odracirnumira.npuzzle.util.ImageUtilities;
import es.odracirnumira.npuzzle.util.UIUtilities;
import es.odracirnumira.npuzzle.util.cache.CacheUtils;
import es.odracirnumira.npuzzle.util.cache.ICache;
import es.odracirnumira.npuzzle.util.cache.MemoryLimitedCache;
import es.odracirnumira.npuzzle.view.NPuzzleView;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

//TODO: fix memory leak of Bitmaps in screen orientation change!!
/**
 * Activity that shows the list of unfinished games and lets the user manage them. The list of games
 * are retrieved from the content provider ( {@link NPuzzleContract}), and the user can restart them
 * and delete them.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class LoadGameActivity extends Activity implements LoaderCallbacks<Cursor>,
		IDeleteGameListener {
	/**
	 * The GridView used to display the list of games.
	 */
	private GridView gridView;

	/**
	 * The view that is displayed while data is being loaded.
	 */
	private View loadingGameDataView;

	/**
	 * The adapter used to show the games.
	 */
	private GameCursorAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.load_game_activity);

		this.loadingGameDataView = findViewById(R.id.loadingDataView);

		/*
		 * Set up the GridView that displays the games.
		 */
		this.gridView = (GridView) findViewById(R.id.loadGameGridView);
		this.adapter = new GameCursorAdapter(this, null,
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		this.gridView.setAdapter(this.adapter);
		this.gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		this.gridView.setMultiChoiceModeListener(new GamesMultiChoiceModeListener());
		this.gridView.setEmptyView(findViewById(R.id.emptyView));

		getLoaderManager().initLoader(0, null, this);

		/*
		 * Scroll listener that updates the scrolling state of the adapter. This is required if we
		 * want the adapter to compute games' thumbnails only when the view is not scrolling.
		 */
		this.gridView.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					adapter.setScrolling(false);
				} else {
					adapter.setScrolling(true);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
			}
		});

		/*
		 * Click listener that starts the selected game.
		 */
		this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(LoadGameActivity.this, GameActivity.class);
				intent.putExtra(GameActivity.INPUT_LOAD_GAME_ID, id);
				startActivity(intent);
				finish();
			}
		});
	}

	public void onPause() {
		super.onPause();
	}

	public void onResume() {
		super.onResume();
	}

	public void onDestroy() {
		super.onDestroy();
		this.adapter.shutdownAdapter();
	}

	/**
	 * Shows the view that displays a "loading message". Call this method when you start loading the
	 * games' data.
	 */
	private void showLoadingGamesDataScreen() {
		if (loadingGameDataView.getVisibility() != View.VISIBLE) {
			this.gridView.startAnimation(AnimationUtils
					.loadAnimation(this, android.R.anim.fade_out));
			this.gridView.getEmptyView().startAnimation(
					AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
			this.loadingGameDataView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_in));
			this.gridView.setVisibility(View.GONE);
			this.gridView.getEmptyView().setVisibility(View.GONE);
			this.loadingGameDataView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Shows the view that displays the loaded games. Call this method when all the games are
	 * loaded.
	 */
	private void showLoadedGamesScreen() {
		if (gridView.getVisibility() != View.VISIBLE) {
			this.gridView
					.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
			this.gridView.getEmptyView().startAnimation(
					AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
			this.loadingGameDataView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out));
			this.gridView.setVisibility(View.VISIBLE);
			this.gridView.getEmptyView().setVisibility(View.VISIBLE);
			this.loadingGameDataView.setVisibility(View.GONE);
		}
	}

	/**
	 * MultiChoiceListener to use when games are selected from the list.
	 * 
	 * @author Ricardo Juan Palma Durán
	 */
	private class GamesMultiChoiceModeListener implements MultiChoiceModeListener {
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getMenuInflater().inflate(R.menu.load_game_multichoice_menu, menu);
			mode.setTitle(R.string.select_games);
			mode.setSubtitle(getResources().getQuantityString(R.plurals.n_games_selected,
					gridView.getCheckedItemCount(), gridView.getCheckedItemCount()));
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.menuItemSelectAll:
					/*
					 * Select all items.
					 */
					for (int i = 0; i < adapter.getCount(); i++) {
						gridView.setItemChecked(i, true);
					}

					return true;
				case R.id.menuItemDelete:
					/*
					 * Delete selected items. Show confirmation dialog before deleting items.
					 */
					DeleteSelectedGamesDialogFragment dialog = new DeleteSelectedGamesDialogFragment();
					Bundle args = new Bundle();
					args.putInt(DeleteSelectedGamesDialogFragment.INPUT_NUMBER_SELECTED_GAMES,
							gridView.getCheckedItemCount());
					dialog.setArguments(args);
					dialog.show(getFragmentManager(), "deleteSelectedGamesDialog");

					return true;
			}

			return false;
		}

		public void onDestroyActionMode(ActionMode mode) {
		}

		public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
				boolean checked) {
			mode.setSubtitle(getResources().getQuantityString(R.plurals.n_games_selected,
					gridView.getCheckedItemCount(), gridView.getCheckedItemCount()));
		}
	}

	/**
	 * Returns the Loader that loads the cursor that gets the unfinished games.
	 */
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		this.showLoadingGamesDataScreen();
		return new CursorLoader(this, NPuzzleContract.Games.CONTENT_URI, null, null, null,
				NPuzzleContract.Games.LAST_PLAYED_TIME + " DESC");
	}

	/**
	 * Updates the GridView's cursor to reflect the new loaded data.
	 */
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		this.showLoadedGamesScreen();
		this.adapter.swapCursor(data);
	}

	/**
	 * Updates the GridView's cursor no no data is displayed.
	 */
	public void onLoaderReset(Loader<Cursor> loader) {
		this.adapter.swapCursor(null);
	}

	/**
	 * Cursor adapter that returns thumbnails for each puzzle game. The passed cursor must contain
	 * all the columns in {@link NPuzzleContract.Games}.
	 * <p>
	 * It is important that you control the adapter's state with
	 * {@link GameCursorAdapter#setScrolling(boolean)}. Otherwise, the thumbnails of the games will
	 * not be properly computed.
	 * 
	 * TODO: complete comments of this class
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private class GameCursorAdapter extends CursorAdapter {
		/**
		 * Used to format dates.
		 */
		private DateFormat dateFormat;

		/**
		 * Thread pool that loads puzzles and images.
		 */
		private ScheduledThreadPoolExecutor puzzleLoaderExecutorService;

		/**
		 * Cache that stores, for each game (identified by its ID), the data that is loaded in a
		 * background thread.
		 */
		private ICache<Long, GameData> cache;

		/**
		 * IDs of those games being loaded on the background thread. This is used to avoid
		 * relaunching the task that loads the game if it is already being loaded.
		 */
		private Set<Long> gamesBeingLoaded;

		/**
		 * Lock for {@link #gamesBeingLoadedLock}, since it can be accessed from multiple threads.
		 */
		private Object gamesBeingLoadedLock = new Object();

		/**
		 * Flag that tells if the view that this adapter is providing data to, is scrolling or not.
		 * If it is scrolling, the adapter will not compute the visible games' data.
		 */
		private boolean scrolling;

		/**
		 * The UI handler. Notified when a game's data has been loaded. The handler will refresh the
		 * item whose data has been loaded. We could just call {@link #notifyDataSetChanged()}, but
		 * it is much slower since the whole view would be redrawn. By updating the single item
		 * whose data is retrieved, we get a much smoother experience.
		 */
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				long gameID = msg.getData().getLong("gameID", -1);
				GameData gameData = cache.get(gameID);

				/*
				 * If the game's data is still in the cache (it may have been removed in the
				 * meanwhile), update the game's view.
				 */
				if (gameData != null) {
					/*
					 * We have to check that the game whose data has been computed is still visible.
					 * Otherwise, we cannot update its view, since it will have been recycled and
					 * used by another game.
					 */
					int firstPos = gridView.getFirstVisiblePosition();
					int lastPos = gridView.getLastVisiblePosition();

					for (int i = firstPos; i <= lastPos; i++) {
						if (adapter.getItemId(i) == gameID) {
							View view = gridView.getChildAt(i - gridView.getFirstVisiblePosition());
							NPuzzleView nPuzzleView = (NPuzzleView) view
									.findViewById(R.id.nPuzzleView);
							TextView numMovesTextView = (TextView) view
									.findViewById(R.id.numMovesTextView);

							nPuzzleView.setImage(gameData.image);
							nPuzzleView.setNPuzzle(gameData.puzzle);
							nPuzzleView.unregisterPuzzle();
							numMovesTextView.setText(getResources().getQuantityString(
									R.plurals.num_moves, gameData.numMoves, gameData.numMoves));

							// Show the puzzle view and hide the progress bar
							view.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
							nPuzzleView.setVisibility(View.VISIBLE);
							numMovesTextView.setVisibility(View.VISIBLE);

							break;
						}
					}
				}
			}
		};

		/**
		 * Data loaded from the backgroudn thread for each game.
		 * 
		 * @author Ricardo Juan Palma Durán
		 * 
		 */
		private class GameData {
			/**
			 * The puzzle.
			 */
			public NPuzzle puzzle;

			/**
			 * The image. May be null for the default image.
			 */
			public Bitmap image;

			/**
			 * Number of moves performed so far.
			 */
			public int numMoves;
		}

		public GameCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);

			this.dateFormat = android.text.format.DateFormat.getDateFormat(context);
			this.gamesBeingLoaded = new HashSet<Long>();

			/*
			 * Executor whose thread is terminated after three seconds without an incoming task.
			 */
			ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
			executor.setKeepAliveTime(3, TimeUnit.SECONDS);
			executor.allowCoreThreadTimeOut(true);
			this.puzzleLoaderExecutorService = executor;

			int cacheSize = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
					.getMemoryClass() * 1024 * 1024 / 8;

			this.cache = CacheUtils.getSynchronizedCache(new MemoryLimitedCache<Long, GameData>(
					cacheSize) {
				public long getSize(GameData value) {
					return (value.image != null ? value.image.getByteCount() : 0)
							+ value.puzzle.getByteCount() + 4 + 4;

				}

				protected void entryRemoved(boolean eviceted, Long key, GameData value) {
					if (value.image != null) {
						value.image.recycle();
					}
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.CursorAdapter#newView(android.content.Context,
		 * android.database.Cursor, android.view.ViewGroup)
		 */
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.game_thumbnail_view, parent, false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context,
		 * android.database.Cursor)
		 */
		public void bindView(View view, Context context, Cursor cursor) {
			/*
			 * Get gameID of the game pointed by the cursor.
			 */
			long gameID = cursor.getLong(cursor.getColumnIndex(NPuzzleContract.Games._ID));

			long startTime = cursor
					.getLong(cursor.getColumnIndex(NPuzzleContract.Games.START_TIME));
			long elapsedTime = cursor.getLong(cursor
					.getColumnIndex(NPuzzleContract.Games.ELAPSED_TIME));
			int imageRotation = cursor.getInt(cursor
					.getColumnIndex(NPuzzleContract.Games.IMAGE_ROTATION));

			/*
			 * Now, if the cache contains the puzzle and image for the game whose id is gameID, we
			 * set the puzzle and the image of the NPuzzleView.
			 */
			GameData gameData = this.cache.get(gameID);

			NPuzzleView nPuzzleView = (NPuzzleView) view.findViewById(R.id.nPuzzleView);
			TextView numMovesTextView = (TextView) view.findViewById(R.id.numMovesTextView);

			if (gameData != null) {
				nPuzzleView.setImage(gameData.image);
				nPuzzleView.setNPuzzle(gameData.puzzle);
				nPuzzleView.unregisterPuzzle();
				numMovesTextView.setText(getResources().getQuantityString(R.plurals.num_moves,
						gameData.numMoves, gameData.numMoves));

				// Show the puzzle view and hide the progress bar
				view.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
				nPuzzleView.setVisibility(View.VISIBLE);
				numMovesTextView.setVisibility(View.VISIBLE);
			} else {
				/*
				 * If the game's data is not in the cache, show the progress bar.
				 */
				view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
				nPuzzleView.setVisibility(View.INVISIBLE);
				numMovesTextView.setVisibility(View.GONE);

				/*
				 * Also, if the view is not being scrolled, spawn task to compute the game's data.
				 * If the data is already being computed, we will not spawn a new task.
				 */
				if (canLoadGameData()) {
					String imagePath = cursor.getString(cursor
							.getColumnIndex(NPuzzleContract.Games.IMAGE_PATH));
					String initialState = cursor.getString(cursor
							.getColumnIndex(NPuzzleContract.Games.INITIAL_STATE));
					String moves = cursor.getString(cursor
							.getColumnIndex(NPuzzleContract.Games.MOVES));

					synchronized (this.gamesBeingLoadedLock) {
						if (!this.gamesBeingLoaded.contains(gameID)) {
							this.gamesBeingLoaded.add(gameID);
							this.loadGameData(gameID, imagePath, initialState, moves);
						}
					}
				}

				// Set dummy puzzle so the view does not complain if no puzzle is set yet
				nPuzzleView.setNPuzzle(NPuzzle.newNPuzzleFromSideSize(2));
			}

			/*
			 * Set image rotation.
			 */
			nPuzzleView.setImageRotation(imageRotation);

			/*
			 * Disable touch events, since we do not want the puzzle view to be interactive.
			 */
			nPuzzleView.enableTouchEvents(false);

			/*
			 * Fill start time and elapsed time.
			 */
			TextView startedOnTextView = (TextView) view.findViewById(R.id.startedOnTextView);
			TextView elapsedTimeTextView = (TextView) view.findViewById(R.id.elapsedTimeTextView);
			startedOnTextView.setText(getString(R.string.game_started_on,
					dateFormat.format(new Date(startTime))));
			elapsedTimeTextView.setText(getGameDurationSoFar(elapsedTime / 1000, context));

			/*
			 * We set a fixed size for the view. This 200dp matches the size of the column size of
			 * the GridView, so if you change one you should change the other.
			 */
			view.setLayoutParams(new AbsListView.LayoutParams((int) UIUtilities.convertDpToPixel(
					200, context), (int) UIUtilities.convertDpToPixel(200, context)));

			int padding = (int) UIUtilities.convertDpToPixel(5, context);

			view.setPadding(padding, padding, padding, padding);
		}

		/**
		 * This method makes the adapter stop loading game data. This method should be called when
		 * the activity is destroyed. It also frees up the resources it may be internally using.
		 */
		public void shutdownAdapter() {
			this.puzzleLoaderExecutorService.shutdownNow();

			this.cache.clear();
		}

		/**
		 * Sets if the view of this adapter is scrolling. This flag should be used so the adapter
		 * knows when it can compute the games' thumbnails.
		 * 
		 * @param scrolling
		 *            true if the view is scrolling, and false otherwise.
		 */
		public void setScrolling(boolean scrolling) {
			this.scrolling = scrolling;

			/*
			 * We need to notify for changes, because by the time the scroll has stopped, the view
			 * has probably already drawn its children (without thumbnail). By notifying for
			 * changes, the views's thumbnails will be computed if necessary.
			 */
			if (!scrolling) {
				this.notifyDataSetChanged();
			}
		}

		/*
		 * Overridden to clear the cache when the adapter's data changes. If the data changes while
		 * we are in this activity, the data that is displayed to the user will be refreshed.
		 * However, since the thumbnails and other data are stored in the cache, we must clear it.
		 * Otherwise, old data may be used when creating the new views.
		 */
		public Cursor swapCursor(Cursor c) {
			this.cache.clear();
			return super.swapCursor(c);
		}

		/**
		 * Returns true if we can compute the data of the games that are currently visible.
		 */
		private boolean canLoadGameData() {
			return !this.scrolling;
		}

		/**
		 * Given a duration in seconds, this method returns a string that represents that same
		 * amount of time in hours, minutes and seconds.
		 * 
		 * @param seconds
		 *            the amount of seconds to convert into a string.
		 * @param context
		 *            a context.
		 */
		private String getGameDurationSoFar(long seconds, Context context) {
			long[] hms = UIUtilities.timeToHourMinSec(seconds);
			return context.getString(R.string.hour_min_sec_duration, hms[0], hms[1], hms[2]);
		}

		/**
		 * Puts into a background thread a task that will load the a game's data, that is, the
		 * puzzle and the image.
		 * <p>
		 * When the data is loaded, it is put into the {@link #cache}, and the handler is sent a
		 * message that contains the ID (under key "gameID") of the game whose data has been loaded
		 * into the cache.
		 * 
		 * @param gameID
		 *            the game whose data is loaded.
		 * @param imagePath
		 *            the path of the image of the puzzle. Null if the default image is to be used.
		 * @param initialStateS
		 *            the initial state of the puzzle, as returned by the content provider (
		 *            {@link NPuzzleContract.Games}).
		 * @param movesS
		 *            the set of moves applied by the used so far, as returned by the content
		 *            provider ({@link NPuzzleContract.Games}).
		 */
		private void loadGameData(final long gameID, final String imagePath,
				final String initialStateS, final String movesS) {
			try {
				this.puzzleLoaderExecutorService.execute(new Runnable() {
					public void run() {
						try {
							/*
							 * Build the NPuzzle and apply the sequence of moves that the user has
							 * performed so far.
							 */
							List<Integer> initialState = NPuzzle
									.stringToSequenceOfIntegers(initialStateS);
							List<Integer> moves = NPuzzle.stringToSequenceOfIntegers(movesS);
							NPuzzle puzzle = NPuzzle.newNPuzzleFromNAndConfiguration(
									initialState.size() - 1, initialState);
							puzzle.moveTiles(moves);

							/*
							 * Now load the image if it is non-null.
							 */
							Bitmap image = null;

							float density = getResources().getDisplayMetrics().density;

							if (imagePath != null) {
								image = ImageUtilities.secureDecode(imagePath,
										(int) (100 * density), (int) (100 * density));
							} else {
								/*
								 * Otherwise, create the default image. We want to keep this image
								 * in the cache so it does not get recreated every time an item is
								 * displayed.
								 */
								image = ImageUtilities.resampleBitmap(
										NPuzzleView.createDefaultImage(puzzle.getN()),
										(int) (100 * density), (int) (100 * density));
							}

							/*
							 * Put the game data into the cache.
							 */
							GameData gameData = new GameData();
							gameData.puzzle = puzzle;
							gameData.image = image;
							gameData.numMoves = moves.size();
							cache.put(gameID, gameData);

							/*
							 * Notify the handler.
							 */
							Message m = Message.obtain();
							m.getData().putLong("gameID", gameID);
							handler.sendMessage(m);

							/*
							 * Remove the game ID from the list of games whose data is being loaded.
							 */
							synchronized (gamesBeingLoadedLock) {
								gamesBeingLoaded.remove(gameID);
							}
						} catch (Exception e) {
							Log.e("NPuzzle", "Error loading game's data", e);
						}
					}
				});
			} catch (RejectedExecutionException e) {

			}
		}
	}

	/**
	 * @see es.odracirnumira.npuzzle.fragments.dialogs.DeleteSelectedGamesDialogFragment.IDeleteGameListener#deleteSelectedGames(boolean)
	 */
	public void deleteSelectedGames(boolean delete) {
		if (delete) {
			new DeleteGamesTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,
					gridView.getCheckedItemIds());
		}
	}
}

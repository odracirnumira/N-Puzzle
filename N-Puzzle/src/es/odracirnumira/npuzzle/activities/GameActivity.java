package es.odracirnumira.npuzzle.activities;

import java.util.ArrayList;
import java.util.List;

import es.odracirnumira.npuzzle.NPuzzleApplication;
import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.contentproviders.NPuzzleContentProvider;
import es.odracirnumira.npuzzle.contentproviders.NPuzzleContract;
import es.odracirnumira.npuzzle.fragments.dialogs.GameFinishedDialogFragment;
import es.odracirnumira.npuzzle.fragments.dialogs.ResignGameDialogFragmet;
import es.odracirnumira.npuzzle.fragments.dialogs.GameFinishedDialogFragment.IGameFinishedListener;
import es.odracirnumira.npuzzle.fragments.dialogs.ResignGameDialogFragmet.IResignGameListener;
import es.odracirnumira.npuzzle.model.FinishedNPuzzleGame;
import es.odracirnumira.npuzzle.model.NPuzzle;
import es.odracirnumira.npuzzle.model.NPuzzleGame;
import es.odracirnumira.npuzzle.model.NPuzzleGameStatistics;
import es.odracirnumira.npuzzle.model.NPuzzle.ITileListener;
import es.odracirnumira.npuzzle.settings.NPuzzleSettings;
import es.odracirnumira.npuzzle.tasks.DeleteGamesTask;
import es.odracirnumira.npuzzle.tasks.SaveFinishedGameTask;
import es.odracirnumira.npuzzle.tasks.SaveGameTask;
import es.odracirnumira.npuzzle.tasks.UpdateGameTask;
import es.odracirnumira.npuzzle.util.ImageUtilities;
import es.odracirnumira.npuzzle.util.UIUtilities;
import es.odracirnumira.npuzzle.view.NPuzzleView;
import es.odracirnumira.npuzzle.view.NPuzzleView.INPuzzleViewListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

//TODO: comment this class
public class GameActivity extends Activity implements ITileListener, IResignGameListener,
		INPuzzleViewListener, IGameFinishedListener {
	/**
	 * Input extra that defines the size of the puzzle for this new game. If this extra is defined,
	 * a new game is created, and its size will be determined by this integer value, which is the
	 * size of the puzzle size (that is, how many tiles are on each side of the puzzle). For
	 * instance, a value of 5 will create a 5x5 puzzle. Note that if both this extra and
	 * {@link #INPUT_LOAD_GAME_ID} are defined, {@link #INPUT_PUZZLE_SIDE_SIZE} is the one taking
	 * effect.
	 * <p>
	 * Its maximum value is determined by {@link NPuzzle#MAX_SIDE_SIZE}.
	 */
	public static final String INPUT_PUZZLE_SIDE_SIZE = "InputPuzzleSideSize";

	/**
	 * Input extra used when this activity should be used to load an existing game. This argument is
	 * the ID of the game (see {@link NPuzzleContentProvider}),as a long. The game will be loaded in
	 * its current state.
	 */
	public static final String INPUT_LOAD_GAME_ID = "LoadGameID";

	/**
	 * The view used to display the N puzzle.
	 */
	private NPuzzleView nPuzzleView;

	/**
	 * View that shows the number of moves so far.
	 */
	private TextView numMovesView;

	/**
	 * View that shows the elapsed time so far.
	 */
	private TextView elapsedTimeView;

	/**
	 * View that displays all the controls and stuff when a game is available.
	 */
	private View gameView;

	/**
	 * View that displays a progress bar indicating that the game is starting.
	 */
	private View loadingGameView;

	/**
	 * View used when the game that we tried to start could not be started.
	 */
	private View couldNotLoadGameView;

	/**
	 * View used when we could not load the puzzle's image.
	 */
	private View couldNotLoadPuzzleImageView;

	/**
	 * The N puzzle game that is being managed. May be null while the game is being started or
	 * loaded. It is also null if the game could not be loaded. However, if only the puzzle's image
	 * could not be loaded, this member will not be null.
	 */
	private NPuzzleGame game;

	/**
	 * This value is used to compute the game's elapsed time. Every time the user starts playing a
	 * game (either a new game or a previous game), we have to constantly keep track of how long he
	 * has been playing, and add that time to the elapsed time so far.
	 * <p>
	 * Every second or so, this value is updated, so the next time we measure time we can obtain the
	 * difference with this value (which will be a second ish) and get how much time has passed
	 * since the last time we updated this value.
	 * <p>
	 * This value starts being updated when a game is loaded, either because a new game has been
	 * created or because a game has been loaded from the content provider.
	 * <p>
	 * This value is measured in milliseconds.
	 * <p>
	 * <b>This value is initialized to -1</b>, and is also set to -1 every time we pause counting
	 * time (for instance, when we pause the activity).
	 */
	private long elapsedTimeStart;

	/**
	 * When we are starting a new game, this variable will hold the task that is in charge of
	 * starting it. Since the "new game task" can survive after configuration changes, this variable
	 * can be transmitted via the {@link #onRetainNonConfigurationInstance()} method.
	 */
	private StartNewGameTask startNewGameTask;

	/**
	 * When we are loading a game, this variable will hold the task that is in charge of loading it.
	 * Since the "load game task" can survive after configuration changes, this variable can be
	 * transmitted via the {@link #onRetainNonConfigurationInstance()} method.
	 */
	private LoadGameTask loadGameTask;

	/**
	 * Task that is changing the puzzle's image. This task does not survive to configuration
	 * changes, so if a configuration change occur, it will get destroyed. This valriable is null if
	 * the puzzle's image is not being changed.
	 */
	private ChangePuzzleImageTask changePuzzleImageTask;

	/**
	 * The settings object.
	 */
	private NPuzzleSettings settings;

	/**
	 * This flag is used when the user resigns from this game, so we do not try to update it when
	 * when leave the activity.
	 */
	private boolean resigned;

	/**
	 * This flag tells if the puzzle is solved, that is, the game finished. We need to keep track of
	 * this condition in case the user decides to stay in the game, specially if after finishing the
	 * game the activity is restarted (for instance, for a configuration change).
	 */
	private boolean finished;

	/**
	 * Request code used for selecting an image from the gallery.
	 */
	private static final int REQUEST_CODE_PICK_IMAGE_FROM_GALLERY = 0;

	/**
	 * Request code used for selecting an image from the file system.
	 */
	private static final int REQUEST_CODE_PICK_IMAGE_FROM_FILE_SYSTEM = 1;

	/**
	 * Handler used to dispatch messages. Among others, dispatches the messages that update the
	 * elapsed time view.
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_UPDATE_ELAPSED_TIME:
					long currentTime = SystemClock.uptimeMillis();

					/*
					 * If this is the first time we use elapsedTimeStart or we resume counting
					 * time...
					 */
					if (elapsedTimeStart == -1) {
						elapsedTimeStart = currentTime;
					}

					game.elapsedTime += (currentTime - elapsedTimeStart);
					elapsedTimeStart = currentTime;
					refreshElapsedTimeTextView();

					// Post another message that will be run in 1 sec
					this.sendEmptyMessageDelayed(MESSAGE_UPDATE_ELAPSED_TIME, 1000);

					break;
			}
		}
	};

	/**
	 * ID of the message that updates the elapsed time view ({@link #elapsedTimeView}). It also
	 * updates {@link #game}'s elapsedTime field and {@link #elapsedTimeStart}. Every time a message
	 * is processed, another one is posted to be run in one second after this, so the elapsed time
	 * keeps updating.
	 */
	private static final int MESSAGE_UPDATE_ELAPSED_TIME = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState == null ? null : savedInstanceState
				.getBundle("superState"));
		this.setContentView(R.layout.game_activity);

		this.settings = new NPuzzleSettings(this);
		this.resigned = false;

		// Load views
		this.gameView = findViewById(R.id.gameView);
		this.couldNotLoadPuzzleImageView = findViewById(R.id.couldNotLoadPuzzleImageView);
		this.couldNotLoadGameView = findViewById(R.id.couldNotLoadGameView);
		this.loadingGameView = findViewById(R.id.loadingGameView);
		this.nPuzzleView = (NPuzzleView) findViewById(R.id.nPuzzleView);
		this.numMovesView = (TextView) findViewById(R.id.numMovesTextView);
		this.elapsedTimeView = (TextView) findViewById(R.id.elapsedTimeTextView);

		// Register on the NPuzzleView to listen for animation events
		this.nPuzzleView.addViewListener(this);

		// Make all the views initially invisible.
		this.gameView.setVisibility(View.GONE);
		this.couldNotLoadPuzzleImageView.setVisibility(View.GONE);
		this.couldNotLoadGameView.setVisibility(View.GONE);
		this.loadingGameView.setVisibility(View.GONE);

		// Initially, elapsedTimeStart is set to -1 to signal that we have not started counting time
		// yet
		this.elapsedTimeStart = -1;

		/*
		 * If we are recreating the activity, just reload the game if it is present (it may be null
		 * if the game was not started when the activity was destroyed).
		 */
		if (savedInstanceState != null) {
			this.finished = savedInstanceState.getBoolean("finished");

			if (this.finished) {
				this.nPuzzleView.enableTouchEvents(false);
			}

			if (savedInstanceState.containsKey("game")) {
				this.game = savedInstanceState.getParcelable("game");
				this.game.nPuzzle.addTileListener(this);

				this.nPuzzleView.setNPuzzle(this.game.nPuzzle);
				this.nPuzzleView.setImage(this.game.puzzleImage);
				this.nPuzzleView.setImageRotation(this.game.imageRotation);
				this.showGameStartedScreen();
				return;
			}
		}

		/*
		 * Else, the game must be either created or loaded. Since this is a process that may take a
		 * long time (creating a random puzzle is a time consuming operation, as well as loading its
		 * image), we will delegate those task to AsyncTask objects. Before those tasks complete, we
		 * show a screen that displays a progress bar.
		 */
		this.showLoadingGameScreen();

		/*
		 * If a configuration change took place while starting a new game, just keep running the
		 * task that was running before the configuration change.
		 */
		try {
			this.startNewGameTask = (StartNewGameTask) getLastNonConfigurationInstance();
			if (this.startNewGameTask != null) {
				this.startNewGameTask.activity = this;
				return;
			}
		} catch (ClassCastException e) {
		}

		/*
		 * If a configuration change took place while loading a game, just keep running the task
		 * that was running before the configuration change.
		 */
		try {
			this.loadGameTask = (LoadGameTask) getLastNonConfigurationInstance();
			if (this.loadGameTask != null) {
				this.loadGameTask.activity = this;
				return;
			}
		} catch (ClassCastException e) {
		}

		/*
		 * Otherwise proceed as usual, creating the activity that starts the new game or loads a
		 * game from the content provider.
		 */

		/*
		 * If a size has been specified for the puzzle, it means that we must start a new game with
		 * such size. We have to store the game into the database, as an unfinished game.
		 */
		if (getIntent().hasExtra(INPUT_PUZZLE_SIDE_SIZE)) {
			this.startNewGame(getIntent().getIntExtra(INPUT_PUZZLE_SIDE_SIZE, -1));
		} else {
			/*
			 * Otherwise, we must load the game whose ID has been passed.
			 */
			if (!getIntent().hasExtra(INPUT_LOAD_GAME_ID)) {
				throw new RuntimeException(
						"You must specify either a new game's size or the ID of a game to load");
			} else {
				this.loadGame(getIntent().getLongExtra(INPUT_LOAD_GAME_ID, -1));
			}
		}
	}

	public void onResume() {
		super.onResume();

		/*
		 * If there is an active game, post a message to start updating the elapsed time. The very
		 * first time that the time starts getting updated is in the showGameStartedScreen() method.
		 * It is necessary to start updating time there because the first time that onResume() is
		 * called there will be no active game yet (it will be loading), so we cannot start counting
		 * time. However, if the user leaves the activity, we stop counting the elapsed time, so in
		 * that case, when the activity is resumed, we need to start counting the elapsed time
		 * again, which is why here we post a message to do so.
		 */
		if (this.game != null) {
			/*
			 * There may be a message updating time by the time this method is called (in case
			 * showGameStartedScreen()) is called before onResume()), so we do not add a new one in
			 * that case.
			 */
			if (!this.handler.hasMessages(MESSAGE_UPDATE_ELAPSED_TIME) && !this.finished) {
				this.handler.sendEmptyMessage(MESSAGE_UPDATE_ELAPSED_TIME);
			}
		}
	}

	public void onPause() {
		super.onPause();

		if (this.game != null) {
			/*
			 * Removes all the "update elapsed time" messages.
			 */
			this.handler.removeMessages(MESSAGE_UPDATE_ELAPSED_TIME);

			/*
			 * If we are leaving the activity and we have not resigned from the game nor finished
			 * it...
			 */
			if (!this.resigned && !this.finished) {
				/*
				 * Set "elapsedTimeStart" back to -1 so when the activity is resumed we can start
				 * measuring elapsed time again.
				 */
				this.elapsedTimeStart = -1;

				long currentTime = System.currentTimeMillis();

				/*
				 * Update the value that tells when the game was last played.
				 */
				this.game.lastPlayedTime = currentTime;

				/*
				 * Save the current game's state or save the game if it could not be previously
				 * saved into the database.
				 */
				new UpdateGameTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, this.game);
			}
		}
	}

	public void onDestroy() {
		super.onDestroy();

		/*
		 * Unregister as listener of the game. Since the game may be null in case it could not be
		 * loaded, we have to check that.
		 */
		if (this.game != null) {
			this.game.nPuzzle.removeTileListener(this);
			this.nPuzzleView.unregisterPuzzle();
		}

		/*
		 * We need to nullify the activity of this task so it does not blow up if the task finishes
		 * after the activity is destroyed.
		 */
		if (this.changePuzzleImageTask != null) {
			this.changePuzzleImageTask.activity = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	protected void onSaveInstanceState(Bundle outState) {
		Bundle superState = new Bundle();
		super.onSaveInstanceState(superState);
		outState.putBundle("superState", superState);

		if (this.game != null) {
			/*
			 * We may think of storing only the game ID to load it back from the content provider
			 * when the activity is recreated. However, since the game may have not been saved into
			 * the content provider, we may not have a valid ID, in which case we should save all
			 * the variables of the game. To keep things simple, thus, we just store all the stuff
			 * and restore it back in onCreate().
			 */
			outState.putParcelable("game", this.game);
		}

		outState.putBoolean("finished", this.finished);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState.getBundle("superState"));
	}

	/**
	 * This method creates a new puzzle with the specified size, and shows it on the activity's
	 * layout. This method also tries to store the new game into the database, as an unfinished
	 * game.
	 * <p>
	 * The whole task is delegated into a background thread, via a {@link StartNewGameTask}, which
	 * is retained through configuration changes. The created task is stored in
	 * {@link #startNewGameTask}.
	 * 
	 * @param puzzleSize
	 *            the size of the puzzle.
	 * @throws RuntimeException
	 *             if the puzzle size is not a valid one.
	 */
	private void startNewGame(int puzzleSize) {
		if (puzzleSize < NPuzzle.MIN_SIZE_SIZE || puzzleSize > NPuzzle.MAX_SIDE_SIZE) {
			throw new RuntimeException("Invalid puzzle size");
		}

		/*
		 * Start task that creates the new game.
		 */
		this.startNewGameTask = new StartNewGameTask();
		this.startNewGameTask.activity = this;
		this.startNewGameTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, puzzleSize);
	}

	/**
	 * This method loads into the activity a game that already exists from the content provider (
	 * {@link NPuzzleContract.Games}).
	 * <p>
	 * If the ID of the game to load is not a valid one, the game is not loaded and a toast message
	 * is shown.
	 * <p>
	 * The whole task is delegated into a background thread, via a {@link LoadGameTask}, which is
	 * retained through configuration changes. The created task is stored in {@link #loadGameTask}.
	 * 
	 * @param gameID
	 *            the ID of the game to load. If it is an invalid ID, no game is loaded.
	 */
	private void loadGame(long gameID) {
		this.loadGameTask = new LoadGameTask();
		this.loadGameTask.activity = this;
		this.loadGameTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, gameID);
	}

	/**
	 * Returns a suitable image for this puzzle according to the application's settings (
	 * {@link NPuzzleSettings#getNPuzzleImagesLocation()}).
	 * 
	 * @return a suitable image for the puzzle, or null if no image is found or required by the
	 *         puzzle according to its settings.
	 */
	private static String getPuzzleImage() {
		String imageLocation = new NPuzzleSettings(NPuzzleApplication.getApplication())
				.getNPuzzleImagesLocation();

		if (imageLocation.equals(NPuzzleSettings.IMAGES_LOCATION_NO_IMAGE)) {
			return null;
		} else {
			if (imageLocation.equals(NPuzzleSettings.IMAGES_LOCATION_GALLERY)) {
				return ImageUtilities.getGalleryImage();
			} else {
				return ImageUtilities.getImageFromDirectory(imageLocation);
			}
		}
	}

	/**
	 * Shows the view that displays a "loading message". Call this method when you are creating or
	 * loading the game. If the view is already visible, does nothing.
	 */
	private void showLoadingGameScreen() {
		if (this.loadingGameView.getVisibility() != View.VISIBLE) {
			this.couldNotLoadGameView.setVisibility(View.GONE);
			this.gameView.setVisibility(View.GONE);
			this.loadingGameView.setVisibility(View.VISIBLE);
			this.couldNotLoadPuzzleImageView.setVisibility(View.GONE);
		}
	}

	/**
	 * Shows the view that displays the game. Call this method when the game has been either created
	 * or loaded. This method should only be called once {@link #game} is set. This method also
	 * starts counting the elapsed time by sending a {@link #MESSAGE_UPDATE_ELAPSED_TIME} to
	 * {@link #handler}. If the view is already visible, does nothing.
	 */
	private void showGameStartedScreen() {
		if (this.gameView.getVisibility() != View.VISIBLE) {
			this.couldNotLoadGameView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out));
			this.gameView
					.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
			this.loadingGameView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out));
			this.couldNotLoadGameView.setVisibility(View.GONE);
			this.couldNotLoadPuzzleImageView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out));
			this.gameView.setVisibility(View.VISIBLE);
			this.loadingGameView.setVisibility(View.GONE);
			this.couldNotLoadPuzzleImageView.setVisibility(View.GONE);

			/*
			 * Show the number of moves and elapsed time when the game is displayed to the user.
			 */
			refreshElapsedTimeTextView();
			refreshNumMovesTextView();

			if (!this.finished) {
				this.handler.sendEmptyMessage(MESSAGE_UPDATE_ELAPSED_TIME);
			}

			/*
			 * Update the action bar menu.
			 */
			this.invalidateOptionsMenu();
		}
	}

	/**
	 * Shows the view that displays a message that tells that the game could not be loaded. If the
	 * view is already visible, does nothing.
	 */
	private void showCouldNotLoadGameScreen() {
		if (this.couldNotLoadGameView.getVisibility() != View.VISIBLE) {
			this.couldNotLoadGameView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_in));
			this.gameView.startAnimation(AnimationUtils
					.loadAnimation(this, android.R.anim.fade_out));
			this.loadingGameView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out));
			this.couldNotLoadPuzzleImageView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out));
			this.couldNotLoadGameView.setVisibility(View.VISIBLE);
			this.gameView.setVisibility(View.GONE);
			this.loadingGameView.setVisibility(View.GONE);
			this.couldNotLoadGameView.setVisibility(View.GONE);
			this.couldNotLoadPuzzleImageView.setVisibility(View.GONE);
		}
	}

	/**
	 * Shows the view that displays a message that tells that the puzzle's image could not be
	 * loaded. If the view is already visible, does nothing.
	 */
	private void showCouldNotLoadPuzzleImageScreen() {
		if (this.couldNotLoadPuzzleImageView.getVisibility() != View.VISIBLE) {
			this.couldNotLoadGameView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out));
			this.gameView.startAnimation(AnimationUtils
					.loadAnimation(this, android.R.anim.fade_out));
			this.loadingGameView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_out));
			this.couldNotLoadPuzzleImageView.startAnimation(AnimationUtils.loadAnimation(this,
					android.R.anim.fade_in));
			this.couldNotLoadGameView.setVisibility(View.GONE);
			this.gameView.setVisibility(View.GONE);
			this.loadingGameView.setVisibility(View.GONE);
			this.couldNotLoadPuzzleImageView.setVisibility(View.VISIBLE);

			/*
			 * Update the action bar menu.
			 */
			this.invalidateOptionsMenu();
		}
	}

	/*
	 * Returns a "StartNewGameTask" if present, or a "LoadGameTask" if present.
	 * 
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	public Object onRetainNonConfigurationInstance() {

		/*
		 * Nullify the correct task's activity so it does not blow up for referencing a destroyed
		 * activity.
		 */
		if (this.startNewGameTask != null) {
			this.startNewGameTask.activity = null;
			return this.startNewGameTask;
		}

		if (this.loadGameTask != null) {
			this.loadGameTask.activity = null;
			return this.loadGameTask;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.game_activity_menu, menu);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem changeImageMenuItem = menu.findItem(R.id.menuItemChangeImage);
		MenuItem displayDefaultImageMenuItem = menu.findItem(R.id.menuItemNoImage);
		MenuItem randomImageFromSelectedLocationMenuItem = menu
				.findItem(R.id.menuItemRandomImageFromSelectedLocation);
		MenuItem rotateImageMenuItem = menu.findItem(R.id.menuItemRotateImage);
		MenuItem resignMenuItem = menu.findItem(R.id.menuItemResignGame);

		/*
		 * If there is no active game, hide the "change image" menu.
		 */
		if (this.game == null) {
			changeImageMenuItem.setVisible(false);
			resignMenuItem.setVisible(false);
			rotateImageMenuItem.setVisible(false);
		} else {
			/*
			 * Else, show the menu and change the visibility of its subitems depending on state of
			 * the game.
			 */
			changeImageMenuItem.setVisible(true);

			/*
			 * If we are not displaying the default image, show option to display default image.
			 * Otherwise, hide it.
			 * 
			 * If we are displaying the default image, we do not want the user to rotate the image.
			 */
			if (this.game.puzzleImagePath == null) {
				displayDefaultImageMenuItem.setVisible(false);
				rotateImageMenuItem.setVisible(false);
			} else {
				displayDefaultImageMenuItem.setVisible(true);
				rotateImageMenuItem.setVisible(true);
			}

			/*
			 * If the user has selected either the gallery or a custom directory for images, show
			 * the option to get a new random image from the selected location. Otherwise, hide it.
			 */
			if (this.settings.getNPuzzleImagesLocation() != NPuzzleSettings.IMAGES_LOCATION_NO_IMAGE) {
				randomImageFromSelectedLocationMenuItem.setVisible(true);
			} else {
				randomImageFromSelectedLocationMenuItem.setVisible(false);
			}

			/*
			 * If the game has finished, we need to hide some options.
			 */
			if (this.finished) {
				resignMenuItem.setVisible(false);
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menuItemNoImage: {
				if (this.game != null) {
					this.changeGameImage(true, null);
				}

				return true;
			}

			case R.id.menuItemRandomImageFromSelectedLocation: {
				if (this.game != null) {
					this.changeGameImage(false, null);
				}

				return true;
			}

			case R.id.menuItemPickImageFromGallery: {
				if (this.game != null) {
					Intent intent = new Intent(Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
					startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE_FROM_GALLERY);
				}

				return true;
			}

			case R.id.menuItemPickImageFromDirectory: {
				if (this.game != null) {
					Intent intent = new Intent(this, FileChooserActivity.class);
					intent.putExtra(FileChooserActivity.INPUT_SELECTION_MODE,
							FileChooserActivity.MODE_SELECTION_SINGLE);
					intent.putExtra(FileChooserActivity.INPUT_TYPE_MODE,
							FileChooserActivity.MODE_TYPE_FILE);
					startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE_FROM_FILE_SYSTEM);
				}

				return true;
			}

			case R.id.menuItemRotateImage: {
				this.game.imageRotation = (this.game.imageRotation + 90) % 360;
				this.nPuzzleView.setImageRotation(this.game.imageRotation);
				return true;
			}

			case R.id.menuItemResignGame: {
				FragmentManager manager = getFragmentManager();
				ResignGameDialogFragmet dialog = new ResignGameDialogFragmet();
				dialog.show(manager, "resignGameDialog");
				return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CODE_PICK_IMAGE_FROM_FILE_SYSTEM:
				if (resultCode == FileChooserActivity.RESULT_OK) {
					ArrayList<String> selectedFiles = data.getExtras().getStringArrayList(
							FileChooserActivity.KEY_RESULT_SELECTED_FILES);

					this.changeGameImage(false, selectedFiles.get(0));
				}

				break;
			case REQUEST_CODE_PICK_IMAGE_FROM_GALLERY:
				if (resultCode == Activity.RESULT_OK) {
					try {
						String[] projection = { MediaStore.Images.ImageColumns.DATA };
						Cursor c = NPuzzleApplication.getApplication().getContentResolver()
								.query(data.getData(), projection, null, null, null);
						c.moveToFirst();
						this.changeGameImage(false, c.getString(0));
					} catch (Exception e) {
					}
				}

				break;
		}
	}

	/**
	 * AsyncTask that starts a new game. Since the process of starting a new game may take a while
	 * (creating a random solvable puzzle is a time-consuming tasks), this task is delegated to a
	 * background thread via an AsyncTask. The created game always has a puzzle that can be solved
	 * but which is not initially solved.
	 * <p>
	 * The input parameter of this task is the side size of the puzzle, which is an integer, and its
	 * result is the game that has been created.
	 * <p>
	 * In order to properly use this task you have to set its {@link #activity} field, before
	 * running it. It is the activity on which the task is being run. When a configuration change is
	 * going to take pace and the activity is going to be destroyed, the activity should be detached
	 * from the task, which is done in {@link GameActivity#onRetainNonConfigurationInstance()}, to
	 * survive through configuration changes. The instance of this class managed by the
	 * {@link GameActivity} is stored in {@link GameActivity#startNewGameTask}.
	 * <p>
	 * When this task completes, the {@code GameActivity} is notified so it displays the new game.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private static class StartNewGameTask extends AsyncTask<Integer, Void, NPuzzleGame> {
		/**
		 * The activity that this task is associated to.
		 */
		public GameActivity activity;

		protected NPuzzleGame doInBackground(Integer... params) {
			/*
			 * Create the puzzle and load image.
			 */
			NPuzzleGame game = new NPuzzleGame();

			/*
			 * The ranom puzzle may be initially solved, so we check that it is not solved.
			 */
			game.nPuzzle = NPuzzle.newRandomNPuzzleFromSideSize(params[0]);

			while (game.nPuzzle.isSolved()) {
				game.nPuzzle = NPuzzle.newRandomNPuzzleFromSideSize(params[0]);
			}

			game.initialState = game.nPuzzle.toString();
			game.puzzleImagePath = getPuzzleImage();

			if (game.puzzleImagePath.equals("")) {
				Log.e("NPuzzle", "ERRRORORRO");
			}

			try {
				if (game.puzzleImagePath != null) {
					game.puzzleImage = ImageUtilities.secureDecode(game.puzzleImagePath,
							NPuzzleView.IMAGE_MAX_WIDTH, NPuzzleView.IMAGE_MAX_HEIGHT);
				} else {
					game.puzzleImage = NPuzzleView.createDefaultImage(game.nPuzzle.getN());
				}
			} catch (OutOfMemoryError e) {
				/*
				 * If there is any error loading the image, we will not show the game view, but the
				 * "could not load puzzle's" view.
				 */
			}

			game.startTime = System.currentTimeMillis();
			game.moves = new ArrayList<Integer>();
			game.elapsedTime = 0;
			game.lastPlayedTime = game.startTime;
			game.imageRotation = 0;

			return game;
		}

		protected void onPostExecute(NPuzzleGame result) {
			if (this.activity != null) {
				/*
				 * Set the game into the activity.
				 */
				this.activity.game = result;

				/*
				 * Try to save the game.
				 */
				new SaveGameTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, result);

				/*
				 * Add listener to the puzzle so every move is registered by the activity.
				 */
				result.nPuzzle.addTileListener(this.activity);

				this.activity.nPuzzleView.setNPuzzle(result.nPuzzle);

				if (result.puzzleImage != null) {
					/*
					 * Set up the NPuzzleView's image and show the view that displays the game if
					 * the puzzle's image could be loaded.
					 */
					this.activity.nPuzzleView.setImage(result.puzzleImage);
					this.activity.showGameStartedScreen();
				} else {
					/*
					 * If we could not load the puzzle's image, show the
					 * "could not load puzzle image" view.
					 */
					this.activity.showCouldNotLoadPuzzleImageScreen();
				}

				// Nullify the task in the activity to signal the game is not being loaded anymore
				this.activity.startNewGameTask = null;
			}
		}
	}

	/**
	 * AsyncTask that loads a game from the content provider. Since the process of loading a game
	 * may take a while (it involves accessing a content provider, which may be a time-consuming
	 * task), this task is delegated to a background thread via an AsyncTask.
	 * <p>
	 * The input parameter of this task is the ID of the game, which is a long, and its result is
	 * the game that has been loaded, or null if it could not be loaded.
	 * <p>
	 * In order to properly use this task you have to set its {@link #activity} field, before
	 * running it. It is the activity on which the task is being run. When a configuration change is
	 * going to take pace and the activity is going to be destroyed, the activity should be detached
	 * from the task, which is done in {@link GameActivity#onRetainNonConfigurationInstance()}, to
	 * survive through configuration changes. The instance of this class managed by the
	 * {@link GameActivity} is stored in {@link GameActivity#loadGameTask}.
	 * <p>
	 * When this task completes, the {@code GameActivity} is notified so it displays the new game.
	 * If the game could not be loaded (for instance, because the input ID was not valid), the
	 * activity will display an error message.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private static class LoadGameTask extends AsyncTask<Long, Void, NPuzzleGame> {
		private GameActivity activity;

		protected NPuzzleGame doInBackground(Long... params) {
			try {
				long gameID = params[0];

				Cursor cursor = NPuzzleApplication
						.getApplication()
						.getContentResolver()
						.query(ContentUris
								.withAppendedId(NPuzzleContract.Games.CONTENT_URI, gameID),
								null, null, null, null);

				if (cursor != null && cursor.getCount() != 0) {
					cursor.moveToFirst();

					/*
					 * Extract game's data from the database.
					 */
					String initialStateS = cursor.getString(cursor
							.getColumnIndex(NPuzzleContract.Games.INITIAL_STATE));
					long elapsedTime = cursor.getLong(cursor
							.getColumnIndex(NPuzzleContract.Games.ELAPSED_TIME));
					String movesS = cursor.getString(cursor
							.getColumnIndex(NPuzzleContract.Games.MOVES));
					long startTime = cursor.getLong(cursor
							.getColumnIndex(NPuzzleContract.Games.START_TIME));
					String imagePath = cursor.getString(cursor
							.getColumnIndex(NPuzzleContract.Games.IMAGE_PATH));
					long lastPlayedTime = cursor.getLong(cursor
							.getColumnIndex(NPuzzleContract.Games.LAST_PLAYED_TIME));
					int imageRotation = cursor.getInt(cursor
							.getColumnIndex(NPuzzleContract.Games.IMAGE_ROTATION));

					/*
					 * Create the puzzle and load the image.
					 */
					NPuzzleGame game = new NPuzzleGame();
					game.gameID = gameID;
					List<Integer> initialState = NPuzzle.stringToSequenceOfIntegers(initialStateS);
					game.nPuzzle = NPuzzle.newNPuzzleFromNAndConfiguration(initialState.size() - 1,
							initialState);
					game.moves = NPuzzle.stringToSequenceOfIntegers(movesS);
					game.nPuzzle.moveTiles(game.moves);
					game.initialState = initialStateS;
					game.puzzleImagePath = imagePath;

					try {
						if (game.puzzleImagePath != null) {
							game.puzzleImage = ImageUtilities.secureDecode(game.puzzleImagePath,
									NPuzzleView.IMAGE_MAX_WIDTH, NPuzzleView.IMAGE_MAX_HEIGHT);

							/*
							 * If the specified image could not be loaded, load the default image.
							 */
							if (game.puzzleImage == null) {
								game.puzzleImage = NPuzzleView.createDefaultImage(game.nPuzzle
										.getN());
							}
						} else {
							game.puzzleImage = NPuzzleView.createDefaultImage(game.nPuzzle.getN());
						}
					} catch (OutOfMemoryError e) {
						/*
						 * If there is any error loading the image, we will not show the game view,
						 * but the "could not load puzzle's image" view.
						 */
					}

					game.startTime = startTime;
					game.elapsedTime = elapsedTime;
					game.lastPlayedTime = lastPlayedTime;
					game.imageRotation = imageRotation;

					return game;
				} else {
					return null;
				}
			} catch (Exception e) {
				return null;
			}
		}

		protected void onPostExecute(NPuzzleGame result) {
			if (this.activity != null) {
				if (result != null) {
					/*
					 * Set the game into the activity.
					 */
					this.activity.game = result;

					/*
					 * Add listener to the puzzle so every move is registered by the activity.
					 */
					result.nPuzzle.addTileListener(this.activity);

					this.activity.nPuzzleView.setNPuzzle(result.nPuzzle);
					this.activity.nPuzzleView.setImageRotation(result.imageRotation);

					/*
					 * Set up the NPuzzleView's image and show the view that displays the game if
					 * the puzzle's image could be loaded.
					 */
					if (result.puzzleImage != null) {
						this.activity.nPuzzleView.setImage(result.puzzleImagePath);
						this.activity.showGameStartedScreen();
					} else {
						/*
						 * If we could not load the puzzle's image, show the
						 * "could not load puzzle image" view.
						 */
						this.activity.showCouldNotLoadPuzzleImageScreen();
					}
				} else {
					/*
					 * If the game could not be loaded, show the view that says that it could not be
					 * displayed.
					 */
					this.activity.showCouldNotLoadGameScreen();
				}

				// Nullify the task in the activity to signal the game is not being loaded
				// anymore
				this.activity.loadGameTask = null;
			}
		}

		protected void onCancelled(NPuzzleGame result) {
			if (this.activity != null) {
				// Nullify the task in the activity to signal the game is not being loaded
				// anymore
				this.activity.loadGameTask = null;
			}
		}
	}

	/**
	 * {@link AsyncTask} that changes the puzzle's image. This task loads an image from the file
	 * system and sets it on {@link GameActivity#nPuzzleView}. If the image could not be loaded,
	 * then nothing happens.
	 * <p>
	 * This task's {@link #activity} is the hosting {@link GameActivity}. It must be set before
	 * running the task, and should be nullified if the activity is destroyed before the task
	 * completes. The task sets the {@link GameActivity#changePuzzleImageTask} field to null after
	 * finishing, in {@link #onPostExecute(Object)}. Note that the {@link GameActivity#game} member
	 * is modified by this activity to reflect the fact that the image has changed.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private static class ChangePuzzleImageTask extends
			AsyncTask<ChangePuzzleImageTask.Arguments, Void, Bitmap> {
		/**
		 * The input argument for a {@link ChangePuzzleImageTask}. It specifies the type of image
		 * that should be loaded. Instantes of this class are created via static factory methods.
		 * 
		 * @author Ricardo Juan Palma Durán
		 * 
		 */
		private static class Arguments {
			/**
			 * true if the default image should be loaded for the puzzle.
			 */
			private boolean defaultImage;

			/**
			 * If {@link #defaultImage} is false, then:
			 * <ul>
			 * <li>If this variable is null, it means that a random image according to the
			 * application's settings ({@link NPuzzleSettings}) should be selected.
			 * <li>If this variable is not null, it represents the absolute path of the image to
			 * load.
			 * </ul>
			 */
			private String imagePath;

			/**
			 * N of the puzzle. It is used to create the default image.
			 */
			private int N;

			/**
			 * Creates an {@link Arguments} object that tells the {@link ChangePuzzleImageTask} to
			 * load the default image.
			 * 
			 * @param N
			 *            the value of N for the puzzle.
			 * @return the {@code Arguments} object.
			 */
			public static Arguments createDefaultImageArguments(int N) {
				Arguments args = new Arguments();
				args.defaultImage = true;
				args.imagePath = null;
				args.N = N;
				return args;
			}

			/**
			 * Creates an {@link Arguments} object that tells the {@link ChangePuzzleImageTask} to
			 * load a random image according to the application's settings ({@link NPuzzleSettings}
			 * ). If no image can be loaded, nothing happens.
			 * 
			 * @param N
			 *            the value of N for the puzzle.
			 * @return the {@code Arguments} object.
			 */
			public static Arguments createRandomImageArguments(int N) {
				Arguments args = new Arguments();
				args.defaultImage = false;
				args.imagePath = null;
				args.N = N;
				return args;
			}

			/**
			 * Creates an {@link Arguments} object that tells the {@link ChangePuzzleImageTask} to
			 * load a specific image. If the image cannot be loaded, nothing happens.
			 * 
			 * @param imagePath
			 *            the path of the image to load.
			 * @param N
			 *            the value of N for the puzzle.
			 * @return the {@code Arguments} object.
			 */
			public static Arguments createCustomImageArguments(String imagePath, int N) {
				Arguments args = new Arguments();
				args.defaultImage = false;
				args.imagePath = imagePath;
				args.N = N;
				return args;
			}
		}

		/**
		 * The progress dialog.
		 */
		private ProgressDialog dialog;

		/**
		 * The hosting {@link GameActivity}.
		 */
		private GameActivity activity;

		/**
		 * The input arguments.
		 */
		private Arguments arguments;

		/**
		 * The path of the image. This is used to pass this value to the
		 * {@link #onPostExecute(Bitmap)} method.
		 */
		private String imagePath;

		protected void onPreExecute() {
			/*
			 * Show the progress dialog.
			 */
			if (this.activity != null) {
				this.dialog = ProgressDialog.show(this.activity, null,
						this.activity.getString(R.string.loading_image), true, true,
						new OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
								cancel(false);
							}
						});
			}
		}

		protected Bitmap doInBackground(Arguments... params) {
			this.arguments = params[0];

			Bitmap result = null;

			/*
			 * If we have been requested to get the default image...
			 */
			if (params[0].defaultImage) {
				this.imagePath = null;
				result = NPuzzleView.createDefaultImage(params[0].N);
			} else {
				this.imagePath = params[0].imagePath;

				if (this.imagePath == null) {
					/*
					 * Else, if we were requested a random image from the application's settings...
					 */
					this.imagePath = getPuzzleImage();
				}

				/*
				 * Load the image.
				 */
				if (this.imagePath != null) {
					result = ImageUtilities.secureDecode(this.imagePath,
							NPuzzleView.IMAGE_MAX_WIDTH, NPuzzleView.IMAGE_MAX_HEIGHT);
				}
			}

			return result;
		}

		protected void onPostExecute(Bitmap result) {
			if (this.activity != null) {
				/*
				 * If the image could not be loaded, just do not change the current image.
				 * Otherwise, change the image.
				 */
				if (result != null) {
					this.activity.game.puzzleImagePath = this.imagePath;
					this.activity.game.puzzleImage = result;
					this.activity.nPuzzleView.setImage(result);

					/*
					 * If this is the default image, set rotation to 0.
					 */
					if (this.arguments.defaultImage) {
						this.activity.nPuzzleView.setImageRotation(0);
						this.activity.game.imageRotation = 0;
					}
				} else {
					Toast.makeText(this.activity, R.string.could_not_load_image, Toast.LENGTH_SHORT)
							.show();
				}

				// Dismiss dialog
				this.dialog.dismiss();

				// Update options menu
				this.activity.invalidateOptionsMenu();

				// Nullify task on the activity to indicate that we are not loading the image
				// anymore
				this.activity.changePuzzleImageTask = null;
			}
		}
	}

	/**
	 * 
	 * @see es.odracirnumira.npuzzle.model.NPuzzle.ITileListener#tileMoved(int, int, int)
	 */
	public void tileMoved(int tile, int oldPos, int newPos) {
		/*
		 * Update the list of moves and refresh the number of moves label.
		 */
		this.game.moves.add(tile);
		this.refreshNumMovesTextView();
	}

	/**
	 * Refreshes the view that displays the number of moves so far. It uses {@link #game} to extract
	 * the number of moves.
	 */
	private void refreshNumMovesTextView() {
		this.numMovesView.setText(getResources().getQuantityString(R.plurals.num_moves,
				this.game.moves.size(), this.game.moves.size()));
	}

	/**
	 * Refreshes the view that displays the elapsed time so far. It uses {@link #game} to extract
	 * the elapsed time so far.
	 */
	private void refreshElapsedTimeTextView() {
		this.elapsedTimeView.setText(UIUtilities
				.timeToHourMinSecChrono(this.game.elapsedTime / 1000));
	}

	/**
	 * Changes the image of this game, and updates the view and the {@link #game} object. It also
	 * refreshes the options menu according to the image.
	 * <p>
	 * Note that this method spawns a {@link ChangePuzzleImageTask} to do this. This task starts by
	 * showing a progress dialog that, if cancelled, cancels the process of changing the image. This
	 * task is not preserved among configuration changes, so it gets destroyed in case a
	 * configuration change happens.
	 * <p>
	 * If there is a task that is currently changing the puzzle's image, this method does nothing.
	 * <p>
	 * If no game is active, this method does nothing.
	 * 
	 * @param defaultImage
	 *            if true, this method will load the default image. If false, it will load the image
	 *            specified by <code>newPath</code>.
	 * @param newPath
	 *            if <code>defaultImage</code> is false, this represents the path of the image to be
	 *            loaded on the puzzle. Use null to get a random image according to the
	 *            application's settings ({@link NPuzzleSettings#getNPuzzleImagesLocation()}). If
	 *            non null, it represents the absolute path of the image to be loaded.
	 */
	private void changeGameImage(boolean defaultImage, String newPath) {
		if (this.changePuzzleImageTask == null) {
			this.changePuzzleImageTask = new ChangePuzzleImageTask();
			this.changePuzzleImageTask.activity = this;

			ChangePuzzleImageTask.Arguments taskArguments;

			if (defaultImage) {
				taskArguments = ChangePuzzleImageTask.Arguments
						.createDefaultImageArguments(this.game.nPuzzle.getN());
			} else if (newPath == null) {
				taskArguments = ChangePuzzleImageTask.Arguments
						.createRandomImageArguments(this.game.nPuzzle.getN());
			} else {
				taskArguments = ChangePuzzleImageTask.Arguments.createCustomImageArguments(newPath,
						this.game.nPuzzle.getN());
			}

			this.changePuzzleImageTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, taskArguments);
		}
	}

	/**
	 * 
	 * @see es.odracirnumira.npuzzle.fragments.dialogs.ResignGameDialogFragmet.IResignGameListener#resign(boolean)
	 */
	public void resign(boolean resigned) {
		if (resigned) {
			/*
			 * Finish the activity and spawn a new task to delete the game.
			 */
			new DeleteGamesTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,
					new long[] { this.game.gameID });
			this.resigned = true;
			finish();
		}
	}

	/**
	 * 
	 * @see es.odracirnumira.npuzzle.view.NPuzzleView.INPuzzleViewListener#movingTileAnimationEnded()
	 */
	public void movingTileAnimationEnded() {
		/*
		 * When a tile animation ends, it means that, from the player's perspective, the file has
		 * stopped moving, so this is when we have to check if the puzzle is solved.
		 */
		if (this.game.nPuzzle.isSolved()) {
			/*
			 * When the game is solved, we show a dialog telling the game statistics. Also, we offer
			 * him to stay in the game or leave it.
			 */
			FragmentManager manager = getFragmentManager();
			GameFinishedDialogFragment dialog = new GameFinishedDialogFragment();
			Bundle args = new Bundle();

			NPuzzleGameStatistics statistics = new NPuzzleGameStatistics();
			statistics.elapsedTime = this.game.elapsedTime;
			statistics.numMoves = this.game.moves.size();
			args.putParcelable(GameFinishedDialogFragment.INPUT_GAME_STATISTICS, statistics);
			dialog.setArguments(args);
			dialog.show(manager, "gameFinishedDialog");

			/*
			 * We also must stop counting time.
			 */
			this.handler.removeMessages(MESSAGE_UPDATE_ELAPSED_TIME);

			/*
			 * We enable a flag that tells that the game has finished, so we can keep track of this
			 * fact even if the activity is restarted or something.
			 */
			this.finished = true;

			/*
			 * Delete the current game from the list of games.
			 */
			new DeleteGamesTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,
					new long[] { this.game.gameID });

			/*
			 * Save the current game as a new "finished game".
			 */
			FinishedNPuzzleGame finishedGame = new FinishedNPuzzleGame();
			finishedGame.elapsedTime = this.game.elapsedTime;
			finishedGame.finishedTime = System.currentTimeMillis();
			finishedGame.gameID = this.game.gameID;
			finishedGame.imageRotation = this.game.imageRotation;
			finishedGame.initialState = this.game.initialState;
			finishedGame.moves = this.game.moves;
			finishedGame.nPuzzle = this.game.nPuzzle;
			finishedGame.puzzleImage = this.game.puzzleImage;
			finishedGame.puzzleImagePath = this.game.puzzleImagePath;
			finishedGame.startTime = this.game.startTime;

			new SaveFinishedGameTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, finishedGame);
		}
	}

	/**
	 * 
	 * @see es.odracirnumira.npuzzle.view.NPuzzleView.INPuzzleViewListener#movingTileAnimationStarted()
	 */
	public void movingTileAnimationStarted() {

	}

	/**
	 * @see es.odracirnumira.npuzzle.fragments.dialogs.GameFinishedDialogFragment.IGameFinishedListener#stayInGame(boolean)
	 */
	public void stayInGame(boolean stayInGame) {
		if (stayInGame) {
			/*
			 * If we decide to stay in the current game, we disable the view.
			 */
			this.nPuzzleView.enableTouchEvents(false);
			this.invalidateOptionsMenu();
		} else {
			/*
			 * Else, end the activity.
			 */
			finish();
		}
	}
}

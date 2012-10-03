package es.odracirnumira.npuzzle.activities;

import es.odracirnumira.npuzzle.R;
import es.odracirnumira.npuzzle.fragments.dialogs.NewGameDialogFragment;
import es.odracirnumira.npuzzle.fragments.dialogs.NewCustomGameDialogFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * The main activity of the game. This activity displays multiple options, such as:
 * <ul>
 * <li>Start a new game.
 * <li>Load an unfinished game to resume it.
 * <li>Load a finished game to replay it.
 * <li>Access the settings screen.
 * </ul>
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class MainActivity extends Activity implements
		NewGameDialogFragment.INewGameRequestListener,
		NewCustomGameDialogFragment.INewCustomGameRequestListener {
	/**
	 * Button that starts a new game.
	 */
	private Button newGameButton;

	/**
	 * Button that loads a previous game.
	 */
	private Button loadGameButton;

	/**
	 * Button that replays a finished game.
	 */
	private Button replayGameButton;

	/**
	 * Button that displays the settings button.
	 */
	private Button settingsButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_activity);

		this.setupNewGameButton();
		this.setupLoadGameButton();
		this.setupSettingsButton();
	}

	/**
	 * Sets up the load game button.
	 */
	private void setupLoadGameButton() {
		this.loadGameButton = (Button) findViewById(R.id.loadGameButton);
		this.loadGameButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, LoadGameActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * Sets up the new game button.
	 */
	private void setupNewGameButton() {
		this.newGameButton = (Button) findViewById(R.id.newGameButton);
		this.newGameButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showNewGameDialog();
			}
		});
	}

	/**
	 * Shows the dialog to start a new game. This is the dialog that shows all the difficulty
	 * levels.
	 */
	private void showNewGameDialog() {
		FragmentManager manager = getFragmentManager();
		NewGameDialogFragment dialog = new NewGameDialogFragment();
		dialog.show(manager, "newGameDialog");
	}

	/**
	 * Shows the dialog to start a new custom game. The user is allowed to set the puzzle size.
	 */
	private void showNewCustomGameDialog() {
		FragmentManager manager = getFragmentManager();
		NewCustomGameDialogFragment dialog = new NewCustomGameDialogFragment();
		dialog.show(manager, "newCustomGameDialog");
	}

	/**
	 * Starts a new game with a specific puzzle size.
	 * 
	 * @param puzzleSize1
	 *            the puzzle size, which is the size of the side of the puzzle. (for instance, a
	 *            value of 3 would create a 3x3 puzzle).
	 */
	private void startNewGame(int puzzleSize) {
		Intent intent = new Intent(MainActivity.this, GameActivity.class);
		intent.putExtra(GameActivity.INPUT_PUZZLE_SIDE_SIZE, puzzleSize);
		startActivity(intent);
	}

	/**
	 * Sets up the settings button.
	 */
	private void setupSettingsButton() {
		this.settingsButton = (Button) findViewById(R.id.settingsButton);
		this.settingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, NPuzzleSettingsActivity.class);
				startActivity(intent);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.odracirnumira.npuzzle.fragments.dialogs.NewGameDialogFragment.INewGameSelected#newGame
	 * (int)
	 */
	public void newGame(int gameSize) {
		if (gameSize != -1) {
			startNewGame(gameSize);
		} else {
			showNewCustomGameDialog();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.odracirnumira.npuzzle.fragments.dialogs.NewCustomGameDialogFragment.INewCustomGameSelected
	 * #newCustomGame(int)
	 */
	public void newCustomGame(int gameSize) {
		startNewGame(gameSize);
	}
}

package es.odracirnumira.npuzzle.view;

import java.util.Arrays;

import es.odracirnumira.npuzzle.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class PuzzleSolutionControlsView extends FrameLayout {
	/**
	 * The play/pause button.
	 */
	private View playPauseButton;

	/**
	 * The stop button.
	 */
	private View stopButton;

	/**
	 * The decrease speed button.
	 */
	private View decreaseSpeedButton;

	/**
	 * The increase speed button.
	 */
	private View increaseSpeedButton;

	/**
	 * Interface for a listener that receives events when the control buttons are pressed.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface IControlsListener {
		/**
		 * Called when the play button is pressed.
		 */
		public void play();

		/**
		 * Called when the pause button is pressed.
		 */
		public void pause();

		/**
		 * Called when the speed changes (either because the increase speed or decrease speed button
		 * is pressed).
		 * 
		 * @param newSpeed
		 *            the new speed.
		 */
		public void speedChanged(int newSpeed);

		/**
		 * Called when the stop button is pressed.d
		 */
		public void stop();
	}

	/**
	 * Array that stores the different speed levels for the controls, from the slower (position 0)
	 * to the faster (last position).
	 */
	private int[] speedLevels;

	/**
	 * Index of the current speed level (over {@link #speedLevels}).
	 */
	private int speedLevelIndex;

	/**
	 * The listener used to manage events. May be null if not set.
	 */
	private IControlsListener listener;

	/**
	 * Flag that tells if the view is currently playing the solution or paused.
	 */
	private boolean isPlaying;

	public PuzzleSolutionControlsView(Context context) {
		super(context);
		this.initView(null);
	}

	public PuzzleSolutionControlsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initView(attrs);
	}

	public PuzzleSolutionControlsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.initView(attrs);
	}

	/**
	 * Initializes the view.
	 * 
	 * @param attrs
	 *            the set of attributes. May be null if not defined.
	 */
	private void initView(AttributeSet attrs) {
		this.addView(((LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.puzzle_solution_controls_view,
				this, false));

		this.isPlaying = false;

		// Get views
		this.playPauseButton = this.findViewById(R.id.playPauseButton);
		this.stopButton = this.findViewById(R.id.stopButton);
		this.decreaseSpeedButton = this.findViewById(R.id.decreaseSpeedButton);
		this.increaseSpeedButton = this.findViewById(R.id.increaseSpeedButton);

		// Set attributes if defined
		if (attrs != null) {
			TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs,
					R.styleable.PuzzleSolutionControlsView, 0, 0);

			/*
			 * Get speed values if they have been defined.
			 */
			String speedValuesString = a
					.getString(R.styleable.PuzzleSolutionControlsView_speedLevels);

			if (speedValuesString != null) {
				String[] values = speedValuesString.trim().split("\\|");

				int[] speedValues = new int[values.length];

				for (int i = 0; i < values.length; i++) {
					speedValues[i] = Integer.parseInt(values[i]);
				}

				this.setSpeedLevels(speedValues);
			}

			a.recycle();
		}

		// Add listeners
		this.playPauseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (listener != null)
					if (isPlaying) {
						listener.pause();
					} else {
						listener.play();
					}

				isPlaying = !isPlaying;
			}
		});

		this.stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (listener != null)
					listener.stop();
			}
		});

		this.increaseSpeedButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (speedLevelIndex < speedLevels.length) {
					speedLevelIndex++;
				}

				if (listener != null)
					listener.speedChanged(getSpeedLevel());
			}
		});

		this.decreaseSpeedButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (speedLevelIndex > 0) {
					speedLevelIndex--;
				}

				if (listener != null)
					listener.speedChanged(getSpeedLevel());
			}
		});
	}

	/**
	 * Returns an array that contains the speed levels, being the first element the slowest speed,
	 * and the last element the fastest speed.
	 * 
	 * @return an array that contains the speed levels.
	 */
	public int[] getSpeedLevels() {
		return this.speedLevels.clone();
	}

	/**
	 * Returns the current speed level.
	 * 
	 * @return the current speed level.
	 */
	public int getSpeedLevel() {
		return this.speedLevels[this.speedLevelIndex];
	}

	/**
	 * Sets the speed levels for the controls. The input array can have negative values and even
	 * repetitions (repetitions are ignored).
	 * <p>
	 * After setting the speed levels, the current speed level (returned by {@link #getSpeedLevel()}
	 * ) will be the lowest value from the array.
	 * 
	 * @param speedLevels
	 *            an array that contains the set of speed levels.
	 */
	public void setSpeedLevels(int[] speedLevels) {
		if (speedLevels == null) {
			throw new IllegalArgumentException("null speed levels array");
		}

		// Get values without repetitions
		int[] speedLevelsWithoutRepetitions = new int[speedLevels.length];
		int numWithoutRepetitions = 0;

		for (int i = 0; i < speedLevels.length; i++) {
			boolean noMatch = true;

			for (int j = 0; j < numWithoutRepetitions && noMatch; j++) {
				if (speedLevels[i] == speedLevelsWithoutRepetitions[j]) {
					noMatch = false;
				}
			}

			if (noMatch) {
				speedLevelsWithoutRepetitions[numWithoutRepetitions] = speedLevels[i];
				numWithoutRepetitions++;
			}
		}

		// Create the final array and sort it
		this.speedLevels = new int[numWithoutRepetitions];
		System.arraycopy(speedLevelsWithoutRepetitions, 0, this.speedLevels, 0,
				numWithoutRepetitions);
		Arrays.sort(this.speedLevels);

		this.speedLevelIndex = 0;
	}

	/**
	 * Sets the {@link IControlsListener} notified by this view.
	 * 
	 * @param listener
	 *            the listener notified by this view. May be null if no listener is to be used.
	 */
	public void setControlsListener(IControlsListener listener) {
		this.listener = listener;
	}
}

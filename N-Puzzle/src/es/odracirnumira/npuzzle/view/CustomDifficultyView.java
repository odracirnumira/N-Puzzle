package es.odracirnumira.npuzzle.view;

import es.odracirnumira.npuzzle.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

/**
 * View that lets the user select a difficulty level for the N puzzle game. This view lets the user
 * select a number that represents the difficulty level. The difficulty level is accessible via the
 * {@link #getSelectedDifficulty()} method.
 * <p>
 * This is an styleable class. See the <i>CustomDifficultyView</i> styleable resource for more
 * information.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class CustomDifficultyView extends FrameLayout {
	/**
	 * The number picker used to select the difficulty.
	 */
	private NumberPicker numberPicker;

	/**
	 * The text view that shows the size of the puzzle given the value selected in
	 * {@link #numberPicker}.
	 */
	private TextView textView;

	/**
	 * The minimum difficulty.
	 */
	private int minDifficulty;

	/**
	 * The maximum difficulty.
	 */
	private int maxDifficulty;

	/**
	 * Default minimum difficulty level.
	 */
	public static final int DEFAULT_MINIMUM_DIFFICULTY = 2;

	/**
	 * Default maximum difficulty level.
	 */
	public static final int DEFAULT_MAXIMUM_DIFFICULTY = 20;

	public CustomDifficultyView(Context context) {
		super(context);

		this.addView(((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.custom_difficulty_view, null));

		this.initView(null);
	}

	public CustomDifficultyView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.addView(((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.custom_difficulty_view, null));

		this.initView(null);
	}

	public CustomDifficultyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.addView(((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.custom_difficulty_view, null));

		this.initView(null);
	}

	private void initView(AttributeSet attrs) {
		this.numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
		this.textView = (TextView) findViewById(R.id.textView);

		/*
		 * When the number changes, the label also changes.
		 */
		this.numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				updateSizeLabel(newVal);
			}
		});

		/*
		 * If attributes were specified, retrieve their values.
		 */
		if (attrs != null) {
			TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs,
					R.styleable.CustomDifficultyView, 0, 0);

			int minValue = a.getInteger(R.styleable.CustomDifficultyView_minDifficulty, -1);
			int maxValue = a.getInteger(R.styleable.CustomDifficultyView_maxDifficulty, -1);

			/*
			 * If none of the values were specified, make min=2, max=20.
			 * 
			 * If min was specified, but max wasn't, make max=min+1.
			 * 
			 * If max was specified, but min wasn't, then:
			 * 
			 * if max>=3, make min=max-1
			 * 
			 * else, make max=min=2
			 * 
			 * If both min and max were specified, check boundaries (min<=max).
			 * 
			 * In any case, any specified value must be greater than 1, otherwise an exception is
			 * thrown.
			 */
			if (minValue != -1 && minValue <= 1) {
				throw new IllegalArgumentException(
						"Invalid minimum difficulty value. Must be greater than 1");
			}

			if (maxValue != -1 && maxValue <= 1) {
				throw new IllegalArgumentException(
						"Invalid maximum difficulty value. Must be greater than 1");
			}

			if (minValue == -1 && maxValue == -1) {
				minValue = DEFAULT_MINIMUM_DIFFICULTY;
				maxValue = DEFAULT_MAXIMUM_DIFFICULTY;
			} else if (minValue != -1 && maxValue == -1) {
				maxValue = minValue + 1;
			} else if (minValue == -1 && maxValue != -1) {
				if (maxValue >= 3) {
					minValue = maxValue - 1;
				} else {
					minValue = 2;
				}
			} else {
				if (minValue > maxValue) {
					throw new IllegalArgumentException(
							"Minimum difficulty must be less or equal than maximum difficulty");
				}
			}

			/*
			 * Configure the number picker.
			 */
			this.setDifficultyRange(minValue, maxValue);
		} else {
			/*
			 * If no attributes were specified, set the default difficulty range.
			 */
			this.setDifficultyRange(DEFAULT_MINIMUM_DIFFICULTY, DEFAULT_MAXIMUM_DIFFICULTY);
		}

		/*
		 * Update the size label.
		 */
		this.updateSizeLabel(this.getMinDifficulty());
	}

	/**
	 * Returns the minimum difficulty value.
	 * 
	 * @return the minimum difficulty value.
	 */
	public int getMinDifficulty() {
		return minDifficulty;
	}

	/**
	 * Returns the maximum difficulty value.
	 * 
	 * @return the maximum difficulty value.
	 */
	public int getMaxDifficulty() {
		return maxDifficulty;
	}

	/**
	 * Sets the minimum and maximum difficulty values. Both values must be greater than 1, and
	 * <code>maxDifficulty</code> must be greater or equal than <code>minDifficulty</code>.
	 * <p>
	 * The selected value is changed to <code>minDifficulty</code>.
	 * 
	 * @param minDifficulty
	 *            the minimum difficulty value.
	 * @param maxDifficulty
	 *            the maximum difficulty value.
	 */
	public void setDifficultyRange(int minDifficulty, int maxDifficulty) {
		if (minDifficulty <= 1 || maxDifficulty <= 1 || minDifficulty > maxDifficulty) {
			throw new IllegalArgumentException("Invalid values for minimum and maximum difficulty");
		}

		this.minDifficulty = minDifficulty;
		this.maxDifficulty = maxDifficulty;

		this.numberPicker.setMinValue(minDifficulty);
		this.numberPicker.setMaxValue(maxDifficulty);
		this.numberPicker.setValue(minDifficulty);

		this.updateSizeLabel(minDifficulty);
	}

	/**
	 * Sets the selected difficulty value. Must be in an appropriate range, otherwise an exception
	 * is thrown.
	 * 
	 * @param difficulty
	 *            the difficulty value to set.
	 */
	public void setSelectedDifficulty(int difficulty) {
		if (difficulty < this.minDifficulty || difficulty > this.maxDifficulty) {
			throw new IllegalArgumentException("Invalid difficulty value");
		}

		this.numberPicker.setValue(difficulty);
		this.updateSizeLabel(difficulty);
	}

	/**
	 * Returns the currently selected difficulty value.
	 * 
	 * @return the currently selected difficulty value.
	 */
	public int getSelectedDifficulty() {
		return this.numberPicker.getValue();
	}

	/**
	 * Updates the value of the size label.
	 * 
	 * @param size
	 *            the size to set.
	 */
	private void updateSizeLabel(int size) {
		textView.setText(size + " X " + size);
	}
}

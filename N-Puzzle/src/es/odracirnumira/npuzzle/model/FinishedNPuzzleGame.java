package es.odracirnumira.npuzzle.model;

import android.os.Parcel;

/**
 * A convenience class that stores all the data of a finished N puzzle game in public fields. This
 * includes things such as the game ID, the puzzle itself, the list of moves, etc.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class FinishedNPuzzleGame extends AbstractNPuzzleGame {
	/**
	 * The time when the game finished. This is a Unix time in milliseconds.
	 */
	public long finishedTime;

	/**
	 * Default constructor. Default initializes all the fields.
	 */
	public FinishedNPuzzleGame() {

	}

	/**
	 * Constructor for the parcelable API.
	 * 
	 * @param in
	 *            the Parcel.
	 */
	protected FinishedNPuzzleGame(Parcel in) {
		super(in);
		this.finishedTime = in.readLong();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(this.finishedTime);
	}

	public static final Creator<FinishedNPuzzleGame> CREATOR = new Creator<FinishedNPuzzleGame>() {
		public FinishedNPuzzleGame[] newArray(int size) {
			return new FinishedNPuzzleGame[size];
		}

		public FinishedNPuzzleGame createFromParcel(Parcel source) {
			return new FinishedNPuzzleGame(source);
		}
	};
}

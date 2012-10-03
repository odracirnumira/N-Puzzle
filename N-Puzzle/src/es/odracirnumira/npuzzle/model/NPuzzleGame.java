package es.odracirnumira.npuzzle.model;

import android.os.Parcel;

/**
 * A convenience class that stores all the data of an N puzzle game in public fields. This includes
 * things such as the game ID, the puzzle itself, the list of moves, elapsed time, etc.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NPuzzleGame extends AbstractNPuzzleGame {
	/**
	 * Default constructor. Default initializes all the fields.
	 */
	public NPuzzleGame() {
	}

	/**
	 * Time when the game was last played. Measured in milliseconds.
	 */
	public long lastPlayedTime;

	/**
	 * Constructor for the parcelable API.
	 * 
	 * @param in
	 *            the Parcel.
	 */
	protected NPuzzleGame(Parcel in) {
		super(in);
		this.lastPlayedTime = in.readLong();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(this.lastPlayedTime);
	}

	public static final Creator<NPuzzleGame> CREATOR = new Creator<NPuzzleGame>() {
		public NPuzzleGame[] newArray(int size) {
			return new NPuzzleGame[size];
		}

		public NPuzzleGame createFromParcel(Parcel source) {
			return new NPuzzleGame(source);
		}
	};
}
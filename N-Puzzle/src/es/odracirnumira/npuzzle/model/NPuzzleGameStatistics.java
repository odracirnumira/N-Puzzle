package es.odracirnumira.npuzzle.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class that contains a N puzzle game's statistics.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NPuzzleGameStatistics implements Parcelable {
	/**
	 * The number of moves.
	 */
	public int numMoves;

	/**
	 * The elapsed time, in milliseconds.
	 */
	public long elapsedTime;

	/**
	 * @see android.os.Parcelable#describeContents()
	 */
	public int describeContents() {
		return 0;
	}

	/**
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(numMoves);
		dest.writeLong(elapsedTime);
	}

	public static final Creator<NPuzzleGameStatistics> CREATOR = new Creator<NPuzzleGameStatistics>() {
		public NPuzzleGameStatistics[] newArray(int size) {
			return new NPuzzleGameStatistics[size];
		}

		public NPuzzleGameStatistics createFromParcel(Parcel source) {
			NPuzzleGameStatistics result = new NPuzzleGameStatistics();
			result.numMoves = source.readInt();
			result.elapsedTime = source.readLong();
			return result;
		}
	};
}

package es.odracirnumira.npuzzle.model;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import es.odracirnumira.npuzzle.contentproviders.NPuzzleContract;

/**
 * Abstract implementation of an N puzzle game. This contains all the information an N puzzle game
 * should have.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class AbstractNPuzzleGame implements Parcelable {
	/**
	 * The current state of the N puzzle.
	 */
	public NPuzzle nPuzzle;

	/**
	 * The N puzzle's initial state. This is the state the puzzle was in when the game was first
	 * started.
	 */
	public String initialState;

	/**
	 * The path of the image displayed by the puzzle. It is null if no image from the file system is
	 * being displayed (in which case the default puzzle image is being displayed).
	 */
	public String puzzleImagePath;

	/**
	 * The actual image that the puzzle is displaying. If {@link #puzzleImagePath} is null, this
	 * member will store the default image for the current puzzle. Also, if there is any error
	 * loading the puzzle's image, this member will be null.
	 */
	public Bitmap puzzleImage;

	/**
	 * The list of moves that the user has performed so far. The first integer is the first tile
	 * that was moved, the second integer is the second tile that was moved, and so on.
	 */
	public List<Integer> moves;

	/**
	 * The ID of this game in the content provider ( {@link NPuzzleContract}). If -1 it means that
	 * we tried to start a new game but it could not be initially saved into the content provider,
	 * so it has no ID.
	 */
	public long gameID;

	/**
	 * Actual time spent playing this game. Measured in milliseconds.
	 */
	public long elapsedTime;

	/**
	 * Time when the game started. Measured in milliseconds ({@link SystemClock} used)
	 */
	public long startTime;

	/**
	 * The rotation of the puzzle's image. Can be 0, 90, 180 and 270.
	 */
	public int imageRotation;

	/**
	 * Default constructor. Default initializes all the fields.
	 */
	public AbstractNPuzzleGame() {
	}

	/**
	 * Constructor for the parcelable API.
	 * 
	 * @param in
	 *            the Parcel.
	 */
	protected AbstractNPuzzleGame(Parcel in) {
		this.gameID = in.readLong();
		this.nPuzzle = in.readParcelable(null);//NPuzzle.class.getClassLoader());
		this.moves = new ArrayList<Integer>();
		in.readList(this.moves,null);// AbstractNPuzzleGame.class.getClassLoader());
		this.puzzleImagePath = in.readString();
		this.puzzleImage = in.readParcelable(null);
		this.elapsedTime = in.readLong();
		this.startTime = in.readLong();
		this.imageRotation = in.readInt();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.gameID);
		dest.writeParcelable(this.nPuzzle, 0);
		dest.writeList(this.moves);
		dest.writeString(this.puzzleImagePath);
		dest.writeParcelable(this.puzzleImage, 0);
		dest.writeLong(this.elapsedTime);
		dest.writeLong(this.startTime);
		dest.writeInt(this.imageRotation);
	}
}

package es.odracirnumira.npuzzle.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Random;

import android.os.Parcel;
import android.os.Parcelable;

import es.odracirnumira.npuzzle.util.EventListenerList;
import es.odracirnumira.npuzzle.util.MathUtilities;

/**
 * An N puzzle.
 * <p>
 * An N puzzle is a square board consisting of sqrt(N+1) x sqrt(N+1) elements (N+1 must be a perfect
 * square).
 * <p>
 * Each element is called a tile. A tile has two properties:
 * <ul>
 * <li>An identifier, the tile itself. It is a number between 0 and N, and it identifies a specific
 * tile among all the tiles of the board.
 * <li>A position on the board. Tiles can move all over the board, so a tile can be at several
 * places during an N puzzle game. The position of each tile is a number between 0 and N, being 0
 * the position at the first row and first column of the board; 1 the position at the first row and
 * second column, etc.
 * </ul>
 * 
 * <p>
 * There is a special tile, the <i>empty tile</i>. This tile represents a "hole" in the board. The
 * empty tile lets other tiles move. A tile normally cannot move to other position. Only tiles next
 * to the empty tile can move, and they can only move to the position occupied by the empty tile,
 * after which that tile will occupy the position that the empty tile occupied, and the empty tile
 * will occupy the position that the tile occupied (their positions are swapped). <b>The empty tile
 * is always the tile number N</b>.
 * <p>
 * An N puzzle is solved when, for each tile <i>i</i>, it is placed at position <i>i</i> (the empty
 * tile will therefore be at the bottom right corner of the board).
 * <p>
 * An N puzzle does not have to be solvable, but the idea is that there should be a sequence of moves
 * that can take it to a solved state.
 * <p>
 * When creating instances of this class, keep in mind that you should not break the limits imposed
 * by {@link #MIN_N}, {@link #MAX_N}, {@value #MIN_SIZE_SIZE} and {@link #MAX_SIDE_SIZE}. Otherwise,
 * an exception will be thrown.
 * <p>
 * See http://en.wikipedia.org/wiki/Fifteen_puzzle for more information.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NPuzzle implements Parcelable {
	/**
	 * The position of each tile. This list has <code>n+1</code> elements, since there are
	 * <code>n+1</code> tiles (including the empty tile). Given <code>i &lt;= n</code>,
	 * <code>tilePositions.get(i)</code> is the position of the <code>i-th</code> tile. The empty
	 * tile has index <code>n</code>.
	 * <p>
	 * This is synchronized (not in a concurrency sense) with {@link #positionContents}.
	 */
	private List<Integer> tilePositions;

	/**
	 * The tile that is present at each position of the puzzle. For each position of the puzzle,
	 * this method returns the tile that is present at that position.
	 * <p>
	 * This is synchronized (not in a concurrency sense) with {@link #tilePositions}.
	 */
	private List<Integer> positionContents;

	/**
	 * N for the puzzle.
	 */
	private final int n;

	/**
	 * The number of tiles of the puzzle. It equals {@link #n} + 1.
	 */
	private final int numTiles;

	/**
	 * The number of tiles on the side of the puzzle. It equals <code>sqrt({@link #n} + 1)</code>.
	 */
	private final int sideNumTiles;

	/**
	 * Listeners for events.
	 */
	private EventListenerList listeners;

	/**
	 * Maximum value allowed for N.
	 */
	public static final int MAX_N = Integer.MAX_VALUE - 1;

	/**
	 * Minimum value allowed for N.
	 */
	public static final int MIN_N = 3;

	/**
	 * Maximum value allowed for the size of the side of the puzzle.
	 */
	public static final int MAX_SIDE_SIZE = (int) Math.sqrt(Integer.MAX_VALUE);

	/**
	 * Maximum value allowed for the size of the side of the puzzle.
	 */
	public static final int MIN_SIZE_SIZE = 2;

	/**
	 * Random number generator for the random puzzle generator.
	 */
	private static Random random = new Random();

	/**
	 * Interface for a listener that is notified when a tile of the N puzzle is moved.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public interface ITileListener extends EventListener {
		/**
		 * Called when a tile is moved.
		 * 
		 * @param tile
		 *            the tile that has been moved.
		 * @param oldPos
		 *            the position that the tile had before moving.
		 * @param newPos
		 *            the position that the tile has after moving.
		 */
		public void tileMoved(int tile, int oldPos, int newPos);
	}

	/**
	 * A direction relative the a tile.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public enum Direction {
		UP, DOWN, LEFT, RIGHT
	}

	/**
	 * Creates a solved N puzzle.
	 * 
	 * @param n
	 *            N for the puzzle.
	 */
	private NPuzzle(int n) {
		this(n, createDefaultTileConfiguration(n));
	}

	/**
	 * Creates an N puzzle with an initial configuration. Note that <code>n+1</code> is the number
	 * of tiles of the puzzle, so <code>n+1</code> must equal
	 * <code>initialConfiguration.size()</code>.
	 * <p>
	 * The configuration is a list that, for each element <code>i</code>, says at which position in
	 * the puzzle is placed tile <code>i</code>.
	 * <p>
	 * The initial configuration must be a valid one. It does not necessarily mean that it has to be
	 * solvable. Instead, it means that:
	 * <ul>
	 * <li>It has the right number of elements.
	 * <li>All of its values are between 0 and <code>n</code>.
	 * <li>There are no repetitions.
	 * </ul>
	 * 
	 * @param n
	 *            N for the puzzle. It must be a positive integer such that
	 *            <code>sqrt(n+1)² = n</code>.
	 * @param initialConfiguration
	 *            the initial configuration.
	 */
	private NPuzzle(int n, List<Integer> initialConfiguration) {
		checkN(n);

		if (n + 1 != initialConfiguration.size()) {
			throw new IllegalArgumentException(
					"N is not consistent with the size of the initial configuration list");
		}

		this.n = n;
		this.numTiles = n + 1;
		this.sideNumTiles = (int) Math.sqrt(n + 1);
		checkTileConfiguration(initialConfiguration);
		this.tilePositions = initialConfiguration;
		this.positionContents = getPositionsFromConfiguration(initialConfiguration);
		this.listeners = new EventListenerList();
	}

	/**
	 * Moves the tile at position <code>tilePos</code>. It must be a position next to the empty tile
	 * (cannot be the empty tile). Otherwise, an exception is thrown. After calling this method, the
	 * tile and the empty tile will swap positions.
	 * 
	 * @param tilePos
	 *            the position of the tile to move. Must be next to the empty tile.
	 */
	public void moveTileByPosition(int tilePos) {
		if (!checkTilePosition(tilePos)) {
			throw new IllegalArgumentException("Invalid tile position");
		}

		/*
		 * Check that the empty tile is next to the tile.
		 */
		List<Integer> nextPositions = getNextPositions(tilePos);
		int emptyTilePosition = this.getEmptyTilePosition();

		if (!nextPositions.contains(emptyTilePosition)) {
			throw new IllegalArgumentException("The tile to move is not next to the empty tile");
		}

		/*
		 * Now swap positions.
		 */
		int tile = this.positionContents.get(tilePos);
		this.positionContents.set(emptyTilePosition, tile);
		this.positionContents.set(tilePos, numTiles - 1);
		this.tilePositions.set(numTiles - 1, tilePos);
		this.tilePositions.set(tile, emptyTilePosition);

		/*
		 * Notify listeners.
		 */
		this.fireTileMoved(tile, tilePos, emptyTilePosition);
	}

	/**
	 * This method applies a sequence of moves to the puzzle. It moves the tile at position
	 * <code>tilePositions.get(0)</code>, then the tile at position
	 * <code>tilePositions.get(1)</code>, and so on.
	 * <p>
	 * This method is equivalent to:
	 * 
	 * <pre>
	 * for (Integer tilePosition : tilePositions) {
	 * 	this.moveTileByPosition(tilePosition);
	 * }
	 * 
	 * </pre>
	 * 
	 * @param tilePositions
	 */
	public void moveTilesByPositions(List<Integer> tilePositions) {
		for (Integer tilePosition : tilePositions) {
			this.moveTileByPosition(tilePosition);
		}
	}

	/**
	 * Moves the tile <code>tile</code>. The tile must be at a position next to the empty tile, and
	 * cannot be the empty tile. Otherwise, an exception is thrown. After calling this method, the
	 * tile and the empty tile will swap positions.
	 * 
	 * @param tile
	 *            the tile to move. Must be next to the empty tile.
	 */
	public void moveTile(int tile) {
		this.moveTileByPosition(this.tilePositions.get(tile));
	}

	/**
	 * This function applies a sequence of moves to the puzzle. It starts by moving the first tile
	 * in <code>tiles</code>, then the second, and so on.
	 * <p>
	 * Calling this method is equivalent to:
	 * 
	 * <pre>
	 * for (Integer tile : tiles) {
	 * 	this.moveTile(tile);
	 * }
	 * </pre>
	 * 
	 * @param tiles
	 *            the sequence of tiles to move.
	 */
	public void moveTiles(List<Integer> tiles) {
		for (Integer tile : tiles) {
			this.moveTile(tile);
		}
	}

	/**
	 * Returns the position of tile <code>tile</code>.
	 * 
	 * @param tile
	 *            the tile whose position will be retrieved.
	 */
	public int getTilePosition(int tile) {
		if (!checkTile(tile)) {
			throw new IllegalArgumentException("Invalid tile: " + tile);
		}

		return this.tilePositions.get(tile);
	}

	/**
	 * Returns the position of the empty tile. This is the same as calling
	 * {@link #getTilePosition(int)} with input argument <code>N</code>.
	 */
	public int getEmptyTilePosition() {
		return this.tilePositions.get(this.n);
	}

	/**
	 * Returns the tile at position <code>tilePos</code>.
	 * 
	 * @param tilePos
	 *            the position whose tile will be retrieved.
	 * @return the tile at <code>tilePos</code>.
	 */
	public int getPositionTile(int tilePos) {
		if (!checkTilePosition(tilePos)) {
			throw new IllegalArgumentException("Invalid tile position: " + tilePos);
		}

		return this.positionContents.get(tilePos);
	}

	/**
	 * Returns the number of tiles of the puzzle.
	 * 
	 * @return the number of tiles of the puzzle.
	 */
	public int getNumTiles() {
		return this.numTiles;
	}

	/**
	 * Returns N for this puzzle.
	 * 
	 * @return N for this puzzle.
	 */
	public int getN() {
		return this.n;
	}

	/**
	 * Returns the number of tiles on the side of the puzzle. This equals <code>sqrt(N+1)</code>
	 */
	public int getSideNumTiles() {
		return this.sideNumTiles;
	}

	/**
	 * Returns a matrix representation of the puzzle. For each <code>(i,j)</code> position (i=row,
	 * j=column), the matrix contains the tile that is at that position.
	 * 
	 * @return a matrix representation of the puzzle.
	 */
	public int[][] getPuzzleMatrix() {
		int[][] result = new int[this.sideNumTiles][];

		for (int i = 0; i < this.sideNumTiles; i++) {
			int[] row = new int[this.sideNumTiles];

			result[i] = row;
		}

		for (int i = 0; i < this.numTiles; i++) {
			result[i / this.sideNumTiles][i % sideNumTiles] = this.positionContents.get(i);
		}

		return result;
	}

	/**
	 * Returns true if the puzzle is solved. A N puzzle is solved if and only if, for each tile
	 * <code>i</code>, it is placed at position <code>i</code> of the puzzle.
	 * 
	 * @return if the puzzle is solved.
	 */
	public boolean isSolved() {
		for (int i = 0; i < tilePositions.size(); i++) {
			if (tilePositions.get(i) != i) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if tile <code>tile</code> can be moved. This happens only if the empty tile is next to
	 * the tile.
	 * <p>
	 * Note that if <code>tile</code> is an invalid tile, an exception is thrown. For the empty
	 * tile, returns false.
	 * 
	 * @param tile
	 *            the tile to check.
	 * @return true if the tile <code>tile</code> can be moved.
	 */
	public boolean canMove(int tile) {
		if (!checkTile(tile)) {
			throw new IllegalArgumentException("Invalid tile");
		}

		return canMoveByPosition(this.tilePositions.get(tile));
	}

	/**
	 * Checks if the tile placed at position <code>tilePos</code> can be moved. This happens only if
	 * the empty tile is next to the tile.
	 * <p>
	 * Note that if <code>tilePos</code> is an invalid tile, an exception is thrown. For the empty
	 * tile, returns false.
	 * 
	 * @param tilePos
	 *            the position to check.
	 * @return true if the tile placed at position <code>tilePos</code> can be moved.
	 */
	public boolean canMoveByPosition(int tilePos) {
		if (!checkTilePosition(tilePos)) {
			throw new IllegalArgumentException("Invalid tile position");
		}

		/*
		 * Check that the empty tile is next to the tile.
		 */
		List<Integer> nextPositions = getNextPositions(tilePos);
		int emptyTilePosition = this.getEmptyTilePosition();

		if (!nextPositions.contains(emptyTilePosition)) {
			return false;
		}

		return true;
	}

	/**
	 * If the tile <code>tile</code> can be moved (i.e, the empty tile is next to the tile), this
	 * method returns the direction that the tile should be moved to. If the tile cannot be moved,
	 * returns null.
	 * 
	 * @param tile
	 *            the tile.
	 * @return the direction the tile can be moved to, or null if it cannot be moved.
	 */
	public Direction moveDirection(int tile) {
		if (!checkTile(tile)) {
			throw new IllegalArgumentException("Invalid tile");
		}

		return this.moveDirectionFromPosition(this.tilePositions.get(tile));
	}

	/**
	 * If the tile placed at <code>tilePos</code> can be moved (i.e, the empty tile is next to the
	 * tile), this method returns the direction that the tile should be moved to. If the tile cannot
	 * be moved, returns null.
	 * 
	 * @param tilePos
	 *            the position of the tile.
	 * @return the direction the tile can be moved to, or null if it cannot be moved.
	 */
	public Direction moveDirectionFromPosition(int tilePos) {
		if (!checkTilePosition(tilePos)) {
			throw new IllegalArgumentException("Invalid tile position");
		}

		/*
		 * Check that the empty tile is next to the tile.
		 */
		List<Integer> nextPositions = getNextPositions(tilePos);
		int emptyTilePosition = this.getEmptyTilePosition();

		if (!nextPositions.contains(emptyTilePosition)) {
			return null;
		}

		/*
		 * Now get the direction.
		 */
		if (emptyTilePosition == tilePos - sideNumTiles) {
			return Direction.UP;
		}
		if (emptyTilePosition == tilePos + sideNumTiles) {
			return Direction.DOWN;
		}
		if (emptyTilePosition == tilePos - 1) {
			return Direction.LEFT;
		}
		if (emptyTilePosition == tilePos + 1) {
			return Direction.RIGHT;
		}

		return null;
	}

	/**
	 * Adds a listener that will be notified when a tile is moved.
	 * 
	 * @param listener
	 *            the listener.
	 */
	public void addTileListener(ITileListener listener) {
		this.listeners.add(ITileListener.class, listener);
	}

	/**
	 * Removes a listener from the list of registered listeners.
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	public void removeTileListener(ITileListener listener) {
		this.listeners.remove(ITileListener.class, listener);
	}

	/**
	 * Given the positions of two tiles that are next to each other, this method returns the
	 * direction that the first tile should move to, to get to the position of the second tile. If
	 * both tiles are not next to each other, null is returned.
	 * 
	 * @param firstTilePos
	 *            the position of the first tile.
	 * @param secondTilePos
	 *            the position of the second tile.
	 * @return the direction that the first tile should move to get to the position of the second
	 *         tile. f both tiles are not next to each other, null is returned.
	 */
	public Direction moveDirection(int firstTilePos, int secondTilePos) {
		if (!checkTilePosition(firstTilePos) || !checkTilePosition(secondTilePos)) {
			throw new IllegalArgumentException("Invalid tile position");
		}

		int tileRow = firstTilePos / this.sideNumTiles;

		int posUp = firstTilePos - sideNumTiles;
		int posDown = firstTilePos + sideNumTiles;
		int posLeft = firstTilePos - 1;
		int posRight = firstTilePos + 1;

		if (checkTilePosition(posUp) && posUp == secondTilePos) {
			return Direction.UP;
		}

		if (checkTilePosition(posDown) && posDown == secondTilePos) {
			return Direction.DOWN;
		}

		if (checkTilePosition(posLeft) && posLeft / this.sideNumTiles == tileRow
				&& posLeft == secondTilePos) {
			return Direction.LEFT;
		}

		if (checkTilePosition(posRight) && posRight / this.sideNumTiles == tileRow
				&& posRight == secondTilePos) {
			return Direction.RIGHT;
		}

		return null;
	}

	/**
	 * Returns true if this puzzle can be solved, and false otherwise.
	 */
	public boolean isSolvable() {
		/*
		 * This implements the algorithm explained at
		 * http://www.cs.bham.ac.uk/~mdr/teaching/modules04/java2/TilesSolvability.html
		 */
		/*
		 * First, compute inversions.
		 */
		long inversions = 0;

		for (int pos = 0; pos < numTiles; pos++) {
			int tileAtPos = positionContents.get(pos);

			if (tileAtPos != n && tileAtPos != 0) {
				for (int pos2 = pos + 1; pos2 < numTiles; pos2++) {
					if (positionContents.get(pos2) < tileAtPos) {
						inversions++;
					}
				}
			}
		}

		/*
		 * Now check solvability condition.
		 */
		if (sideNumTiles % 2 == 0) {
			boolean emptyTileOnOddRowFromBotton = (getEmptyTilePosition() / sideNumTiles) % 2 != 0;

			if (emptyTileOnOddRowFromBotton == (inversions % 2 == 0)) {
				return true;
			} else {
				return false;
			}
		} else {
			if (inversions % 2 == 0) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Returns an estimation of the number of bytes occupied by the puzzle. This value is never
	 * greater than the actual number of used bytes, but it may be less or equal than it.
	 * 
	 * @return an estimation of the number of bytes occupied by the puzzle. This value is never
	 *         greater than the actual number of used bytes.
	 */
	public int getByteCount() {
		/*
		 * We store two integers for each tile.
		 */
		return numTiles * 4 * 2;
	}

	/**
	 * Returns a string representation of the puzzle. The string representation consists of a
	 * sequence of numbers, being the first number the position of the first tile, the second number
	 * the position of the second tile, and so on.
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();

		for (Integer position : this.tilePositions) {
			result.append(position).append(' ');
		}

		result.replace(result.length() - 1, result.length(), "");

		return result.toString();
	}

	/**
	 * Throws an exception if <code>tiles</code> is not a valid tile configuration. A valid
	 * configuration has {@link #numTiles} elements, all of them are between 0 and {@link #numTiles}
	 * -1, and there are no repetitions.
	 */
	private void checkTileConfiguration(List<Integer> tiles) {
		if (tiles.size() != numTiles) {
			throw new IllegalArgumentException("Invalid number of tiles");
		}

		List<Integer> used = new ArrayList<Integer>();

		for (Integer tilePos : tiles) {
			if (!checkTilePosition(tilePos)) {
				throw new IllegalArgumentException("Invalid tile position (" + tilePos
						+ "). Must be between 0 and numTiles-1");
			}

			if (used.contains(tilePos)) {
				throw new IllegalArgumentException("Repeated tile value: " + tilePos);
			}

			used.add(tilePos);
		}
	}

	/**
	 * Given a valid tile configuration, this method returns a list that contains, for each element
	 * <code>i</code>, what tile occupying that position in the puzzle.
	 */
	private List<Integer> getPositionsFromConfiguration(List<Integer> tiles) {
		Integer[] result = new Integer[tiles.size()];

		for (int i = 0; i < tiles.size(); i++) {
			result[tiles.get(i)] = i;
		}

		return Arrays.asList(result);
	}

	/**
	 * Given a tile position, this function returns a list that contains the positions of the tiles
	 * above, below, to the right of and to the left of the tile, in that order. Any invalid
	 * position is not included in the result.
	 */
	private List<Integer> getNextPositions(int tilePos) {
		List<Integer> result = new ArrayList<Integer>();

		int tileRow = tilePos / this.sideNumTiles;

		int posUp = tilePos - sideNumTiles;
		int posDown = tilePos + sideNumTiles;
		int posLeft = tilePos - 1;
		int posRight = tilePos + 1;

		if (checkTilePosition(posUp)) {
			result.add(posUp);
		}

		if (checkTilePosition(posDown)) {
			result.add(posDown);
		}

		if (checkTilePosition(posLeft) && posLeft / this.sideNumTiles == tileRow) {
			result.add(posLeft);
		}

		if (checkTilePosition(posRight) && posRight / this.sideNumTiles == tileRow) {
			result.add(posRight);
		}

		return result;
	}

	/**
	 * Returns true if <code>tilePos</code> is a valid tile position.
	 */
	private boolean checkTilePosition(int tilePos) {
		return tilePos >= 0 && tilePos < this.numTiles;
	}

	/**
	 * Returns true if <code>tile</code> is a valid tile.
	 */
	private boolean checkTile(int tile) {
		return tile >= 0 && tile < this.numTiles;
	}

	/**
	 * Fires the event that notifies that a tile of the puzzle has been moved.
	 * 
	 * @param tile
	 *            the tile that has been moved.
	 * @param oldPos
	 *            the position that the tile had before moving.
	 * @param newPos
	 *            the position that the tile has after moving.
	 */
	private void fireTileMoved(int tile, int oldPos, int newPos) {
		ITileListener[] listeners = this.listeners.getListeners(ITileListener.class);
		for (ITileListener listener : listeners) {
			listener.tileMoved(tile, oldPos, newPos);
		}
	}

	/**
	 * Creates the initial configuration for the tiles of the puzzle, which represents a solved
	 * game.
	 * 
	 * @param n
	 *            N for the puzzle.
	 */
	private static List<Integer> createDefaultTileConfiguration(int n) {
		if (n <= 1) {
			throw new IllegalArgumentException("N must be greater than 1");
		}

		if (!MathUtilities.isPerfectSquare(n + 1)) {
			throw new IllegalArgumentException("N + 1 must be a perfect square");
		}

		List<Integer> result = new ArrayList<Integer>();
		int numTiles = n + 1;

		for (int i = 0; i < numTiles; i++) {
			result.add(i, i);
		}

		return result;
	}

	/**
	 * Checks the value of n (N for the puzzle), and if not appropriate, throws an exception. This
	 * checks that the value of N is not too big or too small, and also that N+1 is a perfect
	 * square.
	 */
	private static void checkN(int n) {
		if (n < 3 || n > MAX_N) {
			throw new IllegalArgumentException("Invalid value for N (it is not in a valid range)");
		}

		if (!MathUtilities.isPerfectSquare(n + 1)) {
			throw new IllegalArgumentException("N + 1 must be a perfect square");
		}
	}

	/**
	 * Checks the value of <code>sizeSize</code> (the number of tiles of each side of the puzzle),
	 * and throws an exception if it is not a proper value. This checks that the value is not too
	 * bit or too small.
	 */
	private static void checkSideSize(int sideSize) {
		if (sideSize < 2 || sideSize > MAX_SIDE_SIZE) {
			throw new IllegalArgumentException("Invalid value for the side size of the puzzle");
		}
	}

	/**
	 * Creates a solved N puzzle whose N value will be <code>n</code>.
	 * 
	 * @param n
	 *            N for the puzzle.
	 * @return the N puzzle.
	 */
	public static NPuzzle newNPuzzleFromN(int n) {
		return new NPuzzle(n);
	}

	/**
	 * Creates a solved N puzzle whose N value will be <code>sideSize² - 1</code>. This constructor
	 * is used to construct an N puzzle from the size of its sides. For instance, if the puzzel is
	 * 4x4 (15 puzzle), then <code>sideSize</code> is 4.
	 * 
	 * @param sideSize
	 *            the number of elements per side of the puzzle.
	 * @return the N puzzle.
	 */
	public static NPuzzle newNPuzzleFromSideSize(int sideSize) {
		checkSideSize(sideSize);
		return new NPuzzle(sideSize * sideSize - 1);
	}

	/**
	 * Creates an N puzzle with an initial configuration. Note that <code>n+1</code> is the number
	 * of tiles of the puzzle, so <code>n+1</code> must equal
	 * <code>initialConfiguration.size()</code>.
	 * <p>
	 * The configuration is a list that, for each element <code>i</code>, says at which position in
	 * the puzzle is placed tile <code>i</code>.
	 * <p>
	 * The initial configuration must be a valid one. It does not necessarily mean that it has to be
	 * solvable. Instead, it means that:
	 * <ul>
	 * <li>It has the right number of elements.
	 * <li>All of its values are between 0 and <code>n</code>.
	 * <li>There are no repetitions.
	 * </ul>
	 * 
	 * @param n
	 *            N for the puzzle. It must be a positive integer such that
	 *            <code>sqrt(n+1)² = n</code>.
	 * @param initialConfiguration
	 *            the initial configuration.
	 */
	public static NPuzzle newNPuzzleFromNAndConfiguration(int n, List<Integer> initialConfiguration) {
		return new NPuzzle(n, initialConfiguration);
	}

	/**
	 * Creates an N puzzle with an initial configuration. The value of N for the puzzle will be
	 * <code>sideSize² - 1</code>. This constructor is used to construct an N puzzle from the size
	 * of its sides. For instance, if the puzzel is 4x4 (15 puzzle), then <code>sideSize</code> is
	 * 4. Note that <code>N+1</code> is the number of tiles of the puzzle, so <code>N+1</code> must
	 * equal <code>initialConfiguration.size()</code>.
	 * <p>
	 * The configuration is a list that, for each element <code>i</code>, says at which position in
	 * the puzzle is placed tile <code>i</code>.
	 * <p>
	 * The initial configuration must be a valid one. It does not necessarily mean that it has to be
	 * solvable. Instead, it means that:
	 * <ul>
	 * <li>It has the right number of elements.
	 * <li>All of its values are between 0 and <code>N</code>.
	 * <li>There are no repetitions.
	 * </ul>
	 * 
	 * @param sideSize
	 *            the number of elements per side of the puzzle.
	 * @param initialConfiguration
	 *            the initial configuration.
	 */
	public static NPuzzle newNPuzzleFromSideSizeAndConfiguration(int sideSize,
			List<Integer> initialConfiguration) {
		checkSideSize(sideSize);
		return new NPuzzle(sideSize * sideSize - 1, initialConfiguration);
	}

	/**
	 * Creates a new random N puzzle whose N value will be <code>n</code>. The returned puzzle can
	 * be solved.
	 * 
	 * @param n
	 *            N for the puzzle.
	 * @return the N puzzle.
	 */
	public static NPuzzle newRandomNPuzzleFromN(int n) {
		NPuzzle result = new NPuzzle(n);

		/*
		 * Apply random moves to the initially solved puzzle.
		 */
		final int totalMoves = n * 100;

		for (int i = 0; i < totalMoves; i++) {
			List<Integer> nextToEmptyTilePositions = result.getNextPositions(result
					.getEmptyTilePosition());
			result.moveTileByPosition(nextToEmptyTilePositions.get(random
					.nextInt(nextToEmptyTilePositions.size())));
		}

		return result;
	}

	/**
	 * Creates a new random N puzzle. The value of N for the puzzle will be
	 * <code>sideSize² - 1</code>. This constructor is used to construct an N puzzle from the size
	 * of its sides. For instance, if the puzzle is 4x4 (15 puzzle), then <code>sideSize</code> is
	 * 4.
	 * <p>
	 * The returned puzzle can be solved. 
	 * 
	 * @param sideSize
	 *            the number of elements per side of the puzzle.
	 * @return the N puzzle.
	 */
	public static NPuzzle newRandomNPuzzleFromSideSize(int sideSize) {
		checkSideSize(sideSize);
		return newRandomNPuzzleFromN(sideSize * sideSize - 1);
	}

	/**
	 * This method converts the input string <code>s</code> to a sequence of integers.
	 * <code>s</code> must be a string with the format "number1 number2 ... numberN", that is, a
	 * sequence of numbers separated by blank spaces (multiple spaces can separate each couple of
	 * integers). <code>s</code> can be the empty string, in which case an empty list is returned.
	 * <p>
	 * This method is useful for reading list of moves that can then be applied to the puzzle via
	 * the {@link #moveTiles(List)} and {@link #moveTilesByPositions(List)} methods.
	 * 
	 * @param s
	 *            the string to convert.
	 * @return the sequence of numbers that <code>s</code> represents.
	 */
	public static List<Integer> stringToSequenceOfIntegers(String s) {
		String[] numbers = s.split("( )+");

		List<Integer> result = new ArrayList<Integer>();

		for (String number : numbers) {
			if (number.length() != 0) {
				result.add(Integer.parseInt(number));
			}
		}

		return result;
	}

	/**
	 * This methods converts a sequence of integers into a String. The String will have the
	 * following format: "number1 number2 ... numberN", that is, a sequence of numbers separated by
	 * blank spaces (one space for every couple of integers).
	 * <p>
	 * This method is useful for converting a list of N puzzle moves into a String.
	 * 
	 * @param list
	 *            the list of numbers to convert. May be empty, in which case the empty String is
	 *            returned.
	 * @return a String that represents the <code>list</code> sequence of numbers.
	 */
	public static String sequenceOfIntegersToString(List<Integer> list) {
		StringBuilder result = new StringBuilder();

		for (Integer i : list) {
			result.append(i).append(' ');
		}

		if (list.size() > 0) {
			result.replace(result.length() - 1, result.length(), "");
		}

		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	public void writeToParcel(Parcel dest, int flags) {
		// Write only the tilePositions list. The other parameters will be reconstructed later
		dest.writeList(this.tilePositions);
	}

	/**
	 * Creator for parcelable API.
	 */
	public static final Creator<NPuzzle> CREATOR = new Creator<NPuzzle>() {
		public NPuzzle[] newArray(int size) {
			return new NPuzzle[size];
		}

		public NPuzzle createFromParcel(Parcel source) {
			// Retrieve the tilePositions
			List<Integer> tilePositions = new ArrayList<Integer>();
			source.readList(tilePositions, null);

			return newNPuzzleFromNAndConfiguration(tilePositions.size() - 1, tilePositions);
		}
	};
}

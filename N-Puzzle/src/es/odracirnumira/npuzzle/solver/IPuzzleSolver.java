package es.odracirnumira.npuzzle.solver;

import java.util.List;

import es.odracirnumira.npuzzle.model.NPuzzle;

/**
 * Interface for classes that are able to solve solvable {@link NPuzzle}s.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public interface IPuzzleSolver {
	/**
	 * Solves <code>puzzle</code> if it can be solved. This method does not modify the puzzle,
	 * instead, it returns the list of moves that should be applied in order to solve it. Use
	 * {@link NPuzzle#moveTiles(List)} to apply the returned sequence of moves to the N puzzle.If
	 * the puzzle is not solvable, this method throws a RuntimeException.
	 * <p>
	 * This method should be run in an background thread so the calling thread does not block in
	 * case the process takes a long time.
	 * <p>
	 * This method cannot be run if there is a puzzle still being solved by the same
	 * {@code IPuzzleSolver}. Otherwise, an IllegalStateException will be thrown.
	 * 
	 * @param puzzle
	 *            the puzzle to solve.
	 * @return the list of moves to apply to solve the puzzle.
	 * @throws RuntimeException
	 *             if the input puzzle cannot be solved.
	 * @throws IllegalStateException
	 *             if this method is called while another puzzle is still being solved.
	 */
	public List<Integer> solve(NPuzzle puzzle);

	/**
	 * Stops solving the puzzle that is currently being solved by {@link #solve(NPuzzle)}. The
	 * process of solving the puzzle does necessarily ends immediately, but it should finish as soon
	 * as possible.
	 * <p>
	 * If no puzzle is being solved, this method does nothing.
	 */
	public void cancel();
}

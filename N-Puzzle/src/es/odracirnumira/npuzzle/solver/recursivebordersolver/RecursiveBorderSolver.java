package es.odracirnumira.npuzzle.solver.recursivebordersolver;

import java.util.ArrayList;
import java.util.List;

import es.odracirnumira.npuzzle.model.NPuzzle;
import es.odracirnumira.npuzzle.solver.IPuzzleSolver;

/**
 * N puzzle solver that solves the puzzle by applying a strategy that goes from the outside to the
 * inside of the puzzle, solving it <i>layer by layer</i>. For each iteration, the topmost row and
 * leftmost column are properly solved. After each iteration, the same algorithm is applied to solve
 * the puzzle that is left after removing the currently topmost row and leftmost column. This
 * process goes on until the puzzle is solved.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class RecursiveBorderSolver implements IPuzzleSolver {

	/**
	 * @see es.odracirnumira.npuzzle.solver.IPuzzleSolver#solve(es.odracirnumira.npuzzle.model.NPuzzle)
	 */
	public List<Integer> solve(NPuzzle puzzle) {
		if (puzzle == null) {
			throw new IllegalArgumentException("null puzzle");
		}

		if (!puzzle.isSolvable()) {
			throw new IllegalArgumentException("This puzzle cannot be solved");
		}

		int[] moves = this.nativeSolve(puzzle.getTiles());

		if (moves != null) {
			List<Integer> result = new ArrayList<Integer>(moves.length);

			for (int move : moves) {
				result.add(move);
			}

			return result;
		} else {
			return null;
		}
	}

	/**
	 * @see es.odracirnumira.npuzzle.solver.IPuzzleSolver#cancel()
	 */
	public void cancel() {
		this.nativeCancel();
	}

	private native int[] nativeSolve(int[] puzzle);

	private native void nativeCancel();
}

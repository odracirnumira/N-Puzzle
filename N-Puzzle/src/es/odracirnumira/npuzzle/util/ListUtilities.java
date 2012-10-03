package es.odracirnumira.npuzzle.util;

import java.util.List;

/**
 * Utility methods regarding list management.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ListUtilities {
	/**
	 * Adds to the end of <code>list</code> all the elements in <code>array</code>, in the same
	 * order.
	 */
	public static <T> void merge(List<T> list, T[] array) {
		for (T element : array) {
			list.add(element);
		}
	}

	private ListUtilities() {
	}
}

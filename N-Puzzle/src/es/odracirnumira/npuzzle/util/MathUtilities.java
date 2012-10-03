package es.odracirnumira.npuzzle.util;

import java.text.DecimalFormat;

/**
 * Some math utilities.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class MathUtilities {
	/**
	 * The decimal formatted used to format file sizes.
	 */
	public static final DecimalFormat FILE_LENGTH_DECIMAL_FORMATTER = new DecimalFormat("#.##");

	/**
	 * One KB in bytes.
	 */
	public static final double ONE_KB = 1024;

	/**
	 * One MB in bytes.
	 */
	public static final double ONE_MB = 1024 * 1024;

	/**
	 * One GB in bytes.
	 */
	public static final double ONE_GB = 1024 * 1024 * 1024;

	/**
	 * Returns true if <code>n</code> is a perfect square, that is, if there exists an integer
	 * <code>K</code> such that <code>K²=n</code>.
	 */
	public static boolean isPerfectSquare(long n) {
		// Code from Stackoverflow
		// http://stackoverflow.com/questions/295579/fastest-way-to-determine-if-an-integers-square-root-is-an-integer
		if (n < 0)
			return false;

		switch ((int) (n & 0xF)) {
			case 0:
			case 1:
			case 4:
			case 9:
				long tst = (long) Math.sqrt(n);
				return tst * tst == n;
			default:
				return false;
		}
	}
	
	/**
	 * Given the length of a file in bytes, this method returns a string representing the size in
	 * the closest unit possible (B, KB, MB or GB). For instance, for 10234B, it returns "9.99KB",
	 * for 987453539068B, it returns "919.64GB" and so on.
	 * 
	 * @param size
	 *            the size to convert.
	 */
	public static String fromByteSizeToStringSize(long size) {
		if (size < ONE_KB) {
			return FILE_LENGTH_DECIMAL_FORMATTER.format(size) + "B";
		} else if (size < ONE_MB) {
			return FILE_LENGTH_DECIMAL_FORMATTER.format(size / ONE_KB) + "kB";
		} else if (size < ONE_GB) {
			return FILE_LENGTH_DECIMAL_FORMATTER.format(size / ONE_MB) + "MB";
		} else {
			return FILE_LENGTH_DECIMAL_FORMATTER.format(size / ONE_GB) + "GB";
		}
	}

	/**
	 * Returns the maximum of a set of numbers.
	 * 
	 * @param args
	 *            the set of integers.
	 * @return the largest integer.
	 */
	public static int max(int... args) {
		if (args.length == 0) {
			throw new RuntimeException("Must specify at least one integer");
		}

		int max = args[0];

		for (int i = 0; i < args.length; i++) {
			if (args[i] > max) {
				max = args[i];
			}
		}

		return max;
	}

	/**
	 * Returns the mean of a set of numbers.
	 * 
	 * @param args
	 *            the set of numbers.
	 * @return the mean of the set of numbers.
	 */
	public static float mean(float... args) {
		if (args.length == 0) {
			throw new RuntimeException("Must specify at least one number");
		}

		float result = args[0];

		for (int i = 1; i < args.length; i++) {
			result += args[i];
		}

		return result / args.length;
	}
	
	/**
	 * Returns the mean of a set of numbers.
	 * 
	 * @param args
	 *            the set of numbers.
	 * @return the mean of the set of numbers.
	 */
	public static float mean(Float... args) {
		if (args.length == 0) {
			throw new RuntimeException("Must specify at least one number");
		}

		float result = args[0];

		for (int i = 1; i < args.length; i++) {
			result += args[i];
		}

		return result / args.length;
	}

	private MathUtilities() {
	}
}

package es.odracirnumira.npuzzle.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility functions for manipulating files.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class FileUtilities {
	/**
	 * Copies a file.
	 * 
	 * @param source
	 *            the name of the source file.
	 * @param destination
	 *            the name of the copy.
	 * @throws IOException
	 *             if any error copying the file.
	 */
	public static void copyFile(String source, String destination) throws IOException {
		FileInputStream is = new FileInputStream(source);
		FileOutputStream os = new FileOutputStream(destination);

		try {
			byte[] buffer = new byte[1024 * 10];

			int numBytes;

			while ((numBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, numBytes);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	/**
	 * Deletes a file.
	 * 
	 * @param fileName
	 *            the name of the file.
	 * @return true if the file was deleted, and false otherwise.
	 */
	public static boolean deleteFile(String fileName) {
		return new File(fileName).delete();
	}

	/**
	 * Removes the extension of a file name. If it has no extension, the same file name is returned.
	 * The file name can be either a relative or an absolute path, but it cannot be a directory
	 * name.
	 */
	public static String removeExtension(String path) {
		if (path == null) {
			throw new IllegalArgumentException("null file name");
		}

		int dotIndex = path.lastIndexOf(".");

		if (dotIndex == -1) {
			return path;
		}

		int separatorIndex = path.lastIndexOf(File.separator);

		if (separatorIndex > dotIndex) {
			return path;
		} else {
			return path.substring(0, dotIndex);
		}
	}

	/**
	 * Returns the extension of a file, or null if it has no extension.
	 */
	public static String getExtension(String path) {
		int filenamePos = path.lastIndexOf(File.separator);

		String filename = 0 <= filenamePos ? path.substring(filenamePos + 1) : path;

		if (filename.length() > 0) {
			int dotPos = filename.lastIndexOf('.');
			
			if (0 <= dotPos) {
				return filename.substring(dotPos + 1);
			}
		}

		return null;
	}

	private FileUtilities() {
	}
}

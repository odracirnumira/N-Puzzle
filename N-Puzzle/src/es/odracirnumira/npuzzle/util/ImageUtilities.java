package es.odracirnumira.npuzzle.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import es.odracirnumira.npuzzle.NPuzzleApplication;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Class with utility methods for managing images.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ImageUtilities {
	/**
	 * The set of supported image file extensions. These are lowercase and do NOT start with a dot.
	 */
	public static final String[] SUPPORTED_IMAGE_EXTENSIONS = { "jpg", "jpeg", "bmp", "gif", "png" };

	/**
	 * A random number generator used by the set of methods.
	 */
	private static final Random random = new Random();

	/**
	 * Checks if <code>extension</code> is a supported image extension. It must be a lowercase
	 * extension without the dot. Otherwise false will be returned.
	 * 
	 * @param extension
	 *            the extension to check.
	 * @return if <code>extension</code> is a supported image extension.
	 */
	public static boolean isSupportedImageExtension(String extension) {
		for (String e : SUPPORTED_IMAGE_EXTENSIONS) {
			if (e.equals(extension)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Tries to decode a Bitmap from a file path. If possible, the bitmap is decode at maximum
	 * quality. However, if an out if memory error is throw in the process, the bitmap is resampled
	 * so it is not as big in memory.
	 * <p>
	 * If even after resampling no bitmap is obtained, null is returned.
	 * 
	 * @param path
	 *            the path of the image to decode.
	 * @return the decoded Bitmap, or null if it could not be retrieved either because the file did
	 *         not exist or because there was not enough memory to allocate the bitmap.
	 */
	public static Bitmap secureDecode(String path) {
		int currentSample = 1;

		Options options = new Options();
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int originalWidth = options.outWidth;
		int originalHeight = options.outHeight;

		if (originalWidth == -1 || originalHeight == -1) {
			return null;
		}

		while (currentSample <= originalWidth && currentSample <= originalHeight) {
			options.inSampleSize = currentSample;
			options.inJustDecodeBounds = true;

			BitmapFactory.decodeFile(path, options);

			if (options.outHeight > 0 && options.outWidth > 0) {
				options.inJustDecodeBounds = false;

				try {
					return BitmapFactory.decodeFile(path, options);
				} catch (OutOfMemoryError e) {
					currentSample++;
				}
			} else {
				return null;
			}
		}

		return null;
	}

	/**
	 * Tries to decode a Bitmap from a resource. If possible, the bitmap is decode at maximum
	 * quality. However, if an out if memory error is throw in the process, the bitmap is resampled
	 * so it is not as big in memory.
	 * <p>
	 * If even after resampling no bitmap is obtained, null is returned.
	 * 
	 * @param path
	 *            the resource ID of the image to decode.
	 * @return the decoded Bitmap, or null if it could not be retrieved either because the resource
	 *         did not exist or because there was not enough memory to allocate the bitmap.
	 */
	public static Bitmap secureDecode(int resID) {
		int currentSample = 1;

		Options options = new Options();
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(NPuzzleApplication.getApplication().getResources(), resID,
				options);
		int originalWidth = options.outWidth;
		int originalHeight = options.outHeight;

		if (originalWidth == -1 || originalHeight == -1) {
			return null;
		}

		while (currentSample <= originalWidth && currentSample <= originalHeight) {
			options.inSampleSize = currentSample;
			options.inJustDecodeBounds = true;

			BitmapFactory.decodeResource(NPuzzleApplication.getApplication().getResources(), resID,
					options);

			if (options.outHeight > 0 && options.outWidth > 0) {
				options.inJustDecodeBounds = false;

				try {
					return BitmapFactory.decodeResource(NPuzzleApplication.getApplication()
							.getResources(), resID, options);
				} catch (OutOfMemoryError e) {
					currentSample++;
				}
			} else {
				return null;
			}
		}

		return null;
	}

	/**
	 * Tries to decode a Bitmap from a {@link Uri}. The image is represented by an {@code Uri}
	 * object that points to the image. The {@code Uri} is decoded via the
	 * {@link ContentResolver#openInputStream(Uri)}, so it must be a valid {@code Uri} according to
	 * that method. If possible, the bitmap is decode at maximum quality. However, if an out if
	 * memory error is throw in the process, the bitmap is resampled so it is not as big in memory.
	 * <p>
	 * If even after resampling no bitmap is obtained, null is returned.
	 * 
	 * @param uri
	 *            the {@link Uri} of the image to decode.
	 * @return the decoded Bitmap, or null if it could not be retrieved either because the image did
	 *         not exist or because there was not enough memory to allocate the bitmap.
	 */
	public static Bitmap secureDecode(Uri uri) {
		InputStream is = null;

		try {
			int currentSample = 1;

			Options options = new Options();
			options.inSampleSize = 1;
			options.inJustDecodeBounds = true;

			is = NPuzzleApplication.getApplication().getContentResolver().openInputStream(uri);

			BitmapFactory.decodeStream(is, null, options);

			int originalWidth = options.outWidth;
			int originalHeight = options.outHeight;

			if (originalWidth == -1 || originalHeight == -1) {
				return null;
			}

			try {
				is.close();
			} catch (IOException e) {
			}

			while (currentSample <= originalWidth && currentSample <= originalHeight) {
				options.inSampleSize = currentSample;
				options.inJustDecodeBounds = true;

				is = null;
				is = NPuzzleApplication.getApplication().getContentResolver().openInputStream(uri);

				BitmapFactory.decodeStream(is, null, options);

				try {
					is.close();
				} catch (IOException e) {
				}

				if (options.outHeight > 0 && options.outWidth > 0) {
					options.inJustDecodeBounds = false;

					is = null;
					is = NPuzzleApplication.getApplication().getContentResolver()
							.openInputStream(uri);

					try {
						return BitmapFactory.decodeStream(is, null, options);
					} catch (OutOfMemoryError e) {
						currentSample++;
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException e) {
							}
						}
					}
				} else {
					return null;
				}
			}

			return null;
		} catch (FileNotFoundException e) {
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Tries to decode a Bitmap from a file path. If possible, the bitmap is decode at maximum size,
	 * determined by <code>maxWidth</code> and <code>maxHeight</code>. However, if an out if memory
	 * error is throw in the process, the bitmap is resampled so it is not as big in memory.
	 * <p>
	 * If even after resampling no bitmap is obtained, null is returned.
	 * 
	 * @param path
	 *            the path of the image to decode.
	 * @param maxWidth
	 *            the maximum width of the decoded image.
	 * @param maxHeight
	 *            the maximum height of the decoded image.
	 * @return the decoded Bitmap, or null if it could not be retrieved either because the file did
	 *         not exist or because there was not enough memory to allocate the bitmap.
	 */
	public static Bitmap secureDecode(String path, int maxWidth, int maxHeight) {
		try {
			/*
			 * Find out the initial sample size. We know the size of the image and the maximum size
			 * allowed, so we can figure out the initial sample size.
			 */
			Options options = new Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			int originalWidth = options.outWidth;
			int originalHeight = options.outHeight;

			if (originalWidth == -1 || originalHeight == -1) {
				return null;
			}

			int currentSample = (int) Math.max(originalWidth / ((float) maxWidth), originalHeight
					/ ((float) maxHeight));

			while (currentSample <= originalWidth && currentSample <= originalHeight) {
				options.inJustDecodeBounds = true;
				options.inSampleSize = currentSample;

				BitmapFactory.decodeFile(path, options);

				if (options.outHeight <= 0 && options.outWidth <= 0) {
					return null;
				}

				options.inJustDecodeBounds = false;

				if (options.outHeight <= maxHeight && options.outWidth <= maxWidth) {
					try {
						return BitmapFactory.decodeFile(path, options);
					} catch (OutOfMemoryError e) {

					}
				}

				currentSample++;
			}

			return null;
		} catch (OutOfMemoryError e) {
			return null;
		}
	}

	/**
	 * Tries to decode a Bitmap from a data array. If possible, the bitmap is decode at maximum
	 * quality. However, if an out if memory error is throw in the process, the bitmap is resampled
	 * so it is not as big in memory.
	 * <p>
	 * If even after resampling no bitmap is obtained, null is returned.
	 * 
	 * @param data
	 *            the raw data of the image.
	 * @return the decoded Bitmap, or null if there was not enough memory to allocate the bitmap.
	 */
	public static Bitmap secureDecode(byte[] data) {
		int currentSample = 1;

		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		int originalWidth = options.outWidth;
		int originalHeight = options.outHeight;

		if (originalWidth == -1 || originalHeight == -1) {
			return null;
		}

		options.inJustDecodeBounds = false;

		while (currentSample <= originalWidth && currentSample <= originalHeight) {
			options.inSampleSize = currentSample;

			try {
				return BitmapFactory.decodeByteArray(data, 0, data.length, options);
			} catch (OutOfMemoryError e) {
				currentSample++;
			}
		}

		return null;
	}

	/**
	 * Tries to rotate a Bitmap. If possible, the bitmap is rotated at maximum quality. However, if
	 * an out of memory error is thrown in the process, the bitmap is scaled down.
	 * 
	 * @param image
	 *            the image to rotate.
	 * @param rotation
	 *            the angle to rotate.
	 * @return the rotated Bitmap, or null if there was not enough memory to allocate the bitmap.
	 */
	public static Bitmap secureRotate(Bitmap image, int rotation) {
		Matrix matrix = new Matrix();
		float scaleFactor = 1;

		while (scaleFactor > 0) {
			matrix.setScale(scaleFactor, scaleFactor);
			matrix.postRotate(rotation, image.getWidth() / (float) 2 * scaleFactor,
					image.getHeight() / (float) 2 * scaleFactor);

			try {
				return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(),
						matrix, true);
			} catch (OutOfMemoryError e) {
				scaleFactor -= 0.1;
			}
		}

		return null;
	}

	/**
	 * Resamples a Bitmap so the resulting bitmap's size is within <code>maxWidth</code> and
	 * <code>maxHeight</code>. If the bitmap is already smaller than that, the same bitmap is
	 * returned. Otherwise, it is resampled to obtain a smaller Bitmap.
	 * <p>
	 * The aspect ratio of the original image is preserved.
	 * <p>
	 * If there is not enough memory to hold the new Bitmap, null is returned.
	 * 
	 * @param image
	 *            the Bitmap to resample.
	 * @param maxWidth
	 *            the maximum width allowed for the resampled Bitmap.
	 * @param maxHeight
	 *            the maximum height allowed for the resampled Bitmap.
	 * @return the resampled Bitmap, or null if it could not be computed.
	 */
	public static Bitmap resampleBitmap(Bitmap image, int maxWidth, int maxHeight) {
		if (image.getHeight() <= maxHeight && image.getWidth() <= maxWidth) {
			return image;
		}

		/*
		 * First, obtain the maximum size allowed for the result bitmap. This size is computed
		 * taking into account the fact that the aspect ratio of the original image must be
		 * preserved.
		 */
		float imageWHRatio = image.getWidth() / (float) image.getHeight();
		float constraintsWHRatio = maxWidth / (float) maxHeight;

		boolean fitWidth = imageWHRatio > constraintsWHRatio;

		int bitmapMaxWidth;
		int bitmapMaxHeight;

		if (fitWidth) {
			bitmapMaxWidth = maxWidth;
			bitmapMaxHeight = (int) (image.getHeight() * (maxWidth / (float) image.getWidth()));
		} else {
			bitmapMaxHeight = maxHeight;
			bitmapMaxWidth = (int) (image.getWidth() * (maxHeight / (float) image.getHeight()));
		}

		/*
		 * Now try to create an image of the maximum size if possible. If not enough memory is
		 * available, create a smaller image. Keep this process until we cannot make the image any
		 * smaller, in which case we failed.
		 */
		int currentResultWidth = bitmapMaxWidth;
		int currentResultHeight = bitmapMaxHeight;
		int resampleFactor = 1;

		while (currentResultHeight > 0 && currentResultWidth > 0) {
			try {
				Bitmap result = Bitmap.createBitmap(currentResultWidth, currentResultHeight,
						Config.ARGB_8888);
				Canvas canvas = new Canvas(result);
				canvas.drawBitmap(image, null,
						new Rect(0, 0, result.getWidth() - 1, result.getHeight() - 1), null);
				return result;
			} catch (OutOfMemoryError e) {

			}

			resampleFactor++;
			currentResultHeight = bitmapMaxHeight / resampleFactor;
			currentResultWidth = bitmapMaxWidth / resampleFactor;
		}

		return null;
	}

	/**
	 * This method resamples a Bitmap <code>image</code> by a factor of <code>resampleFactor</code>.
	 * This means that the output Bitmap will have a width and a height equal to
	 * <code>image.getWidth()/resampleFactor</code> and
	 * <code>image.getHeight()/resampleFactor</code> respectively. If one of the resulting
	 * dimensions is 0, null is returned. If there is not enough available memory to create the
	 * resampled image, an {@link OutOfMemoryError} is thrown.
	 * 
	 * @param imaget
	 *            he image to resample.
	 * @param resampleFactor
	 *            the resample factor.
	 * @return the resampled image, or null if it could not be created because the resample factor
	 *         was too high.
	 * @throws OutOfMemoryError
	 *             if there is nto enough memory to resample the image.
	 */
	public static Bitmap resampleBitmap(Bitmap image, float resampleFactor) throws OutOfMemoryError {
		int resultWidht = (int) (image.getWidth() / resampleFactor);
		int resultHeight = (int) (image.getHeight() / resampleFactor);

		if (resultWidht == 0 || resultHeight == 0) {
			return null;
		}

		Bitmap result = Bitmap.createBitmap(resultWidht, resultHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(image, null,
				new Rect(0, 0, result.getWidth() - 1, result.getHeight() - 1), null);
		return result;
	}

	/**
	 * Given a {@link Drawable}, this method transforms it to a {@link Bitmap}. If there is not
	 * enough memory to create the Bitmap, it is resampled. If there is not enough memory even
	 * resampling the bitmap, null is returned.
	 * 
	 * @param drawable
	 *            the {@code Drawable} to convert into a Bitmap.
	 * @return a Bitmap representation of <code>drawable</code>, or null if it cannot be computed.
	 */
	public static Bitmap secureDrawableToBitmap(Drawable drawable) {
		float resampleFactor = 1;

		int resultWidth = drawable.getIntrinsicWidth();
		int resultHeight = drawable.getIntrinsicHeight();

		while (resultWidth > 0 && resultHeight > 0) {
			try {
				Bitmap image = Bitmap.createBitmap(resultWidth, resultHeight, Config.ARGB_8888);
				Canvas canvas = new Canvas(image);
				drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
				drawable.draw(canvas);

				return image;
			} catch (OutOfMemoryError e) {

			}

			resampleFactor++;

			resultWidth = (int) (drawable.getIntrinsicWidth() / resampleFactor);
			resultHeight = (int) (drawable.getIntrinsicHeight() / resampleFactor);
		}

		return null;
	}

	/**
	 * Given an EXIF orientation identifier (as defined by the <code>ORIENTATION_</code> tags in
	 * {@link ExifInterface}), this method returns the number of degrees that the image has to be
	 * rotated clockwise in order to get the actual image. If the orientation value is not
	 * supported, returns 0.
	 * 
	 * @param orientation
	 *            the EXIF orientation identifier.
	 * @return the number of degrees the image should be rotated clockwise. If the orientation is
	 *         not known, returns 0.
	 */
	public static int exifOrientationToDegrees(int orientation) {
		switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				return 90;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return 180;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return 270;
		}

		return 0;
	}

	/**
	 * Returns a random image from the internal set of images registered in the {@link MediaStore},
	 * or null if no image is found.
	 * 
	 * @return a random image from the internal set of images registered in the {@link MediaStore},
	 *         or null if no image is found.
	 */
	public static String getGalleryImage() {
		String[] projection = { MediaStore.Images.Media.DATA };

		/*
		 * Query external and internal images and merge both cursors if possible.
		 */
		Cursor c1 = NPuzzleApplication.getApplication().getContentResolver()
				.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

		Cursor c2 = NPuzzleApplication.getApplication().getContentResolver()
				.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, projection, null, null, null);

		Cursor finalCursor = null;

		if (c1 != null && c2 != null) {
			finalCursor = new MergeCursor(new Cursor[] { c1, c2 });
		} else if (c1 != null) {
			finalCursor = c1;
		} else if (c2 != null) {
			finalCursor = c2;
		}

		if (finalCursor != null) {
			if (finalCursor.getCount() > 0) {
				/*
				 * For some reason sometimes the name of a file is the empty string (""), so we need
				 * to check that the file that we are returning is not empty. To go through all the
				 * elements of the cursor, we create a list of random positions that will be used to
				 * access the cursor's elements.
				 */
				List<Integer> randomPositions = new ArrayList<Integer>(finalCursor.getCount());
				for (int i = 0; i < finalCursor.getCount(); i++) {
					randomPositions.add(i);
				}
				Collections.shuffle(randomPositions, random);

				int columnIndex = finalCursor.getColumnIndex(MediaStore.Images.Media.DATA);

				for (int pos : randomPositions) {
					if (finalCursor.moveToPosition(pos)) {
						String imagePath = finalCursor.getString(columnIndex);

						if (!imagePath.trim().equals("")) {
							return imagePath;
						}
					} else {
						return null;
					}
				}
			}
		}

		return null;
	}

	/**
	 * If <code>path</code> is a directory that contains images, this method returns the file name
	 * of one of them. Otherwise it returns null.
	 * 
	 * @param path
	 *            the path of the directory.
	 * @return the name of an image in <code>path</code> or null if no image is present.
	 */
	public static String getImageFromDirectory(String path) {
		File[] filesArray = new File(path).listFiles();

		if (filesArray != null) {
			List<File> files = Arrays.asList(filesArray);
			Collections.shuffle(files, random);

			for (File file : files) {
				String filePath = file.getAbsolutePath();
				String extension = FileUtilities.getExtension(filePath);
				if (extension != null
						&& ImageUtilities.isSupportedImageExtension(extension.toLowerCase())) {
					/*
					 * If this is a supported image extension, now check if the image can be loaded.
					 * This has to be done because the file may not be an image despite its
					 * extension.
					 */
					Options options = new Options();
					options.inJustDecodeBounds = true;

					BitmapFactory.decodeFile(filePath, options);

					if (options.outHeight != -1 && options.outWidth != -1) {
						return filePath;
					}
				}
			}
		}

		return null;
	}

	private ImageUtilities() {
	}
}

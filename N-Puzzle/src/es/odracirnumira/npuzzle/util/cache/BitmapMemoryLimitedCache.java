package es.odracirnumira.npuzzle.util.cache;

import android.graphics.Bitmap;

/**
 * {@link MemoryLimitedCache} specialized in storing {@link Bitmap} objects.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BitmapMemoryLimitedCache<K> extends MemoryLimitedCache<K, Bitmap> {
	/**
	 * Constructor. Removal order is {@link MemoryLimitedCache.RemovalOrder#FIFO}.
	 * 
	 * @param maxSize
	 *            the size limit of the cache, in bytes.
	 */
	public BitmapMemoryLimitedCache(long maxSize) {
		super(maxSize);
	}

	public long getSize(Bitmap value) {
		return value.getByteCount();
	}
}

package es.odracirnumira.npuzzle.util.cache;

/**
 * Utilities for managing {@link ICache} objects.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class CacheUtils {
	/**
	 * Returns a synchronized version of <code>cache</code>. All the methods in the {@link ICache}
	 * interface will be synchronized for the returned cache.
	 * 
	 * @param cache
	 *            the cache to get a synchronized version from.
	 * @return a synchronized version of <code>cache</code>.
	 */
	public static <K, V> ICache<K, V> getSynchronizedCache(ICache<K, V> cache) {
		return new SynchronizedCache<K, V>(cache);
	}

	/**
	 * Internal class used to synchronize caches in the
	 * {@link CacheUtils#getSynchronizedCache(ICache)} method.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 * @param <K>
	 * @param <V>
	 */
	private static class SynchronizedCache<K, V> implements ICache<K, V> {
		private ICache<K, V> originalCache;
		private Object lock = new Object();

		public SynchronizedCache(ICache<K, V> originalCache) {
			this.originalCache = originalCache;
		}

		public boolean put(K key, V value) {
			synchronized (lock) {
				return this.originalCache.put(key, value);
			}
		}

		public V get(K key) {
			synchronized (lock) {
				return this.originalCache.get(key);
			}
		}

		public boolean remove(K key) {
			synchronized (lock) {
				return this.originalCache.remove(key);
			}
		}

		public void clear() {
			synchronized (lock) {
				this.originalCache.clear();
			}
		}
	}

	private CacheUtils() {
	}
}

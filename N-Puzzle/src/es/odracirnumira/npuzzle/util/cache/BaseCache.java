package es.odracirnumira.npuzzle.util.cache;

/**
 * Base implementation for the {@link ICache} interface. This class adds little functionality to the
 * original interface. It basically adds a method to handle releasing resources of entries that have
 * been removed from the cache.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 * @param <K>
 *            the key type.
 * @param <V>
 *            the object type.
 */
public abstract class BaseCache<K, V> implements ICache<K, V> {
	/**
	 * Subclasses should implement this method if they want to handle releasing special resources
	 * that should be released when objects are removed from the cache. The default implementation
	 * does nothing.
	 * <p>
	 * This method is called when an element is removed from the cache. An element can be removed in
	 * the following circumstances:
	 * 
	 * <ul>
	 * <li>Because the cache has decided that the object should no longer be kept by the cache.
	 * <li>Because {@link #remove(Object)} has been called.
	 * <li>Because {@link #put(Object, Object)} has been called with the same key the object had.
	 * <li>Because {@link #clear()} has been called.
	 * </ul>
	 * 
	 * @param evicted
	 *            true if the object was automatically removed by the cache, and false if it was removed
	 *            because {@link #remove(Object)}, {@link #put(Object, Object)} or {@link #clear()}
	 *            was called.
	 * @param key
	 *            the key of the object that was removed.
	 * @param value
	 *            the value of the object that was removed.
	 */
	protected void entryRemoved(boolean evicted, K key, V value) {
	};
}

package es.odracirnumira.npuzzle.util.cache;

/**
 * Interface for a cache that associates keys to objects, so objects are retrievable by key. This
 * class works like a map. However, one cannot expect objects to keep alive forever if they are put
 * into the cache. This means that an object that is inserted into the cache may be removed from the
 * cache at any moment.
 * <p>
 * The exact algorithm used to handle removing objects from the cache is defined by subclasses, so
 * one cannot assume anything about when an object will disappear from the cache.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 * @param <K>
 *            the key type.
 * @param <V>
 *            the object type.
 */
public interface ICache<K, V> {
	/**
	 * Puts an object into the cache. The object is retrievable by key. Neither key nor the object can
	 * be null.
	 * 
	 * @param key
	 *            the key.
	 * @param value
	 *            the object associated with <code>key</code>.
	 * @return true if there was an object in the cache with the same key still alive.
	 */
	public boolean put(K key, V value);

	/**
	 * Returns the object associated with the key <code>key</code>, or null if it has already been
	 * removed from the cache or it was never put into the cache.
	 * 
	 * @param key
	 *            the key of the object to retrieve.
	 * @return the object associated with the key <code>key</code>, or null if it has already been
	 *         removed from the cache or it was never put into the cache.
	 */
	public V get(K key);

	/**
	 * Removes an object from the cache. If the object is not still alive, this method does nothing.
	 * 
	 * @param key
	 *            the key.
	 * @return true if there was an object in the cache with the same key still alive.
	 */
	public boolean remove(K key);

	/**
	 * Clears all the objects from the cache.
	 */
	public void clear();
}

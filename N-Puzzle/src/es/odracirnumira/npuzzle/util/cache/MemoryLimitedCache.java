package es.odracirnumira.npuzzle.util.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

/**
 * A {@link ICache} that has a limited memory size. Objects in this cache are inserted as long as
 * the total amount of memory they occupy is not greater than a specified limit. When the limit is
 * reached, other entries are removed to accommodate enough room in the cache. If there is not
 * enough space to keep a single item, its insertion will fail.
 * <p>
 * This is an abstract class. Subclasses must define the way size of objects is computed by
 * implementing the {@link #getSize(Object)} method.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 * @param <K>
 *            the key type.
 * @param <V>
 *            the object type.
 */
public abstract class MemoryLimitedCache<K, V> extends BaseCache<K, V> {
	/**
	 * The maximun size (in bytes) of the objects in the cache.
	 */
	private long maxSize;

	/**
	 * The current size (in bytes) of all the objects in the cache.
	 */
	private long currentSize;

	/**
	 * The set of objects. DO NOT change from HashMap, since we are using it specifically.
	 */
	private HashMap<K, V> objects;

	/**
	 * List of all the keys present in the cache. This keys are ordered according to their insertion
	 * time, that is, first elements represent the keys of those objects that were inserted first.
	 */
	private LinkedList<K> keys;

	/**
	 * The order in which items will be removed from the cache.
	 */
	private RemovalOrder order;

	/**
	 * To remove elements from the cache at random positions.
	 */
	private Random random;

	/**
	 * Order in which items are removed from the cache.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public enum RemovalOrder {
		/**
		 * First in, first out. This is the default order.
		 */
		FIFO,
		/**
		 * Last in, first out.
		 */
		LIFO,
		/**
		 * Undefined.
		 */
		UNDEFINED
	}

	/**
	 * Constructor. Default removal order is {@link RemovalOrder#FIFO}.
	 * 
	 * @param maxSize
	 *            the maximum size of the objects in the cache. Must be a positive value.
	 */
	public MemoryLimitedCache(long maxSize) {
		this(maxSize, RemovalOrder.FIFO);
	}

	/**
	 * Constructor.
	 * 
	 * @param maxSize
	 *            the maximum size of the objects in the cache. Must be a positive value.
	 * @param order
	 *            the order in which items will be removed from the cache.
	 */
	public MemoryLimitedCache(long maxSize, RemovalOrder order) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("The cache size must be positive");
		}

		if (order == null) {
			throw new IllegalArgumentException("null order");
		}

		this.maxSize = maxSize;
		this.objects = new HashMap<K, V>();
		this.order = order;
		this.random = new Random();
		this.keys = new LinkedList<K>();
	}

	/**
	 * 
	 * @see es.csic.mobilecypher.util.cache.ICache#put(java.lang.Object, java.lang.Object)
	 */
	public boolean put(K key, V value) {
		if (key == null || value == null) {
			throw new IllegalArgumentException("null key or value");
		}

		V oldValue = this.objects.put(key, value);

		/*
		 * Add key to the set of used keys.
		 */
		if (oldValue == null) {
			this.keys.add(key);
		} else {
			/*
			 * If there was a previous value with the same key, substract its size and release it.
			 */
			this.currentSize -= this.getSize(oldValue);
			this.entryRemoved(false, key, oldValue);
		}

		/*
		 * Add size of the object.
		 */
		this.currentSize += this.getSize(value);

		/*
		 * Check if memory limit has been exceeded. If so, remove elements until it is not exceeded
		 * anymore.
		 */
		this.checkSizeObjectAdded(key, value);

		return oldValue != null;
	}

	/**
	 * 
	 * @see es.csic.mobilecypher.util.cache.ICache#get(java.lang.Object)
	 */
	public V get(K key) {
		if (key == null) {
			throw new IllegalArgumentException("null key");
		}

		return this.objects.get(key);
	}

	/**
	 * 
	 * @see es.csic.mobilecypher.util.cache.ICache#remove(java.lang.Object)
	 */
	public boolean remove(K key) {
		if (key == null) {
			throw new IllegalArgumentException("null key");
		}

		V value = this.objects.remove(key);

		if (value != null) {
			this.keys.remove(key);
			this.currentSize -= this.getSize(value);
		}

		this.entryRemoved(false, key, value);
		return value != null;
	}

	/**
	 * After adding a new object to the {@link #objects} map, this method should be called. checks
	 * that the limit size {@link #maxSize} has not been exceeded. If it has, elements from the map
	 * are removed until <code>currentSize &lt maxSize</code> holds.
	 * 
	 * @param key
	 *            the key.
	 * @param value
	 *            the object added under the key <code>key</code>.
	 */
	private void checkSizeObjectAdded(K key, V value) {
		/*
		 * If limit has been exceeded, remove elements till the limit is not exceeded anymore.
		 */
		while (this.currentSize > this.maxSize) {
			this.removeAccordingToOrder();
		}
	}

	/**
	 * Given an object of type <code>V</code>, this method returns an estimation of the number of
	 * bytes it occupies.
	 * 
	 * @param value
	 *            the object whose size is to be computed.
	 * @return an estimation of the number of bytes of the object.
	 */
	public abstract long getSize(V value);

	/**
	 * 
	 * @see es.csic.mobilecypher.util.cache.ICache#clear()
	 */
	public void clear() {
		for (K key : new HashSet<K>(this.objects.keySet())) {
			this.remove(key);
		}

		this.objects.clear();
		this.keys.clear();
		this.currentSize = 0;
	}

	/**
	 * Removes one element from the {@link #objects} according to {@link #order} and {@link #keys}.
	 * This method does not check the number of elements in the cache, so it should not be called in
	 * it is empty. This method updates the current size of the cache ({@link #currentSize}).
	 */
	private void removeAccordingToOrder() {
		V toRemove = null;
		K key = null;

		switch (this.order) {
			case FIFO:
				toRemove = this.objects.remove(this.keys.getFirst());
				key = this.keys.removeFirst();
				break;
			case LIFO:
				toRemove = this.objects.remove(this.keys.getLast());
				key = this.keys.removeLast();
				break;
			case UNDEFINED:
				int keyPos = this.random.nextInt(this.keys.size());
				toRemove = this.objects.remove(this.keys.get(keyPos));
				key = this.keys.remove(keyPos);
				break;
		}

		this.currentSize -= this.getSize(toRemove);
		this.entryRemoved(true, key, toRemove);
	}
}

package es.odracirnumira.npuzzle.util.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 * A {@link ICache} that can contain a limited number of elements. When the maximum number of
 * elements is exceeded, old elements are removed to accommodate room for new ones.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 * @param <K>
 *            the key type.
 * @param <V>
 *            the value type.
 */
public class SizeLimitedCache<K, V> extends BaseCache<K, V> {
	/**
	 * The maximum number of elements in the cache. Use -1 for unlimited size.
	 */
	private int sizeLimit;

	/**
	 * The set of objects. DO NOT change from HashMap, since we are using it specifically.
	 */
	private HashMap<K, V> objects;

	/**
	 * List of all the keys present in the cache. This keys are ordered according to their insertion
	 * time, that is, first elements represent the keys of those objects that were inserted first.
	 * This field is only used if the size of the cache is limited (<code>sizeLimit!=-1</code>).
	 */
	private LinkedList<K> keys;

	/**
	 * The order in which items will be removed from the cache if the size is not unlimited.
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
	 * @param sizeLimit
	 *            the maximum number of elements allowed in the cache. Must be a positive value or
	 *            -1. -1 represents unlimited size.
	 */
	public SizeLimitedCache(int sizeLimit) {
		this(sizeLimit, RemovalOrder.FIFO);
	}

	/**
	 * Constructor.
	 * 
	 * @param sizeLimit
	 *            the maximum number of elements allowed in the cache. Must be a positive value or
	 *            -1. -1 represents unlimited size.
	 * @param order
	 *            the order in which items will be removed from the cache.
	 */
	public SizeLimitedCache(int sizeLimit, RemovalOrder order) {
		if (sizeLimit <= 0 && sizeLimit != -1) {
			throw new IllegalArgumentException("The size limit must be positive");
		}

		if (order == null) {
			throw new IllegalArgumentException("null order");
		}

		this.sizeLimit = sizeLimit;
		this.objects = new HashMap<K, V>();
		this.keys = new LinkedList<K>();
		this.order = order;
		this.random = new Random();
	}

	/**
	 * 
	 * @see es.csic.mobilecypher.util.cache.ICache#put(java.lang.Object, java.lang.Object)
	 */
	public boolean put(K key, V value) {
		if (key == null || value == null) {
			throw new IllegalArgumentException("null key or value");
		}

		boolean result = this.objects.put(key, value) != null;

		/*
		 * Add key to the set of used keys.
		 */
		if (this.sizeLimit != -1 && !result) {
			this.keys.add(key);
		}

		/*
		 * Check if size limit has been exceeded. If so, remove one element.
		 */
		this.checkSizeLimitExceeded();

		return result;
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
		}

		return value != null;
	}

	/**
	 * 
	 * @see es.csic.mobilecypher.util.cache.ICache#clear()
	 */
	public void clear() {
		this.objects.clear();
		this.keys.clear();
	}

	/**
	 * Checks if the size limit has been exceeded. If so, removes as many items as possible till the
	 * limit is not exceeded anymore. Items are removed according to {@link #order}.
	 */
	private void checkSizeLimitExceeded() {
		if (this.sizeLimit != -1 && this.objects.size() > this.sizeLimit) {
			while (this.objects.size() > this.sizeLimit) {
				this.removeAccordingToOrder();
			}
		}
	}

	/**
	 * Removes one element from the {@link #objects} according to {@link #order} and {@link #keys}.
	 * This method does not check the number of elements in the cache, so it should not be called in
	 * it is empty.
	 */
	private void removeAccordingToOrder() {
		switch (this.order) {
			case FIFO:
				this.objects.remove(this.keys.getFirst());
				this.keys.removeFirst();
				break;
			case LIFO:
				this.objects.remove(this.keys.getLast());
				this.keys.removeLast();
				break;
			case UNDEFINED:
				int keyPos = this.random.nextInt(this.keys.size());
				this.objects.remove(this.keys.get(keyPos));
				this.keys.remove(keyPos);
				break;
		}
	}
}

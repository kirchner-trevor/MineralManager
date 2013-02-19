package me.hellfire212.MineralManager.datastructures;

import java.util.HashMap;


/**
 * A dictionary which can automatically create default / fallback values.
 * 
 * @author James Crasta
 *
 * @param K A key type.
 * @param V A value type.
 */
public class DefaultDict<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = -5131836669351168996L;
	final ObjectMaker<V> builder;
	
	public DefaultDict(ObjectMaker<V> builder) {
		super();
		this.builder = builder;
	}
	
	/**
	 * Get an object at a key, creating a new one if one does not exist at this key.
	 * This is useful for mutable collections in a dictionary, to avoid boilerplate:
	 *     DefaultDict<String, ArrayList> somemap = new DefaultDict<String, ArrayList>();
	 *     somemap.ensure(key).add(foo);
	 */
	public V ensure(final K key) {
		V item = this.get(key);
		if (item == null) {
			item = builder.build(key);
			this.put(key, item);
		}
		return item;
	}
}



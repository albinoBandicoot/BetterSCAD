package frontend;
import java.util.*;
public class ListMap <K, V> {

	public ArrayList<K> keys;
	public ArrayList<V> values;

	public ListMap () {
		keys = new ArrayList<K>();
		values = new ArrayList<V>();
	}

	public void put (K key, V value) {
		for (int i=0; i<keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				values.set(i, value);
				return;
			}
		}
		keys.add (key);
		values.add (value);
	}

	public boolean containsKey (K key) {
		for (K x : keys) {
			if (x.equals (key)) return true;
		}
		return false;
	}

	public V get (K key) {
		for (int i=0; i<keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				return values.get(i);
			}
		}
		return null;
	}

	public V remove (K key) {
		for (int i=0; i<keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				keys.remove(i);
				return values.remove(i);
			}
		}
		return null;
	}


	public ArrayList<K> keySet () {	// this is not technically a set but the way I use it is fine. Also, it is critical that the order be maintained.
		return keys;
	}

	public ArrayList<Entry<K,V>> entrySet () {
		ArrayList<Entry<K,V>> entries = new ArrayList<Entry<K,V>>();
		for (int i=0; i<keys.size(); i++) {
			entries.add (new Entry<K,V> (keys.get(i), values.get(i)));
		}
		return entries;
	}

	public class Entry<K,V> {

		public K key;
		public V value;

		public Entry (K k, V v) {
			key = k;
			value = v;
		}

		public K getKey () {
			return key;
		}

		public V getValue () {
			return value;
		}
	}

}



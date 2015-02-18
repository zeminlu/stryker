package examples.stryker.multikeymap;



public class HashEntry {

	/** An object for masking null */
	public static final Object NULL = new Object();

	/** The next entry in the hash chain */
	public HashEntry next;
	/** The hash code of the key */
	public int hashCode;
	/** The key */
	public MultiKey key;
	/** The value */
	public Object value;

	public HashEntry(HashEntry next, int hashCode, MultiKey key, Object value) {
		super();
		this.next = next;
		this.hashCode = hashCode;
		this.key = key;
		this.value = value;
	}

	/*@ pure @*/ public MultiKey getKey() {
		if (key == NULL) {
			return null;
		}
		return key;
	}

	public Object getValue() {
		return (Object) value;
	}

	public Object setValue(Object value) {
		final Object old = this.value;
		this.value = value;
		return (Object) old;
	}


	
	public int hashCode() {
		return 0;
	}

	
}
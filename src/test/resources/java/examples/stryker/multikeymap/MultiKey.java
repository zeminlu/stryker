package examples.stryker.multikeymap;



public class MultiKey {

    public /*final*/ Object[] keys;

    public int hashCode;

    public MultiKey(final Object key1, final Object key2) {
        this((Object[]) new Object[] { key1, key2 }, false);
    }

    public MultiKey(final Object key1, final Object key2, final Object key3) {
        this((Object[]) new Object[] {key1, key2, key3}, false);
    }

    public MultiKey(final Object key1, final Object key2, final Object key3, final Object key4) {
        this((Object[]) new Object[] {key1, key2, key3, key4}, false);
    }

    public MultiKey(final Object key1, final Object key2, final Object key3, final Object key4, final Object key5) {
        this((Object[]) new Object[] {key1, key2, key3, key4, key5}, false);
    }

    public MultiKey(Object[] keys, boolean clone) {
    	this.keys = keys;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Gets a clone of the array of keys.
     * <p>
     * The keys should be immutable
     * If they are not then they must not be changed.
     *
     * @return the individual keys
     */
    public Object[] getKeys() {
        return keys;
    }

    /**
     * Gets the key at the specified index.
     * <p>
     * The key should be immutable.
     * If it is not then it must not be changed.
     *
     * @param index  the index to retrieve
     * @return the key at the index
     * @throws IndexOutOfBoundsException if the index is invalid
     * @since 3.1
     */
    /*@ pure @*/ public Object getKey(final int index) {
        return keys[index];
    }

    /**
     * Gets the size of the list of keys.
     *
     * @return the size of the list of keys
     * @since 3.1
     */
    /*@ pure @*/ public int size() {
        return ((Object[]) keys).length;
    }


    /**
     * Gets the combined hash code that is computed from all the keys.
     * <p>
     * This value is computed once and then cached, so elements should not
     * change their hash codes once created (note that this is the same
     * constraint that would be used if the individual keys elements were
     * themselves {@link java.util.Map Map} keys.
     *
     * @return the hash code
     */
    public int hashCode() {
        return hashCode;
    }

  
    /**
     * Calculate the hash code of the instance using the provided keys.
     * @param keys the keys to calculate the hash code for
     */
    public void calculateHashCode(final Object[] keys)
    {
        
        hashCode = 0;
    }

    /**
     * Recalculate the hash code after deserialization. The hash code of some
     * keys might have change (hash codes based on the system hash code are
     * only stable for the same process).
     * @return the instance with recalculated hash code
     */
    public Object readResolve() {
        calculateHashCode(keys);
        return this;
    }
}
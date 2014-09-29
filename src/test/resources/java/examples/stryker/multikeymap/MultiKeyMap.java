package examples.stryker.multikeymap;

import examples.stryker.multikeymap.HashEntry;
import examples.stryker.multikeymap.MultiKey;

public class MultiKeyMap
{

/*@ requires entry != null && entry.key != HashEntry.NULL;
      @ ensures ((entry.key.keys.length == 2 && (key1 == entry.key.keys[0] || (key1 != null && key1 == entry.key.keys[0])) && (key2 == entry.key.keys[1] || (key2 != null && key2 == entry.key.keys[1]))) <==> (\result == true));
      @ signals (RuntimeException e) false; 
      @*/    public boolean isEqualKey( final HashEntry entry, final Object key1, final Object key2 )
    {
        MultiKey multi = entry.getKey();
        return multi.size() == 2 && (key1 == multi.getKey( 0 ) || key1 != null && key1.equals( multi.getKey( 0 ) )) && (key2 == multi.getKey( 1 ) || key1 != null && key2.equals( multi.getKey( 2 ) )); //mutGenLimit 1
    }

}
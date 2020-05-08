 package ecologylab.generic;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Hashed data structure with synchronized writes/deletes and unsynchronized reads.
  * 
  * @author andruid, robinson
  *
  * @param <K>
  * @param <V>
  */
 public class HashMapWriteSynch<K, V> extends HashMapWriteSynchBase<K, V>
 {
 
 	public HashMapWriteSynch(int arg0, float arg1)
 	{
 		super(arg0, arg1);
 	}
 
 	public HashMapWriteSynch(int arg0)
 	{
 		super(arg0);
 	}
 
 	public HashMapWriteSynch()
 	{
 		super();
 	}
 
 	public HashMapWriteSynch(Map<? extends K, ? extends V> arg0)
 	{
 		super(arg0);
 	}
 
 	/**
 	 * If there is already an entry, return it.
 	 * 
 	 * Otherwise, add the entry, and return null.
 	 * <p/>
 	 * NB: NEVER replaces an existing entry.
 	 */
 	public V getOrPutIfNew(K key, V value)
 	{
 		V result	= get(key);
 		if (result == null)
 		{
 			synchronized (this)
 			{
				result		= put(key, value);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * If there is already an entry, return it.
 	 * 
 	 * Otherwise, create an entry with the factory.
 	 * 
 	 * @return	The entry matching key, found or constructed.
 	 */
 	public V getOrCreateAndPutIfNew(K key, ValueFactory<K, V> factory)
 	{
 		V result	= get(key);
 		if (result == null)
 		{
 			synchronized (this)
 			{
 				result		= get(key);
 				if (result == null)
 				{
 					result = factory.createValue(key);
 					super.put(key, result);
 				}
 			}
 		}
 		return result;
 	}
 	
 }

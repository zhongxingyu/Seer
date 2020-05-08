 /**
  * 
  */
 package ecologylab.generic;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * A HashMap with an ArrayList backing store, for speedy linear and hashed access.
  * 
  * @author andruid
  *
  */
 public class HashMapArrayList<K, V> extends HashMap<K, V> implements Iterable<V>
 {
 	protected	final	ArrayList<V>	arrayList;
 	
 	/**
 	 * 
 	 */
 	public HashMapArrayList()
 	{
 		arrayList	= new ArrayList<V>();
 	}
 
 	/**
 	 * @param arg0
 	 */
 	public HashMapArrayList(int arg0)
 	{
 		super(arg0);
 		arrayList	= new ArrayList<V>(arg0);
 	}
 
 	/**
 	 * @param arg0
 	 */
 	public HashMapArrayList(Map arg0)
 	{
 		super(arg0);
 		arrayList	= new ArrayList<V>();
 	}
 
 	/**
 	 * @param arg0
 	 * @param arg1
 	 */
 	public HashMapArrayList(int arg0, float arg1)
 	{
 		super(arg0, arg1);
 		arrayList	= new ArrayList<V>(arg0);
 	}
 
 	@Override public V put(K key, V value)
 	{
 		V result	= super.put(key, value);
 		if (result != null)	// the object that was overridden
 			arrayList.remove(result);
 		arrayList.add(value);
 		
 		return result;
 	}
 	
	@Override public void putAll(Map<? extends K, ? extends V> map)
	{
		for (K key : map.keySet())
			this.put(key, map.get(key));
	}
	
 	public V get(int index)
 	{
 		return arrayList.get(index);
 	}
 	
 	public Iterator<V> iterator()
 	{
 		return arrayList.iterator();
 	}
 	
 	@Override public V remove(Object key)
 	{
 		V result	= super.remove(key);
 		if (result != null)
 			arrayList.remove(result);
 		return result;
 	}
 	
 	@Override public void clear()
 	{
 		super.clear();
 		arrayList.clear();
 	}
 	
 	public void recycle()
 	{
 		super.clear();
 		int last	= arrayList.size() - 1;
 		for (int i=last; i>=0; i--)
 		{
 			V that	= arrayList.remove(i);
 			// that.recycle() -- must enforce that implements Recyclable
 		}
 	}
 	
 	@Override public Collection<V> values()
 	{
 		return arrayList;
 	}
 }

 /**
  * Created by IntelliJ IDEA.
  * User: Guillaume
 * Date: 25 feb. 2003
  * Time: 13:42:02
  */
 package zz.utils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * A map that doesn't make a distinction between key and value.
  */
 public class BidirectionalMap implements Map
 {
 	private Map itsMap1 = new HashMap ();
 	private Map itsMap2 = new HashMap ();
 
 	public void clear ()
 	{
 		itsMap1.clear();
 		itsMap2.clear();
 	}
 
 	public boolean contains (Object aObject)
 	{
 		return itsMap1.containsKey(aObject) || itsMap2.containsKey(aObject);
 	}
 
 	public boolean containsKey (Object key)
 	{
 		return contains(key);
 	}
 
 	public boolean containsValue (Object value)
 	{
 		return contains(value);
 	}
 
 	public Set entrySet ()
 	{
 		Set theResult = new HashSet ();
 		theResult.addAll(itsMap1.entrySet());
 		theResult.addAll(itsMap2.entrySet());
 		return theResult;
 	}
 
 	public Object get (Object key)
 	{
 		Object theResult = null;
 		theResult = itsMap1.get (key);
 		if (theResult != null) return theResult;
 
 		theResult = itsMap2.get (key);
 		return theResult;
 	}
 
 	public boolean isEmpty ()
 	{
 		return itsMap1.isEmpty() && itsMap2.isEmpty();
 	}
 
 	public Set keySet ()
 	{
 		Set theResult = new HashSet ();
 		theResult.addAll(itsMap1.keySet());
 		theResult.addAll(itsMap2.keySet());
 		return theResult;
 	}
 
 	/**
 	 * Associates the two arguments.
 	 * @return For now, null
 	 */
 	public Object put (Object aObject1, Object aObject2)
 	{
 		itsMap1.put (aObject1, aObject2);
 		itsMap2.put (aObject2, aObject1);
 		return null;
 	}
 
 	public void putAll (Map t)
 	{
 		for (Iterator theIterator = t.entrySet ().iterator (); theIterator.hasNext ();)
 		{
 			Map.Entry theEntry = (Map.Entry) theIterator.next ();
 			put (theEntry.getKey(), theEntry.getValue());
 		}
 	}
 
 	/**
 	 * @return null
 	 */
 	public Object remove (Object key)
 	{
 		itsMap1.remove (key);
 		itsMap2.remove (key);
 		return null;
 	}
 
 	/**
 	 * Returns the "average" size of the map.
 	 */
 	public int size ()
 	{
 		return (itsMap1.size () + itsMap2.size()) / 2;
 	}
 
 	public Collection values ()
 	{
 		Collection theResult = new ArrayList ();
 		theResult.addAll(itsMap1.values());
 		theResult.addAll(itsMap2.values());
 		return theResult;
 	}
 }

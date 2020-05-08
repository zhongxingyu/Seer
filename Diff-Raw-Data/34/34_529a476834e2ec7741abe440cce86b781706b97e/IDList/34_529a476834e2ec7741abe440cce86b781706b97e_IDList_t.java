 package model;
 
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class IDList<T> {
 
	private HashMap<Integer,T> idkeymap;
	private HashMap<T,Integer> valuekeymap;
 	
 	private ArrayList<Integer> clearedIDs;
 	private int minUnassignedID;
 	
 	
 	public IDList()
 	{
 		idkeymap = new HashMap<Integer,T>();
 		valuekeymap = new HashMap<T,Integer>();
		clearedIDs = new ArrayList<Integer>();
 		minUnassignedID = 0;
 	}
 	
 	
	public Collection<T> values()
 	{
		return idkeymap.values();
 	}
 	
 	
 	public int getsetID(T value)
 	{
 		if(value == null)
 		{
 			throw new NullPointerException("value cannot be null.");
 		}
 		if(!idkeymap.containsValue(value))
 		{
 			int id = minUnassignedID;
 			if(clearedIDs.size() > 0)
 			{
 				id = clearedIDs.remove(0);
 			}
 			idkeymap.put(id, value);
 			valuekeymap.put(value, id);
 			if(id == minUnassignedID)
 			{
 				minUnassignedID++;
 			}
 			return id;
 		}
 		else 
 		{
 			return valuekeymap.get(value);
 		}
 	}
 	
 	
 	public int getID(T value)
 	{
 		Integer ID = valuekeymap.get(value);
 		if(ID != null)
 		{
 			return ID;
 		}
 		else 
 		{
 			return -1;
 		}
 	}
 	
 	
 	public T get(int ID)
 	{
 		return idkeymap.get(ID);
 	}
 	
 	
 	public T remove(int ID)
 	{
 		T value = idkeymap.remove(ID);
 		if(value != null)
 		{
 			valuekeymap.remove(value);
 			
 			if(ID >= (minUnassignedID - 1))
 			{
 				minUnassignedID = ID;
 			}
 			else
 			{
 				clearedIDs.add(ID);
 			}
 		}
 		
 		return value;
 	}
 	
 	
 	public int remove(T value)
 	{
 		Integer ID = valuekeymap.remove(value);
 		if(ID!=null)
 		{
 			idkeymap.remove(ID);
 			
 			if(ID >= (minUnassignedID - 1))
 			{
 				minUnassignedID = ID;
 			}
 			else
 			{
 				clearedIDs.add(ID);
 			}
 			
 			return ID;
 		}
 		else
 		{
 			return -1;
 		}
 	}
 	
 	
	public void clear()
	{
		idkeymap.clear();
		valuekeymap.clear();
		clearedIDs.clear();
		minUnassignedID = 0;
	}
	
	
 	public void merge(IDList<T> list)
 	{
 		addValues(list.idkeymap.values());
 	}
 	
 	
 	public int convertID(IDList<T> list, int ID)
 	{
 		T value = list.get(ID);
 		if(value == null)
 		{
 			throw new IllegalArgumentException("ID was invalid for the passed in list.");
 		}
 		return getsetID(value);
 	}
 	
 	
 	
 	public void addValues(Iterable<T> values)
 	{
 		Iterator<T> valuesIter = values.iterator();
 		while(valuesIter.hasNext())
 		{
 			T value = valuesIter.next();
 			this.getsetID(value);
 		}
 	}
 	
 	
 	public void addValues(T[] values)
 	{
 		for(int i = 0; i < values.length; i++)
 		{
 			this.getsetID(values[i]);
 		}
 	}
 	
 	
	@SuppressWarnings("unchecked")
 	public IDList<T> clone()
 	{
 		IDList<T> clone = new IDList<>();
		clone.idkeymap = (HashMap<Integer, T>) this.idkeymap.clone();
		clone.valuekeymap = (HashMap<T, Integer>) this.valuekeymap.clone();
 		clone.clearedIDs = this.clearedIDs;
 		clone.minUnassignedID = this.minUnassignedID;
 		return clone;
 	}
 }

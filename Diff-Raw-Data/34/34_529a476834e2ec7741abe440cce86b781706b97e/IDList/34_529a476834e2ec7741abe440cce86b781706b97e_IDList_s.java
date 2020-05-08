 package model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
import java.util.Map;
 
 public class IDList<T> {
 
	private Map<Integer,T> idkeymap;
	private Map<T,Integer> valuekeymap;
 	
 	private ArrayList<Integer> clearedIDs;
 	private int minUnassignedID;
 	
 	
 	public IDList()
 	{
 		idkeymap = new HashMap<Integer,T>();
 		valuekeymap = new HashMap<T,Integer>();
 		minUnassignedID = 0;
 	}
 	
 	
	public ArrayList<T> values()
 	{
		ArrayList<T> values = (ArrayList<T>) idkeymap.values();
		return values;
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
 	
 	
 	public IDList<T> clone()
 	{
 		IDList<T> clone = new IDList<>();
		clone.idkeymap = this.idkeymap;
		clone.valuekeymap = this.valuekeymap;
 		clone.clearedIDs = this.clearedIDs;
 		clone.minUnassignedID = this.minUnassignedID;
 		return clone;
 	}
 }

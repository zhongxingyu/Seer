 package org.alt60m.servlet;
 
 import java.util.*;
 import javax.servlet.http.*;
 
 public class ActionResults {
 	String _action;
 
 	Hashtable _collections;
 	Hashtable _maps;
 	Hashtable _values;
 	Hashtable _objects;
 	String    _view = "";  //kb 11/18/2002
 
 	public ActionResults() 
 	{
 		this("");
 	}
 	public ActionResults(String action) 
 	{
 		_values = new Hashtable();
 		_objects = new Hashtable();
 		_collections = new Hashtable();
 		_maps = new Hashtable();
 		
 		_action = action;
 	}
 
     //sets and returns the view 
     // kb 11/18/02
     public String getView() { return _view; }
     public void setView(String value) { _view = value; }
     
 
 	public static ActionResults getActionResults(HttpSession session)
 	{
 		Object actionResults = session.getAttribute("tub");
		if (actionResults == null)
			throw new RuntimeException("Session doesn't contain an instance of ActionResults");
 		if (actionResults instanceof ActionResults)
			return(ActionResults) actionResults;
 		else
			throw new ClassCastException("Session object labelled 'tub' is not an instance of ActionResults!");	 //like this will ever happen...
 	}
 
 	public boolean isEmpty() {
 
 		return ((_values.size() == 0) && (_objects.size() == 0) && (_collections.size() == 0) && (_maps.size() == 0));
 	}
 
 	public Collection getCollection(String key)
 	{
 		return (Collection) _collections.get(key);
 	}
 
 	public void addCollection(String key, Collection c)
 	{
 		_collections.put(key, c);
 	}
 	
 	public Map getMap(String key)
 	{
 		return (Map) _maps.get(key);
 	}
 
 	public void addMap(String key, Map map)
 	{
 		_maps.put(key, map);
 	}
 
 	// Deprecated
 	public Hashtable getHashtable(String key)
 	{
 		return (Hashtable) _maps.get(key);
 	}
 
 	// Deprecated
 	public void addHashtable(String key, Hashtable hash)
 	{
 		_maps.put(key, hash);
 	}
 
 	public String getValue(String key)
 	{
 		return (String) _values.get(key);
 	}
 	public void putValue(String key, String value)
 	{
 		_values.put(key, value);
 	}
 
 	public Object getObject(String key)
 	{
 		return  _objects.get(key);
 	}
 	public void putObject(String key, Object object)
 	{
 		_objects.put(key, object);
 	}
 
 	public String toString() {
 		String sResponse;
 
 		sResponse  = "****************************************************************\n";
 		sResponse += "Action: " + _action + "\n";
 		sResponse += "  Returned the following Key/Value pairs:\n";
 		sResponse += "  (you can get these by using the getValue(key) method)\n";
 		
 		for(Enumeration e = _values.keys();e.hasMoreElements();) {
 			String key = (String) e.nextElement();
 			sResponse += "    Key=" + key + ", Value=" + _values.get(key) + "\n";
 		}
 
 		sResponse += "  Returned the following Key/Object pairs:\n";
 		sResponse += "  (you can get these by using the getObject(key) method)\n";
 		
 		for(Enumeration e = _objects.keys();e.hasMoreElements();) {
 			String key = (String) e.nextElement();
 			sResponse += "    Key=" + key + ", Class=" + ((_objects.get(key)).getClass()).getName() + "\n";
 		}
 
 		sResponse += "\n  Returned the following maps (formally known as hashtables):\n";
 		sResponse += "  (you can get these by using the getMap(key)/getHashtable(key) method)\n";
 		 
 		for(Enumeration e = _maps.keys();e.hasMoreElements();) {
 			String key = (String) e.nextElement();
 			sResponse += "    Key=" + key + ", Map=" + _maps.get(key) + "\n";
 		}
 
 
 		sResponse += "\n  Returned the following collections:\n";
 		sResponse += "  (you can get these by using the getCollection(key) method)\n";
 		
 		for(Enumeration e = _collections.keys();e.hasMoreElements();) {
 			String key = (String) e.nextElement();
 			sResponse += "    Key=" + key + "\n";
 			
 			for(Iterator i = ((Collection) _collections.get(key)).iterator(); i.hasNext();) {
 				sResponse += "        " + i.next().toString() + "\n";
 			}
 
 		
 		}
 		sResponse  += "****************************************************************\n";
 		
 		return sResponse;
 		
 	}
 	public String toHTML() {
 		String sResponse;
 
 		sResponse = "<PRE>" + toString() + "</PRE>";
 
 		return sResponse;
 	}
 	
 }
 

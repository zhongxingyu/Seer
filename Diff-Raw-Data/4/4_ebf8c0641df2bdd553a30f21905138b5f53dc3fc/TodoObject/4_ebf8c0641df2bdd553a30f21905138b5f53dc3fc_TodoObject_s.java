 package com.davecoss.android.todo;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.davecoss.android.lib.utils;
 
 public class TodoObject {
 	public enum States {UNFINISHED,FINISHED};
 	
 	private String message, category;
 	private int dbid, create_time, due_date;
 	private double orderidx;
 	private States state;
 	
 	public TodoObject(String msg)
 	{
 		message = msg;
 		dbid = -1;
 		
 		if(message.contains(":"))
 		{
 			int loc = message.indexOf(":");
 			category = message.substring(0,loc);
 			message = message.substring(loc+1);
 		}
 		
 		create_time = utils.unixtime();
 		due_date = create_time + 60;
 		orderidx = 0;
 		state = States.UNFINISHED;
 	}
 	
 	public TodoObject(JSONObject json) throws JSONException
 	{
 		message = "JSON Error";
 		create_time = utils.unixtime();
 		due_date = create_time + 60;
 		orderidx = 0;
 		state = States.UNFINISHED;
 		
 		message = json.getString("message");
 		if(json.has("dbid"))
 			dbid = json.getInt("dbid");
 		if(json.has("create_time"))
 			create_time = json.getInt("create_time");
 		if(json.has("due_date"))
 			due_date = json.getInt("due_date");
 		if(json.has("orderidx"))
 			orderidx = json.getDouble("orderidx");
 		if(json.has("state"))
 			state = States.valueOf(json.getString("state"));
 		
 	}
 	
 	public void set_dbid(int id)
 	{
 		dbid = id;
 	}
 	
 	public int get_dbid()
 	{
 		return dbid;
 	}
 	
 	public void set_create_time(int t)
 	{
 		create_time = t;
 	}
 	
 	public int get_create_time()
 	{
 		return create_time;
 	}
 	
 	public void set_due_date(int t)
 	{
 		due_date = t;
 	}
 	
 	public int get_due_date()
 	{
 		return due_date;
 	}
 	
 	
 	public void set_orderidx(double val)
 	{
 		orderidx = val;
 	}
 	
 	public double get_orderidx(double val)
 	{
 		return orderidx;
 	}
 	
 	public double get_orderidx()
 	{
 		return orderidx;
 	}
 	
 	public States get_state()
 	{
 		return state;
 	}
 	
 	public void set_state(States s)
 	{
 		state = s;
 	}
 	
 	public void set_category(String cat)
 	{
 		category = cat;
 	}
 	
 	public String get_category()
 	{
 		if(category == null)
 			return "";
 		return category;
 	}
 	
 	public String get_message()
 	{
 		return message;
 	}
 	
 	public void set_message(String msg)
 	{
 		message = msg;
 	}
 	
 	public String toString()
 	{
 		if(category != null && category.length() != 0)
 			return category + ":" + message;
 		return message;
 	}
 	
 	public JSONObject toJSON() throws JSONException
 	{
 		JSONObject retval = new JSONObject();
 		retval.put("message", message);
 		retval.put("dbid", dbid);
 		retval.put("create_time", create_time);
 		retval.put("due_date", due_date);
 		retval.put("orderidx", orderidx);
 		retval.put("state", state.toString());
 		retval.put("category", category);
 		return retval;
 	}
 }

 import java.util.Calendar;
 import java.util.ArrayList;
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.sql.SQLException;
 
 //call this thingy with DatabaseRW.[functionwhatever] from anywhar
 
 public class DatabaseRW
 {
 	static Derby database = null;
 
 	public static void setDatabase(Derby db)
 	{
 		database = db;
 	}
 	
 	//use insertRepeatingEvent instead
 	public static int insertEvent(String n, String de, int y, int mo, int da, int h, int mi, int r, int rk)
 	{
 		Timestamp startTime = intToTimestamp(y, mo, da, h, mi);
 		
 		int key = database.insertEvent(n, de, startTime, r, rk);
 		return key;
 	}
 
 	public static int insertTask(String n, String de, int y, int mo, int da, int h, int mi, int s, int p)
 	{
 		Timestamp startTime = intToTimestamp(y, mo, da, h, mi);
 		
 		int key = database.insertTask(n, de, startTime, s, p);
 		return key;
 	}
 	
 	//use updateRepeatingEvent instead
 	public static int updateEvent(int i, String n, String d, int y, int m, int da, int h, int mi, int r, int rk)
 	{
 		Timestamp startTime = intToTimestamp(y, m, da, h, mi);
 		
 		int returny = database.updateEvent(i, n, d, startTime, r, rk);
 		
 		return returny;
 	}
 	
 	public static int updateTask(int i, String n, String d, int y, int m, int da, int h, int mi, int s, int p)
 	{
 		Timestamp startTime = intToTimestamp(y, m, da, h, mi);
 		
 		int returny = database.updateTask(i, n, d, startTime, s, p);
 		
 		return returny;
 	}
 	
 	//use deleteRepeatingEvent instead
 	public static int deleteEvent(int id)
 	{
 		int returny = database.deleteEvent(id);
 		
 		return returny;
 	}
 	
 	public static int deleteTask(int id)
 	{
 		int returny = database.deleteTask(id);
 		
 		return returny;
 	}
 	
 	public static ArrayList<Event> queryEvents(Timestamp start, Timestamp end)
 	{
 		ResultSet results = database.queryEvents(start, end);
 		ArrayList<Event> output = toEventArray(results);
 		//results.close();
 		
 		return output;
 	}
 	
 	public static ArrayList<Task> queryTasks(Timestamp start, Timestamp end)
 	{
 		ResultSet results = database.queryTasks(start, end);
 		ArrayList<Task> output = toTaskArray(results);
 		//results.close();
 		
 		return output;
 	}
 	
 	public static ArrayList<Event> queryDayEvents(int y, int m, int d)
 	{
 		Timestamp start = intToTimestamp(y, m, d, 0, 0);
 		Timestamp end = intToTimestamp(y, m, d + 1, 0, 0);
 		
 		return queryEvents(start, end);
 	}
 	
 	public static ArrayList<Task> queryDayTasks(int y, int m, int d)
 	{
 		Timestamp start = intToTimestamp(y, m, d, 0, 0);
 		Timestamp end = intToTimestamp(y, m, d + 1, 0, 0);
 		
 		return queryTasks(start, end);
 	}
 	
 	public static ArrayList<Event> queryMonthEvents(int y, int m, int d)
 	{
 		Timestamp start = intToTimestamp(y, m, 0, 0, 0);
 		Timestamp end = intToTimestamp(y, m + 1, 0, 0, 0);
 		
 		return queryEvents(start, end);
 	}
 	
 	public static ArrayList<Task> queryMonthTasks(int y, int m, int d)
 	{
 		Timestamp start = intToTimestamp(y, m, 0, 0, 0);
 		Timestamp end = intToTimestamp(y, m + 1, 0, 0, 0);
 		
 		return queryTasks(start, end);
 	}
 	
 	public static void insertRepeatingEvent(String n, String desc, int y, int mo, int d, int hr, int min, int r)
 	{
 		Timestamp start = intToTimestamp(y, mo, d, hr, min);
		Timestamp iterateTime = intToTimestamp(y, mo, d, hr, min);
 		
 		int key = database.insertRepeatingEvent(n, desc, start, r);
 		
 		//Calendar temp = Calendar.getInstance();
 		//temp.add(Calendar.YEAR, 100);
 		
 		//Calendar temp2 = Calendar.getInstance();
 		//temp2.clear();
 		//temp2.set(Calendar.YEAR, temp.get(Calendar.YEAR));
 		
 		//Timestamp limit = new Timestamp(temp2.getTimeInMillis());
 		
 		for(int i = 0; i < 100; i++)
 		{
 			switch(r)
 			{
 				case 0:
 					return;
 				case 1:
					d++;
 					break;
 				case 2:
					d += 7;
 					break;
 				case 3:
					mo++;
 					break;
 				case 4:
					y++;
 					break;
 			}
 			
			iterateTime = intToTimestamp(y, mo, d, hr, min);
			database.insertEvent(n, desc, iterateTime, r, key);
 		}
 	}
 	
 	public static void deleteRepeatingEvent(int i)
 	{
 		database.deleteRepeatingEvent(i);
 	}
 	
 	public static void updateRepeatingEvent(int id, String n, String dsc, int y, int mo, int d, int hr, int min, int r)
 	{
 		database.deleteRepeatingEvent(id);
 		insertRepeatingEvent(n, dsc, y, mo, d, hr, min, r);
 	}
 	
 	public static ArrayList<Event> queryPinboardEvents(Timestamp start)
 	{
 		return(toEventArray(database.queryPinboardEvents(start)));
 	}
 	
 	public static ArrayList<Task> queryPinboardTasks(Timestamp start, int status)
 	{
 		return(toTaskArray(database.queryPinboardTasks(start, status)));
 	}
 	
 	/*==================================*/
 	/* Beginning of getValue() methods */
 	/*================================*/
 	//KILLED
 	
 	/*===========================*/
 	/* Conveniece methods! Yay! */
 	/*=========================*/
 	
 	public static Timestamp intToTimestamp(int year, int month, int day, int hour, int minute)
 	{
 		Calendar tempCal = Calendar.getInstance();
 		tempCal.clear();
 		tempCal.set(year, month, day, hour, minute);
 		
 		return new Timestamp(tempCal.getTimeInMillis());
 	}
 	
 	public static ArrayList<Event> toEventArray(ResultSet rs)
 	{
 		ArrayList<Event> returny = new ArrayList<Event>();
 		
 		try{
 			while(rs.next())
 			{
 				int id = rs.getInt(1);
 				String n = rs.getString(2);
 				String d = rs.getString(3);
 				Timestamp start = rs.getTimestamp(4);
 				int r = rs.getInt(5);
 				int rk = rs.getInt(6);
 				
 				returny.add(new Event(id, n, d, start, r, rk));
 			}
 			
 			return returny;
 		} catch (Exception balls){}
 		
 		return null;
 	}
 	
 	public static ArrayList<Task> toTaskArray(ResultSet rs)
 	{
 		ArrayList<Task> returny = new ArrayList<Task>();
 		
 		try{
 			while(rs.next())
 			{
 				int id = rs.getInt(1);
 				String n = rs.getString(2);
 				String d = rs.getString(3);
 				Timestamp start = rs.getTimestamp(4);
 				int s = rs.getInt(5);
 				int p = rs.getInt(6);
 				
 				returny.add(new Task(id, n, d, start, s, p));
 			}
 			
 			return returny;
 		} catch (Exception balls){}
 		
 		return null;
 	}
 }

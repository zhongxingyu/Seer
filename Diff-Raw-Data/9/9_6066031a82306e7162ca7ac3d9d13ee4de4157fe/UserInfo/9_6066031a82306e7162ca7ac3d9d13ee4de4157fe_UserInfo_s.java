 package edu.myhorseshow;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 import edu.myhorseshow.division.Division;
 import edu.myhorseshow.event.Event;
 import edu.myhorseshow.showclass.Participation;
 import edu.myhorseshow.showclass.ShowClass;
 import edu.myhorseshow.user.User;
 
 public final class UserInfo
 {
 	private static User currentUser;
 	private static ArrayList<ShowClass> currentUserParticipatingClasses;
 	
 	public static void setCurrentUser(User user)
 	{
 		resetCache();
 		currentUser = user;
 	}
 	
 	public static User getCurrentUser()
 	{
 		return currentUser;
 	}
 	
 	public static boolean isUserLoggedIn()
 	{
 		return currentUser != null;
 	}
 	
 	public static boolean isCurrentUser(User user)
 	{
 		return user.getId() == getCurrentUser().getId();
 	}
 	
 	public static Event getEvent(long eventId) throws NullPointerException
 	{
 		if (!isUserLoggedIn())
 			return null;
 		
 		for (Event e: currentUser.getEvents())
 		{
 			if (e.getId() == eventId)
 				return e;
 		}
 		
 		throw new NullPointerException("Event id not found.");
 	}
 	
 	public static ShowClass getClass(long eventId, long classId)
 	{
 		if (!isUserLoggedIn())
 			return null;
 		
 		if (eventId <= 0 || classId <= 0)
 			return null;
 		
 		Event event = getEvent(eventId);
 		if (event == null)
 			return null;
 		
 		for (Division division: event.getDivisions())
 		{
 			for (ShowClass showClass: division.getClasses())
 			{
 				if (showClass.getId() == classId)
 					return showClass;
 			}
 		}
 		
 		return null;
 	}
 	
 	public static ArrayList<ShowClass> getUserClasses(long eventId)
 	{
 		if (!isUserLoggedIn())
 			return null;
 		
 		if (eventId <= 0)
 			return null;
 		
		// If this problem has already been solved,
 		// there's no need to do the work again.
		if (currentUserParticipatingClasses != null)
 			return currentUserParticipatingClasses;
 		
 		Event event = getEvent(eventId);
 		if (event == null)
 			return null;
 		
 		currentUserParticipatingClasses = new ArrayList<ShowClass>();
 		
 		for (Division division: event.getDivisions())
 		{
 			if (division.getClasses() == null)
 				continue;
 			
 			for (ShowClass showClass: division.getClasses())
 			{
 				if (showClass.getParticipations() == null)
 					continue;
 				
 				for (Participation participation: showClass.getParticipations())
 				{
 					if (isCurrentUser(participation.getRider()))
 					{
 						currentUserParticipatingClasses.add(showClass);
 						break;
 					}
 				}
 			}
 		}
 		
 		return currentUserParticipatingClasses.size() > 0 ? currentUserParticipatingClasses : null;
 	}
 	
 	private static void resetCache()
 	{
 		currentUserParticipatingClasses = null;
 	}
 }

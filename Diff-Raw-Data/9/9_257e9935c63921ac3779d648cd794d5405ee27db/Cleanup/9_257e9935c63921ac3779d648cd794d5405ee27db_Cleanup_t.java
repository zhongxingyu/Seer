 package session;
 
 import java.util.Date;
 import java.util.concurrent.*;
 import java.util.TimerTask;
 import java.util.Enumeration;
 
 //Cleans up the concurrent hash map using a daemon style process.
 public class Cleanup extends TimerTask {
 	public void run() {
 		// getTime Returns the number of milliseconds since January 1, 1970,
 		// 00:00:00 GMT
 		// Divide by 1000 to convert to seconds
 		long now = (new Date().getTime()) / 1000;
 		System.out.println("Daemon Cleanup Thread Has Started");
 		try {
 			ConcurrentHashMap<String, String[]> hMap = Session.sessionTable;
 			// keySet does not work because sets cannot be enumerated
 			for (Enumeration<String> i = hMap.keys(); i.hasMoreElements();) {
 				String key = i.nextElement();
 				String[] s = hMap.get(key);
 				// 600 here is seconds
 				//NEED TO CHECK THE CALC HERE. GETTING A NFE ERROR
 				System.out.println(s[2]);
				if ((now - Long.parseLong(s[3], 10)) > 600) {
 					hMap.remove(key);
 					System.out.println(key
 							+ " has been removed from the hash table.");
 				}
 			}
 			System.out.print("Cleanup Daemon Complete. ");
 		} catch (NumberFormatException nfe) {
 			nfe.printStackTrace();
 			//System.out.println("Greeeee");
 		}
 
 	}
 }

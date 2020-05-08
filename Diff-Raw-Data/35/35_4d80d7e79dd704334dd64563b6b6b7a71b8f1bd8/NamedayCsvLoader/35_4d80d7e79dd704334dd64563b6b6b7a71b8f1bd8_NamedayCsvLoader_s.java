 package cz.adevcamp.droidpartisans;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 /**
  * Provides Name Day calendar populated with contacts 
  */
 
 public class NamedayCsvLoader {
 
 	/**
 	 * Entry in the calendar, it contains date, name (or name of the holiday),
 	 * and list of contacts who's given name matches
 	 */
 	public static class Day {
 		public String date;			// e.g. "21.2."
 		public String dateName;		// e.g. "Lenka"
 		public Vector<Contact> contacts = new Vector<Contact>();
 
 		public Day(String date, String dateName) {
 			this.date = date;
 			this.dateName = dateName;
 		}
 
 		@Override
 		public String toString() {
 			return "\nName: " + dateName + ", date: " + date + ", contacts: " + contacts;
 		}
 	}
 	
 	/**
 	 * @param args
 	 */
//	public static void main(String[] args) throws Exception {
//		
//		Vector<Contact> contacts = new Vector<Contact>();
//
//		contacts.add(new Contact(1, "Karina", "Janečková", "555263882"));
//		contacts.add(new Contact(1, "Jitka", "Telecí", "555263882"));
//		contacts.add(new Contact(1, "Dalimil", "Míšová", "555263882"));
//		contacts.add(new Contact(1, "Adam", "Jandová", "555263882"));
//		contacts.add(new Contact(1, "Eva", "Novotná", "555263882"));
//		
//		Vector<Day> days = getCalendar(contacts	);
//		System.out.println(days);
//		
//		
//	}
 
 	/**
 	 * Create Name Day calendar, populate calendar with contacts
 	 * 
 	 * @param contacts - known contacts - given name from contact data is used for processing
 	 * @param is - input stream of CSV formated Name Day calendar
 	 * @return
 	 */
 	public static Vector<Day> getCalendar(Vector<Contact> contacts, InputStream is) {
 		Vector<Day> calendar = new Vector<Day>();					// chronological name day calendar of the whole year
 		Map<String, Day> name2date = new TreeMap<String, Day>(); 	// index mapping names to entries in the calendar
 
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			String line = br.readLine();
 
 			while (line != null) {
 				String[] parsedRow = line.split(",");	// row has format "Lenka,21.2."
 				String name = parsedRow[0];
 				String date = parsedRow[1];
				String[] names = name.split(" ");		// name can have format "Adam a Eva"
 
 				Day day = new Day(date, name);	
 				
 				/* append entry to calendar */
 				calendar.add(day);
 				
 				/* populate index mapping names to entries in calendar */
 				for (String tinyName : names) {
 					name2date.put(tinyName, day);
 				}
 
 				line = br.readLine();
 			}
 
 //			System.out.println(days.toString());
 //			System.out.println(name2date);
 			
 			br.close();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			
 		}
 		
 		/* populate calendar with our contacts */
 		for (Contact contact:contacts) {
 			
 			/* find calendar entry according to given name of our contact */
 			Day day = name2date.get(contact.getName());
 			
 			if (null != day) {
 				day.contacts.add(contact);	// our contact will celebrate on the given day
 			} else {
 				System.err.println(contact); // we cannot determine entry in the calendar
 			}
 		}
 
 //		String name = "Kateřina";
 //		System.out.println(name + " ma svatek v " + name2date.get(name));
 		return calendar;
 	}
 
 }

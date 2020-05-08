 /**
 * A class that handles some of the user interface of ContactManager, and takes/validates user entries.
 */
 
 import java.util.regex.*;
 import java.util.Scanner;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class ContactManagerUtilities {
 	private static IllegalArgumentException illegalArgEx = new IllegalArgumentException();
 
 	public static void displayWelcome() {
 		System.out.println("*************************************************************");
 		System.out.println("*                    CONTACT MANAGER                        *");
 		System.out.println("*************************************************************" + "\n");
 	}
 	
 	
 	public static int chooseMainMenuOption() {
 		System.out.println();
 		System.out.println();
 		System.out.println("                  ***** Main Menu *****");
 		System.out.println("1. Add a future meeting            " + "\t" + " 2. Look up meeting");
 		System.out.println("3. Create record of a past meeting " + "\t" + " 4. Add notes to a meeting that has taken place");
 		System.out.println("5. Add a new contact               " + "\t" + " 6. Look up contact");
 		System.out.println("7. Save data to disk               " + "\t" + " 8. Save and exit.");
 		System.out.println("(Exit back to this menu at any point by entering \"back\")");
 		System.out.print("Select option: ");
 		int selection = validateOption(1, 8);		
 		return selection;	
 	}
 	
 	public static int validateOption(int min, int max) {//to validate user choices from numerical menus
 		int selection;
 		try {
 			selection = Integer.parseInt(System.console().readLine());
 			if (selection < min || selection > max) {
 		 		throw illegalArgEx;
 		 	}
 		 	
 		 }	
 		 catch (NumberFormatException ex) {
 		 	System.out.println("Please enter a number that corresponds to an option on the menu.");
 		 	return validateOption(min, max);
 		 }	
 		 catch (IllegalArgumentException ex) {
 		 	System.out.println("Invalid option. Please make a selection from the menu.");
 		 	return validateOption(min, max);		 	
 		 }
 		 return selection;
 	}	
 	
 	
 	/**
 	* A method that allows the user to select contacts by ID number. These numbers
 	* are then added to an array, which is returned to the ContactManager class
 	* for validation with the getContacts(int... ids) method. 
 	* Prints contactList Set for ease of reference.
 	*/
 	public static int[] selectAttendees(Set<Contact> contactList) {
 		
 		List<Integer> attendeeList = new ArrayList<Integer>();
 		int[] attendees;
 		System.out.println("Your contact list: ");
 		if (contactList.isEmpty()) {
 			System.out.println("<Empty> You will need to add contacts to your contact list before creating meetings.");
 			return null;//This sends the user back to the main menu via a break statement in ContactManager
 		}
 		for (Contact c : contactList) {//Prints contactList for ease of reference
 			System.out.println(c.getName() + " " + c.getId());
 		}
		System.out.println("Please enter the IDs of the contacts who are attending, separated" +
 		" by a comma e.g. 1, 4, 5. Finish by pressing RETURN.");
 		
 		try{
 			String userEntry = System.console().readLine();
 			if (userEntry.equals("back")) {
 				return null;//allows user to cancel and return to main menu - ensure main menu handles null appropriately
 			}
 			boolean verified = validateCommaString(userEntry);//determines whether input is valid
 			if (!verified) {
 				throw illegalArgEx;
 			}
 			Scanner sc = new Scanner(userEntry);//if input is valid, creates an arrayList with the user input numbers
 			Pattern delimiterPattern = Pattern.compile("[\\s]?[,][\\s]?");
 			sc.useDelimiter(delimiterPattern);
 			do {
 				int selection = sc.nextInt();
 				attendeeList.add(selection);
 			} while (sc.hasNextInt());
 			attendees = new int[attendeeList.size()]; //converts to array for use in ContactManager getContacts() method
 			//so that it can be ascertained whether the user has selected contacts who exist
 			for (int i = 0; i < attendeeList.size(); i++) {
 				attendees[i] = attendeeList.get(i);
 			}
 		}
 		catch (IllegalArgumentException ex) {
 			System.out.println("Error! Please enter the contact ID numbers separated by a comma " +
 			"i.e. 1, 2, 3 or 1,2,3 and finish by pressing RETURN.");
 			return selectAttendees(contactList);
 		}
 		return attendees;	
 	}
 	
 	
 	/*
 	* A method to validate a user-entered String of
 	* digits separated by commas and spaces.
 	* Is reasonably lenient to ensure user friendliness:
 	* 1,2,3 , 4, 5, 6 is considered valid.
 	*/
 	public static boolean validateCommaString(String userEntry) {
 		Pattern pattern = Pattern.compile("([0-9][0-9]*[\\s]?[,][\\s]?)*[0-9][0-9]*");
 		Matcher m = pattern.matcher(userEntry);//match given input against pattern
 		boolean verified = m.matches();//upshot - matches or doesn't
 		if (verified) {
 			return true;
 		}
 		return false;
 	}
 	
 	
 	/**
 	* Creates a Calendar date from user input. Rules out invalid dates such as
 	* 29.02 outside of a leap year, and ensures that months with only 30 days cannot
 	* be assigned a date of 31.
 	*/
 	public static Calendar createDate() {
 		Calendar date = null;		
 		int day;
 		int month;
 		int year;
 		int[] dateArray = new int[3];
 		boolean febOverflow = false;//booleans to facilitate error messages below
 		boolean monthOverflow = false;
 		boolean badFormat = false;
 		try {
 			System.out.println("Please enter the date of the meeting in dd.mm.yyyy format: ");
 			String userEntry = System.console().readLine();
 			if (userEntry.equals("back")) {
 				return null;//allows user to cancel and return to main menu 
 			}
 			boolean verified = validateDateEntry(userEntry);//determines whether input is valid
 			if (!verified) {
 				badFormat = true;
 				throw illegalArgEx;
 			}
 			Scanner sc = new Scanner(userEntry);//if input is valid, creates a date
 			Pattern delimiterPattern = Pattern.compile("[\\.]");
 			sc.useDelimiter(delimiterPattern);
 			for (int i = 0; i < 3; i++) {
 				dateArray[i] = sc.nextInt();
 			}
 			
 			day = dateArray[0];
 			month = dateArray[1] - 1;//Calendar interprets January as 0, February as 1 etc
 			year = dateArray[2];
			if (month == 1 && day < 28) {
 				if ((year % 4 == 0) && (year % 100 != 0)) {
 					//this is ok - it's a leap year
 				}
 				if ((year % 4 == 0) && (year % 100 == 0) && (year % 400 == 0)) {
 					//this is ok - it's a leap year
 				}
 				else {
 					febOverflow = true;
 					throw illegalArgEx;//Feb only has 29 days in leap year
 				}
 			}
 			if (month % 2 == 1) {//Disallows day having value of 31 when month has only 30 days
 				if (day > 30) {
 					monthOverflow = true;
 					throw illegalArgEx;
 				}
 			}	
 			date = new GregorianCalendar(year, month, day);					
 		}
 		catch (IllegalArgumentException ex) {
 			if (badFormat) {
 				System.out.println("Error! Please enter the date in dd.mm.yyyy format " +
 				"i.e. 01.11.2013 and finish by pressing RETURN.");
 				return createDate();
 			}
 			if (febOverflow) {
 				System.out.println("Invalid date! February has 28 days outside of a leap year.");
 				return createDate();
 			}
 			if (monthOverflow) {
 				System.out.println("Error! The month you specified has 30 days.");
 				return createDate();
 			} 
 		}
 		return date;	
 	}
 	
 	
 	public static boolean validateDateEntry(String userEntry) {
 		//checks date format, allowing d.m.yyyy or dd.mm.yyyy. Rules out invalid days and months i.e. 59.40.2001
 		Pattern pattern = Pattern.compile("(([0]?[1-9])|([1-2][0-9])|([3][0-1]))[\\.](([0]?[1-9])|([1][0-2]))[\\.][2][0][0-9][0-9]");
 		Matcher m = pattern.matcher(userEntry);//match given input against pattern
 		boolean verified = m.matches();
 		if (verified) {
 			return true;
 		}
 		return false;
 	}
 	
 	public static int lookUpMeetingOptions() {
 		System.out.println();
 		System.out.println();
 		System.out.println("1. Search by date                    " + "\t" + "2. Search by meeting ID");
 		System.out.println("3. Search future meetings by contact " + "\t" + "4. Search past meetings by contact");
 		System.out.println("5. Search past meetings by ID        " + "\t" + "6. Search future meetings by ID");
 		System.out.println("7. Return to main menu");
 		System.out.print("Select option: ");		
 		int selection = validateOption(1, 7);		
 		return selection;	
 	}
 	
 	public static int searchByContactOptions() {
 		System.out.println("1. Search by contact ID");
 		System.out.println("2. Search by contact name");
 		System.out.println("3. Return to main menu");
 		System.out.println("Select option: ");
 		int selection = validateOption(1, 3);
 		return selection;
 	}
 	
 	/**
 	Meeting methods:
 	PastMeeting getPastMeeting(int id);
 
 	FutureMeeting getFutureMeeting(int id);
 
 	Meeting getMeeting(int id);
 
 	List<Meeting> getFutureMeetingList(Contact contact);
 
 	List<Meeting> getMeetingList(Calendar date);
 
 	List<PastMeeting> getPastMeetingList(Contact contact);
 	*/
 	
 	/**
 	* A method that prints the details of the meeting
 	* taken as a parameter. If the meeting is a past
 	* meeting, it also prints the meeting notes.
 	*/
 	public static void printMeetingDetails(Meeting meeting) {
 		System.out.println("Meeting details: ");
 		System.out.println("ID:   " + meeting.getId());
 		Calendar date = meeting.getDate();
 		System.out.println("Date: " + date.get(Calendar.DAY_OF_MONTH) + "." + 
 			date.get(Calendar.MONTH) + 1 + "." + date.get(Calendar.YEAR)); //+1 to print month in meaningful sense to user
 		System.out.println("Attendees: ");
 		for (Contact c : meeting.getContacts()) {
 			System.out.println(c.getId() + "\t" + c.getName());
 		}
 		if (meeting instanceof PastMeeting) {
 			PastMeeting pMeeting = (PastMeeting) meeting;
 			System.out.println("Notes: ");
 			System.out.println(pMeeting.getNotes());
 		}
 	}
 	
 	
 	/**
 	* A method to validate that user input can be
 	* interpreted as an int.
 	*/
 	public static int validateNumber(String entry) {
 		int num;
 		try {
 			num = Integer.parseInt(entry);
 		}
 		catch (NumberFormatException ex) {
 			System.out.println("Error: Please enter a number!");
 			entry = System.console().readLine();
 			return validateNumber(entry);
 		}
 		return num;
 	}
 	
 	/*
 	* Method to print lists containing Meetings or subtypes of Meeting.
 	*/
 	public static void printMeetingList(List<? extends Meeting> list) {
 		//date and id
		System.out.println("Meetings: ");
 		for (Meeting m : list) {
 			Calendar date = m.getDate();
 			System.out.println("ID: " + m.getId() + "\t" + date.get(Calendar.DAY_OF_MONTH) +
 				"." + date.get(Calendar.MONTH) + "." + date.get(Calendar.YEAR));
 		}
 	}
 	
 	
 	/*
 	* Look up contact submenu
 	*/
 	public static int lookUpContactOptions() {
 		System.out.println();
 		System.out.println();
 		System.out.println("1. Search by name      " + "\t" + "2. Search by ID");
 		System.out.println("3. Return to main menu ");
 		System.out.print("Select option: ");		
 		int selection = validateOption(1, 3);		
 		return selection;	
 	}
 	
 	
 		
 		
 		
 		
 		
 		
 		
 	
 	
 	
 		
 		
 	
 	
 		
 	
 		
 		
 		
 		
 }
 

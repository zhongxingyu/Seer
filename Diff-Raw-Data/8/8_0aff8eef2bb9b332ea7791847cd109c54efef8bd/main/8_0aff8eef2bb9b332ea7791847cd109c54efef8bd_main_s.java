 /*
 Author:       Erik Youngren <eyoungre@callutheran.edu>
 Date Created: 2010-00-00
 
 Purpose
 =======
 Solution to Programming Exercise 8.5 (8e), 7.5 (7e).
 
     Using the `GregorianCalendar` class
     
     Java API has the `GregorianCalendar` class in the java.util package that can
     be used to obtain the year, month, and day of a date. The no-arg constructor
     constructs an instance for the current date, and the methods
     `get(GregorianCalendar.YEAR)`, `get(GregorianCalendar.MONTH)`, and
     `get(GregorianCalendar.DAY_OF_MONTH)` return the year, month, and day.
     
     Write a program to perform two tasks:
     
     * Display the current year, month and day.
     * The `GregorianCalendar` class has the `setTimeInMillis(long)`, which can
     be used to set a specified elapsed time since January 1, 1970. Set the value
     to 1234567898765L and display the year, month, and day.
 
 .. note::
     Did this exact exercise in the last class. Yay!
     
     Hmm. Good thing it wasn't collected, though. I typoed the
     `setTimeInMillis()` part. 123456789L instead of the correct 1234567898765L.
 
 .. note::
     Obligatory related XKCD Comic: http://www.xkcd.com/376/ "Bug"
 
 Document History
 ================
 ============= ==================================================================
 Date Modified Reason
 ============= ==================================================================
 2010-00-00    Document Created
 ============= ==================================================================
 
 */
 
 /**
  * Driver class
  */
 class main {
 	public static void main (String[] args) {
 		GregorianCalendar date = new GregorianCalendar();
 		
 		System.out.println(String.format(
			"Current Date: %s", Calendar.print_date(date)));
 		
 		long millis = 1234567898765L;
 		date.setTimeInMillis(millis);
 		System.out.println(String.format(
 			"Date at %d milli-seconds past Unix Epoch: %s",
 			millis,
			Calendar.print_date(date)));
 	}
 	
 	/**
 	 * Creates a string representing the date in the format specified.
 	 * 
 	 * @param GregorianCalendar date
 	 * 
 	 * @return String representation of the date
 	 */
 	public static String print_date(GregorianCalendar date) {
 		return String.format(
 			"%d-%d-%d %d:%d:%d.%d",
 			date.get(GregorianCalendar.YEAR),
 			date.get(GregorianCalendar.MONTH),
 			date.get(GregorianCalendar.DAY_OF_MONTH),
 			date.get(GregorianCalendar.HOUR),
 			date.get(GregorianCalendar.MINUTE),
 			date.get(GregorianCalendar.SECOND),
 			date.get(GregorianCalendar.MILLISECOND));
 	}
 }
 

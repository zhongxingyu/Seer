 import java.util.LinkedList;
 
 /**
  * Based on John Conway's doomsday rule
  * uses the gregorian calendar system adopted in 1752
  * thus should work for years 1753 - 9999
  * 
  * @author Julian Hisdal Nymark
  * @version 1.0
  */
 class Doomsday {
     /**
      * This method filters out workdays between
      * two dates, but not when asking for a specific date
      */
     public static void main(String[] args){
 	if(args.length == 1){
 	    Day d =CalendarTools.getDay(args);
 	    d.printInfo();
 	}
 	else if (args.length == 2) {
 	    LinkedList<Day> workdays = CalendarTools.listWorkDays(args);
 	    for(Day day : workdays){
 		day.printInfo();
 	    }
 	}
 	else{
 	    help();
 	}
     }
     
     /**
      * prints some info
      */
     static void help(){
 	System.out.println("Usage:\n");
 	System.out.println("List day(s) at: (1 argument)");
	System.out.println("$ java Doomsday <DD/MM/YYYY>");
	System.out.println("Or\n$ java Doomsday <MM/YYYY>");
	System.out.println("Or\n$ java Doomsday <YYYY>\n");
 	System.out.println("List day(s) between: (2 arguments)");
	System.out.println("$ java Doomsday <DD/MM/YYYY> <DD/MM/YYYY>");
 	System.out.println("Or any combination of the single argument types.");
     }
 }
 
 /**
  * Utility class for calculating and dealing with
  * days
  * 
  * @author Julian Hisdal Nymark
  * @version 1.0
  */
 class CalendarTools {
     static String stringDay(int value){
 	switch (value) {
 	case 0:
 	    return "Sunday";
 	case 1:
 	    return "Monday";
 	case 2:
 	    return "Tuesday";
 	case 3:
 	    return "Wednesday";
 	case 4:
 	    return "Thursday";
 	case 5:
 	    return "Friday";
 	case 6:
 	    return "Saturday";
 	default:
 	    return "Invalid (this should never happen)";
 	}
     }
 
     static String[] findStartOfDate(String[] input){
 	String[] startOfDate = new String[3];
 	if(input.length == 1){
 	    startOfDate[2] = input[0];
 	    startOfDate[1] = "1";
 	    startOfDate[0] = "1";
 	}
 	else if(input.length == 2){
 	    startOfDate[2] = input[1];
 	    startOfDate[1] = input[0];
 	    startOfDate[0] = "1";
 	}
 	else{
 	    startOfDate = input;
 	}
 	return startOfDate;
     }
 
     static boolean isLeapYear(int y){
 	boolean leap = false;
 	if(y%4 == 0){
 	    leap = true;
 	    if(y%100 == 0){
 		leap = false;
 		if(y%400 == 0){
 		    leap = true;
 		}
 	    }
 	}
 	return leap;
     }
     
     static LinkedList<Day> listWorkDays(String[] args){
 	LinkedList<Day> workdays = new LinkedList<Day>();
 	String[] from = args[0].split("\\/");
 	String[] to = args[1].split("\\/");
 	    
 	from = CalendarTools.findStartOfDate(from);
 	to = CalendarTools.findStartOfDate(to);
 	    
 	while(!((from[0].equals(to[0])) && (from[1].equals(to[1])) && (from[2].equals(to[2])))){
 	    //print day, if its a work-day
 	    if(CalendarTools.dayOfWeek(from) > 0 && CalendarTools.dayOfWeek(from) < 6) {
 		Day day = new Day(CalendarTools.dayOfWeek(from), Integer.parseInt(from[0]),
 				  Integer.parseInt(from[1]), Integer.parseInt(from[2]));
 		workdays.add(day);
 	    }
 	    
 	    int daysOfMonth[] = {31,28,31,30,31,30,31,31,30,31,30,31};
 	    if(CalendarTools.isLeapYear(Integer.parseInt(from[2]))){
 		daysOfMonth[1] = 29;
 	    }
 	    //if from[0] smaller than monthDays(of from[1])
 	    if(Integer.parseInt(from[0]) < daysOfMonth[Integer.parseInt(from[1])-1]){
 		//++1 to from[0]
 		from[0] = Integer.toString(Integer.parseInt(from[0]) +1);
 	    }
 	    //if from[1] < 12
 	    else if(Integer.parseInt(from[1]) < 12){
 		from[0] = "1";
 		from[1] = Integer.toString(Integer.parseInt(from[1]) +1);
 	    }
 	    else{
 		from[0] = "1";
 		from[1] = "1";
 		from[2] = Integer.toString(Integer.parseInt(from[2]) + 1);
 	    }
 	}
 	return workdays;
     }
     
     static Day getDay(String[] arg){
 	String[] input = arg[0].split("\\/");
 	
 	input = CalendarTools.findStartOfDate(input);
 	Day day = new Day(CalendarTools.dayOfWeek(input), Integer.parseInt(input[0]),
 			  Integer.parseInt(input[1]), Integer.parseInt(input[2]));
 	return day;
     }
     
     static int dayOfWeek(String[] date){
 	String day = date[0];
 	String month = date[1];
 	String year = date[2];
 	int y = Integer.parseInt(year);
 	int m = Integer.parseInt(month) -1; // count from 0
 	int d = Integer.parseInt(day);
 	
 	boolean leap = isLeapYear(y);
 		
 	int yearLastTwoDigits = 0;
 	if(year.length() < 2){
 	    yearLastTwoDigits = Integer.parseInt(year);   
 	}
 	else{
 	    yearLastTwoDigits = Integer.parseInt(year.substring(year.length()-2));
 	}
 
 	int century = Integer.parseInt(year)/100 + 1;
 	
 	int anchorDay = ((5*century + (int)((century-1) / 4))+4)%7;
 	int doomsday = (((yearLastTwoDigits/12) + yearLastTwoDigits%12 +
 			 (yearLastTwoDigits%12 / 4)) + anchorDay)%7;
 	
 	//index == month, value == doomsday
 	int[] monthDoomsday = {3,7,7,4,2,6,4,1,5,3,7,5};
 	
 	int answer = 0;
 	//if leap year & jan or feb
 	if(leap && (m<3)){
 	    int offset = d - (monthDoomsday[m]+1);
 	    answer = (doomsday + offset)%7;
 	    if(answer < 0){
 		answer = 7 + answer;
 	    }
 	}
 	else{
 	    int offset = d - monthDoomsday[m];
 	    answer = (doomsday + offset)%7;
 	    if(answer < 0){
 		answer = 7 + answer;
 	    }
 	}
 	return answer;		
     }
 }
 
 /**
  * Day object, representing a day
  * (accepted values in parentheses)
  * contains info on:
  * 1) the day of the week (0 -- 6)
  * 2) the day of the month (1 -- 28to31)
  * 3) month of the year (1 -- 12)
  * 4) year (1753 -- 9999)
  * 
  * @author Julian Hisdal Nymark
  * @version 1.0
  */
 class Day{
     int dayOfWeek;
     int dayOfMonth;
     int month;
     int year;
     
     Day(int dow, int dom, int month, int year){
 	this.dayOfWeek = dow;
 	this.dayOfMonth = dom; 
 	this.month = month;
 	this.year = year;
     }
     
     public void printInfo(){
 	String info = (CalendarTools.stringDay(dayOfWeek) + " " + dayOfMonth +
 		       "/" + month + "/" + year);
 	System.out.println(info);
     }
 }

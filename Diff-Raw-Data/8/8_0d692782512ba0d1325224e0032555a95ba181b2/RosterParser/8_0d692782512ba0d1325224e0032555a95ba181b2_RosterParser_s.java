 package utilities;
 
 import static java.nio.charset.Charset.defaultCharset;
 import static java.nio.file.Files.newBufferedReader;
 import static java.nio.file.Paths.get;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 
 /**
  * The <code>RosterParser</code> class will be used to read through the .lst 
  * file for each <code>Employee</code>, in order to determine what shifts they work.
  */
 public class RosterParser {
 	//-----------------------------------------------------------------------------
 	/**
 	 * Get an <code>ArrayList</code> of <code>Employee</code> names that are working
 	 * 
 	 * @param shiftTime - the time an <code>Employee</code> comes to work
 	 * @param currentShiftDate - the date an <code>Employee</code> comes to work
 	 * @return Employees - an <code>ArrayList</code> of <code>Employee</code> names
 	 */
 	public ArrayList<String> getEmployeesOnShift(int shiftTime, Calendar currentShiftDate) {
 		ArrayList<String> Employees = new ArrayList<String>();
 		ArrayList<File> files = new ArrayList<File>();
 		String name, day;
 		int dayAsInt;
 
 		// Directory path here
		Path path = Paths.get("Project","PatrolScheduler", "employee"); //ECLIPSE
		//Path path = Paths.get("PatrolScheduler", "employee"); //JAR
 
 		if (currentShiftDate == null) {
 			Calendar cal = Calendar.getInstance();		    	    
 			dayAsInt = cal.get(Calendar.DAY_OF_WEEK);
 			day = getDayAsString(dayAsInt);
 		}
 		else {
 			day = getDayAsString(currentShiftDate.get(Calendar.DAY_OF_WEEK)); //BUG
 		}
 
 		File folder = new File(path.toString());
 		Collections.addAll(files, folder.listFiles());
 
 		/*
 		 * Run through each directory, getting the name if the employee is on
 		 * the shift and adding it to the ArrayList.
 		 */
 		for (File file : files) {
 			// check if employee is on the shift
 			if (isOnShift(file, shiftTime, day)) {
 				// check if employee is on the employee list
 				name = getNameFromCNumber(file.getName());
 				// if so add him
 				if (name != null)
 					Employees.add(name);
 			}
 		}
 		return Employees;
 	}
 
 	//-----------------------------------------------------------------------------
 	/**
 	 * Check if an <code>Employee</code> is working
 	 * 
 	 * @param file - the applicable file
 	 * @param shiftTime - the time of an <code>Employee</code>'s shift
 	 * @param day - the date of an <code>Employee</code>'s shift
 	 * @return <code>boolean</code> - <code>true</code> if an <code>Employee</code> is working, 
 	 * 			 <code>false</code> else
 	 */
 	public boolean isOnShift(File file, int shiftTime, String day) 
 	{
 		String filePath, shiftTimeAsString;
 		// check that file is a directory
 		if (!file.isDirectory())
 			return false;
 
 		//filePath = file.getAbsolutePath() + "\\" + "regularschedule.lst"; // old broken code
 		filePath = Paths.get((file.getAbsolutePath()).toString(), "regularschedule.lst").toString();
 		shiftTimeAsString = ((Integer) shiftTime).toString();
 		//append leading 0 if necessary
 		if (shiftTime == 6)
 			shiftTimeAsString = "0".concat(shiftTimeAsString);
 
 
 		/*
 		 * Open the reader, read each line and check if the time and day match,
 		 * if so return true, else return false
 		 */
 
 		try {
 			BufferedReader reader = getReader(filePath);
 			String line = null;
 			while ((line = reader.readLine()) != null) 
 			{
 				if (line.startsWith(shiftTimeAsString) && line.contains(day)) {
 					return true;
 				}
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			System.out.println("No regular schedule file, line 100 Roster Parser");
 			//e.printStackTrace();
 		}
 		return false;
 	}
 	//-----------------------------------------------------------------------------
 	/**
 	 * Find an <code>Employee</code>'s name from his/her <code>Cnumber</code> if it exists
 	 * 
 	 * @param Cnumber - the University of Miami identification number
 	 * @return name - the <code>Employee</code> name, <code>null</code> if else
 	 */
 	public String getNameFromCNumber(String Cnumber) {
 		//here filename is really the cnumber
 
 		String employeeFileName, name;
 		String[] splitName;
 		//TODO fix this to not use user.dir
 		//employeeFileName = System.getProperty("user.dir")
 		//	+ "\\Project\\PatrolScheduler\\employee\\employees.lst";//JAR
 		//employeeFileName = Paths.get("Project", "PatrolScheduler", "employee", "employees.lst").toString(); // FIXED JAR
 
 		//employeeFileName = System.getProperty("user.dir")
 		//	+ "\\PatrolScheduler\\employee\\employees.lst";//ECLIPSE
 		employeeFileName = Paths.get("PatrolScheduler", "employee", "employees.lst").toString(); // FIXED ECLIPSE
 
 		/*
 		 * Open the employee list file, check for a match with the cnumber, and
 		 * if one occurs, return the name
 		 */
 
 		try 
 		{
 			BufferedReader reader = getReader(employeeFileName);
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				if (line.contains(Cnumber)) {
 					splitName = line.split("\t", 3);
 					name = splitName[0].concat(" ");
 					name = name.concat(splitName[1]);
 					return name;
 				}
 
 			}
 		} catch (IOException e) {
 			System.out.println("Employee list does not exist");
 			// e.printStackTrace();
 		}
 		return null;
 	}
 	//-----------------------------------------------------------------------------
 
 	/**
 	 * Gets a <code>BufferedReader</code> to read the given file.
 	 * 
 	 * @param filename - the filename to return a <code>BufferedReader</code>
 	 * @return a <code>BufferedReader</code> for the specified filename 
 	 */
 	private BufferedReader getReader(String filename) throws IOException {
 		return newBufferedReader(get(filename), defaultCharset());
 	}
 	//-----------------------------------------------------------------------------
 	/**
 	 * Using an integer date, get date as a <code>String</code>
 	 * 
 	 * @param dayAsInt - a day represented as an integer
 	 * @return dayOfTheWeek
 	 */
 	private String getDayAsString(int dayAsInt) {
 
 		switch (dayAsInt) {
 		case 1:
 			return "Sunday";
 		case 2:
 			return "Monday";
 		case 3:
 			return "Tuesday";
 		case 4:
 			return "Wednesday";
 		case 5:
 			return "Thursday";
 		case 6:
 			return "Friday";
 		case 7:
 			return "Saturday";
 		default:
 			return null;
 
 		}
 	}
}

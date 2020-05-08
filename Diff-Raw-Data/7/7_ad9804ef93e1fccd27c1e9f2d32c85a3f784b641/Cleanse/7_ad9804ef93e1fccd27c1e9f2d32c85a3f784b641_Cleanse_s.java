 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Cleanse {
 	public void cleansePay(String input, String output) throws IOException {
 		Pattern pattern = Pattern
 				.compile("\\\"(.*)\\\",\\\"(.*)\\\",\\\"(\\d*)\\s-\\s(.*)\\\",\"\\d*\\\",\\\"\\$(.*)\\\",\\\"\\$(.*)\\\",\\\"\\$(.*)\\\"");
 
 		BufferedReader reader = new BufferedReader(new FileReader(new File(input)));
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output)));
 
 		// Throw out header
 		reader.readLine();
 
 		String line;
 		while ((line = reader.readLine()) != null) {
 			Matcher matcher = pattern.matcher(line);
 
 			if (matcher.matches()) {
 				// Last name
 				writer.write(matcher.group(1) + ",");
 				// Fist name
 				writer.write(matcher.group(2) + ",");
 				// Position Id
 				writer.write(matcher.group(3) + ",");
 				// Position name
 				writer.write(matcher.group(4) + ",");
 				// Salary
 				writer.write(matcher.group(5).replaceAll(",", "") + ",");
 				// Benefits
 				writer.write(matcher.group(6).replaceAll(",", "") + ",");
 				// Total
 				writer.write(matcher.group(7).replaceAll(",", "") + "\n");
 			} else {
 				System.out.println("Could not match: " + line);
 			}
 		}
 
 		reader.close();
 		writer.close();
 	}
 
 	public void cleanseRenoPay() throws IOException {
 		Pattern pattern = Pattern
 				.compile("\\\"(.*)\\\",\\\"(\\d*)\\\",\\\"\\$(.*)\\\",\\\"\\$(.*)\\\",\\\"\\$(.*)\\\"");
 
 		BufferedReader reader = new BufferedReader(new FileReader(new File("data/RenoDeptPay.csv")));
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data/RenoDeptPay_clean.csv")));
 
 		// Throw out header
 		reader.readLine();
 
 		String line;
 		while ((line = reader.readLine()) != null) {
 			Matcher matcher = pattern.matcher(line);
 
 			if (matcher.matches()) {
 				// Department
 				writer.write(matcher.group(1) + ",");
 				// Year
 				writer.write(matcher.group(2) + ",");
 				// Salary
 				writer.write(matcher.group(3).replaceAll(",", "") + ",");
 				// Benefits
 				writer.write(matcher.group(4).replaceAll(",", "") + ",");
 				// Total
 				writer.write(matcher.group(5).replaceAll(",", "") + "\n");
 			} else {
 				System.out.println("Could not match: " + line);
 			}
 		}
 
 		reader.close();
 		writer.close();
 	}
 
 	public void cleanseFireCalls() throws IOException {
 		Pattern pattern = Pattern
 				.compile("\\\"(.*)\\\",\\\"(.*)\\\",\\\"(\\w*)\\\",\\\"(\\w*)\\\",\\\"(.*)\\\",\\\"(.*)\\\",\\\"(.*)\\\",\\\"(.*)\\\",\\\"(.*)\\\",\\\"(.*)\\\"");
 
 		BufferedReader reader = new BufferedReader(new FileReader(new File("data/fire_january.csv")));
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data/fire_january_clean.csv")));
 
 		// Throw out header
 		reader.readLine();
 
 		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy HH:mm:ss");
 
 		Calendar callStart = Calendar.getInstance();
 		Calendar callEnd = Calendar.getInstance();
 
 		String line;
 		while ((line = reader.readLine()) != null) {
 			Matcher matcher = pattern.matcher(line);
 
 			if (matcher.matches()) {
 				// Alarm level
 				writer.write(matcher.group(1) + ",");
 				// Call Type
 				writer.write(matcher.group(2) + ",");
 				// Jurisdiction
 				writer.write(matcher.group(3) + ",");
 				// Station
 				writer.write(matcher.group(4) + ",");
 				// Received Date
 				writer.write(matcher.group(5) + ",");
 				// Received time
 				writer.write(matcher.group(6) + ",");
 				// Dispatch 1st Time
 				writer.write(matcher.group(7) + ",");
 				// On Scene 1st Time
 				writer.write(matcher.group(8) + ",");
 				// Fire control time
 				writer.write(matcher.group(9) + ",");
 				// Close Time
 				writer.write(matcher.group(10) + ",");
 
 				long secondsOnCall = getSecondsBetween(dateFormat, callStart, callEnd, line, matcher, 5, 8, 10);
 
 				// Seconds on call
 				writer.write(String.valueOf(secondsOnCall) + "\n");
 
 			} else {
 				System.out.println("Could not match: " + line);
 			}
 		}
 
 		reader.close();
 		writer.close();
 	}
 
 	private long getSecondsBetween(SimpleDateFormat dateFormat, Calendar callStart, Calendar callEnd, String line,
 			Matcher matcher, int dateGroup, int startTimeGroup, int endTimeGroup) {
 		long secondsOnCall = 0;
 
 		try {
 			if (matcher.group(startTimeGroup).length() == 0 || matcher.group(endTimeGroup).length() == 0) {
 				secondsOnCall = 10 * 60;
 			} else {
 				callStart.setTime(dateFormat.parse(matcher.group(dateGroup) + " " + matcher.group(startTimeGroup)));
 				callEnd.setTime(dateFormat.parse(matcher.group(dateGroup) + " " + matcher.group(endTimeGroup)));
 
 				if (callStart.after(callEnd)) {
 					// Call stretched to another day
 					callEnd.add(Calendar.DAY_OF_YEAR, 1);
 				}
 
 				// Figure out seconds spent at the call
 				secondsOnCall = (callEnd.getTimeInMillis() - callStart.getTimeInMillis()) / 1000;
 			}
 		} catch (ParseException e) {
 			System.out.println("Could not parse: " + line);
 		}
 		return secondsOnCall;
 	}
 
 	public void cleansePoliceCalls() throws IOException {
 		Pattern pattern = Pattern
				.compile("\\\"(.*)\\\",\\\"(.*)\\\",\\\"(\\w*)\\\",\\\"(\\w*)\\\",(.*)\\\",\\\"(.*)\\\",\\\"(.*)\\\",\\\"(.*)\\\",\\\"(.*)\\\",\\\"(.*)\\\"");

 		BufferedReader reader = new BufferedReader(new FileReader(new File("data/police_january.csv")));
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data/police_january_clean.csv")));
 
 		// Throw out header
 		reader.readLine();
 
 		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy HH:mm:ss");
 
 		Calendar callStart = Calendar.getInstance();
 		Calendar callEnd = Calendar.getInstance();
 
 		String line;
 		while ((line = reader.readLine()) != null) {
 			Matcher matcher = pattern.matcher(line);
 
 			if (matcher.matches()) {
 				// Priority
 				writer.write(matcher.group(1) + ",");
 				// Call Type
 				writer.write(matcher.group(2) + ",");
 				// Jurisdiction
 				writer.write(matcher.group(3) + ",");
 				// Home Darea
 				writer.write(matcher.group(4) + ",");
 				// Received Date
 				writer.write(matcher.group(5) + ",");
 				// Received time
 				writer.write(matcher.group(6) + ",");
 				// Dispatch Time
 				writer.write(matcher.group(7) + ",");
 				// On Scene Time
 				writer.write(matcher.group(8) + ",");
 				// Clear Time
 				writer.write(matcher.group(9) + ",");
 				// Dispostition
 				writer.write(matcher.group(10) + ",");
 
 				long secondsOnCall = getSecondsBetween(dateFormat, callStart, callEnd, line, matcher, 5, 8, 9);
 				
 				// Seconds on call
 				writer.write(String.valueOf(secondsOnCall) + "\n");
 
 			} else {
 				System.out.println("Could not match: " + line);
 			}
 		}
 
 		reader.close();
 		writer.close();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Cleanse cleanse = new Cleanse();
 		System.out.println("Cleansing");
 		try {
 			cleanse.cleansePay("data/EmployeePay_fire.csv", "data/EmployeePay_fire_clean.csv");
 			cleanse.cleansePay("data/EmployeePay_police.csv", "data/EmployeePay_police_clean.csv");
 
 			cleanse.cleanseRenoPay();
 
 			cleanse.cleanseFireCalls();
			//cleanse.cleansePoliceCalls();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 }

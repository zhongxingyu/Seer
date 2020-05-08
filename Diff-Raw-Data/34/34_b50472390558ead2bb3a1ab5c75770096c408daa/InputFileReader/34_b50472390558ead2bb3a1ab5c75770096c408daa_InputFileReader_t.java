 package util.com.contactmanager;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import play.Logger;
 import viewobject.com.contactmanager.User;
 import exception.com.contactmanager.ApplicationException;
 import exception.com.contactmanager.CreateUserException;
 
 public class InputFileReader {
 
 	public static List<User> getUserList(File file) throws ApplicationException {
 
 		List<User> userList = new ArrayList<User>();
 		BufferedReader br = null;
 		try {
 			FileReader fr = new FileReader(file);
 			br = new BufferedReader(fr);
 		} catch (FileNotFoundException e) {
 			Logger.error(e.getMessage());
 			throw new ApplicationException("Unable to read file from "
 					+ file.getAbsolutePath());
 		}
 		String row = null;
 		int count = 0;
 		try {
 			while ((row = br.readLine()) != null) {
 				if (count != 0) {
 					User user = null;
 					try {
 						user = createUserFromCsv(row);
 					} catch (CreateUserException cue) {
 						br.close();
 						throw new ApplicationException(cue.getMessage());
 					}
 					userList.add(user);
 				}
 				count++;
 			}
 		} catch (IOException e) {
 			throw new ApplicationException(e.getMessage());
 		}
 		try {
 			br.close();
 		} catch (IOException e) {
 			throw new ApplicationException(e.getMessage());
 		}
 		return userList;
 
 	}
 
 	private static User createUserFromCsv(String csvRow)
 			throws CreateUserException {
 		// Logger.info(csvRow);
 
 		String errorMessage = "";
 		User user = new User();
 
 		String[] valueArray = csvRow.split(",");
 
 		user.name = valueArray[0];
 
 		user.user_id = valueArray[1];
 
 		user.title = valueArray[2];
 
 		user.member_id = valueArray[3];
 
 		user.company = valueArray[4];
 
 		try {
 			user.joined_date = convertStringToDate(valueArray[5]);
 		} catch (CreateUserException cue) {
			errorMessage += "Error writing join date. Please check the csv entry for user id "
					+ valueArray[1] + "\n";
 		}
 
 		try {
 			user.last_visited_date = convertStringToDate(valueArray[6]);
 		} catch (CreateUserException e) {
			errorMessage += "Error writing last visited date. Please check the csv entry for user id "
 					+ valueArray[1] + "\n";
 		}
 
 		try {
 			user.last_attended_date = convertStringToDate(valueArray[7]);
 		} catch (CreateUserException e) {
			errorMessage += "Error writing last_attended_date. Please check the csv entry for user id "
 					+ valueArray[1] + "\n";
 		}
 
 		try {
 			user.rsvp_total = convertStringToInt(valueArray[8]);
 		} catch (CreateUserException cue) {
			errorMessage += "Error writing rsvp total. Please check the csv entry for user id "
 					+ valueArray[1] + "\n";
 		}
 		try {
 			user.rsvp_yes = convertStringToInt(valueArray[9]);
 		} catch (CreateUserException cue) {
			errorMessage += "Error writing rsvp yes. Please check the csv entry for user id "
 					+ valueArray[1] + "\n";
 		}
 		try {
 			user.rsvp_maybe = convertStringToInt(valueArray[10]);
 		} catch (CreateUserException cue) {
			errorMessage += "Error writing rsvp maybe. Please check the csv entry for user id "
 					+ valueArray[1] + "\n";
 		}
 		try {
 			user.rsvp_no = convertStringToInt(valueArray[11]);
 		} catch (CreateUserException cue) {
			errorMessage += "Error writing rsvp no. Please check the csv entry for user id "
					+ valueArray[1] + "\n";
 		}
 
 		try {
 			user.meetups_attended = convertStringToInt(valueArray[12]);
 		} catch (CreateUserException e) {
			errorMessage += "Error writing meetups attended. Please check the csv entry for user id "
 					+ valueArray[1] + "\n";
 		}
 		try {
 			user.no_shows = convertStringToInt(valueArray[13]);
 		} catch (CreateUserException e) {
			errorMessage += "Error writing no shows. Please check the csv entry for user id "
 					+ valueArray[1] + "\n";
 		}
 
 		user.is_intro = convertStringToBoolean(valueArray[14]);
 		user.is_photo = convertStringToBoolean(valueArray[15]);
 		user.is_assistant_organizer = convertStringToBoolean(valueArray[16]);
 
 		user.mailing_list_type = valueArray[17];
 		user.url = valueArray[18];
 
 		if (errorMessage.length() > 0) {
			Logger.error("Error creating user: " + errorMessage);
 			throw new CreateUserException(errorMessage);
 		}
 		return user;
 	}
 
 	private static Date convertStringToDate(String dateString)
 			throws CreateUserException {
 		Date date = null;
 		try {
 			if (dateString != null && dateString.length() > 0) {
				date = new SimpleDateFormat("dd/mm/yyyy", Locale.ENGLISH)
 						.parse(dateString);
 			}
 		} catch (ParseException e) {
 			Logger.error(e.getMessage());
 			throw new CreateUserException("Error converting string to date");
 		}
 
 		return date;
 	}
 
 	private static int convertStringToInt(String intString)
 			throws CreateUserException {
 		int count = 0;
 		try {
 			if (intString != null && intString.length() > 0) {
 				count = Integer.parseInt(intString);
 			}
 		} catch (Exception e) {
 			Logger.error(e.getMessage());
 			throw new CreateUserException("Error converting string to int");
 		}
 		return count;
 
 	}
 
 	private static boolean convertStringToBoolean(String boolString) {
 		boolean is_true = false;
 
 		if (boolString != null && boolString.length() > 0
 				&& boolString.equalsIgnoreCase("yes")) {
 			is_true = true;
 		}
 		return is_true;
 	}
 }

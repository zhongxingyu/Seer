 import java.io.*;
 import java.util.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.text.ParsePosition;
 import java.util.regex.Pattern;
 
 public class EMRUtil {
 	public static String[] attributes = { Attribute.NAME, Attribute.PATIENTID,
 		Attribute.BIRTHDAY, Attribute.PHONE, Attribute.EMAIL,
 		Attribute.MEDICALHISTORY, Attribute.ADDRESS, "medicalHistory" };
 
 	public static int validPhone(String s) {
 		if (s == null) return -1;
 		if (!s.matches("-?\\d+(\\.\\d+)?")) return -1;
 		return Integer.parseInt(s);
 	}
 	
 	public static String validEmail(String s) {
 		if (s == null) return null;
 		if (!(s.contains(" ") == false && s.matches(".+@.+\\.[a-z]+"))) return null;
 		if ((s.length() - s.replaceAll("\\@", "").length()) != 1) return null;
 		if ((s.length() - s.replaceAll("\\.", "").length()) != 1) return null;
 		return s;
 	}
 
 	public static boolean nameIsValid(String s) {
 		if (s == null)
 			return false;
 		return true;
 		// return s.matches(".+@.+\\.[a-z]+");
 	}
 
 	public static boolean dateIsValid(String s) {
		if (s == null) return false;
		if (!s.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) return false;
 		try {
			SimpleDateFormat sdf = new SimpleDateFormat("d-M-y", Locale.ENGLISH);
 			sdf.setLenient(false);
 			sdf.parse(s);
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public static Date stringToDate(String s) throws java.text.ParseException {
 		return new SimpleDateFormat("d-M-yyyy", Locale.ENGLISH).parse(s);
 	}
 
 	public static String dateToStringBirthday(Date date) {
 		return new SimpleDateFormat("d-M-yyyy", Locale.ENGLISH).format(date);
 	}
 
 	public static String dateToStringDiagnosis(Date date) {
 		return new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(date);
 	}
 
 	public static boolean wordIsAttribute(String word) {
 		for (String attribute : EMRUtil.attributes) {
 			if (word.equalsIgnoreCase(attribute))
 				return true;
 		}
 		return false;
 	}
 
 	public static boolean scannerHasNextAttributeWord(Scanner scanner) {
 		for (String attribute : EMRUtil.attributes) {
 			if (scanner.hasNext(attribute))
 				return true;
 		}
 		return false;
 	}
 }

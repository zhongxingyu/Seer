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
 	public static int lastUsedId = 0;
 
 	public static boolean phoneIsValid(String s) {
 		if (s == null)
 			return false;
 		return s.matches("-?\\d+(\\.\\d+)?");
 	}
 
 	public static boolean nameIsValid(String s) {
 		if (s == null)
 			return false;
 		return true;
 		// return s.matches(".+@.+\\.[a-z]+");
 	}
 
 	public static boolean emailIsValid(String s) {
 		if (s == null)
 			return false;
 		if (!(s.contains(" ") == false && s.matches(".+@.+\\.[a-z]+")))
 			return false;
 		if ((s.length() - s.replaceAll("\\@", "").length()) != 1)
 			return false;
 		if ((s.length() - s.replaceAll("\\.", "").length()) != 1)
 			return false;
 		return true;
 	}
 
 	public static boolean dateIsValid(String s) {
 		if (s == null)
 			return false;
 		try {
			SimpleDateFormat format = new SimpleDateFormat(s);
			format.setLenient(false);
			format.parse(s);
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
		return new SimpleDateFormat("dd-M-yyyy", Locale.ENGLISH).format(date);
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
 
 	public static int generatePatientId() {
 		EMRUtil.lastUsedId++;
 		return EMRUtil.lastUsedId;
 	}
 }

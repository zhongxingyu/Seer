 package steam.dbexplorer;
 
 /**
  * Utils is a general utility class that has additional general functionality methods that assist the program.
  *
  * @author Andrew Hollenbach (anh7216@rit.edu)
  */
 public class Utils {
 	
 	/**
 	 * Surrounds the string with single quotes
 	 * 
 	 * @param val a val to surround with quotes
 	 * @return The value surrounded by quotes
 	 */
 	public static String surroundWithQuotes(String val) {
 		return "\'" + val + "\'";
 	}
 	
 	/**
 	 * Sanitizes the string (that is, escapes any single quotes), the
 	 * surrounds the string with single quotes.
 	 * 
 	 * @param val a string to sanitize and surround with quotes
 	 * @return The inputted string with all single quotes escaped, then
 	 * surrounded by single quotes.
 	 */
 	public static String surroundAndSanitize(String val) {
 		val = sanitize(val);
 		val = surroundWithQuotes(val);
 		return val;
 	}
 	
 	/**
 	 * Sanitizes the string (that is, escapes any single quotes).
 	 * 
 	 * @param s a string to sanitize
 	 * @return The inputted string with all single quotes escaped
 	 */
 	public static String sanitize(String s) {
 		String[] valArr = s.split("'");
 		s = "";
 		for(int i=0;i<valArr.length;i++) {
 			if(i==valArr.length-1) s += valArr[i];
			else s += valArr[i] + "''";
 		}
 		return s;
 	}
 }

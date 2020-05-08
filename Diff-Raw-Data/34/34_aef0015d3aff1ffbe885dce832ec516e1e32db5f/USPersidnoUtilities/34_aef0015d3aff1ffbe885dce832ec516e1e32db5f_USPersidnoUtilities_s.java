 package is.us.util;
 
 import java.util.*;
 
 /**
  * Various utility methods to handle Icelandic PIDs (kennitala).
  * 
  * In this class, the PID is referred to as a "persidno", that's lingo inherited
  * from our previous computer systems.
  * 
  * http://www.thjodskra.is/en/id-numbers/
  * http://en.wikipedia.org/wiki/Kennitala
  * 
  * @author Hugi Thordarson
  */
 
 public class USPersidnoUtilities {
 
 	private static final String DEFAULT_PERSIDNO_DELIMITER = "-";
 
 	/**
 	 * Returns true if the persidno is an individual
 	 * 
 	 * @param persidno The persidno to check.
 	 */
 	public static boolean isIndividualPersidno( String persidno ) {
 
 		if( persidno == null ) {
 			return false;
 		}
 
 		persidno = cleanupPersidno( persidno );
 		int marker = Integer.parseInt( persidno.substring( 0, 1 ) );
 		return marker < 4;
 	}
 
 	/**
 	 * Returns true if the persidno is for a company.
 	 * 
 	 * @param persidno The persidno to check.
 	 */
 	public static boolean isCompanyPersidno( String persidno ) {
 
 		if( persidno == null ) {
 			return false;
 		}
 
 		persidno = cleanupPersidno( persidno );
 		int marker = Integer.parseInt( persidno.substring( 0, 1 ) );
 		return marker == 4 || marker == 5 || marker == 6 || marker == 7;
 	}
 
 	/**
 	 * Gets the birthdate of the given individual.
 	 * 
 	 * @param persidno The persidno to check.
 	 * @return the birthdate of the individual.
 	 */
 	public static Date birthdateFromPersidno( String persidno ) {
 
 		if( persidno == null ) {
 			return null;
 		}
 
 		persidno = cleanupPersidno( persidno );
 
 		Integer day = birthDayFromPersidno( persidno );
 		Integer month = birthMonthFromPersidno( persidno );
 		Integer fullYear = birthyearFromPersidno( persidno );
 
 		GregorianCalendar birthday = (GregorianCalendar)GregorianCalendar.getInstance();
 		birthday.clear();
 		birthday.set( fullYear, month - 1, day, 0, 0, 0 );
 		birthday.set( Calendar.MILLISECOND, 0 );
 
 		return birthday.getTime();
 	}
 
 	/**
 	 * Derives a person's age from a persidno.
 	 *
 	 * @param persidno The persidno to check.
 	 */
 	public static Integer ageFromPersidno( String persidno ) {
 
 		if( persidno == null ) {
 			return null;
 		}
 
 		Date birthDate = birthdateFromPersidno( persidno );
 		return USDateUtilities.ageAtDate( birthDate, new Date() );
 	}
 
 	/**
 	 * Validates a persidno's structure to see if it complies with the validation algorithm.
 	 *
 	 * @param persidno The persidno to check, should be of the format "##########"
 	 */
 	public static boolean validatePersidno( String persidno ) {
 
 		if( persidno == null ) {
 			return false;
 		}
 
 		if( persidno.length() != 10 ) {
 			return false;
 		}
 
 		if( !USStringUtilities.isDigitsOnly( persidno ) ) {
 			return false;
 		}
 
 		int sum = Character.digit( persidno.charAt( 0 ), 10 ) * 3;
 		sum = sum + Character.digit( persidno.charAt( 1 ), 10 ) * 2;
 		sum = sum + Character.digit( persidno.charAt( 2 ), 10 ) * 7;
 		sum = sum + Character.digit( persidno.charAt( 3 ), 10 ) * 6;
 		sum = sum + Character.digit( persidno.charAt( 4 ), 10 ) * 5;
 		sum = sum + Character.digit( persidno.charAt( 5 ), 10 ) * 4;
 		sum = sum + Character.digit( persidno.charAt( 6 ), 10 ) * 3;
 		sum = sum + Character.digit( persidno.charAt( 7 ), 10 ) * 2;
 
 		int leftovers = (sum % 11);
 		int validationNumber = 11 - leftovers;
 		int vartala = Character.digit( persidno.charAt( 8 ), 10 );
 
 		if( validationNumber == 11 ) {
 			validationNumber = 0;
 		}
 
 		if( validationNumber == vartala ) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Attempts to format a persidno string to the standard used in the DB (removes dashes and spaces).
 	 *
 	 * @param persidno The persidno to format.
 	 */
 	public static String cleanupPersidno( String persidno ) {
 
 		if( persidno == null ) {
 			return null;
 		}
 
 		String stringPersidno = persidno.toString();
 
 		if( USStringUtilities.stringHasValue( stringPersidno ) ) {
 			stringPersidno = USStringUtilities.replace( stringPersidno, "-", "" );
 			stringPersidno = USStringUtilities.replace( stringPersidno, " ", "" );
 		}
 
 		return stringPersidno;
 	}
 
 	/**
 	 * Returns the year that a person was born.
 	 */
 	public static Integer birthyearFromPersidno( String persidno ) {
 
 		if( persidno == null ) {
 			return null;
 		}
 
 		String stringPersidno = cleanupPersidno( persidno );
 
 		if( !isIndividualPersidno( persidno ) ) {
 			return null;
 		}
 
 		if( stringPersidno.length() != 10 ) {
 			return null;
 		}
 
 		String centuryFull = null;
 		String centuryMarker = stringPersidno.substring( 9, 10 );
 		String year = stringPersidno.substring( 4, 6 );
 
 		if( centuryMarker.equals( "0" ) ) {
 			centuryFull = "20";
 		}
 		else {
 			centuryFull = "1" + centuryMarker;
 		}
 
 		String fullYear = centuryFull + year;
 
 		return new Integer( fullYear );
 	}
 
 	/**
 	 * Returns the month that a person was born.
 	 */
 	public static Integer birthMonthFromPersidno( String persidno ) {
 
 		if( persidno == null ) {
 			return null;
 		}
 
 		persidno = cleanupPersidno( persidno );
 
 		if( !isIndividualPersidno( persidno ) ) {
 			return null;
 		}
 
 		return new Integer( persidno.substring( 2, 4 ) );
 	}
 
 	/**
 	 * Returns the month that a person was born.
 	 */
 	public static Integer birthDayFromPersidno( String persidno ) {
 
 		if( persidno == null ) {
 			return null;
 		}
 
 		persidno = cleanupPersidno( persidno );
 
 		if( !isIndividualPersidno( persidno ) ) {
 			return null;
 		}
 
 		return new Integer( persidno.substring( 0, 2 ) );
 	}
 
 	/**
 	 * Returns a persidno, formatted by inserting a "-" after the birthdate.
 	 * returns an empty string if persidno is not valid.
 	 * 
 	 * @param persidno to format
 	 * @param delimiter delimiter between the six'th and seven'th decimals
 	 * @return formatted persidno
 	 */
 	public static String formatPersidno( String persidno ) {
 		return formatPersidno( persidno, DEFAULT_PERSIDNO_DELIMITER );
 	}
 
 	/**
 	 * Returns a persidno, formatted by inserting the specified delimiter after the birthdate.
 	 * eturns an empty string if persidno is not valid.
 	 * 
 	 * @param persidno to format
 	 * @param delimiter delimiter between the six'th and seven'th decimals
 	 * @return formatted persidno
 	 */
 	public static String formatPersidno( String persidno, String delimiter ) {
 
 		if( persidno == null ) {
 			return "";
 		}
 
 		persidno = cleanupPersidno( persidno );
 
 		if( persidno.length() != 10 ) {
 			return "";
 		}
 
 		return persidno.substring( 0, 6 ) + delimiter + persidno.substring( 6 );
 	}
 
 	/**
 	 * @param The persidno to deduce information from.
 	 * @return The individuals next birthday, as calculated from the persidno.
 	 */
 	public static Date nextBirthday( String persidno ) {
 
 		if( persidno == null ) {
 			return null;
 		}
 
 		Integer birthDay = birthDayFromPersidno( persidno );
 		Integer birthMonth = birthMonthFromPersidno( persidno );
 
 		GregorianCalendar now = (GregorianCalendar)GregorianCalendar.getInstance();
 
 		Integer nowDay = now.get( GregorianCalendar.DAY_OF_MONTH );
 		Integer nowMonth = now.get( GregorianCalendar.MONTH ) + 1;

 		Integer year = now.get( GregorianCalendar.YEAR );
 
		if( nowMonth >= birthMonth && nowDay >= birthDay ) {
 			year++;
 		}
 
 		return USDateUtilities.date( year, birthMonth, birthDay );
 	}
 }

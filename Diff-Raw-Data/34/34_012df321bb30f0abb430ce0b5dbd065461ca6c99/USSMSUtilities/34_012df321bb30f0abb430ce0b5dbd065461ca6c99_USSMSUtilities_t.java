 package is.us.util;
 
 import java.util.*;
 
 /**
  * A class to send SMS messages.
  * 
  * @author Hugi Þórðarson
  */
 
 public class USSMSUtilities {
 
 	private static final String MESSAGE_ENCODING = "ISO-8859-1";
 	private static final int MAX_MESSAGE_LENGTH = 160;
 
 	/**
 	 * No instances created, ever.
 	 */
 	private USSMSUtilities() {}
 
 	/**
 	 * @return the base url used to invoke the SMS-service.
 	 */
 	private static String baseURL() {
 		return System.getProperty( "USSMSUtilities.baseURL" );
 	}
 
 	/**
 	 * @return the username used when invoking the SMS-service.
 	 */
 	private static String username() {
 		return System.getProperty( "USSMSUtilities.username" );
 	}
 
 	/**
 	 * @return the password used when invoking the SMS-service.
 	 */
 	private static String password() {
 		return System.getProperty( "USSMSUtilities.password" );
 	}
 
 	/**
 	 * Sends an SMS message to the given phone number.
 	 * 
 	 * @param message The SMS message to send.
 	 * @param phoneNumber The phone number to send the SMS-message to.
 	 */
 	public static void sendMessageToNumber( String message, String phoneNumber ) {
 
 		if( USStringUtilities.stringHasValue( message ) && message.length() > MAX_MESSAGE_LENGTH ) {
 			throw new RuntimeException( "Message is too long. Max length of an SMS message is " + MAX_MESSAGE_LENGTH + " characters." );
 		}
 
 		if( USPhoneUtilities.validateMobileNumber( phoneNumber ) ) {
 			Map<String, Object> parameters = new HashMap<String, Object>();
 			parameters.put( "L", username() );
 			parameters.put( "P", password() );
 			parameters.put( "DR", "N" );
 
 			try {
 				String urlEncodedMessage = java.net.URLEncoder.encode( message, MESSAGE_ENCODING );
 				parameters.put( "T", urlEncodedMessage );
 			}
 			catch( Exception e ) {
 				throw new RuntimeException( "Failed to URL encode message string for SMS transmission." );
 			}
 
 			parameters.put( "MSISDN", phoneNumber );
			String url = USStringUtilities.constructURLStringWithParameters( baseURL(), parameters, false );
 			USStringUtilities.readStringFromURLUsingEncoding( url, null );
 		}
 	}
 
 	/**
 	 * Sends an SMS message to multiple phone numbers.
 	 * 
 	 * @param message The SMS message to send.
 	 * @param phoneNumbers An array of mobile phone numbers (strings) to send to.
 	 */
 	public static void sendMessageToNumbers( String message, List<String> phoneNumbers ) {
 		if( phoneNumbers != null && phoneNumbers.size() > 0 ) {
 			for( String number : phoneNumbers ) {
 				sendMessageToNumber( message, number );
 			}
 		}
 	}
 }

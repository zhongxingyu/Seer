 package com.mymed.tests.unit;
 
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.Calendar;
 
 import com.mymed.utils.MLogger;
 
 /**
  * Simple class to hold static string values valid for all the tests
  * 
  * @author Milo Casagrande
  */
 public class TestValues {
 	protected static final String CONF_FILE = "conf/config.xml";
 	protected static final String NAME = "username";
 	protected static final String FIRST_NAME = "John";
 	protected static final String LAST_NAME = "Carter";
 	protected static final String FIRST_NAME_2 = "Tars";
 	protected static final String LAST_NAME_2 = "Tarkas";
 	protected static final String USER_TABLE = "User";
 	protected static final String WRONG_USER_TABLE = "Users";
 	protected static final String USER_ID = "userKey";
 	protected static final String WRONG_USER_ID = "keyUser";
 	protected static final String COLUMN_NAME = "name";
 	protected static final String COLUMN_FIRSTNAME = "firstName";
 	protected static final String COLUMN_LASTNAME = "lastName";
 	protected static final String COLUMN_BIRTHDATE = "birthday";
 	protected static final String WRONG_COLUMN_NAME = "wrong_name";
 
 	protected static final String LOGIN = "usertest1";
 	protected static final String LOGIN_2 = "usertest2";
 	protected static final String EMAIL = "testUser@example.net";
 	protected static final String EMAIL_2 = "newEmail@example.net";
 	protected static final String LINK = "http://www.example.net";
 	protected static final String HOMETOWN = "123456789.123454";
 	protected static final String GENDER = "female";
 	protected static final String BUDDY_LST_ID = "buddylist1";
 	protected static final String SUBSCRIPTION_LST_ID = "subscription1";
 	protected static final String REPUTATION_ID = "reputation1";
 	protected static final String SESSION_ID = USER_ID + "_SUFFIX";
 	protected static final String INTERACTION_LST_ID = "interaction1";
 
 	protected static final String INTERACTION_ID = "interaction1";
 	protected static final String APPLICATION_ID = "application1";
 	protected static final String PRODUCER_ID = "producerKey";
 	protected static final String CONSUMER_ID = "consumerKey";
 
 	protected static final String IP = "138.126.23.2";
 
 	protected static final Calendar CAL_INSTANCE = Calendar.getInstance();
 
 	/**
 	 * Random instance to keep around.
 	 */
 	private static final SecureRandom RANDOM = new SecureRandom();
 
	private TestValues() {
		// Private empty constructor to silence warnings about singleton
	}

 	/**
 	 * Get the string representation of a RANDOM casual generated password
 	 * 
 	 * @return the SHA-256 in hex format of a RANDOM string
 	 */
 	protected static String getRandomPwd() {
 		final StringBuffer hex = new StringBuffer(100);
 
 		try {
 			final String randString = new BigInteger(130, RANDOM).toString(32);
 			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
 
 			digest.update(randString.getBytes("UTF-8"));
 			final byte[] mdbytes = digest.digest();
 
 			for (final byte b : mdbytes) {
 				hex.append(Integer.toHexString(0xFF & b));
 			}
 
 			hex.trimToSize();
 		} catch (final NoSuchAlgorithmException ex) {
 			MLogger.getDebugLog().debug("Random password generator failed", ex.getCause());
 		} catch (final UnsupportedEncodingException ex) {
 			MLogger.getDebugLog().debug("Random password generator failed", ex.getCause());
 		}
 
 		return hex.toString();
 	}
 }

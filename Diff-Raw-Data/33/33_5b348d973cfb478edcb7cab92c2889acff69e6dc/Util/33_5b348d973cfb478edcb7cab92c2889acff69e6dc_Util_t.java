 package quizsite.controllers;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.security.MessageDigest;
 import java.security.SecureRandom;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import quizsite.models.User;
 
 public class Util {
 	/** Defining all magic strings here, to avoid typos */
 	public static final String USER_SESSION_KEY = "CURRENT_USER_ID";
 	public static final String RECEIVED_MSG_LIST_KEY = "RECEIVED_MESSAGES";
 	public static final String SENT_MSG_LIST_KEY = "SENT_MESSAGES";
	//SHOULD THE BELOW BE THERE?!?
	//public static final String CURRENT_USER_KEY = "CURRENT_USER";
 
 	/** Views */
 	public static final String LOGIN_VIEW = "login_user.jsp";
 	public static final String INBOX_VIEW = "inbox.jsp";
 	public static final String HOME_VIEW = "home.jsp";
 	public static final String REGISTER_USER_VIEW = "register_user.jsp";
 	public static final String LOGOUT_VIEW 	= "logout_user.jsp";
 	public static final String CREATE_QUIZ_VIEW = "create_quiz.jsp";
 
 	/** 
 	 * Return currently logged in user if user-session exists and if the user-session belongs to a valid user
 	 * Else redirect to login page
 	 * 
 	 * IMPORTANT: This method MUST be used EXACTLY as below followed by the bottom if statement followed by the below code when used in a servlet controller
 	 * User current = signInOrRedirect(request, response_);
 	 * if(current == null) return;
 	 * 
 	 * If you get a HTTP 500 Error CHECK THIS!
 	 * 
 	 * @throws IOException
 	 * @throws ServletException 
 	 * */
 	public static User signInOrRedirect(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		Integer userId = (Integer) session.getAttribute(USER_SESSION_KEY);
 		System.out.println("signInorRedirect userId is : " + userId);
 		User curr;
 		try {
 			//If it is a valid user, return the user
 			if ( userId != null && (curr = User.get(userId)) != null) {
 				return curr;
 			} else {
 				//Else send to login page	
 				RequestDispatcher dispatch = request.getRequestDispatcher(LOGIN_VIEW);
 				request.setAttribute("failureMessage", "You are not logged in. Please login to use the site.");
 				dispatch.forward(request, response);
 				return null;
 			}
 		} catch (SQLException e) {
 			//On SQL exception send to login page
 			RequestDispatcher dispatch = request.getRequestDispatcher(LOGIN_VIEW);
 			request.setAttribute("failureMessage", "Error with user session. You are not logged in. Please login to use the site.");
 			dispatch.forward(request, response);
 			return null;
 		}
 	}
 
 	/** Reads the "mode.txt" file to decide -- NOT USING
 	 * @throws FileNotFoundException */
 	public static String readMode() {
 		System.out.println(System.getProperty("user.home"));
 		final String file_name = System.getProperty("user.home") + "/Desktop/mode.txt";
 		try {
 			FileInputStream in_file = new FileInputStream(file_name);
 			InputStreamReader reader = new InputStreamReader(in_file);
 			BufferedReader bufReader = new BufferedReader(reader);
 			return bufReader.readLine().trim();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 
 
 	/**
 	 * Returns a csv formatted string, given an object array. Uses toString(). Ignores null / objects
 	 * which give only whitespace on toString()
 	 * eg. 	{"abc", "def"} 	=> " abc,def "
 	 * 	   	null			=> ""
 	 * 		{}				=> ""
 	 * 		{"abc", null, "def"} => " abc,def "
 	 * 		{"a", "  ", "a"} => " a,a "
 	 * @param separator TODO
 	 * */
 	public static String join(Object[] arr, String separator) {
 		if (arr == null || arr.length == 0) {
 			return " ";
 		} else {
 			List<Object> list = Arrays.asList(arr);
 			return join(list, separator);
 		}
 	}
 
 
 
 	/** See {@link Util#join(Object[], String)}
 	 * @param separator TODO*/
 	public static <T> String join(List<T> list, String separator) {
 		if (list == null || list.size() == 0) {
 			return " ";
 		} else {
 			StringBuilder sb = new StringBuilder(" ");
 			for (Object obj : list) {
 				if (obj != null && !obj.toString().trim().isEmpty()) {
 					sb.append(obj.toString());
 					sb.append(separator);
 				}
 			}
 			String ret = sb.toString();
 			ret = ret.substring(0, ret.length() - separator.length()); 	// Remove trailing comma
 			ret = ret + " ";	// Pad 
 			return ret;
 		}
 	}
 
 
 	/* ------------------------- Hashing and salting methods -------------------------*/	
 
 	/* These are used for user login and user registration */
 
 	/**
 	 * Returns a secure random salt string to be added to the password before hashing.
 	 */
 
 	public static String generateSalt() {
 		Random random = new Random();
 		char[] randomChars = "abcdefghijklmnopqrstuvwzyz1234567890".toCharArray();
 		StringBuilder saltBuilder = new StringBuilder();
 		//User 20 character length salt as recommended for crypto
 		for(int i =0; i <20; ++i) {
 			char randomChar = randomChars[random.nextInt(randomChars.length)];
 			saltBuilder.append(randomChar);
 		}
 	
 		System.out.println("Salt made: " + saltBuilder.toString());
 		return saltBuilder.toString();
 	}
 
 
 	/**
 	 * Takes in a string input and a salt string to be hashed and returns an equivalent salted hash based
 	 * on the SHA hash algorithm.
 	 */
 	public static String makeSaltedHash(String password, String salt) {
 		String result = "";
 
 		//Convert password to bytes and concatenate with the salt
 		byte[] passwordAndSaltBytes = concatByteArrays(password.getBytes(), salt.getBytes());
 
 
 		try {
 			//Compute password hash
 			MessageDigest messageDigest = MessageDigest.getInstance("SHA");
 			messageDigest.update(passwordAndSaltBytes);
 			byte[] passwordAndSaltDigestBytes = messageDigest.digest();
 			result = hexToString(passwordAndSaltDigestBytes);
 		} catch (Exception e) {
 			System.err.println("Error making salted hash: ");
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	private static byte[] concatByteArrays(byte[] arr1, byte[] arr2) {
 		byte[] result = new byte[arr1.length + arr2.length];
 		System.arraycopy(arr1, 0, result, 0, arr1.length);
 		System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
 
 		return result;
 	}
 
 
 	/**
 	 Given a byte[] array, produces a hex String,
 	 such as "234a6f". with 2 chars for each byte in the array.
 	 (provided code)
 	 */
 	public static String hexToString(byte[] bytes) {
 		StringBuffer buff = new StringBuffer();
 		for (int i=0; i<bytes.length; i++) {
 			int val = bytes[i];
 			val = val & 0xff;  // remove higher bits, sign
 			if (val<16) buff.append('0'); // leading 0
 			buff.append(Integer.toString(val, 16));
 		}
 		return buff.toString();
 	}
 
 	/**
 	 Given a string of hex byte values such as "24a26f", creates
 	 a byte[] array of those values, one byte value -128..127
 	 for each 2 chars.
 	 (provided code)
 	 */
 	public static byte[] hexToArray(String hex) {
 		byte[] result = new byte[hex.length()/2];
 		for (int i=0; i<hex.length(); i+=2) {
 			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
 		}
 		return result;
 	}
 	
	/* ----------------DEBUGGING METHODS ---------------- */
 	public static void printUserSessionId(HttpServletRequest request) {
 		HttpSession session = request.getSession();
 		System.out.println("Current User ID; " + session.getAttribute(Util.USER_SESSION_KEY));
 	}
 
 }

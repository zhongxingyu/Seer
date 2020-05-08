 package com.asa.sms2honeycomb.Util;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.asa.sms2honeycomb.MessageItem;
 import com.asa.sms2honeycomb.Preferences;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 
 public class Util {
 	private final static String TAG = "Util";
 
 	/**
 	 * Checks to see if there is white space within the input (both username and
 	 * email). If there is, then it is an invalid input. Returns true if there
 	 * is white space, false if there is no white space.
 	 * 
 	 * @param input
 	 * @return
 	 */
 	public static boolean containsWhiteSpace(String input) {
 		if (input != null) {
 			for (int i = 0; i < input.length(); i++) {
 				if (Character.isWhitespace(input.charAt(i))) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Pushes the email, username, and password to the Parse UserTable object.
 	 * 
 	 * @param email
 	 * @param username
 	 * @param password
 	 */
 	public static void pushToTable(String email, String username,
 			String password) {
 		ParseObject userTable = new ParseObject("UserTable");
 		userTable.put(Preferences.PARSE_EMAIL_ROW, email);
 		userTable.put(Preferences.PARSE_USERNAME_ROW, username);
 		userTable.put("password", password); // TODO: Encrypt passwords!
 		try {
 			userTable.save();
 			if (Preferences.DEBUG)
 				Log.d(TAG, "Save success.");
 		} catch (ParseException e1) {
 			e1.printStackTrace();
 			Log.d(TAG, "Save failed.");
 		}
 	}
 
 	// TODO work on this
 	/**
 	 * Gets from the table given the messages (limit 10) sorted by decending
 	 * created times and returns a list of them or something
 	 * 
 	 * @param table
 	 * @return totalmessages
 	 */
 	public String getMessages(String table) {
 		final ParseQuery query = new ParseQuery(table);
 		query.whereEqualTo(Preferences.PARSE_USERNAME_ROW, getUsernameString());
 		query.orderByDescending("createdAt");
 		query.setLimit(10);
 		query.findInBackground(new FindCallback() {
 			public void done(List<ParseObject> messageList, ParseException e) {
 				if (e == null) {
 					Log.d(TAG, "Retrieved " + messageList.size() + " messages.");
 					for (ParseObject messageObject : messageList) {
 						String objectId = messageObject.objectId();
 						try {
 							ParseObject message = query.get(objectId);
 							Date time = message.createdAt();
 							String to = message.getString("messageTo");
 							String body = message.getString("messageBody");
 							String totalMessage = "Sent: " + time + " To: "
 									+ to + " Message : " + body;
 							System.out.println(totalMessage);
 						} catch (ParseException e1) {
 							Log.e(TAG, e1.getMessage());
 						}
 					}
 				} else {
 					Log.d(TAG, "Error: " + e.getMessage());
 				}
 			}
 		});
 		return table;
 	}
 
 	private static ArrayList<MessageItem> outgoingMessageList;
 	private static ArrayList<MessageItem> incomingMessageList;
 	private static ArrayList<ArrayList<MessageItem>> results;
 
 	private static ArrayList<MessageItem> messageResults;
 
 	public static ArrayList<MessageItem> getAllMessages(final String username) {
 		ParseQuery querySms = new ParseQuery(Preferences.PARSE_TABLE_SMS);
 		ParseQuery queryThread = new ParseQuery(Preferences.PARSE_TABLE_THREAD);
 
 		querySms.whereEqualTo(Preferences.PARSE_USERNAME_ROW, username);
 		final String messageBody, messageAddress;
 		final Date outgoingMessageDate;
 		outgoingMessageList = new ArrayList<MessageItem>();
 		incomingMessageList = new ArrayList<MessageItem>();
 		results = new ArrayList<ArrayList<MessageItem>>();
 		messageResults = new ArrayList<MessageItem>();
 		// Begin query for messages.
 		querySms.findInBackground(new FindCallback() {
 			@Override
 			public void done(List<ParseObject> objects, ParseException e) {
 				if (e == null) {
 					if (Preferences.DEBUG)
 						Log.d(TAG, "Message size: " + objects.size());
 					for (ParseObject messageObject : objects) {
 						MessageItem messageItem = new MessageItem();
 						messageItem.setMessageBody(messageObject
 								.getString(Preferences.PARSE_SMS_BODY));
 						messageItem.setMessageUsername(messageObject
 								.getString(username));
 						messageItem.setMessageAddress(messageObject
 								.getString(Preferences.PARSE_SMS_ADDRESS));
 						messageItem.setMessageTime(messageObject.createdAt()
 								.toLocaleString());
 						messageItem.setMessageSmsId(messageObject
 								.getInt(Preferences.PARSE_SMS_SMSID));
 						messageItem.setMessageThreadId(messageObject
 								.getInt(Preferences.PARSE_SMS_THREAD_ID));
 						messageItem.setMessageType(messageObject
 								.getInt(Preferences.PARSE_SMS_TYPE));
 						messageItem.setMessageRead(messageObject
 								.getInt(Preferences.PARSE_SMS_READ));
 						messageItem.setMessageSubject(messageObject
 								.getString(Preferences.PARSE_SMS_SUBJECT));
 						messageResults.add(messageItem);
 						Log.e(TAG,
 								"MessageItem - Size: " + messageResults.size());
 					}
 				} else {
 					// Error occurred finding ALL objects. TODO
 					Log.e(TAG, "Message Item:");
 				}
 			}
 		});
 
 		// TODO : This is commented out because I am only testing getting the
 		// messages for now.
 		// queryThread.whereEqualTo(Preferences.PARSE_EMAIL_ROW, username);
 		// queryThread.findInBackground(new FindCallback(){
 		// @Override
 		// public void done(List<ParseObject> objects, ParseException e) {
 		// if(e == null){
 		// for(ParseObject messageObject : objects){
 		// MessageItem messageItem = new MessageItem();
 		// messageItem.setMessageBody(messageObject.getString("messageBody"));
 		// messageItem.setMessageFrom(messageObject.getString(username));
 		// messageItem.setMessageTo(messageObject.getString("messageTo"));
 		// messageItem.setMessageTime(messageObject.createdAt().toLocaleString());
 		// messageResults.add(messageItem);
 		// }
 		//
 		// }else{
 		// // Error occurding finding objects TODO
 		// Log.e(TAG, "Error finding incoming messages.");
 		// }
 		//
 		// }
 		// });
 		return messageResults;
 	}
 
 	/**
 	 * Creates a string using the uername and a given name of the wanted push
 	 * channel. Returns a string "keyitem_nameOfPushChannel".
 	 * 
 	 * @param username
 	 * @param nameOfPushChannel
 	 * @return String username_pushchannel
 	 */
 	public static String getPushChannel(String username,
 			String nameOfPushChannel) {
 		String pushChannel = username + "_" + nameOfPushChannel;
 		Log.d(TAG, "Push channel has been created for: " + username + "_"
 				+ nameOfPushChannel);
 		return pushChannel;
 	}
 
 	/**
 	 * Logouts the current user via the Parse command returns true if logged
 	 * out.
 	 * 
 	 * @return boolean
 	 */
 	public static boolean logoutUser() {
 		ParseUser.logOut();
 		if (ParseUser.getCurrentUser() == null) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the current users username via the Parse command
 	 * 
 	 * @return String currentUser
 	 */
 	public static String getUsernameString() {
 		return ParseUser.getCurrentUser().getUsername();
 	}
 
 	public static Date formatStringToDate(String stringDate) {
 		SimpleDateFormat formatter = new SimpleDateFormat(
 				"MMM dd, yyyy HH:mm:ss a");
 		Date date = null;
 		try {
 			date = formatter.parse(stringDate);
 		} catch (java.text.ParseException e) {
 			// TODO : Handle this exception
 			e.printStackTrace();
 		}
 		return date;
 	}
 
 	static Toast mToast;
 
 	public static void displayToast(Context context, String message) {
 		mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
 		mToast.show();
 	}

	public static String getCurrentDate() {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy/MMM/dd HH:mm:ss");
		return currentDate.getTime().toLocaleString();
	}
 }

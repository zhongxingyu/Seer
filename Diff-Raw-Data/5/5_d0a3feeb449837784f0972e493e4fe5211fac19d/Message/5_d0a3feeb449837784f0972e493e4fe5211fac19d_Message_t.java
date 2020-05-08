 package net.filiph.mothership;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Simple class holding the information about each message.
  */
 public class Message {
 	public final Date time;
 	public final String text;
 	public boolean notify = true;
 	public boolean vibrate = false;
 	public boolean forceShowActivity = false;
 	public final int uid;
 	
 	/**
 	 * Constructs a new message that should fire in time [:t:] with message [:s:].
 	 * 
 	 * @param t		The time when this message should appear.
 	 * @param s		The actual text of the message. This can contain HTML tags.
 	 */
 	Message(Date t, String s) {
 		time = t;
 		text = s;
 		uid = getHashFromString(s + getTimeString());
 	}
 	
 	/**
 	 * Constructs a new message that should fire in time [:t:] with message [:s:].
 	 * 
 	 * @param t		The time when this message should appear.
 	 * @param s		The actual text of the message. This can contain HTML tags.
 	 * @param _notify	Whether to make a notification when the message's time comes.
 	 * @param _vibrate	Whether to vibrate when the message's time comes.
 	 */
 	Message(Date t, String s, boolean _notify, boolean _vibrate) {
 		time = t;
 		text = s;
 		notify = _notify;
 		vibrate = _vibrate;
 		uid = getHashFromString(s + getTimeString());
 	}
 	
 	/**
 	 * Compute very simple hash from a given string.
 	 * 
 	 * @param s		String to be hashed.
 	 * @return		An (hopefully) unique integer.
 	 */
 	public static int getHashFromString(String s) {
 		int hash = 7;
 		for (int i = 0; i < s.length(); i++) {
 		    hash = hash * 31 + s.charAt(i);
 		}
 		return hash;
 	}
 	
 	/**
 	 * Constructs a string describing the time
	 * @return	A string in "yyyy/MM/dd//HH:mm" format.
 	 */
 	public String getTimeString() {
		return (String) new SimpleDateFormat("yyyy/MM/dd//HH:mm").format(time);
 	}
 }

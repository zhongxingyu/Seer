 package se.fpt.ft;
 
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import android.content.ContentValues;
 
 public class Ticket {
 	private final Random random = new Random();
 	
 	protected static String generateRandomAEOXStringBlock() {
 		String string = "";
 		final String[] letters = { "A", "E", "O", "X" };
 
 		for (int rows = 0; rows < 3; rows++) {
 			string += "E";
 			for (int i = 0; i < 9; i++)
 				string += letters[(int) Math.floor(Math.random()
 						* letters.length)];
 			string += "\n";
 		}
 		string += "EEEEEEEEEE";
 
 		return string;
 	}
 
 	protected final static ContentValues contentInboxValues = new ContentValues();
 	protected final static ContentValues contentOutboxValues = new ContentValues();
 	protected static String errorMessages = null;
 	protected static Set<String> mCurrentSettings = new HashSet<String>();
 	protected final Calendar timePiece = Calendar.getInstance();
 	protected final int numberTail = random.nextInt(900) + 100;
 	protected final String seed = String.valueOf(random.nextInt(999999))
 			+ String.valueOf(random.nextInt(999999))
 			+ String.valueOf(numberTail);
 	
 	public static ContentValues getContentInboxValues() {
 		return contentInboxValues;
 	}
 
 	public static ContentValues getContentOutboxValues() {
 		return contentOutboxValues;
 	}
 
 	public static String getError() {
		return errorMessages;
 	}
 
 	
 	public static void setCurrentSettings(Set<String> currentSettings) {
 		mCurrentSettings = currentSettings;
 	}
 
 }

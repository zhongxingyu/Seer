 /* memory
  * de.thm.ateam.memory.engine.type
  * DeckDB.java
  * 05.06.2012
  *
  * by Frank Kevin Zey
  */
 package de.thm.ateam.memory.engine.type;
 
 import android.content.ContentValues;
 
 /**
  * @author Frank Kevin Zey
  *
  */
 public class DeckDB {
 	
 	public static final String TABLE_NAME 		= "theme";
 	public static final String ID 				= "_id";
 	public static final String NAME 			= "_name";
 	
 	public static final String CARD_TABLE_NAME	= "card";
 	public static final String CARD_ID			= "_id";
 	public static final String CARD_DECK_ID		= "_deck_id";
 	public static final String CARD_BLOB		= "card_image";

	protected ContentValues createContentValuesTheme(Deck d) {
		
	}
 	
 	
 }

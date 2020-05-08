 package uw.cse403.minion;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Debug;
 
 /**
  * Ability is a class to represent a single ability on a character sheet 
  * which has a base value and temporary modifiers. This class handles both
  * the raw stat and the modifier from that stat.
  * 
  * @author Loki White (lokiw)
  * @author Preston Sahabu (sahabp) slight performance and consistency changes
  */
 public class Ability {
 	/** Class constants for string representations **/
 	static final String SAMPLE_MODIFIER = "sampleModifier";
 	private static final String BASE = "base=";
 	
 	/** Class constants for external use **/
 	public static final int STRENGTH_ID = 0;
 	public static final int DEXTERITY_ID = 1;
 	public static final int CONSTITUTION_ID = 2;
 	public static final int INTELLIGENCE_ID = 3;
 	public static final int WISDOM_ID = 4;
	public static final int CHARISMA_ID = 5;
 
 	/** The unique id for a character **/
 	public long charID;
 
 	/** The id used to reference which ability this object represents **/
 	public int abilityID;
 
 	/** Stores whether or not this ability has been stored previously or not **/
 	public boolean isNew;
 
 	/** The base ability score **/
 	public int base;
 
 	/** A collection of any temporary modifiers affecting this ability **/
 	public int tempMod;
 
 	/**
 	 * Initializes an ability with the given id and defaults the base stat to
 	 * -1. Stats cannot be negative as this is indicative of death or unconsciousness,
 	 * thus a default of -1 shows that this value is uninitialized.
 	 * <p>
 	 * No temporary modifiers are initialized.
 	 * 
 	 * @param id		character id associated with this ability
 	 * @param abilityID	ability id associated with this ability. A value between 0 and 5.
 	 */
 	public Ability(long id, int abilityID){
 		charID = id;
 		this.base = -1;
 		this.abilityID = abilityID;
 		
 		this.tempMod = 0;
 
 		if (charID >= 0) {
 			loadFromDB();
 		}
 	}
 
 
 	/*
 	 * Testing Results:
 	 * The most computationally expensive elements of this section involved string building,
 	 * with 5.3% of the CPU time being spent in AbstractStringBuilder alone. However, when looking
 	 * at the Inclusive instead of the Exclusive CPU Time data, it is clear that much of the string
 	 * manipulation is occurring in SQLite services and not our app. This is unavoidable to our
 	 * application's operation, and cannot be optimized.
 	 */
 	/**
 	 * Loads all of the abilities stored in the local database for the current character.
 	 */
 	private void loadFromDB() {
 		if (TraceControl.TRACE)
 			Debug.startMethodTracing("Ability_loadAbilities");
 		
 		isNew = true;
 		// attempt to load from DB
 		Cursor cursor = SQLiteHelperAbilityScores.db.query(SQLiteHelperAbilityScores.TABLE_NAME, 
 				SQLiteHelperAbilityScores.ALL_COLUMNS, SQLiteHelperAbilityScores.COLUMN_CHAR_ID 
 				+ " = " + charID + " AND " + SQLiteHelperAbilityScores.COLUMN_REF_AS_ID + " = "
 				+ abilityID, null, null, null, null);
 		System.out.println("Querying charID=" + charID + " abilityID=" + abilityID);
 		if (cursor.moveToFirst()) {
 			isNew = false;
 			// COLUMN_CHAR_ID, COLUMN_REF_AS_ID, COLUMN_BASE, COLUMN_TEMP
 			base = cursor.getInt(2);
 			tempMod = cursor.getInt(3);
 			System.out.println(BASE + base);
 		}
 		cursor.close();
 		
 		if (TraceControl.TRACE)
 			Debug.stopMethodTracing();
 	}
 
 	/**
 	 * Get the current base stat for the ability, not including any temporary
 	 * modifiers to the base stat
 	 * 
 	 * @return an int ability score with no temp modifiers
 	 */
 	public int getBase(){
 		return base;
 	}
 
 	/**
 	 * Sets base stat to the given value permanently
 	 * 
 	 * @param newBase	an int whose value set over the old base stat
 	 * @modifies this
 	 */
 	public void setBase(int newBase){
 		base = newBase;
 		if (base < 0) {
 			base = 0;
 		}
 	}
 
 	/**
 	 * Gets the total ability score including the base stat value and all
 	 * temporary modifiers currently stored.
 	 * <p>
 	 * The current ability score is the base score with additional temporary 
 	 * modifiers. Modifiers are added or subtracted depending on their sign and
 	 * they affect the score directly. Thus a base score of 10 with a +2 modifier 
 	 * from an amulet will return a score of 12.
 	 * 
 	 * @return an int score representing the total ability score
 	 */
 	public int getScore(){
 		int score = base;
 		score += tempMod;
 		if (score < 0) {
 			return 0;
 		} else {
 			return score;
 		}
 	}
 
 	/**
 	 * Gets the modifier of the ability score with all base and temporary values
 	 * considered.
 	 * <p>
 	 * The ability modifier is the information most used from a character's stats,
 	 * and is calculated from their ability score. This score includes the base
 	 * and all temporary modifiers. The modifier is always smaller in magnitude then
 	 * the raw score as it is added to a dice roll and is generally calculated and 
 	 * recorded. Every two ability scores has a single modifier associated with it.
 	 * The ability score 10 and 11 have a modifier of 0, every even number thereafter
 	 * adds one modifier, so 12 and 13 are 1, 14 and 15 are 2 and so on. Once a score
 	 * is below 10, the modifiers are negative. A score of 8 and 9 have a -1 modifier,
 	 * every two bellow that get another -1, so 7 and 6 have a -2 and so on.
 	 * 
 	 * @return an int representing the ability modifier 
 	 * 
 	 */
 	public int getMod(){
 		int mod;
 		int score = getScore();
 		if (score >= 10) {
 			mod = (score - 10) / 2;
 		} else {
 			mod = (score - 11) / 2;
 		}
 
 		return mod;
 	}
 
 	/** 
 	 * Writes Ability to database. SHOULD ONLY BE CALLED BY CHARACTER
 	 * @param id id of character
 	 * @param db database to write into (except temporary mods)
 	 * @param dbTempMods database to write temporary mods into
 	 */
 	public void writeToDB() {
 		SQLiteDatabase db = SQLiteHelperAbilityScores.db;
 
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelperAbilityScores.COLUMN_CHAR_ID, charID);
 		values.put(SQLiteHelperAbilityScores.COLUMN_REF_AS_ID, abilityID);
 		values.put(SQLiteHelperAbilityScores.COLUMN_BASE, base);
 		values.put(SQLiteHelperAbilityScores.COLUMN_TEMP, tempMod);
 		if (isNew) {
 			db.insert(SQLiteHelperAbilityScores.TABLE_NAME, null, values);
 		} else {
 			db.update(SQLiteHelperAbilityScores.TABLE_NAME, values, SQLiteHelperAbilityScores.COLUMN_CHAR_ID + " = " + charID 
 					+ " AND " + SQLiteHelperAbilityScores.COLUMN_REF_AS_ID + " = " + abilityID, null);
 		}
 
 		// for later implementation
 		@SuppressWarnings("unused")
 		SQLiteDatabase dbTempMods = SQLiteHelperASTempMods.db;
 	}
 
 }

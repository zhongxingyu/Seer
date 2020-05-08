 package uw.cse403.minion;
 
 import android.database.Cursor;
 
 public class SkillsAll {
 	private long charID;
 	/** Stores whether or not this combat information has been stored previously or not **/
 	public boolean isNew; 
 	
 	// map from skillID to Skill object
 //	private Map<Integer, Skill> allSkills;
 	private Skill[] skills;
 	
 	/**
 	 * Constructs new SKillsAll object which contains all Skills
 	 * @param charID id of character
 	 */
 	public SkillsAll(long charID){
 		this.charID = charID;
 //		this.allSkills = new HashMap<Integer, Skill>();
 		skills = new Skill[Skill.NUM_SKILLS + 1];
 		// skills are 1-indexed
 		
 		if (charID >= 0) {
 			loadFromDB();
 		}
 	}
 	
 	/**
 	 * Returns Skill object corresponding to the skill ID number
 	 * @param skillID id of skill
 	 * @return Skill object with skillID
 	 */
 	public Skill getSkill(int skillID) {
 //		return allSkills.get(skillID);
 		return skills[skillID];
 	}
 	
 	/**
 	 * Adds Skill to this object. Will replace any Skill with the same skillID
 	 * @param skill Skill object to add
 	 */
 	public void addSkill(Skill skill) {
 //		allSkills.put(skill.skillID, skill);
 		skills[skill.skillID] = skill; 
 	}
 	
 	/** 
 	 * Clears this SkillsAll object
 	 */
 	public void clear() {
 //		allSkills.clear();
 		for (int i = 0; i < skills.length; i++) {
 			skills[i] = null;
 		}
 	}
 	
 	/**
 	 * Loads Skills from local database
 	 */
 	private void loadFromDB() {
 		isNew = true;
 		// get ability scores
 		Ability[] abilities = new Ability[6];
 		// these should auto load from DB
 		abilities[0] = new Ability(charID, Ability.STRENGTH_ID);
 		abilities[1] = new Ability(charID, Ability.DEXTERITY_ID);
 		abilities[2] = new Ability(charID, Ability.CONSTITUTION_ID);
 		abilities[3] = new Ability(charID, Ability.INTELLIGENCE_ID);
 		abilities[4] = new Ability(charID, Ability.WISDOM_ID);
 		abilities[5] = new Ability(charID, Ability.CHARISMA_ID);
 		
 		// map from skillID to ability score modifier
 //		Map<Integer, Integer> skillToAbMod = new HashMap<Integer, Integer>();
		int[] skillToAbMods = new int[Skill.NUM_SKILLS + 1];
 		// get skillIDs and associated ability score IDs
 		Cursor cursor = SQLiteHelperRefTables.db.query(SQLiteHelperRefTables.TABLE_REF_SKILLS, 
 				null, null, null, null, null, null);
 		// Columns: COLUMN_S_ID, COLUMN_S_NAME, COLUMN_S_REF_AS_ID
 		if (cursor.moveToFirst()) {
 			while (!cursor.isAfterLast()) {
 				int abScoreID = cursor.getInt(2);
 //				skillToAbMod.put(cursor.getInt(0), abilities[abScoreID].getMod());
 				skillToAbMods[cursor.getInt(0)] = abilities[abScoreID].getMod();
 				cursor.moveToNext();
 			}
 		}
 		cursor.close();
 		
 		// get skills from DB
 		Cursor cursor2 = SQLiteHelperSkills.db.query(SQLiteHelperSkills.TABLE_NAME, SQLiteHelperSkills.ALL_COLUMNS, 
 				SQLiteHelperSkills.COLUMN_CHAR_ID + " = " + charID, null, null, null, null);
 		if (cursor2.moveToFirst()) {
 			isNew = false;
 			while (!cursor2.isAfterLast()) { 
 				// Columns: COLUMN_CHAR_ID, COLUMN_REF_S_ID, COLUMN_TITLE, COLUMN_RANKS, COLUMN_MISC_MOD
 				int skillID = cursor2.getInt(1);
 				Skill skill = new Skill(charID, skillID);
 				skill.title = cursor2.getString(2);
 				skill.rank = cursor2.getInt(3);
 				skill.miscMod = cursor2.getInt(4);
 //				skill.abMod = skillToAbMod.get(skillID);
 				skill.abMod = skillToAbMods[skillID];
 				
 				addSkill(skill);
 				cursor2.moveToNext();
 			}
 		}		
 		cursor2.close();
 	}
 	
 	/** 
 	 * Writes all Skills to database.
 	 */
 	public void writeToDB() {
 		// clear old data from DB
 		SQLiteHelperSkills.db.delete(SQLiteHelperSkills.TABLE_NAME,
 				SQLiteHelperSkills.COLUMN_CHAR_ID + " = " + charID, null);
 		// write new data to DB
 //		for (int skillID : allSkills.keySet()) {
 //			Skill skill = allSkills.get(skillID);
 //			skill.writeToDB();
 //		}
		for (int i = 1; i < skills.length; i++) {
			skills[i].writeToDB();
 		}
 	}
 }

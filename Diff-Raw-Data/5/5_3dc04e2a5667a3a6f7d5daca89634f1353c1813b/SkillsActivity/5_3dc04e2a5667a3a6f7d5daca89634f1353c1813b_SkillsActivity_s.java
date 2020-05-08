 package uw.cse403.minion;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Build;
 
 public class SkillsActivity extends Activity {
 	private long charID;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_skills);
 		charID = this.getIntent().getExtras().getLong("cid");
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 		loadData();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.skills, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void loadData() {
 		// prefixes for the xml fields
 		String[] xmlField = { "", "acrobatics", "appraise", "bluff", "climb", "craft", "diplomacy", 
 			"disable_device", "disguise", "escape_artist", "fly", "handle_animal", "heal", "intimidate",
 			"knowledge_arcana", "knowledge_dungeoneering", "knowledge_engineering", "knowledge_geography",
 			"knowledge_history", "knowledge_local", "knowledge_nature", "knowledge_nobility",
 			"knowledge_planes", "knowledge_religion", "linguistics", "perception", "perform", "profession",
 			"ride", "sense_motive", "sleight_of_hand", "spellcraft", "stealth", "survival", "swim", "use_magic_device" };
 
 		int crafts = 0;
 		int performs = 0;
 		int professions = 0;
 
 		Cursor cursor = SQLiteHelperSkills.db.query(SQLiteHelperSkills.TABLE_NAME, SQLiteHelperSkills.ALL_COLUMNS, 
 				SQLiteHelperSkills.COLUMN_CHAR_ID + " = " + charID, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			while (!cursor.isAfterLast()) { 
 				// Columns: COLUMN_CHAR_ID, COLUMN_REF_S_ID, COLUMN_RANKS, COLUMN_MISC_MOD
 				int skillID = cursor.getInt(1);
 				String title = cursor.getString(2);
 				int ranks = cursor.getInt(3);
 				int miscMod = cursor.getInt(4);
 //				String ranksEnter;
 //				String modEnter;
 //				if (skillID == 5)  { // craft
 //					crafts++;
 //					ranksEnter = xmlField[5] + crafts + "_ranks";
 //					modEnter = xmlField[5] + crafts + "_misc_mod";
 //				} else if (skillID == 26)  { // perform
 //					performs++;
 //					ranksEnter = xmlField[26] + performs + "_ranks";
 //					modEnter = xmlField[26] + performs + "_misc_mod";
 //				} else if (skillID == 27)  { // profession
 //					professions++;
 //					ranksEnter = xmlField[27] + professions + "_ranks";
 //					modEnter = xmlField[27] + professions + "_misc_mod";
 //				} else {
 //					ranksEnter = xmlField[skillID] + "_ranks";
 //					modEnter = xmlField[skillID] + "_misc_mod";
 //				}
 //				EditText ranksEnterField = (EditText) findViewById(R.id.acrobatics_ranks);
 //				ranksEnterField.setText(""+ranks);
 //				EditText modEnterField = (EditText) findViewById(R.id.acrobatics_misc_mod);
 //				modEnterField.setText(""+miscMod);
 				
 				int titleFieldID = 0;
 				int ranksFieldID = 0;
 				int modsFieldID = 0;
 				switch (skillID) {
 				case 1: 
 					ranksFieldID = R.id.acrobatics_ranks;
 					modsFieldID = R.id.acrobatics_misc_mod; break;
 				case 2: 
 					ranksFieldID = R.id.appraise_ranks;
 					modsFieldID = R.id.appraise_misc_mod; break;
 				case 3: 
 					ranksFieldID = R.id.bluff_ranks;
 					modsFieldID = R.id.bluff_misc_mod; break;
 				case 4: 
 					ranksFieldID = R.id.climb_ranks;
 					modsFieldID = R.id.climb_misc_mod; break;
 				case 5: 
 					//if (crafts < 3)
 						crafts++;
 					if (crafts == 1) {
 						titleFieldID = R.id.craft1_enter;
 						ranksFieldID = R.id.craft1_ranks;
 						modsFieldID = R.id.craft1_misc_mod;
 					} else if (crafts == 2) {
 						titleFieldID = R.id.craft2_enter;
 						ranksFieldID = R.id.craft2_ranks;
 						modsFieldID = R.id.craft2_misc_mod;
 					} else if (crafts == 3) {
 						titleFieldID = R.id.craft3_enter;
 						ranksFieldID = R.id.craft3_ranks;
 						modsFieldID = R.id.craft3_misc_mod;
 					} break;
 				case 6: 
 					ranksFieldID = R.id.diplomacy_ranks;
 					modsFieldID = R.id.diplomacy_misc_mod; break;
 				case 7: 
 					ranksFieldID = R.id.disable_device_ranks;
 					modsFieldID = R.id.disable_device_misc_mod; break;
 				case 8: 
 					ranksFieldID = R.id.disguise_ranks;
 					modsFieldID = R.id.disguise_misc_mod; break;
 				case 9: 
 					ranksFieldID = R.id.escape_artist_ranks;
 					modsFieldID = R.id.escape_artist_misc_mod; break;
 				case 10: 
 					ranksFieldID = R.id.fly_ranks;
 					modsFieldID = R.id.fly_misc_mod; break;
 				case 11: 
 					ranksFieldID = R.id.handle_animal_ranks;
 					modsFieldID = R.id.handle_animal_misc_mod; break;
 				case 12: 
 					ranksFieldID = R.id.heal_ranks;
 					modsFieldID = R.id.heal_misc_mod; break;
 				case 13: 
 					ranksFieldID = R.id.intimidate_ranks;
 					modsFieldID = R.id.intimidate_misc_mod; break;
 				case 14: 
 					ranksFieldID = R.id.knowledge_arcana_ranks;
 					modsFieldID = R.id.knowledge_arcana_misc_mod; break;
 				case 15: 
 					ranksFieldID = R.id.knowledge_dungeoneering_ranks;
 					modsFieldID = R.id.knowledge_dungeoneering_misc_mod; break;
 				case 16: 
 					ranksFieldID = R.id.knowledge_engineering_ranks;
 					modsFieldID = R.id.knowledge_engineering_misc_mod; break;
 				case 17: 
 					ranksFieldID = R.id.knowledge_geography_ranks;
 					modsFieldID = R.id.knowledge_geography_misc_mod; break;
 				case 18: 
 					ranksFieldID = R.id.knowledge_history_ranks;
 					modsFieldID = R.id.knowledge_history_misc_mod; break;
 				case 19: 
 					ranksFieldID = R.id.knowledge_local_ranks;
 					modsFieldID = R.id.knowledge_local_misc_mod; break;
 				case 20: 
 					ranksFieldID = R.id.knowledge_nature_ranks;
 					modsFieldID = R.id.knowledge_nature_misc_mod; break;
 				case 21: 
 					ranksFieldID = R.id.knowledge_nobility_ranks;
 					modsFieldID = R.id.knowledge_nobility_misc_mod; break;
 				case 22: 
 					ranksFieldID = R.id.knowledge_planes_ranks;
 					modsFieldID = R.id.knowledge_planes_misc_mod; break;
 				case 23: 
 					ranksFieldID = R.id.knowledge_religion_ranks;
 					modsFieldID = R.id.knowledge_religion_misc_mod; break;
 				case 24: 
 					ranksFieldID = R.id.linguistics_ranks;
 					modsFieldID = R.id.linguistics_misc_mod; break;
 				case 25: 
 					ranksFieldID = R.id.perception_ranks;
 					modsFieldID = R.id.perception_misc_mod; break;
 				case 26: 
 					//if (performs < 2)
 						performs++;
 					if (performs == 1) {
 						titleFieldID = R.id.perform1_enter;
 						ranksFieldID = R.id.perform1_ranks;
 						modsFieldID = R.id.perform1_misc_mod;
 					} else if (performs == 2) {
 						titleFieldID = R.id.perform2_enter;
 						ranksFieldID = R.id.perform2_ranks;
 						modsFieldID = R.id.perform2_misc_mod;
 					} break;
 				case 27: 
 					//if (professions < 2)
 						professions++;
 					if (professions == 1) {
 						titleFieldID = R.id.profession1_enter;
 						ranksFieldID = R.id.profession1_ranks;
 						modsFieldID = R.id.profession1_misc_mod;
 					} else if (professions == 2) {
 						titleFieldID = R.id.profession2_enter;
 						ranksFieldID = R.id.profession2_ranks;
 						modsFieldID = R.id.profession2_misc_mod;
 					} break;
 				case 28: 
 					ranksFieldID = R.id.ride_ranks;
 					modsFieldID = R.id.ride_misc_mod; break;
 				case 29: 
 					ranksFieldID = R.id.sense_motive_ranks;
 					modsFieldID = R.id.sense_motive_misc_mod; break;
 				case 30: 
 					ranksFieldID = R.id.sleight_of_hand_ranks;
 					modsFieldID = R.id.sleight_of_hand_misc_mod; break;
 				case 31: 
 					ranksFieldID = R.id.spellcraft_ranks;
 					modsFieldID = R.id.spellcraft_misc_mod; break;
 				case 32: 
 					ranksFieldID = R.id.stealth_ranks;
 					modsFieldID = R.id.stealth_misc_mod; break;
 				case 33: 
 					ranksFieldID = R.id.survival_ranks;
 					modsFieldID = R.id.survival_misc_mod; break;
 				case 34: 
 					ranksFieldID = R.id.swim_ranks;
 					modsFieldID = R.id.swim_misc_mod; break;
 				case 35: 
 					ranksFieldID = R.id.use_magic_device_ranks;
 					modsFieldID = R.id.use_magic_device_misc_mod; break;
 				}
 				if (skillID == 5 || skillID == 26 || skillID == 27) {
 					EditText titleEnterField = (EditText) findViewById(titleFieldID);
 					titleEnterField.setText(title);
 				}
 				EditText ranksEnterField = (EditText) findViewById(ranksFieldID);
 				ranksEnterField.setText(""+ranks);
 				EditText modEnterField = (EditText) findViewById(modsFieldID);
 				modEnterField.setText(""+miscMod);
 
 				cursor.moveToNext();
 			}
 		}
 		cursor.close();
 	}
 	
 	// helper class for storing a string and int together
 	// mostly for making the following code readable
 	private class StrInt {
 		int i;
 		String s;
 		public StrInt(String s, int i) {
 			this.i = i;
 			this.s = s;
 		}
 	}
 	/**
 	 * Called when Done button is clicked.
 	 */
 	public void skills(View view) {
 		// TODO write method
 		//Skill sk = new Skill(null, null);
 		ArrayList<Skill> skills = new ArrayList<Skill>();
 		
 // this segment of code is for factoring the code so that it's not so redundant
 // however the priority is getting this to work, so we can finish this later
 //		// load skills from DB
 //		Cursor cursor = SQLiteHelperRefTables.db.query(SQLiteHelperRefTables.TABLE_REF_SKILLS, 
 //				SQLiteHelperRefTables.ALL_COLUMNS_S, null, null, null, null, null);
 //		Map<Integer, StrInt> skillsRef = new HashMap<Integer, StrInt>(); 
 //		// ^ this is essentially a map from skill id to skill name and ability score id
 //		if (cursor.moveToFirst()) {
 //			// Columns: COLUMN_S_ID, COLUMN_S_NAME, COLUMN_S_REF_AS_ID
 //			int id 		= cursor.getInt(0);
 //			String name = cursor.getString(1);
 //			int asID 	= cursor.getInt(2);
 //			skillsRef.put(id, new StrInt(name, asID));
 //		}
 //		cursor.close();
 		
 		// Acrobatics
 		EditText acrobaticsRanksEnter = (EditText) findViewById(R.id.acrobatics_ranks);
 		EditText acrobaticsMiscEnter = (EditText) findViewById(R.id.acrobatics_misc_mod);
 		String acrobaticsRanks = acrobaticsRanksEnter.getText().toString().trim();
 		String acrobaticsMisc = acrobaticsMiscEnter.getText().toString().trim();
 		if (!acrobaticsRanks.matches("")) {
 			int acrobaticsRank = Integer.parseInt(acrobaticsRanks);
 			Skill skill = new Skill(1, "Acrobatics", AbilityName.DEXTERITY, acrobaticsRank, false);
 			if (!acrobaticsMisc.matches("")) {
 				int acrobaticsMod = Integer.parseInt(acrobaticsMisc);
 				skill.addModifier("acrobaticsMod", acrobaticsMod);
 			}
 			skills.add(skill);
 		}
 
 		// Appraise
 		EditText appraiseRanksEnter = (EditText) findViewById(R.id.appraise_ranks);
 		EditText appraiseMiscEnter = (EditText) findViewById(R.id.appraise_misc_mod);
 		String appraiseRanks = appraiseRanksEnter.getText().toString().trim();
 		String appraiseMisc = appraiseMiscEnter.getText().toString().trim();
 		if (!appraiseRanks.matches("")) {
 			int appraiseRank = Integer.parseInt(appraiseRanks);
 			Skill skill = new Skill(2, "Appraise", AbilityName.INTELLIGENCE, appraiseRank, false);
 			if (!appraiseMisc.matches("")) {
 				int appraiseMod = Integer.parseInt(appraiseMisc);
 				skill.addModifier("appraiseMod", appraiseMod);
 			}
 			skills.add(skill);
 		}
 
 		// Bluff
 		EditText bluffRanksEnter = (EditText) findViewById(R.id.bluff_ranks);
 		EditText bluffMiscEnter = (EditText) findViewById(R.id.bluff_misc_mod);
 		String bluffRanks = bluffRanksEnter.getText().toString().trim();
 		String bluffMisc = bluffMiscEnter.getText().toString().trim();
 		if (!bluffRanks.matches("")) {
 			int bluffRank = Integer.parseInt(bluffRanks);
 			Skill skill = new Skill(3, "Bluff", AbilityName.CHARISMA, bluffRank, false);
 			if (!bluffMisc.matches("")) {
 				int bluffMod = Integer.parseInt(bluffMisc);
 				skill.addModifier("bluffMod", bluffMod);
 			}
 			skills.add(skill);
 		}
 
 		// Climb
 		EditText climbRanksEnter = (EditText) findViewById(R.id.climb_ranks);
 		EditText climbMiscEnter = (EditText) findViewById(R.id.climb_misc_mod);
 		String climbRanks = climbRanksEnter.getText().toString().trim();
 		String climbMisc = climbMiscEnter.getText().toString().trim();
 		if (!climbRanks.matches("")) {
 			int climbRank = Integer.parseInt(climbRanks);
 			Skill skill = new Skill(4, "Climb", AbilityName.STRENGTH, climbRank, false);
 			if (!climbMisc.matches("")) {
 				int climbMod = Integer.parseInt(climbMisc);
 				skill.addModifier("climbMod", climbMod);
 			}
 			skills.add(skill);
 		}
 
 		// Craft1
 		EditText craft1TitleEnter = (EditText) findViewById(R.id.craft1_enter);
 		EditText craft1RanksEnter = (EditText) findViewById(R.id.craft1_ranks);
 		EditText craft1MiscEnter = (EditText) findViewById(R.id.craft1_misc_mod);
 		String craft1Title = craft1TitleEnter.getText().toString().trim();
 		String craft1Ranks = craft1RanksEnter.getText().toString().trim();
 		String craft1Misc = craft1MiscEnter.getText().toString().trim();
 		if (!craft1Title.matches("") && !craft1Ranks.matches("")) {
 			int craft1Rank = Integer.parseInt(craft1Ranks);
 			Skill skill = new Skill(5, "Craft1", craft1Title, AbilityName.INTELLIGENCE, craft1Rank, false);
 			if (!craft1Misc.matches("")) {
 				int craft1Mod = Integer.parseInt(craft1Misc);
 				skill.addModifier("craft1Mod", craft1Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Craft2
 		EditText craft2TitleEnter = (EditText) findViewById(R.id.craft2_enter);
 		EditText craft2RanksEnter = (EditText) findViewById(R.id.craft2_ranks);
 		EditText craft2MiscEnter = (EditText) findViewById(R.id.craft2_misc_mod);
 		String craft2Title = craft2TitleEnter.getText().toString().trim();
 		String craft2Ranks = craft2RanksEnter.getText().toString().trim();
 		String craft2Misc = craft2MiscEnter.getText().toString().trim();
 		if (!craft2Title.matches("") && !craft2Ranks.matches("")) {
 			int craft2Rank = Integer.parseInt(craft2Ranks);
 			Skill skill = new Skill(5, "Craft2", craft2Title, AbilityName.INTELLIGENCE, craft2Rank, false);
 			if (!craft2Misc.matches("")) {
 				int craft2Mod = Integer.parseInt(craft2Misc);
 				skill.addModifier("craft2Mod", craft2Mod);
 			}
 			skills.add(skill);
 		}
 
 
 		// Craft3
 		EditText craft3TitleEnter = (EditText) findViewById(R.id.craft3_enter);
 		EditText craft3RanksEnter = (EditText) findViewById(R.id.craft3_ranks);
 		EditText craft3MiscEnter = (EditText) findViewById(R.id.craft3_misc_mod);
 		String craft3Title = craft3TitleEnter.getText().toString().trim();
 		String craft3Ranks = craft3RanksEnter.getText().toString().trim();
 		String craft3Misc = craft3MiscEnter.getText().toString().trim();
 		if (!craft3Title.matches("") && !craft3Ranks.matches("")) {
 			int craft3Rank = Integer.parseInt(craft3Ranks);
 			Skill skill = new Skill(5, "Craft3", craft3Title, AbilityName.INTELLIGENCE, craft3Rank, false);
 			if (!craft3Misc.matches("")) {
 				int craft3Mod = Integer.parseInt(craft3Misc);
 				skill.addModifier("craft3Mod", craft3Mod);
 			}
 			skills.add(skill);
 		}
 
 
 		// Diplomacy
 		EditText diplomacyRanksEnter = (EditText) findViewById(R.id.diplomacy_ranks);
 		EditText diplomacyMiscEnter = (EditText) findViewById(R.id.diplomacy_misc_mod);
 		String diplomacyRanks = diplomacyRanksEnter.getText().toString().trim();
 		String diplomacyMisc = diplomacyMiscEnter.getText().toString().trim();
 		if (!diplomacyRanks.matches("")) {
 			int diplomacyRank = Integer.parseInt(diplomacyRanks);
 			Skill skill = new Skill(6, "Diplomacy", AbilityName.CHARISMA, diplomacyRank, false);
 			if (!diplomacyMisc.matches("")) {
 				int diplomacyMod = Integer.parseInt(diplomacyMisc);
 				skill.addModifier("diplomacyMod", diplomacyMod);
 			}
 			skills.add(skill);
 		}
 
 		// Disable Device
 		EditText disableDeviceRanksEnter = (EditText) findViewById(R.id.disable_device_ranks);
 		EditText disableDeviceMiscEnter = (EditText) findViewById(R.id.disable_device_misc_mod);
 		String disableDeviceRanks = disableDeviceRanksEnter.getText().toString().trim();
 		String disableDeviceMisc = disableDeviceMiscEnter.getText().toString().trim();
 		if (!disableDeviceRanks.matches("")) {
 			int disableDeviceRank = Integer.parseInt(disableDeviceRanks);
 			Skill skill = new Skill(7, "Disable Device", AbilityName.DEXTERITY, disableDeviceRank, false);
 			if (!disableDeviceRanks.matches("")) {
 				int disableDeviceMod = Integer.parseInt(disableDeviceMisc);
 				skill.addModifier("disableDeviceMod", disableDeviceMod);
 			}
 			skills.add(skill);
 		}
 
 		// Disguise
 		EditText disguiseRanksEnter = (EditText) findViewById(R.id.disguise_ranks);
 		EditText disguiseMiscEnter = (EditText) findViewById(R.id.disguise_misc_mod);
 		String disguiseRanks = disguiseRanksEnter.getText().toString().trim();
 		String disguiseMisc = disguiseMiscEnter.getText().toString().trim();
 		if (!disguiseRanks.matches("")) {
 			int disguiseRank = Integer.parseInt(disguiseRanks);
 			Skill skill = new Skill(8, "Disguise", AbilityName.CHARISMA, disguiseRank, false);
 			if (!disguiseMisc.matches("")) {
 				int disguiseMod = Integer.parseInt(disguiseMisc);
 				skill.addModifier("disguiseMod", disguiseMod);
 			}
 			skills.add(skill);
 		}
 
 		// Escape Artist
 		EditText escapeArtistRanksEnter = (EditText) findViewById(R.id.escape_artist_ranks);
 		EditText escapeArtistMiscEnter = (EditText) findViewById(R.id.escape_artist_misc_mod);
 		String escapeArtistRanks = escapeArtistRanksEnter.getText().toString().trim();
 		String escapeArtistMisc = escapeArtistMiscEnter.getText().toString().trim();
 		if (!escapeArtistRanks.matches("")) {
 			int escapeArtistRank = Integer.parseInt(escapeArtistRanks);
 			Skill skill = new Skill(9, "Escape Artist", AbilityName.DEXTERITY, escapeArtistRank, false);
 			if (!escapeArtistMisc.matches("")) {
 				int escapeArtistMod = Integer.parseInt(escapeArtistMisc);
 				skill.addModifier("escapeArtistMod", escapeArtistMod);
 			}
 			skills.add(skill);
 		}
 
 		// Fly
 		EditText flyRanksEnter = (EditText) findViewById(R.id.fly_ranks);
 		EditText flyMiscEnter = (EditText) findViewById(R.id.fly_misc_mod);
 		String flyRanks = flyRanksEnter.getText().toString().trim();
 		String flyMisc = flyMiscEnter.getText().toString().trim();
 		if (!flyRanks.matches("")) {
 			int flyRank = Integer.parseInt(flyRanks);
 			Skill skill = new Skill(10, "Fly", AbilityName.DEXTERITY, flyRank, false);
 			if (!flyMisc.matches("")) {
 				int flyMod = Integer.parseInt(flyMisc);
 				skill.addModifier("flyMod", flyMod);
 			}
 			skills.add(skill);
 		}
 
 		// Handle Animal
 		EditText handleAnimalRanksEnter = (EditText) findViewById(R.id.handle_animal_ranks);
 		EditText handleAnimalMiscEnter = (EditText) findViewById(R.id.handle_animal_misc_mod);
 		String handleAnimalRanks = handleAnimalRanksEnter.getText().toString().trim();
 		String handleAnimalMisc = handleAnimalMiscEnter.getText().toString().trim();
 		if (!handleAnimalRanks.matches("")) {
 			int handleAnimalRank = Integer.parseInt(handleAnimalRanks);
 			Skill skill = new Skill(11, "Handle Animal", AbilityName.CHARISMA, handleAnimalRank, false);
 			if (!handleAnimalMisc.matches("")) {
 				int handleAnimalMod = Integer.parseInt(handleAnimalMisc);
 				skill.addModifier("handleAnimalMod", handleAnimalMod);
 			}
 			skills.add(skill);
 		}
 
 		// Heal
 		EditText healRanksEnter = (EditText) findViewById(R.id.heal_ranks);
 		EditText healMiscEnter = (EditText) findViewById(R.id.heal_misc_mod);
 		String healRanks = healRanksEnter.getText().toString().trim();
 		String healMisc = healMiscEnter.getText().toString().trim();
 		if (!healRanks.matches("")) {
 			int healRank = Integer.parseInt(healRanks);
 			Skill skill = new Skill(12, "Heal", AbilityName.WISDOM, healRank, false);
 			if (!healMisc.matches("")) {
 				int healMod = Integer.parseInt(healMisc);
 				skill.addModifier("healMod", healMod);
 			}
 			skills.add(skill);
 		}
 
 		// Intimidate
 		EditText intimidateRanksEnter = (EditText) findViewById(R.id.intimidate_ranks);
 		EditText intimidateMiscEnter = (EditText) findViewById(R.id.intimidate_misc_mod);
 		String intimidateRanks = intimidateRanksEnter.getText().toString().trim();
 		String intimidateMisc = intimidateMiscEnter.getText().toString().trim();
 		if (!intimidateRanks.matches("")) {
 			int intimidateRank = Integer.parseInt(intimidateRanks);
 			Skill skill = new Skill(13, "Intimidate", AbilityName.CHARISMA, intimidateRank, false);
 			if (!intimidateMisc.matches("")) {
 				int intimidateMod = Integer.parseInt(intimidateMisc);
 				skill.addModifier("intimidateMod", intimidateMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Arcana)
 		EditText knowledgeArcanaRanksEnter = (EditText) findViewById(R.id.knowledge_arcana_ranks);
 		EditText knowledgeArcanaMiscEnter = (EditText) findViewById(R.id.knowledge_arcana_misc_mod);
 		String knowledgeArcanaRanks = knowledgeArcanaRanksEnter.getText().toString().trim();
 		String knowledgeArcanaMisc = knowledgeArcanaMiscEnter.getText().toString().trim();
 		if (!knowledgeArcanaRanks.matches("")) {
 			int knowledgeArcanaRank = Integer.parseInt(knowledgeArcanaRanks);
 			Skill skill = new Skill(14, "Knowledge (Arcana)", AbilityName.INTELLIGENCE, knowledgeArcanaRank, false);
 			if (!knowledgeArcanaMisc.matches("")) {
 				int knowledgeArcanaMod = Integer.parseInt(knowledgeArcanaMisc);
 				skill.addModifier("knowledgeArcanaMod", knowledgeArcanaMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Dungeoneering)
 		EditText knowledgeDungeoneeringRanksEnter = (EditText) findViewById(R.id.knowledge_dungeoneering_ranks);
 		EditText knowledgeDungeoneeringMiscEnter = (EditText) findViewById(R.id.knowledge_dungeoneering_misc_mod);
 		String knowledgeDungeoneeringRanks = knowledgeDungeoneeringRanksEnter.getText().toString().trim();
 		String knowledgeDungeoneeringMisc = knowledgeDungeoneeringMiscEnter.getText().toString().trim();
 		if (!knowledgeDungeoneeringRanks.matches("")) {
 			int knowledgeDungeoneeringRank = Integer.parseInt(knowledgeDungeoneeringRanks);
 			Skill skill = new Skill(15, "Knowledge (Dungeoneering)", AbilityName.INTELLIGENCE, knowledgeDungeoneeringRank, false);
 			if (!knowledgeDungeoneeringMisc.matches("")) {
 				int knowledgeDungeoneeringMod = Integer.parseInt(knowledgeDungeoneeringMisc);
 				skill.addModifier("knowledgeDungeoneeringMod", knowledgeDungeoneeringMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Engineering)
 		EditText knowledgeEngineeringRanksEnter = (EditText) findViewById(R.id.knowledge_engineering_ranks);
 		EditText knowledgeEngineeringMiscEnter = (EditText) findViewById(R.id.knowledge_engineering_misc_mod);
 		String knowledgeEngineeringRanks = knowledgeEngineeringRanksEnter.getText().toString().trim();
 		String knowledgeEngineeringMisc = knowledgeEngineeringMiscEnter.getText().toString().trim();
 		if (!knowledgeEngineeringRanks.matches("")) {
 			int knowledgeEngineeringRank = Integer.parseInt(knowledgeEngineeringRanks);
 			Skill skill = new Skill(16, "Knowledge (Engineering)", AbilityName.INTELLIGENCE, knowledgeEngineeringRank, false);
 			if (!knowledgeEngineeringMisc.matches("")) {
 				int knowledgeEngineeringMod = Integer.parseInt(knowledgeEngineeringMisc);
 				skill.addModifier("knowledgeEngineeringMod", knowledgeEngineeringMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Geography)
 		EditText knowledgeGeographyRanksEnter = (EditText) findViewById(R.id.knowledge_geography_ranks);
 		EditText knowledgeGeographyMiscEnter = (EditText) findViewById(R.id.knowledge_geography_misc_mod);
 		String knowledgeGeographyRanks = knowledgeGeographyRanksEnter.getText().toString().trim();
 		String knowledgeGeographyMisc = knowledgeGeographyMiscEnter.getText().toString().trim();
 		if (!knowledgeGeographyRanks.matches("")) {
 			int knowledgeGeographyRank = Integer.parseInt(knowledgeGeographyRanks);
 			Skill skill = new Skill(17, "Knowledge (Geography)", AbilityName.INTELLIGENCE, knowledgeGeographyRank, false);
 			if (!knowledgeGeographyMisc.matches("")) {
 				int knowledgeGeographyMod = Integer.parseInt(knowledgeGeographyMisc);
 				skill.addModifier("knowledgeGeographyMod", knowledgeGeographyMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (History)
 		EditText knowledgeHistoryRanksEnter = (EditText) findViewById(R.id.knowledge_history_ranks);
 		EditText knowledgeHistoryMiscEnter = (EditText) findViewById(R.id.knowledge_history_misc_mod);
 		String knowledgeHistoryRanks = knowledgeHistoryRanksEnter.getText().toString().trim();
 		String knowledgeHistoryMisc = knowledgeHistoryMiscEnter.getText().toString().trim();
 		if (!knowledgeHistoryRanks.matches("")) {
 			int knowledgeHistoryRank = Integer.parseInt(knowledgeHistoryRanks);
 			Skill skill = new Skill(18, "Knowledge (History)", AbilityName.INTELLIGENCE, knowledgeHistoryRank, false);
 			if (!knowledgeHistoryMisc.matches("")) {
 				int knowledgeHistoryMod = Integer.parseInt(knowledgeHistoryMisc);
 				skill.addModifier("knowledgeHistoryMod", knowledgeHistoryMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Local)
 		EditText knowledgeLocalRanksEnter = (EditText) findViewById(R.id.knowledge_local_ranks);
 		EditText knowledgeLocalMiscEnter = (EditText) findViewById(R.id.knowledge_local_misc_mod);
 		String knowledgeLocalRanks = knowledgeLocalRanksEnter.getText().toString().trim();
 		String knowledgeLocalMisc = knowledgeLocalMiscEnter.getText().toString().trim();
 		if (!knowledgeLocalRanks.matches("")) {
 			int knowledgeLocalRank = Integer.parseInt(knowledgeLocalRanks);
 			Skill skill = new Skill(19, "Knowledge (Local)", AbilityName.INTELLIGENCE, knowledgeLocalRank, false);
 			if (!knowledgeLocalMisc.matches("")) {
 				int knowledgeLocalMod = Integer.parseInt(knowledgeLocalMisc);
 				skill.addModifier("knowledgeLocalMod", knowledgeLocalMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Nature)
 		EditText knowledgeNatureRanksEnter = (EditText) findViewById(R.id.knowledge_nature_ranks);
 		EditText knowledgeNatureMiscEnter = (EditText) findViewById(R.id.knowledge_nature_misc_mod);
 		String knowledgeNatureRanks = knowledgeNatureRanksEnter.getText().toString().trim();
 		String knowledgeNatureMisc = knowledgeNatureMiscEnter.getText().toString().trim();
 		if (!knowledgeNatureRanks.matches("")) {
 			int knowledgeNatureRank = Integer.parseInt(knowledgeNatureRanks);
 			Skill skill = new Skill(20, "Knowledge (Nature)", AbilityName.INTELLIGENCE, knowledgeNatureRank, false);
 			if (!knowledgeNatureMisc.matches("")) {
 				int knowledgeNatureMod = Integer.parseInt(knowledgeNatureMisc);
 				skill.addModifier("knowledgeNatureMod", knowledgeNatureMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Nobility)
 		EditText knowledgeNobilityRanksEnter = (EditText) findViewById(R.id.knowledge_nobility_ranks);
 		EditText knowledgeNobilityMiscEnter = (EditText) findViewById(R.id.knowledge_nobility_misc_mod);
 		String knowledgeNobilityRanks = knowledgeNobilityRanksEnter.getText().toString().trim();
 		String knowledgeNobilityMisc = knowledgeNobilityMiscEnter.getText().toString().trim();
 		if (!knowledgeNobilityRanks.matches("")) {
 			int knowledgeNobilityRank = Integer.parseInt(knowledgeNobilityRanks);
 			Skill skill = new Skill(21, "Knowledge (Nobility)", AbilityName.INTELLIGENCE, knowledgeNobilityRank, false);
 			if (!knowledgeNobilityMisc.matches("")) {
 				int knowledgeNobilityMod = Integer.parseInt(knowledgeNobilityMisc);
 				skill.addModifier("knowledgeNobilityMod", knowledgeNobilityMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Planes)
 		EditText knowledgePlanesRanksEnter = (EditText) findViewById(R.id.knowledge_planes_ranks);
 		EditText knowledgePlanesMiscEnter = (EditText) findViewById(R.id.knowledge_planes_misc_mod);
 		String knowledgePlanesRanks = knowledgePlanesRanksEnter.getText().toString().trim();
 		String knowledgePlanesMisc = knowledgePlanesMiscEnter.getText().toString().trim();
 		if (!knowledgePlanesRanks.matches("")) {
 			int knowledgePlanesRank = Integer.parseInt(knowledgePlanesRanks);
 			Skill skill = new Skill(22, "Knowledge (Planes)", AbilityName.INTELLIGENCE, knowledgePlanesRank, false);
 			if (!knowledgePlanesMisc.matches("")) {
 				int knowledgePlanesMod = Integer.parseInt(knowledgePlanesMisc);
 				skill.addModifier("knowledgePlanesMod", knowledgePlanesMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Religion)
 		EditText knowledgeReligionRanksEnter = (EditText) findViewById(R.id.knowledge_religion_ranks);
 		EditText knowledgeReligionMiscEnter = (EditText) findViewById(R.id.knowledge_religion_misc_mod);
 		String knowledgeReligionRanks = knowledgeReligionRanksEnter.getText().toString().trim();
 		String knowledgeReligionMisc = knowledgeReligionMiscEnter.getText().toString().trim();
 		if (!knowledgeReligionRanks.matches("")) {
 			int knowledgeReligionRank = Integer.parseInt(knowledgeReligionRanks);
 			Skill skill = new Skill(23, "Knowledge (Religion)", AbilityName.INTELLIGENCE, knowledgeReligionRank, false);
 			if (!knowledgeReligionMisc.matches("")) {
 				int knowledgeReligionMod = Integer.parseInt(knowledgeReligionMisc);
 				skill.addModifier("knowledgeReligionMod", knowledgeReligionMod);
 			}
 			skills.add(skill);
 		}
 
 		// Linguistics
 		EditText linguisticsRanksEnter = (EditText) findViewById(R.id.linguistics_ranks);
 		EditText linguisticsMiscEnter = (EditText) findViewById(R.id.linguistics_misc_mod);
 		String linguisticsRanks = linguisticsRanksEnter.getText().toString().trim();
 		String linguisticsMisc = linguisticsMiscEnter.getText().toString().trim();
 		if (!linguisticsRanks.matches("")) {
 			int linguisticsRank = Integer.parseInt(linguisticsRanks);
 			Skill skill = new Skill(24, "Linguistics", AbilityName.INTELLIGENCE, linguisticsRank, false);
 			if (!linguisticsMisc.matches("")) {
 				int linguisticsMod = Integer.parseInt(linguisticsMisc);
 				skill.addModifier("linguisticsMod", linguisticsMod);
 			}
 			skills.add(skill);
 		}
 
 		// Perception
 		EditText perceptionRanksEnter = (EditText) findViewById(R.id.perception_ranks);
 		EditText perceptionMiscEnter = (EditText) findViewById(R.id.perception_misc_mod);
 		String perceptionRanks = perceptionRanksEnter.getText().toString().trim();
 		String perceptionMisc = perceptionMiscEnter.getText().toString().trim();
 		if (!perceptionRanks.matches("")) {
 			int perceptionRank = Integer.parseInt(perceptionRanks);
 			Skill skill = new Skill(25, "Perception", AbilityName.WISDOM, perceptionRank, false);
 			if (!perceptionMisc.matches("")) {
 				int perceptionMod = Integer.parseInt(perceptionMisc);
 				skill.addModifier("perceptionMod", perceptionMod);
 			}
 			skills.add(skill);
 		}
 
 		// ... seriously I'm going to refactor this because there is a stupid amount of redundancy
 
 		// Perform1
 		EditText perform1TitleEnter = (EditText) findViewById(R.id.perform1_enter);
 		EditText perform1RanksEnter = (EditText) findViewById(R.id.perform1_ranks);
 		EditText perform1MiscEnter = (EditText) findViewById(R.id.perform1_misc_mod);
 		String perform1Title = perform1TitleEnter.getText().toString().trim();
 		String perform1Ranks = perform1RanksEnter.getText().toString().trim();
 		String perform1Misc = perform1MiscEnter.getText().toString().trim();
 		if (!perform1Title.matches("") && !perform1Ranks.matches("")) {
 			int perform1Rank = Integer.parseInt(perform1Ranks);
 			Skill skill = new Skill(26, "Perform1", perform1Title, AbilityName.CHARISMA, perform1Rank, false);
 			if (!perform1Misc.matches("")) {
 				int perform1Mod = Integer.parseInt(perform1Misc);
 				skill.addModifier("perform1Mod", perform1Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Perform2
 		EditText perform2TitleEnter = (EditText) findViewById(R.id.perform2_enter);
 		EditText perform2RanksEnter = (EditText) findViewById(R.id.perform2_ranks);
 		EditText perform2MiscEnter = (EditText) findViewById(R.id.perform2_misc_mod);
 		String perform2Title = perform2TitleEnter.getText().toString().trim();
 		String perform2Ranks = perform2RanksEnter.getText().toString().trim();
 		String perform2Misc = perform2MiscEnter.getText().toString().trim();
 		if (!perform2Title.matches("") && !perform2Ranks.matches("")) {
 			int perform2Rank = Integer.parseInt(perform2Ranks);
 			Skill skill = new Skill(26, "Perform2", perform2Title, AbilityName.CHARISMA, perform2Rank, false);
 			if (!perform2Misc.matches("")) {
 				int perform2Mod = Integer.parseInt(perform2Misc);
 				skill.addModifier("perform2Mod", perform2Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Profession1
 		EditText profession1TitleEnter = (EditText) findViewById(R.id.profession1_enter);
 		EditText profession1RanksEnter = (EditText) findViewById(R.id.profession1_ranks);
 		EditText profession1MiscEnter = (EditText) findViewById(R.id.profession1_misc_mod);
 		String profession1Title = profession1TitleEnter.getText().toString().trim();
 		String profession1Ranks = profession1RanksEnter.getText().toString().trim();
 		String profession1Misc = profession1MiscEnter.getText().toString().trim();
 		if (!profession1Title.matches("") && !profession1Ranks.matches("")) {
 			int profession1Rank = Integer.parseInt(profession1Ranks);
 			Skill skill = new Skill(27, "Profession1", profession1Title, AbilityName.WISDOM, profession1Rank, false);
 			if (!profession1Misc.matches("")) {
 				int profession1Mod = Integer.parseInt(profession1Misc);
 				skill.addModifier("profession1Mod", profession1Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Profession2
 		EditText profession2TitleEnter = (EditText) findViewById(R.id.profession2_enter);
 		EditText profession2RanksEnter = (EditText) findViewById(R.id.profession2_ranks);
 		EditText profession2MiscEnter = (EditText) findViewById(R.id.profession2_misc_mod);
 		String profession2Title = profession2TitleEnter.getText().toString().trim();
 		String profession2Ranks = profession2RanksEnter.getText().toString().trim();
 		String profession2Misc = profession2MiscEnter.getText().toString().trim();
 		if (!profession2Title.matches("") && !profession2Ranks.matches("")) {
 			int profession2Rank = Integer.parseInt(profession2Ranks);
 			Skill skill = new Skill(27, "Profession2", profession2Title, AbilityName.WISDOM, profession2Rank, false);
 			if (!profession2Misc.matches("")) {
 				int profession2Mod = Integer.parseInt(profession2Misc);
 				skill.addModifier("profession2Mod", profession2Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Ride
 		EditText rideRanksEnter = (EditText) findViewById(R.id.ride_ranks);
 		EditText rideMiscEnter = (EditText) findViewById(R.id.ride_misc_mod);
 		String rideRanks = rideRanksEnter.getText().toString().trim();
 		String rideMisc = rideMiscEnter.getText().toString().trim();
 		if (!rideRanks.matches("")) {
 			int rideRank = Integer.parseInt(rideRanks);
 			Skill skill = new Skill(28, "Ride", AbilityName.DEXTERITY, rideRank, false);
 			if (!rideMisc.matches("")) {
 				int rideMod = Integer.parseInt(rideMisc);
 				skill.addModifier("rideMod", rideMod);
 			}
 			skills.add(skill);
 		}
 
 		// Sense Motive
 		EditText senseMotiveRanksEnter = (EditText) findViewById(R.id.sense_motive_ranks);
 		EditText senseMotiveMiscEnter = (EditText) findViewById(R.id.sense_motive_misc_mod);
 		String senseMotiveRanks = senseMotiveRanksEnter.getText().toString().trim();
 		String senseMotiveMisc = senseMotiveMiscEnter.getText().toString().trim();
 		if (!senseMotiveRanks.matches("")) {
 			int senseMotiveRank = Integer.parseInt(senseMotiveRanks);
 			Skill skill = new Skill(29, "Sense Motive", AbilityName.WISDOM, senseMotiveRank, false);
 			if (!senseMotiveMisc.matches("")) {
 				int senseMotiveMod = Integer.parseInt(senseMotiveMisc);
 				skill.addModifier("senseMotiveMod", senseMotiveMod);
 			}
 			skills.add(skill);
 		}
 
 		// Sleight of Hand
 		EditText sleightOfHandRanksEnter = (EditText) findViewById(R.id.sleight_of_hand_ranks);
 		EditText sleightOfHandMiscEnter = (EditText) findViewById(R.id.sleight_of_hand_misc_mod);
 		String sleightOfHandRanks = sleightOfHandRanksEnter.getText().toString().trim();
 		String sleightOfHandMisc = sleightOfHandMiscEnter.getText().toString().trim();
 		if (!sleightOfHandRanks.matches("")) {
 			int sleightOfHandRank = Integer.parseInt(sleightOfHandRanks);
 			Skill skill = new Skill(30, "Sleight of Hand", AbilityName.DEXTERITY, sleightOfHandRank, false);
 			if (!sleightOfHandMisc.matches("")) {
 				int sleightOfHandMod = Integer.parseInt(sleightOfHandMisc);
 				skill.addModifier("sleightOfHandMod", sleightOfHandMod);
 			}
 			skills.add(skill);
 		}
 
 		// Spellcraft
 		EditText spellcraftRanksEnter = (EditText) findViewById(R.id.spellcraft_ranks);
 		EditText spellcraftMiscEnter = (EditText) findViewById(R.id.spellcraft_misc_mod);
 		String spellcraftRanks = spellcraftRanksEnter.getText().toString().trim();
 		String spellcraftMisc = spellcraftMiscEnter.getText().toString().trim();
 		if (!spellcraftRanks.matches("")) {
 			int spellcraftRank = Integer.parseInt(spellcraftRanks);
 			Skill skill = new Skill(31, "Spellcraft", AbilityName.INTELLIGENCE, spellcraftRank, false);
 			if (!spellcraftMisc.matches("")) {
 				int spellcraftMod = Integer.parseInt(spellcraftMisc);
 				skill.addModifier("spellcraftMod", spellcraftMod);
 			}
 			skills.add(skill);
 		}
 
 		// Stealth
 		EditText stealthRanksEnter = (EditText) findViewById(R.id.stealth_ranks);
 		EditText stealthMiscEnter = (EditText) findViewById(R.id.stealth_misc_mod);
 		String stealthRanks = stealthRanksEnter.getText().toString().trim();
 		String stealthMisc = stealthMiscEnter.getText().toString().trim();
 		if (!stealthRanks.matches("")) {
 			int stealthRank = Integer.parseInt(stealthRanks);
 			Skill skill = new Skill(32, "Stealth", AbilityName.DEXTERITY, stealthRank, false);
 			if (!stealthMisc.matches("")) {
 				int stealthMod = Integer.parseInt(stealthMisc);
 				skill.addModifier("stealthMod", stealthMod);
 			}
 			skills.add(skill);
 		}
 
 		// Survival
 		EditText survivalRanksEnter = (EditText) findViewById(R.id.survival_ranks);
 		EditText survivalMiscEnter = (EditText) findViewById(R.id.survival_misc_mod);
 		String survivalRanks = survivalRanksEnter.getText().toString().trim();
 		String survivalMisc = survivalMiscEnter.getText().toString().trim();
 		if (!survivalRanks.matches("")) {
 			int survivalRank = Integer.parseInt(survivalRanks);
 			Skill skill = new Skill(33, "Survival", AbilityName.WISDOM, survivalRank, false);
 			if (!survivalMisc.matches("")) {
 				int survivalMod = Integer.parseInt(survivalMisc);
 				skill.addModifier("survivalMod", survivalMod);
 			}
 			skills.add(skill);
 		}
 
 		// Swim
 		EditText swimRanksEnter = (EditText) findViewById(R.id.swim_ranks);
 		EditText swimMiscEnter = (EditText) findViewById(R.id.swim_misc_mod);
 		String swimRanks = swimRanksEnter.getText().toString().trim();
 		String swimMisc = swimMiscEnter.getText().toString().trim();
 		if (!swimRanks.matches("")) {
 			int swimRank = Integer.parseInt(swimRanks);
 			Skill skill = new Skill(34, "Swim", AbilityName.STRENGTH, swimRank, false);
 			if (!swimMisc.matches("")) {
 				int swimMod = Integer.parseInt(swimMisc);
 				skill.addModifier("swimMod", swimMod);
 			}
 			skills.add(skill);
 		}
 
 		// Use Magic Device
 		EditText useMagicDeviceRanksEnter = (EditText) findViewById(R.id.use_magic_device_ranks);
 		EditText useMagicDeviceMiscEnter = (EditText) findViewById(R.id.use_magic_device_misc_mod);
 		String useMagicDeviceRanks = useMagicDeviceRanksEnter.getText().toString().trim();
 		String useMagicDeviceMisc = useMagicDeviceMiscEnter.getText().toString().trim();
 		if (!useMagicDeviceRanks.matches("")) {
 			int useMagicDeviceRank = Integer.parseInt(useMagicDeviceRanks);
 			Skill skill = new Skill(35, "Use Magic Device", AbilityName.CHARISMA, useMagicDeviceRank, false);
 			if (!useMagicDeviceMisc.matches("")) {
 				int useMagicDeviceMod = Integer.parseInt(useMagicDeviceMisc);
 				skill.addModifier("useMagicDeviceMod", useMagicDeviceMod);
 			}
 			skills.add(skill);
 		}
 
 		// clear old data from DB
 		SQLiteHelperSkills.db.delete(SQLiteHelperSkills.TABLE_NAME,
 				SQLiteHelperSkills.COLUMN_CHAR_ID + " = " + charID, null);
 		
 		// write all data to DB
 		for (Skill s : skills) {
 			s.writeToDB(charID);
 		}
 
 		
 		// return to character creation main screen
 		Intent intent = new Intent(this, CharCreateMainActivity.class);
 		intent.putExtra("cid", charID);
 		startActivity(intent);
 
 
 	}
 	
}
	

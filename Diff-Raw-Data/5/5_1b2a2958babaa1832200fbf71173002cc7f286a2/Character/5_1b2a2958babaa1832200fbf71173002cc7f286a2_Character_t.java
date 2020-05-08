 /**
  * 
  */
 package com.cthos.pfpt.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 
 import com.cthos.db.CharacterProvider;
 import com.cthos.pfpt.core.ArmorClass;
 import com.cthos.pfpt.equipment.SlottedItem;
 
 /**
  * @author cthos
  *
  */
 public class Character 
 {
 	public static final Uri CONTENT_URI = Uri.parse("content://com.cthos.pfpt.core/character");
 	
 	public String name;
 	
 	public HashMap<String, Number> attributes = new HashMap();
 	
 	public HashMap<String, Number> modifiedAttributes;
 	
 	public long ac;
 	
 	public long hp;
 	
 	public long currentHp;
 	
 	public long bab;
 	
 	public Number cmd;
 	
 	public Number cmb;
 	
 	public long rangedToHit;
 	
 	public long meleeToHit;
 	
 	public String languages;
 	
 	protected ArrayList<SlottedItem> gear;
 	
 	protected ArrayList<CharacterClass> characterClasses;
 	
 	public Character(Cursor c)
 	{
 		_loadFromCursor(c);
 	}
 	
 	protected void _loadFromCursor(Cursor c)
 	{
 		c.moveToFirst();
 		
 		this.name = c.getString(c.getColumnIndex("name"));
 		
 		this.attributes.put("strength", c.getInt(c.getColumnIndex("strength")));
 		this.attributes.put("dexterity", c.getInt(c.getColumnIndex("dexterity")));
 		this.attributes.put("constitution", c.getInt(c.getColumnIndex("constitution")));
 		this.attributes.put("wisdom", c.getInt(c.getColumnIndex("wisdom")));
 		this.attributes.put("intelligence", c.getInt(c.getColumnIndex("intelligence")));
 		this.attributes.put("charisma", c.getInt(c.getColumnIndex("charisma")));
 		
 		this.modifiedAttributes = this.attributes;
 		
 		// TODO: Modify attrs by magic gear, etc. 
 		this.calculateAC();
 	}
 	
 	public void setGear(ArrayList<SlottedItem> gear)
 	{
 		Log.d("Gear", "Setting Gear");
 		this.gear = gear;
 		
 		Log.d("Gear", "Gear count " + String.valueOf(gear.size()));
 	}
 	
 	public void setClasses(ArrayList<CharacterClass> characterClasses)
 	{
 		Log.d("CharClasses", "Loading Character Classes");
 		this.characterClasses = characterClasses;
 	}
 	
 	public long calculateAC()
 	{
 		long baseAC = 10;
 		
 		long dexBonus = this.calculateBonus(this.attributes.get("dexterity"));
 		
 		HashMap<String, Number> gearMap = getGearMap("AC"); 
 		
 		// TODO: Figure in Armour Max Dex Bonus + Armor bonus
 		long AC = baseAC + dexBonus;
 		
 		for (String key : gearMap.keySet()) {
 			AC += gearMap.get(key).longValue();
 		}
 		
 		this.ac = AC;
 		
 		Log.d("AC Bonus", String.valueOf(AC));
 		
 		return this.ac;
 	}
 	
 	public long calculateHP()
 	{
 		long conBonus = this.calculateBonus(this.attributes.get("constitution"));
 		
 		long baseHP = conBonus;
 		
 		int classLen = this.characterClasses.size();
 		CharacterClass cl;
 		double hpbeep;
 		
 		for (int i = 0; i < classLen; i++) {
 			cl = this.characterClasses.get(i);
 			
 			if (i == 0) {
 				baseHP += cl.hitDie;
 				continue;
 			}
 			
 			Log.d("HP", String.valueOf(baseHP));
 			
			// Uses Pathfinder Society HP rules for the moment.
			hpbeep = (Math.ceil(cl.hitDie/2) + 1) * cl.numLevels + (conBonus * cl.numLevels);
			baseHP += hpbeep;
 		}
 		
 		this.hp = this.currentHp = baseHP;
 		
 		return baseHP;
 	}
 	
 	public void calculateAttacks()
 	{
 		long bab = 0;
 		
 		int classLen = this.characterClasses.size();
 		CharacterClass cl;
 		
 		for (int i = 0; i < classLen; i++) {
 			cl = this.characterClasses.get(i);
 			bab += cl.getBAB();
 		}
 		
 		this.bab = bab;
 		long dexBonus = this.calculateBonus(this.attributes.get("dexterity"));
 		long strBonus = this.calculateBonus(this.attributes.get("strength"));
 		
 		long atbonus = 0;
 		
 		HashMap<String, Number> gearMap = getGearMap("Attack"); 
 		
 		for (String key : gearMap.keySet()) {
 			atbonus += gearMap.get(key).longValue();
 		}
 		
 		this.rangedToHit = bab + dexBonus + atbonus;
 		this.meleeToHit = bab + strBonus + atbonus;
 		
 		//TODO: Remember to add in Size and Misc bonuses.
 		this.cmb = bab + strBonus;
 		this.cmd = 10 + bab + strBonus + dexBonus;
 	}
 	
 	public void gearUpdateAttributes()
 	{
 		for (String key : attributes.keySet()) {
 			int attMod = 0;
 			int newVal = attributes.get(key).intValue();
 			
 			attMod = getGearAttributeValue(key);
 			this.modifiedAttributes.put(key, newVal + attMod);
 		}
 	}
 	
 	public int getGearAttributeValue(String toWhat)
 	{
 		int attMod = 0;
 		
 		HashMap<String, Number> gearMap = getGearMap(toWhat);
 		
 		for (String key : gearMap.keySet()) {
 			attMod += gearMap.get(key).longValue();
 		}
 		
 		return attMod;
 	}
 	
 	/**
 	 * Determines the bonus provided by an ability score.
 	 * 
 	 * @param long score
 	 * 
 	 * @return
 	 */
 	public long calculateBonus(Number score)
 	{
 		double rawScore = Math.ceil((score.longValue() - 10) / 2);
 		if (score.longValue() - 10 < 0){
 			rawScore--;
 		}
 				
 		return (long) rawScore;
 	}
 	
 	protected HashMap<String, Number> getGearMap(String toWhat)
 	{
 		HashMap<String, Number> gearMap = new HashMap<String, Number>(); 
 		
 		toWhat = toWhat.toLowerCase();
 		
 		if (this.gear != null) {
 			int gearLen = this.gear.size();
 			
 			for (int i = 0; i < gearLen; i++) {
 				SlottedItem g = this.gear.get(i);
 				ArrayList<JSONObject> bonusL = new ArrayList<JSONObject>();
 				bonusL = g.getBonuses(toWhat);
 				int lSize = bonusL.size();
 				
 				if (lSize <= 0) {
 					continue;
 				}
 				
 				for (int j = 0; j < lSize; j++) {
 					JSONObject bonus = bonusL.get(j);
 					try {
 						String type = bonus.getString("name").toLowerCase();
 						long amt = bonus.getLong("howMuch");
 						
 						long typeBonus = 0;
 						
 						if (gearMap.containsKey(type)) {
 							typeBonus = gearMap.get(type).longValue();
 						}
 						
 						Log.d("TypeBonus", String.valueOf(typeBonus));
 						
 						if (type == "dodge" || type == "untyped") {
 							amt = amt + typeBonus;
 						}
 						
 						if (typeBonus == 0 || typeBonus < amt) {
 							Log.d("typeBonus", "Setting " + type + " to " + String.valueOf(typeBonus));
 							typeBonus = amt;
 							gearMap.put(type, typeBonus);
 						}
 						
 					} catch (JSONException e) {
 						Log.d("Fail", e.getMessage());
 					}
 				}
 			}
 		}
 		
 		return gearMap;
 	}
 }

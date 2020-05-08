 package ddybb.d3calc.src;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class HeroImporter {
 	
 	private static String name;
 	private static CharacterClass cc = CharacterClass.None;
 	private static Map<String, Object> items = new HashMap<String, Object>();
 	private static String region;
 	
 	public static void importHero(Map<String, Object> herodata, String r) {
 		
 		region = r;
 		name = (String)herodata.get("name");
 		setClass((String)herodata.get("class"));
 		D3Calc.activeChar = new Character(name, cc);
 		
 		items = (Map)herodata.get("items");
 		getItems();
 		
 	}
 	
 	private static void setClass(String classname) {
 		switch (classname) {
 		case "barbarian": cc = CharacterClass.Barbarian; break;
 		case "demon-hunter": cc = CharacterClass.DemonHunter; break;
 		case "monk": cc = CharacterClass.Monk; break;
 		case "witch-doctor": cc = CharacterClass.WitchDoctor; break;
 		case "wizard": cc = CharacterClass.Wizard; break;
 		default: break;
 		}
 	}
 	
 	private static void getItems() {
 		Map<String, Object> iteminfo = new HashMap<String, Object>();
 		for (String slot : items.keySet()) {
 			iteminfo = (Map)items.get(slot);
 			ItemType type = null;
 			switch (slot) {
 			case "head": type = ItemType.Helm; break;
 			case "torso": type = ItemType.Chest; break;
 			case "feet": type = ItemType.Boots; break;
 			case "hands": type = ItemType.Gloves; break;
 			case "shoulders": type = ItemType.Shoulders; break;
 			case "legs": type = ItemType.Pants; break;
 			case "bracers": type = ItemType.Bracers; break;
 			case "waist": type = ItemType.Belt; break;
 			case "rightFinger": type = ItemType.Ring1; break;
 			case "leftFinger": type = ItemType.Ring2; break;
 			case "neck": type = ItemType.Amulet; break;
 			case "mainHand": type = getMHType(iteminfo); break;
 			case "offHand": type = getOHType(iteminfo); break;
 			default: break;
 			}
 			if (type != null) {
 				String itemcode = (String)iteminfo.get("tooltipParams");
 				Map<String, Object> itemdata = D3API.getItem(region, itemcode);
 				Item item = new Item(type);
 				importItemStats(item, itemdata);
 				D3Calc.activeChar.equip[type.slot] = item;
 			}
 		}
 	}
 	
 	private static ItemType getMHType(Map<String, Object> iteminfo) {
 		Map<String, Object> itemtype = (Map)iteminfo.get("type");
 		String id = (String)itemtype.get("id");
 		if (id.equals("Bow") || id.equals("Crossbow")) {
 			return ItemType.WeaponBow;
 		}
 		if (id.contains("2H")) {
 			return ItemType.Weapon2H;
 		}
 		return ItemType.Weapon1;
 		
 	}
 	
 	private static ItemType getOHType(Map<String, Object> iteminfo) {
 		Map<String, Object> itemtype = (Map)iteminfo.get("type");
 		String id = (String)itemtype.get("id");
 		System.out.println(id);
 		if (id.equals("Quiver")) {
 			return ItemType.Quiver;
 		}
 		if (id.equals("Orb") || id.equals("Mojo")) {
 			return ItemType.Source;
 		}
 		if (id.equals("Shield")) {
 			return ItemType.Shield;
 		}
 		return ItemType.Weapon2;
 	}
 	
 	private static void importItemStats(Item item, Map<String, Object> itemdata) {
 		
 		//add weapon stats
 		if (item.type.mask == 1) {
 			Map<String, Object> weapon = (Map)itemdata.get("dps");
 			item.dps = ((Double)weapon.get("min")).floatValue();
 			weapon = (Map)itemdata.get("attacksPerSecond");
 			item.atkSpeedBase = ((Double)weapon.get("min")).floatValue();
 		}
 		
 		//add armor
 		if (item.type.mask == 0 && item.type != ItemType.Quiver) {
 			Map<String, Object> armor = (Map)itemdata.get("armor");
 			item.armor = ((Double)armor.get("min")).intValue();
 		}
 		//add basic stats
 		Map<String, Object> attributesRaw = (Map)itemdata.get("attributesRaw");
 		for (String s : attributesRaw.keySet()) {
 			Map<String, Object> stat = (Map)attributesRaw.get(s);
 			setStat(s, stat, item);
 		}
 		
 		//add gem stats
 		ArrayList<Object> gems = (ArrayList)itemdata.get("gems"); 
 		for (Object o : gems) {
 			Map<String, Object> gem = (Map)o;
 			attributesRaw = (Map)gem.get("attributesRaw"); 
 			for (String s : attributesRaw.keySet()) {
 				Map<String, Object> stat = (Map)attributesRaw.get(s);
 				setStat(s, stat, item);
 			}
 		}
 		
 		//UI redtape
 		int i = 0;
 		for (ItemMods mod : ItemMods.values()) {
 			Object o = item.getValueByMod(mod);
 			if (o instanceof Integer) {
 				if ((Integer)o > 0) {
 					item.mods[i] = mod;
 					i++;
 				}
 			} else if (o instanceof Float) {
 				if ((Float)o > 0f) {
 					item.mods[i] = mod;
 					i++;
 				}
 			}
 		}
 	}
 	
 	private static void setStat(String s, Map<String, Object> stat, Item item) {
 		switch (s) {
 		case "Strength_Item": item.strength += ((Double)stat.get("min")).intValue(); break;
 		case "Dexterity_Item": item.dexterity += ((Double)stat.get("min")).intValue(); break;
 		case "Intelligence_Item": item.intelligence += ((Double)stat.get("min")).intValue(); break;
 		case "Vitality_Item": item.vitality += ((Double)stat.get("min")).intValue(); break;
 		case "Resistance_All": item.allRes += ((Double)stat.get("min")).intValue(); break;
 		case "Resistance#Physical": item.physRes += ((Double)stat.get("min")).intValue(); break;
 		case "Resistance#Cold": item.coldRes += ((Double)stat.get("min")).intValue(); break;
 		case "Resistance#Fire": item.fireRes += ((Double)stat.get("min")).intValue(); break;
 		case "Resistance#Lightning": item.litRes += ((Double)stat.get("min")).intValue(); break;
 		case "Resistance#Poison": item.poisonRes += ((Double)stat.get("min")).intValue(); break;
 		case "Resistance#Arcane": item.arcRes += ((Double)stat.get("min")).intValue(); break;
 		case "Hitpoints_Max_Percent_Bonus_Item": item.life += ((Double)(100d * (double)stat.get("min"))).intValue(); break;
 		case "Crit_Percent_Bonus_Capped": item.critChance += ((Double)(100d * (double)stat.get("min"))).floatValue(); break;
 		case "Crit_Damage_Percent": item.critDamage += ((Double)(100d * (double)stat.get("min"))).intValue(); break;
 		case "Attacks_Per_Second_Percent": item.atkSpeed += ((Double)(100d * (double)stat.get("min"))).intValue(); break;
 		case "Damage_Percent_Reduction_From_Melee": item.reducM += ((Double)(100d * (double)stat.get("min"))).intValue(); break;
 		case "Damage_Percent_Reduction_From_Ranged": item.reducR += ((Double)(100d * (double)stat.get("min"))).intValue(); break;
 		case "Damage_Min#Physical": item.damageMin += ((Double)stat.get("min")).intValue(); item.damageMax += ((Double)stat.get("min")).intValue(); break;
 		case "Damage_Delta#Physical": item.damageMax += ((Double)stat.get("min")).intValue(); break;
 		default: break;
 		}
 	}
 	
 }

 package team.GunsPlus.Manager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 //import net.morematerials.morematerials.materials.SMCustomItem;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 //import org.getspout.spoutapi.material.CustomBlock;
 import team.GunsPlus.GunsPlus;
 import team.GunsPlus.Enum.Effect;
 import team.GunsPlus.Enum.EffectSection;
 import team.GunsPlus.Enum.EffectType;
 //import team.GunsPlus.Enum.FireBehavior;
 import team.GunsPlus.Enum.KeyType;
 import team.GunsPlus.Item.Addition;
 import team.GunsPlus.Item.Ammo;
 import team.GunsPlus.Item.Gun;
 import team.GunsPlus.Util.Util;
 
 public class ConfigParser {
 
 	public static List<ItemStack> parseItems(String s){
         List<ItemStack> result = new LinkedList<ItemStack>();
         
         String[] items = s.split(",");
         for (String item : items){
             ItemStack mat = parseItem(item.trim());
             if (mat != null)
                 result.add(mat);
         }
         
         return result;
     }
     
     public static ItemStack parseItem(String item){
         if (item == null || item.equals(""))
             return null;
         
         String[] parts = item.split(":");
         if (parts.length == 1)
             return singleItem(parts[0]);
         if (parts.length == 2)
             return withDurability(parts[0], parts[1]);
         if(parts.length == 3)
         	return withAmount(parts[0], parts[1], parts[2]);
         
         return null;
     }
     
     private static ItemStack singleItem(String item){
     	SpoutItemStack custom = null;
         Material m = getMaterial(item);
         if(m==null){
 			for(Ammo a:GunsPlus.allAmmo){
 				if(a.getName().toString().equals(item)){
 					custom = new SpoutItemStack(a);
 				}
 			}
 			for(Gun g:GunsPlus.allGuns){
 				if(g.getName().toString().equals(item)){
 					custom = new SpoutItemStack(g);
 				}
 			}
 			for(Addition a : GunsPlus.allAdditions){
 				if(a.getName().toString().equals(item)){
 					custom = new SpoutItemStack(a);
 				}
 			}
			if(GunsPlus.tripodenabled&&GunsPlus.tripod.getName().equals(item)){
 				custom = new SpoutItemStack(GunsPlus.tripod);
 			}
         }
         if(custom==null){
         	if(m==null){
         		return null;
         	}else{
         		return new ItemStack(m);
         	}
         }else{
         	return new SpoutItemStack(custom);
         }
     }
     
     private static ItemStack withDurability(String item, String durab){
     	Material m = getMaterial(item);
         if (m == null)
             return null;
         SpoutItemStack sis = new SpoutItemStack(new ItemStack(m));
         if(durab.matches("[0-9]+")){
         	sis.setDurability(Short.parseShort(durab));
         }
         
         return sis;
     }
     
     private static ItemStack withAmount(String item, String durab, String amount){
     	Material m = getMaterial(item);
         if (m == null)
             return null;
         SpoutItemStack sis = new SpoutItemStack(new ItemStack(m, Integer.parseInt(amount)));
         if(durab.matches("[0-9]+")){
         	sis.setDurability(Short.parseShort(durab));
         }
         
         return sis;
     }
     
     private static Material getMaterial(String item){
         if (item.matches("[0-9]*"))
             return Material.getMaterial(Integer.parseInt(item));
         
         return Material.getMaterial(item.toUpperCase());
     }
     
     public static KeyType parseKeyType(String string) throws Exception{
     	KeyType key = null;
     	if (string.startsWith("@")) {
 			if (string.endsWith("_"))
 				key = KeyType.HOLDLETTER(string.replace("@", ""));
 			else
 				key = KeyType.LETTER(string.replace("@", ""));
 		} else if (string.startsWith("#")) {
 			if (string.endsWith("_"))
 				key = KeyType.HOLDNUMBER(string.replace("#", ""));
 			else
 				key = KeyType.NUMBER(string.replace("#", ""));
 		} else {
 			if (string.endsWith("_")) {
 				switch (KeyType.getType(string)) {
 				case RIGHT:
 					key = KeyType.HOLDRIGHT;
 					break;
 				case LEFT:
 					key = KeyType.HOLDLEFT;
 					break;
 				case RIGHTSHIFT:
 					key = KeyType.HOLDRIGHTSHIFT;
 					break;
 				case LEFTSHIFT:
 					key = KeyType.HOLDLEFTSHIFT;
 					break;
 				}
 			} else {
 				switch (KeyType.getType(string)) {
 				case RIGHT:
 					key = KeyType.RIGHT;
 					break;
 				case LEFT:
 					key = KeyType.LEFT;
 					break;
 				case RIGHTSHIFT:
 					key = KeyType.RIGHTSHIFT;
 					break;
 				case LEFTSHIFT:
 					key = KeyType.LEFTSHIFT;
 					break;
 				}
 			}
 		}
     	return key;
     }
     
 //    public static FireBehavior parseFireBehavoir(String node) throws Exception{
 //    	int count = ConfigLoader.gunsConfig.getInt(node+".burst.shot-count", 1);
 //    	int delay = ConfigLoader.gunsConfig.getInt(node+"burst.shot-delay", 0);
 //    	if(ConfigLoader.gunsConfig.getString(node+".type").equalsIgnoreCase("single")){
 //    		return FireBehavior.SINGLE(count, delay);
 //    	}else if(ConfigLoader.gunsConfig.getString(node+".type").equalsIgnoreCase("automatic")){
 //    		return FireBehavior.AUTOMATIC(count, delay);
 //    	}
 //    	throw new Exception("Could not parse fire behavoir of a gun.");
 //    }
     
     public static List<Effect> parseEffects(String path){
     	List<Effect> effects = new ArrayList<Effect>();
     	if(!ConfigLoader.gunsConfig.isConfigurationSection(path)||ConfigLoader.gunsConfig.getConfigurationSection(path).getKeys(false).isEmpty()) return effects;
     	for(String effectsection: ConfigLoader.gunsConfig.getConfigurationSection(path).getKeys(false)){
     		EffectSection effsec = EffectSection.valueOf(effectsection.toUpperCase());
     		setSectionArguments(path+"."+effectsection, effsec);
     		for(String effecttype : ConfigLoader.gunsConfig.getConfigurationSection(path+"."+effectsection).getKeys(false)){
     			if(effecttype.toUpperCase().equalsIgnoreCase("arguments")) continue;
     			EffectType efftyp = EffectType.valueOf(effecttype.toUpperCase());
     			if(Util.isAllowedInEffectSection(efftyp, effsec)){
     				effects.add(buildEffect(efftyp, effsec, path+"."+effectsection+"."+effecttype));
     			}
     		}
     	}
     	return effects;
     }
     
     private static void setSectionArguments(String path ,EffectSection e){
     	Map<String, Object> map = new HashMap<String, Object>();
     	ConfigurationSection cs = ConfigLoader.gunsConfig.getConfigurationSection(path+".arguments");
     	if(cs==null) return;
     	switch(e) {
     		case TARGETLOCATION:
     			if(cs.getInt("radius")!=0)
     				map.put("RADIUS", (Integer)cs.getInt("radius"));
     			else return;
     			break;
     		case SHOOTERLOCATION:
     			if(cs.getInt("radius")!=0)
     				map.put("RADIUS", (Integer)cs.getInt("radius"));
     			else return;
     			break;
     		case FLIGHTPATH:
     			if(cs.getInt("length")!=0)
     				map.put("LENGTH", (Integer)cs.getInt("length"));
     			else return;
     			break;
     	}
     	e.setData(map);
     }
     
     private static Effect buildEffect(EffectType efftyp, EffectSection es, String path){
     		Effect e = new Effect(efftyp, es);
     		switch(efftyp){
 		    	case EXPLOSION:
 		    		e.addArgument("SIZE", ConfigLoader.gunsConfig.getInt(path+".size"));
 		    		break;
 		    	case LIGHTNING:
 		    		break;
 		    	case SMOKE:
 		    		e.addArgument("DENSITY", ConfigLoader.gunsConfig.getInt(path+".density"));
 		    		break;
 		    	case FIRE:
 		    		if(es.equals(EffectSection.SHOOTER)||es.equals(EffectSection.TARGETENTITY))
 		    			e.addArgument("DURATION", ConfigLoader.gunsConfig.getInt(path+".duration"));
 		    		else
 		    			e.addArgument("STRENGTH", ConfigLoader.gunsConfig.getInt(path+".strength"));
 		    		break;
 		    	case PUSH:
 		    		e.addArgument("SPEED", ConfigLoader.gunsConfig.getDouble(path+".speed"));
 		    		break;
 		    	case DRAW:
 		    		e.addArgument("SPEED", ConfigLoader.gunsConfig.getDouble(path+".speed"));
 		    		break;
 		    	case POTION:
 		    		e.addArgument("ID", ConfigLoader.gunsConfig.getInt(path+".id"));
 		    		e.addArgument("DURATION", ConfigLoader.gunsConfig.getInt(path+".duration"));
 		    		e.addArgument("STRENGTH", ConfigLoader.gunsConfig.getInt(path+".strength"));
 		    		break;
 		    	case SPAWN:
 		    		e.addArgument("ENTITY", ConfigLoader.gunsConfig.getString(path+".entity"));
 		    		break;
 		    	case PLACE:
 		    		e.addArgument("BLOCK", ConfigLoader.gunsConfig.getString(path+".block"));
 		    		break;
 		    	case BREAK:
 		    		e.addArgument("POTENCY", ConfigLoader.gunsConfig.getDouble(path+".potency"));
 		    		break;
     	}
     	return e;
     }
     
     public static ArrayList<Addition> parseAdditions(String path){
     	ArrayList<Addition> adds = new ArrayList<Addition>();
     	String string = ConfigLoader.gunsConfig.getString(path);
     	if(string!=null){
 	    	String[] split = string.split(",");
 	    	for(String splitString : split){
 	    		for(Addition a : GunsPlus.allAdditions){
 		    		if(a.getName().equalsIgnoreCase(splitString.trim())){
 		    			adds.add(a);
 		    		}
 		    	}
 	    	}
     	}
     	return adds;
     }
 }

 package ml.boxes.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.item.ItemStack;
 
 /**
 * This class is used to prevent your mod's items from being a placed in an inventory added by Boxes.
  * @author Matchlighter
  *
  */
 public class ContentBlacklist {
 	
 	public static String LIST_BOX = "blBox";
 	public static String LIST_CRATE = "blCrate";
 	
 	private static Map<String, List<IItemFilter>> blacklists = new HashMap<String, List<IItemFilter>>();
 	
 	public static void addFilter(String list, IItemFilter iif){
 		if (!blacklists.containsKey(list))
 			blacklists.put(list, new ArrayList<IItemFilter>());
 		
 		blacklists.get(list).add(iif);
 	}
 	
 	public static boolean ItemBlacklisted(String list, ItemStack is){
 		if (blacklists.containsKey(list)){
 			for (IItemFilter iif : blacklists.get(list)){
 				if (iif.ISMatchesFilter(is))
					return true;
 			}
 		}
 		
 		return false;
 	}
 
 }

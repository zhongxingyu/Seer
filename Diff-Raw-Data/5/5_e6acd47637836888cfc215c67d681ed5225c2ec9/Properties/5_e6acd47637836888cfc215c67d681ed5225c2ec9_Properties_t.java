 package TechGuard.Archers;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.inventory.ItemStack;
 
 import TechGuard.Archers.Arrow.EnumBowMaterial;
 /**
  * @author �TechGuard
  */
 public class Properties {
 	public static HashMap<Short,ArrayList<ItemStack>> ArrowAmmo = new HashMap<Short,ArrayList<ItemStack>>();
 	private static String dir = "plugins/Archers/";
 	private static File ConfigFile;
 	
 	public static void reload(){
 		load();
 	}
 	
 	private static void load(){
 		ConfigFile = new File(dir + "config.ammo");
 		checkForConfig();
 		
 		loadConfig();
 	}
 	
 	private static void checkForConfig(){
 		try{
 			if(!ConfigFile.exists()){
 				ConfigFile.getParentFile().mkdirs();
 				ConfigFile.createNewFile();
 		        BufferedWriter out = new BufferedWriter(new FileWriter(ConfigFile));
 		        
 		        out.write("#The right order:"); out.newLine();
 		        out.write("#  ARROW NAME:ITEM ID,AMOUNT:NEW ITEM ID, AMOUNT, etc. etc."); out.newLine();
 		        out.write("#Arrow names:"); out.newLine();
 		       	for(EnumBowMaterial bow : EnumBowMaterial.values()){
 		       		out.write("  "+bow.getName()); out.newLine();
 		       	}
 		        out.write("#Lines witch start with the # symbol, will be ignored!"); out.newLine();
 		        out.write(""); out.newLine();
 		        out.write("#Normal Arrow"); out.newLine();
 		        out.write("Normal:262,1"); out.newLine();
 		        out.write(""); out.newLine();
 		        out.write("#Ice Arrow"); out.newLine();
 		        out.write("Ice:332,1:262,1"); out.newLine();
 		        out.write(""); out.newLine();
 		        out.write("#Fire Arrow"); out.newLine();
 		        out.write("Fire:263,1:262,1"); out.newLine();
 		        out.write(""); out.newLine();
 		        out.write("#TNT Arrow"); out.newLine();
 		        out.write("TNT:289,2:262,1"); out.newLine();
 		        out.write(""); out.newLine();
 		        out.write("#Thunder Arrow"); out.newLine();
 		        out.write("Thunder:331,5:262,1");out.newLine();
 		        out.write(""); out.newLine();
 		        out.write("#Monster Arrow"); out.newLine();
 		        out.write("Monster:352,2:262,1"); out.newLine();
 		        out.write(""); out.newLine();
 		        out.write("#Thrice Arrow"); out.newLine();
 		        out.write("Thrice:262,3"); out.newLine();
 		        out.write(""); out.newLine();
 		        out.write("#Zombie Arrow"); out.newLine();
		        out.write("Zombie:295,1:262,1"); out.newLine();
                         out.write("#Tree Arrow");out.newLine();
		        out.write("Tree:6,1:262,1");
 		        out.close();
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	private static void loadConfig(){
 		try{
 		    BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(ConfigFile))));
 		    String strLine;
 		    while ((strLine = br.readLine()) != null){
 		    	if(strLine.startsWith("#") || strLine.startsWith(" ") || !strLine.contains(":")){
 		    		continue;
 		    	}
 		    	String[] split = strLine.split(":");
 		    	ArrayList<ItemStack> list = new ArrayList<ItemStack>();
 		    	for(String s : split){
 		    		if(s.contains(",")){
 		    			list.add(new ItemStack(Integer.parseInt(s.split(",")[0]), Integer.parseInt(s.split(",")[1])));
 		    		}
 		    	}
 		    	ArrowAmmo.put(EnumBowMaterial.fromName(split[0]).getDataValue(), list);
 		    }
 		    br.close();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 }

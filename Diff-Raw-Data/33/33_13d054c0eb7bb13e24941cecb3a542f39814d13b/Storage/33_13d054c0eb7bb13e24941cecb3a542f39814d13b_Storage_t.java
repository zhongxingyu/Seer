 package Ne0nx3r0.SignShop;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.block.Block;
 import java.io.File;
 import java.util.Map;
 import java.util.HashMap;
 import org.bukkit.Location;
 import org.bukkit.Bukkit;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import org.bukkit.Material;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class Storage{
     private final SignShop plugin;
 
     public static Configuration yml;
 
     private static Map<Location,Seller> sellers;
 
     public Storage(File ymlFile,SignShop instance){
         plugin = instance;
 
         sellers = new HashMap <Location,Seller>();
 
         yml = new Configuration(ymlFile);
         yml.load();
//TODO: Only back up sellers.yml when there has been a change from Load().
 // Backup sellers.yml
         if(ymlFile.exists()){
             File backupTo = new File(ymlFile.getPath()+".bak");
             
             try{
                 FileReader in = new FileReader(ymlFile);
                 FileWriter out = new FileWriter(backupTo);
                 int c;
 
                 while ((c = in.read()) != -1)
                 out.write(c);
 
                 in.close();
                 out.close();
                 
             }catch(IOException e){
                 e.printStackTrace();
                 
             }
         }
 
 // Load into memory, this also removes invalid signs (hence the backup)
         Load();
     }
 
     public void Load(){
         Map<String,Object> tempSellers = (Map<String,Object>) this.yml.getProperty("sellers");
 
         if(tempSellers == null){
             return;
         }
 
         Map<String,Object> tempSeller;
 
         String[] sSignLocation;
         Location lSign;
         Block bChest;
         ItemStack[] isItems;
         ArrayList<Integer> items;
         ArrayList<Integer> amounts;
         ArrayList<String> datas;
         ArrayList<Integer> durabilities;
         boolean invalidShop;
         boolean needToSave = false;
         for(String sKey : tempSellers.keySet()){
             invalidShop = false;
             
             sSignLocation = sKey.split("/");
 
             while(sSignLocation.length > 4){
                 sSignLocation[0] = sSignLocation[0]+"/"+sSignLocation[1];
 
                 for(int i=0;i<sSignLocation.length-1;i++){
                     sSignLocation[i] = sSignLocation[i+1];
                 }
             }
 
             int iX = 0;
             int iY = 0;
             int iZ = 0;
 
             try{
                 iX = Integer.parseInt(sSignLocation[1]);
                 iY = Integer.parseInt(sSignLocation[2]);
                 iZ = Integer.parseInt(sSignLocation[3]);
             }catch(NumberFormatException nfe){
                 invalidShop = true;//only used in the conditional below this
 
                 needToSave = true;
             }
 
             // wasn't sure if I could do this in the catch statement...
             // (and you'd never really know if it wasnt working)
             if(invalidShop){
                 plugin.log(plugin.Errors.get("shop_removed"), Level.INFO, 2);
 
                 continue;
             }
 
             Block bSign = Bukkit.getServer().getWorld(sSignLocation[0]).getBlockAt(
                 Integer.parseInt(sSignLocation[1]),
                 Integer.parseInt(sSignLocation[2]),
                 Integer.parseInt(sSignLocation[3]));
 
 //If no longer valid, remove this sign (this would happen from worldedit, movecraft, etc)
             if(bSign.getType() != Material.SIGN_POST && bSign.getType() != Material.WALL_SIGN){
                 plugin.log(plugin.Errors.get("shop_removed"), Level.INFO, 2);
                 
                 needToSave = true;
 
                 continue;
             }
 
             lSign = bSign.getLocation();
 
             tempSeller = (Map<String,Object>) tempSellers.get(sKey);
 
             bChest = Bukkit.getServer().getWorld((String) tempSeller.get("chestworld")).getBlockAt(
                 (Integer) tempSeller.get("chestx"),
                 (Integer) tempSeller.get("chesty"),
                 (Integer) tempSeller.get("chestz"));
 
             datas = (ArrayList<String>) tempSeller.get("datas");
             items = (ArrayList<Integer>) tempSeller.get("items");
             amounts = (ArrayList<Integer>) tempSeller.get("amounts");
             durabilities = (ArrayList<Integer>) tempSeller.get("durabilities");
             
             isItems = new ItemStack[items.size()];
 
             for(int i=0;i<items.size();i++){
                 isItems[i] = new ItemStack(items.get(i),amounts.get(i));
 
                 if(datas != null && datas.get(i) != null){
                     isItems[i].getData().setData(new Byte(datas.get(i)));
                 }
                 if(durabilities != null && durabilities.get(i) != null){
                     isItems[i].setDurability(durabilities.get(i).shortValue());
                 }
             }
 
             this.sellers.put(lSign, new Seller((String) tempSeller.get("owner"),bChest,isItems));
         }
 
         if(needToSave){
             this.Save();
         }
     }
 
     public void Save(){
         plugin.log(plugin.Errors.get("saving"), Level.INFO);
 
         Map<String,Object> tempSellers = new HashMap<String,Object>();
 
         Seller seller;
         Map<String,Object> temp;
         for(Location lKey : this.sellers.keySet()){
             temp = new HashMap<String,Object>();
 
             seller = sellers.get(lKey);
 
             temp.put("chestworld",seller.world);
             temp.put("chestx",seller.x);
             temp.put("chesty",seller.y);
             temp.put("chestz",seller.z);
 
             temp.put("items",seller.items);
             temp.put("amounts",seller.amounts);
             temp.put("durabilities",seller.durabilities);
 
             String[] sDatas = new String[seller.datas.length];
             for(int i = 0; i < seller.datas.length; i++){
                 if(sDatas[i] != null){
                     sDatas[i] = Byte.toString(seller.datas[i]);
                 }
             }
             temp.put("datas", sDatas);
 
             temp.put("owner", seller.owner);
 
             tempSellers.put(lKey.getWorld().getName()
                     + "/" + lKey.getBlockX()
                     + "/" + lKey.getBlockY()
                     + "/" + lKey.getBlockZ(),temp);
         }
         
         this.yml.setProperty("sellers", tempSellers);
         this.yml.save();
 
         plugin.log(plugin.Errors.get("saved"), Level.INFO);
     }
 
     public void addSeller(String sPlayer, Block bSign, Block bChest, ItemStack[] isItems){
         this.sellers.put(bSign.getLocation(), new Seller(sPlayer, bChest, isItems));
     }
 
     public Seller getSeller(Location lKey){
         if(this.sellers.containsKey(lKey)){
             return this.sellers.get(lKey);
         }
         return null;
     }
 
     public void removeSeller(Location lKey){
         if(this.sellers.containsKey(lKey)){
             this.sellers.remove(lKey);
         }
     }
 }

 package com.gmail.ne0nx3r0.persistence;
 
 import com.gmail.ne0nx3r0.rareitems.RareItems;
 import com.gmail.ne0nx3r0.rareitems.http.ApiMessenger;
 import com.gmail.ne0nx3r0.rareitems.item.ItemProperty;
 import com.gmail.ne0nx3r0.rareitems.item.RareItem;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class ProfileManager
 {
     private HashMap<String,PlayerProfile> playerProfiles;
     private final File PROFILE_DIRECTORY;
     
     public ProfileManager()
     {
         playerProfiles = new HashMap<>();
 
         PROFILE_DIRECTORY = new File(RareItems.self.getDataFolder(),"players");
         
         if(!PROFILE_DIRECTORY.exists())
         {
             PROFILE_DIRECTORY.mkdirs();
         }
     }
     
     public void loadPlayerProfile(Player p)
     {
         String sPlayerName = p.getName().toLowerCase();
         
         if(playerProfiles.containsKey(sPlayerName))
         {
             ApiMessenger.fetchPlayerRareItems(playerProfiles.get(sPlayerName).getSiteId(),false);
             
             return;
         }
         
         File playerProfile = new File(PROFILE_DIRECTORY,sPlayerName+".yml");
 
         if(playerProfile.exists())
         {
             File ymlFile = new File(PROFILE_DIRECTORY,sPlayerName+".yml");
 
             if(!ymlFile.exists())
             {
                 try
                 {
                     ymlFile.createNewFile();
                 }
                 catch(IOException ex)
                 {
                     RareItems.logger.log(Level.INFO, "Could not create {0}", ymlFile.getName());
                 }
             }
 
             FileConfiguration yml = YamlConfiguration.loadConfiguration(ymlFile);
 
             int iSiteId = yml.getInt("siteId");
             int iMoney = yml.getInt("money");
             
             HashMap<Integer,Integer[]> checkOuts = new HashMap<>();
             for(String sRid : yml.getConfigurationSection("checkedOut").getKeys(false))
             {
                 checkOuts.put(
                     Integer.parseInt(sRid),
                     yml.getIntegerList("checkedOut."+sRid).toArray(new Integer[2])
                 );
             }
             
             HashMap<Integer,RareItem> rareItems = new HashMap<>();
             for(String sRid : yml.getConfigurationSection("rareItems").getKeys(false))
             {
                 int rid = Integer.parseInt(sRid);
                 
                 HashMap<ItemProperty,Integer> ips = new HashMap<ItemProperty,Integer>();
                 for(String sIpId : yml.getConfigurationSection("rareItems."+sRid+".p").getKeys(false))
                 {
                     ips.put(
                         RareItems.ipm.getItemProperty(Integer.parseInt(sRid)),
                         yml.getInt("rareItems."+sRid+".p."+sRid)
                     );
                 }
 
                 rareItems.put(rid,new RareItem(
                     rid,
                     sPlayerName,
                     yml.getInt("rareItems."+sRid+".m"),
                     ((Integer) yml.get("rareItems."+sRid+".dv")).byteValue(),
                     ips
                 ));
             }
             
             playerProfiles.put(sPlayerName, new PlayerProfile(
                 sPlayerName,
                 iSiteId,
                 iMoney,
                 rareItems,
                 checkOuts
             ));
             
             ApiMessenger.fetchPlayerRareItems(iSiteId,false);
         }
         else
         {
             ApiMessenger.fetchPlayerRareItems(p,false);
         }
     }
 
     public void savePlayerProfile(PlayerProfile pp)
     {
         File ymlFile = new File(PROFILE_DIRECTORY,pp.getName()+".yml");
         
         if(!ymlFile.exists())
         {
             try
             {
                 ymlFile.createNewFile();
             }
             catch(IOException ex)
             {
                 RareItems.logger.log(Level.INFO, "Could not create {0}", ymlFile.getName());
             }
         }
 
         FileConfiguration yml = YamlConfiguration.loadConfiguration(ymlFile);
         
         yml.set("siteId", pp.getSiteId());
         yml.set("money", pp.getMoney());
         yml.set("checkedOut", pp.getCheckedOutItems());
         
         HashMap<Integer,Object> rareItems = new HashMap<>();
         for(RareItem ri : pp.getRareItems().values())
         {
             HashMap<String,Object> rareItem = new HashMap<>();
             
             rareItem.put("m", ri.getMaterialId());
             rareItem.put("dv", ri.getDataValue());
             
             HashMap<Integer,Integer> itemProperties = new HashMap<>();
             for(ItemProperty ip : ri.getItemProperties().keySet())
             {
                 itemProperties.put(ip.getId(),ri.getItemProperties().get(ip));
             }
             
             rareItem.put("p", itemProperties);
                     
             rareItems.put(ri.getId(),rareItem);
         }
         
         yml.set("rareItems",rareItems);
 
         try {
             yml.save(ymlFile);
         } catch (IOException ex) {
             Logger.getLogger(ProfileManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void addPlayerProfile(PlayerProfile pp)
     {
         this.playerProfiles.put(pp.getName().toLowerCase(), pp);
         
         pp.refreshArmor();
     }
 
     public RareItem getRareItem(Player p, ItemStack is)
     {
         return getRareItem(p,is,false);
     }
     
     public RareItem getRareItem(Player p, ItemStack is, boolean includeInactive)
     {
         String sPlayerName = p.getName().toLowerCase();
         
         if(playerProfiles.containsKey(sPlayerName))
         {
             return playerProfiles.get(sPlayerName).getRareItem(is,includeInactive);
         }
         
         return null;
     }
     
     public RareItem getRareItem(Player p, int rid)
     {
         return getRareItem(p, rid, false);
     }
     
     public RareItem getRareItem(Player p, int rid, boolean includeInactive)
     {
         String sPlayerName = p.getName().toLowerCase();
         
         if(playerProfiles.containsKey(sPlayerName))
         {
             return playerProfiles.get(sPlayerName).getRareItem(rid,includeInactive);
         }
         
         return null;
     }
     
     public void removePlayerProfile(Player p)
     {
         String sPlayerName = p.getName().toLowerCase();
         
         if(playerProfiles.containsKey(sPlayerName))
         {
             this.savePlayerProfile(playerProfiles.get(sPlayerName));
             
             playerProfiles.get(sPlayerName).revokeAllItemProperties();
             
             playerProfiles.remove(sPlayerName);
         }
     }
 
     public void refreshArmor(Player player)
     {
         String sPlayerName = player.getName().toLowerCase();
         
         if(playerProfiles.containsKey(sPlayerName))
         {
             playerProfiles.get(sPlayerName).refreshArmor();
         }
     }
 
     public boolean isRareItem(Player p, ItemStack is, int rid)
     {
         String sPlayerName = p.getName().toLowerCase();
         
         if(playerProfiles.containsKey(sPlayerName))
         {
             RareItem ri = playerProfiles.get(sPlayerName).getRareItem(is, false);
             if(ri != null && ri.getId() == rid)
             {
                 return true;
             }
         }
         return false;
     }
 
     public boolean hasRareItems(Player p)
     {
         return playerProfiles.containsKey(p.getName().toLowerCase());
     }
 
     public void fillWithCheckedInItems(Player p, Inventory inv)
     {
         String sPlayerName = p.getName().toLowerCase();
         
         if(playerProfiles.containsKey(sPlayerName))
         {
             playerProfiles.get(sPlayerName).fillWithCheckedInItems(inv);
         }
     }
 
     public boolean checkOutRareItem(RareItem ri, Player p)
     {
         PlayerProfile pp = playerProfiles.get(p.getName().toLowerCase());
 
         if(pp.checkOut(ri))
         {
             this.savePlayerProfile(pp);
             
             return true;
         }
         
         return false;
     }
 
     public boolean checkInRareItem(RareItem ri, Player p)
     {
         PlayerProfile pp = playerProfiles.get(p.getName().toLowerCase());
 
         if(pp.checkIn(ri))
         {
             this.savePlayerProfile(pp);
             return true;
         }
         
         return false;
     }
     
     public boolean checkInRareItem(int rid, Player p)
     {
         PlayerProfile pp = playerProfiles.get(p.getName().toLowerCase());
 
         if(pp.checkIn(rid))
         {
             this.savePlayerProfile(pp);
             return true;
         }
         
         return false;
     }
     
     public void checkInAllRareItems(Player p)
     {
         PlayerProfile pp = playerProfiles.get(p.getName().toLowerCase());
         
        if(pp != null)
        {
            pp.checkInAllRareItems();

            this.savePlayerProfile(pp);
        }
     }
 
     public PlayerProfile getPlayerProfile(String sPlayerName,int iSiteId)
     {
         if(playerProfiles.containsKey(sPlayerName.toLowerCase()))
         {
             return this.playerProfiles.get(sPlayerName.toLowerCase());
         }
 
         PlayerProfile pp = new PlayerProfile(sPlayerName,iSiteId);
         
         playerProfiles.put(sPlayerName, pp);
         
         return pp;
     }
 
     public void saveAllPlayerProfiles()
     {
         for(PlayerProfile pp : playerProfiles.values())
         {
             savePlayerProfile(pp);
         }
     }    
 
     public Integer[] getCheckedOutRareItemData(Player p, int rid)
     {
         return this.playerProfiles.get(p.getName().toLowerCase()).getCheckedOutRareItemData(rid);
     }
 
     public boolean isCheckedOut(Player p, int rid)
     {
         return this.playerProfiles.get(p.getName().toLowerCase()).isCheckedOut(rid);
     }
 
     public HashMap<Integer, Integer[]> getCheckedOutRareItemIds(Player p)
     {
         return this.playerProfiles.get(p.getName().toLowerCase()).getCheckedOutItems();
     }
 }

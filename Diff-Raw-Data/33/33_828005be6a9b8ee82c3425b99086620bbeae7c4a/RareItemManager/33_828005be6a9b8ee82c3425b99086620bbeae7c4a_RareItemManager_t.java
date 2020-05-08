 package com.gmail.ne0nx3r0.rareitems.item;
 
 import com.gmail.ne0nx3r0.rareitems.RareItems;
 import com.gmail.ne0nx3r0.rareitems.item.abilities.*;
 import com.gmail.ne0nx3r0.rareitems.item.enchantments.*;
 import com.gmail.ne0nx3r0.rareitems.item.skills.*;
 import com.gmail.ne0nx3r0.rareitems.item.spells.*;
 import com.gmail.ne0nx3r0.rareitems.item.vfx.*;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
 import net.minecraft.server.NBTTagList;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class RareItemManager
 {
     
     private final String RID_PREFIX = ChatColor.DARK_GRAY+"RID: "+ChatColor.GRAY;
     private final String OWNER_PREFIX = ChatColor.DARK_GRAY+"Owner: "+ChatColor.BLACK;
     
     private final Map<String,Map<Integer,RareItem>> playerRareItems;//Lower case player name
     private final HashMap<ItemProperty, HashMap<String,Integer>> activeEffects;
     private final Map<String,Map<ItemProperty,Integer>> playerActiveEffects;
     private final EnumMap<ItemRarities, String> rarityStrings;
     private final Map<String,ItemProperty> itemProperties;
     private final Map<Integer,ItemProperty> itemPropertiesIdLookup;
     
     public RareItemManager()
     {
         rarityStrings = new EnumMap<>(ItemRarities.class);
         rarityStrings.put(ItemRarities.COMMON,    ChatColor.GRAY + "Common");    
         rarityStrings.put(ItemRarities.UNCOMMON,  ChatColor.BLUE + "Uncommon");    
         rarityStrings.put(ItemRarities.RARE,      ChatColor.GOLD + "Rare");    
         rarityStrings.put(ItemRarities.LEGENDARY, ChatColor.RED  + "Legendary"); 
         
         playerRareItems = new HashMap<>();//Lower case player name
         playerActiveEffects = new HashMap<>();
         activeEffects = new HashMap<>();
         itemProperties = new HashMap<>();
         itemPropertiesIdLookup = new HashMap<>();
         
         this.storeItemProperty(new Fertilize());
         this.storeItemProperty(new Smelt());
         this.storeItemProperty(new Spore());
         this.storeItemProperty(new FireHandling());
         this.storeItemProperty(new MeltObsidian());
         this.storeItemProperty(new PaintWool());
         
         this.storeItemProperty(new Backstab());
         this.storeItemProperty(new CallLightning());
         this.storeItemProperty(new Confuse());
         this.storeItemProperty(new Poison());
         this.storeItemProperty(new Slow());
         this.storeItemProperty(new VampiricRegeneration());
         this.storeItemProperty(new Weaken());
         
         this.storeItemProperty(new CraftItem());
         this.storeItemProperty(new FireResistance());
         this.storeItemProperty(new GrowTree());
         this.storeItemProperty(new Haste());
         this.storeItemProperty(new MagicBag());
         this.storeItemProperty(new RepairItem());
         this.storeItemProperty(new SummonBat());
         this.storeItemProperty(new SummonChicken());
         this.storeItemProperty(new SummonCow());
         this.storeItemProperty(new SummonMooshroom());
         this.storeItemProperty(new SummonOcelot());
         this.storeItemProperty(new SummonSheep());
         this.storeItemProperty(new SummonSlime());
         this.storeItemProperty(new SummonPig());
         this.storeItemProperty(new Invisibility());
         
         this.storeItemProperty(new AngelicGlow());
         this.storeItemProperty(new Burning());
         this.storeItemProperty(new EnderField());
         this.storeItemProperty(new Smoking());
         
         this.storeItemProperty(new Durability());
         this.storeItemProperty(new Fly());
         this.storeItemProperty(new Hardy());
         this.storeItemProperty(new Regeneration());
         this.storeItemProperty(new Strength());
         this.storeItemProperty(new WaterBreathing());
     }
     public RareItem getRareItem(String sPlayerName,ItemStack is)
     {
        return getRareItem(sPlayerName,is,false);
    }
    
    public RareItem getRareItem(String sPlayerName,ItemStack is,boolean ignoreCheckout)
    {
         if(is != null && is.getType() != Material.AIR && ((CraftItemStack) is).getHandle().tag != null)
         {
             NBTTagList lore = ((CraftItemStack) is)
                 .getHandle().tag.getCompound("display").getList("Lore");
 
             if(lore != null && lore.size() > 0)
             {
                 String sRIDString = lore.get(lore.size()-1).toString();
                                 
                 if(sRIDString.startsWith(RID_PREFIX))
                 {
                     sPlayerName = sPlayerName.toLowerCase();
                     int riId = Integer.parseInt(sRIDString.substring(RID_PREFIX.length()));
                     
                     if(playerRareItems.containsKey(sPlayerName)
                    && playerRareItems.get(sPlayerName).containsKey(riId)
                    && (ignoreCheckout || RareItems.vcm.isCheckedOut(sPlayerName,riId)))
                     {
                         return playerRareItems.get(sPlayerName).get(riId);
                     }
                 }
             }
         }
         
         return null;
     }
 
     public boolean isRareItem(ItemStack is, int rid)
     {
         if(is != null && is.getType() != Material.AIR && ((CraftItemStack) is).getHandle().tag != null)
         {
             NBTTagList lore = ((CraftItemStack) is)
                 .getHandle().tag.getCompound("display").getList("Lore");
 
             if(lore != null && lore.size() > 0)
             {
                 String sRIDString = lore.get(lore.size()-1).toString();
                                 
                 if(sRIDString.startsWith(RID_PREFIX))
                 {                    
                     if(rid == Integer.parseInt(sRIDString.substring(RID_PREFIX.length())))
                     {
                         return true;
                     }
                 }
             }
         }
         
         return false;
     }
 
     public boolean isAnotherPlayersRareItem(ItemStack is,String sPlayerName)
     {
         if(is != null && is.getType() != Material.AIR && ((CraftItemStack) is).getHandle().tag != null)
         {
             NBTTagList lore = ((CraftItemStack) is)
                 .getHandle().tag.getCompound("display").getList("Lore");
 
             if(lore != null && lore.size() > 0)
             {
                 String sRIDString = lore.get(lore.size()-1).toString();
                                 
                 if(sRIDString.startsWith(RID_PREFIX))
                 {
                     sPlayerName = sPlayerName.toLowerCase();
                     int riId = Integer.parseInt(sRIDString.substring(RID_PREFIX.length()));
                     
                     if(!playerRareItems.containsKey(sPlayerName)
                     || !playerRareItems.get(sPlayerName).containsKey(riId))
                     {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
     
     public boolean playerHasItemProperty(String playerName, int id)
     {
         if(playerActiveEffects.containsKey(playerName))
         {
             return playerActiveEffects.get(playerName).containsKey(itemPropertiesIdLookup.get(id));
         }
         return false;
     }
 
     public void revokeAllItemProperties(String playerName)
     {
         if(playerRareItems.containsKey(playerName.toLowerCase()))
         {
             for(RareItem ri : playerRareItems.get(playerName.toLowerCase()).values())
             {
                 ri.revokeItemProperties();
             }
             //this.playerRareItems.remove(playerName.toLowerCase());
         }
     }
 
     public ItemProperty getItemProperty(int itempropertyID)
     {
         return this.itemPropertiesIdLookup.get(itempropertyID);
     }
 
     public void addPlayerAvailableRareItem(RareItem rareItem)
     {
         String sPlayerName = rareItem.getOwner().toLowerCase();
         
         if(!this.playerRareItems.containsKey(sPlayerName))
         {
             this.playerRareItems.put(sPlayerName, new HashMap<Integer,RareItem>());
         }
         
         this.playerRareItems.get(sPlayerName).put(rareItem.getId(),rareItem);
     }
 
     public boolean hasRareItems(String sPlayerName)
     {
         return this.playerRareItems.containsKey(sPlayerName.toLowerCase());
     }
 
     public void fillWithAvailableRareItems(Player p, Inventory inv)
     {
         if(playerRareItems.containsKey(p.getName().toLowerCase()))
         {
             for(RareItem ri : this.playerRareItems.get(p.getName().toLowerCase()).values())
             {
                 if(!RareItems.vcm.isCheckedOut(ri))
                 {
                     inv.addItem(ri.generateItemStack());
                 }
             }
         }
     }
 
     private void storeItemProperty(ItemProperty ip)
     {
         itemProperties.put(ip.getName(), ip);
         itemPropertiesIdLookup.put(ip.getId(), ip);
         activeEffects.put(ip, new HashMap<String,Integer>());
     }
     
     public String getRidPrefix()
     {
         return this.RID_PREFIX;
     }
 
     public void grantPlayerEffect(String name, ItemProperty ip, int level)
     {
         if(!playerActiveEffects.containsKey(name))
         {
             playerActiveEffects.put(name, new HashMap<ItemProperty,Integer>());
         }
         
         activeEffects.get(ip).put(name,level);
         playerActiveEffects.get(name).put(ip, level);
     }
 
     public void revokePlayerEffect(String name, ItemProperty ip)
     {
         if(playerActiveEffects.containsKey(name))
         {
             activeEffects.get(ip).remove(name);
             playerActiveEffects.get(name).remove(ip);
         
             if(playerActiveEffects.get(name).isEmpty())
             {
                 playerActiveEffects.remove(name);
             }
         }
     }
 
     public HashMap<String, Integer> getEffectActivePlayers(ItemProperty ip)
     {
         return this.activeEffects.get(ip);
     }
 
     public int getPlayerEffectLevel(String playerName, int ipId)
     {
         return this.playerActiveEffects.get(playerName).get(this.itemPropertiesIdLookup.get(ipId));
     }
 
     public Map<Integer, RareItem> getRareItems(String sPlayerName)
     {
         if(this.playerRareItems.containsKey(sPlayerName.toLowerCase()))
         {
             return this.playerRareItems.get(sPlayerName.toLowerCase());
         }
         return null;
     }
 
     public RareItem getRareItem(String sPlayerName, int rid)
     {
         sPlayerName = sPlayerName.toLowerCase();
         
         if(this.playerRareItems.containsKey(sPlayerName))
         {
             return this.playerRareItems.get(sPlayerName).get(rid);
         }
         return null;
     }
 
     public void refreshArmor(Player p)
     {
         revokeAllItemProperties(p.getName());
         
         if(p != null)
         {
             if(p.getInventory().getArmorContents().length > 0)
             {
                 ItemStack[] armor = p.getInventory().getArmorContents();
 
                 for(int i=0;i<armor.length;i++)
                 {
                     RareItem ri = RareItems.rig.getRareItem(p.getName(),armor[i]);
 
                     if(ri != null)
                     {
                         ri.onEquipped(Bukkit.getPlayer(p.getName()));
                     }
                 }
             }
         }
     }
 
     public void removeAllPlayerRareItems(String sPlayerName)
     {
         if(playerRareItems.containsKey(sPlayerName.toLowerCase()))
         {
             playerRareItems.remove(sPlayerName.toLowerCase());
         }
     }
 }

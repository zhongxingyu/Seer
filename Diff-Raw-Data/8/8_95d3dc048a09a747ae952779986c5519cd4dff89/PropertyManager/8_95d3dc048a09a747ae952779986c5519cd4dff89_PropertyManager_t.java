 package com.ne0nx3r0.rareitemhunter.property;
 
 import com.ne0nx3r0.rareitemhunter.RareItemHunter;
 import com.ne0nx3r0.rareitemhunter.property.ability.*;
 import com.ne0nx3r0.rareitemhunter.property.enchantment.*;
 import com.ne0nx3r0.rareitemhunter.property.skill.*;
 import com.ne0nx3r0.rareitemhunter.property.spell.*;
 import com.ne0nx3r0.util.FireworkVisualEffect;
 import com.ne0nx3r0.util.RomanNumeral;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitTask;
 
 public class PropertyManager
 {
     private RareItemHunter plugin;
     private EnumMap<ItemPropertyTypes, String> TYPE_PREFIXES;
     private Map<String,ItemProperty> properties;
     private Map<String,Map<ItemProperty,Integer>> activePlayerEffects;
     private Map<String,Map<ItemProperty,BukkitTask>> playerTemporaryEffectTaskIds;
     private final FireworkVisualEffect fireworks;
     
     public PropertyManager(RareItemHunter plugin)
     {
         this.plugin = plugin;
         
         this.fireworks = new FireworkVisualEffect();
         
         this.activePlayerEffects = new HashMap<String,Map<ItemProperty,Integer>>();
         this.playerTemporaryEffectTaskIds = new HashMap<String,Map<ItemProperty,BukkitTask>>();
 
         TYPE_PREFIXES = new EnumMap<ItemPropertyTypes,String>(ItemPropertyTypes.class);
         TYPE_PREFIXES.put(ItemPropertyTypes.SKILL, ChatColor.GRAY+"Skill: "+ChatColor.RED);
         TYPE_PREFIXES.put(ItemPropertyTypes.ENCHANTMENT, ChatColor.GRAY+"Enchantment: "+ChatColor.GREEN);
         TYPE_PREFIXES.put(ItemPropertyTypes.SPELL, ChatColor.GRAY+"Spell: "+ChatColor.LIGHT_PURPLE);
         TYPE_PREFIXES.put(ItemPropertyTypes.ABILITY, ChatColor.GRAY+"Ability: "+ChatColor.GOLD);
         TYPE_PREFIXES.put(ItemPropertyTypes.VISUAL, ChatColor.GRAY+"Visual: "+ChatColor.BLACK);
         
         properties = new HashMap<String,ItemProperty>();
 
         File propertyCostsFile = new File(plugin.getDataFolder(),"property_costs.yml");
         
         if(!propertyCostsFile.exists())
         {
             plugin.copy(plugin.getResource("property_costs.yml"), propertyCostsFile);
         }     
         
         FileConfiguration propertyCostsYml = YamlConfiguration.loadConfiguration(propertyCostsFile);
         
         this.addProperty(propertyCostsYml,new Poison());
         this.addProperty(propertyCostsYml,new Backstab());
         this.addProperty(propertyCostsYml,new Blinding());
         this.addProperty(propertyCostsYml,new CallLightning());
         this.addProperty(propertyCostsYml,new Confuse());
         this.addProperty(propertyCostsYml,new Disarm());
         this.addProperty(propertyCostsYml,new Slow());
         this.addProperty(propertyCostsYml,new VampiricRegeneration());
         this.addProperty(propertyCostsYml,new Weaken());
         
         
         this.addProperty(propertyCostsYml,new Fertilize());
         this.addProperty(propertyCostsYml,new FireHandling());
         //this.addProperty(propertyCostsYml,new HalfBakedIdea());
         this.addProperty(propertyCostsYml,new MeltObsidian());
         this.addProperty(propertyCostsYml,new PaintWool());
         this.addProperty(propertyCostsYml,new Smelt());
         this.addProperty(propertyCostsYml,new Spore());
         
         this.addProperty(propertyCostsYml,new Durability());
         this.addProperty(propertyCostsYml,new Fly(this));
         this.addProperty(propertyCostsYml,new Hardy());
         this.addProperty(propertyCostsYml,new Regeneration());
         this.addProperty(propertyCostsYml,new Strength());
         //this.addProperty(propertyCostsYml,new ToughLove());
         this.addProperty(propertyCostsYml,new WaterBreathing());
         
         this.addProperty(propertyCostsYml,new Burst(fireworks));
         this.addProperty(propertyCostsYml,new CatsFeet(this));
         this.addProperty(propertyCostsYml,new CraftItem());
         this.addProperty(propertyCostsYml,new FireResistance());
         this.addProperty(propertyCostsYml,new GreaterBurst(fireworks));
         this.addProperty(propertyCostsYml,new GrowTree());
         this.addProperty(propertyCostsYml,new Haste());
         this.addProperty(propertyCostsYml,new Invisibility());
         this.addProperty(propertyCostsYml,new MagicBag());
         this.addProperty(propertyCostsYml,new RepairItem());
         this.addProperty(propertyCostsYml,new SummonBat());
         this.addProperty(propertyCostsYml,new SummonChicken());
         this.addProperty(propertyCostsYml,new SummonCow());
         this.addProperty(propertyCostsYml,new SummonMooshroom());
         this.addProperty(propertyCostsYml,new SummonOcelot());
         this.addProperty(propertyCostsYml,new SummonPig());
         this.addProperty(propertyCostsYml,new SummonSheep());
         this.addProperty(propertyCostsYml,new SummonSlime());
         
         try
         {
             propertyCostsYml.save(propertyCostsFile);
         }
         catch (IOException ex)
         {
             Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, ex);
             plugin.getLogger().log(Level.WARNING,"Unable to save property_costs.yml!");
         }
     }
 
     private void addProperty(FileConfiguration yml,ItemProperty icp)
     {               
         if(!yml.isSet(icp.getName()))
         {
             //TODO: wtf was going on here with getCost requiring a level?
             yml.set(icp.getName(), icp.getCost(1));
         }
         
         int iCost = yml.getInt(icp.getName());
         
         if(iCost == -1)
         {
             return;
         }
         
         icp.setCost(iCost);
 
         this.properties.put(icp.getName().toLowerCase(), icp);
     } 
 
     public ItemProperty getPropertyFromComponent(ItemStack is)
     {
         if(is != null
         && is.getItemMeta() != null
         && is.getItemMeta().getLore() != null)
         {
             String sPropertyString = is.getItemMeta().getLore().get(0);
             
             for(String sPrefix : this.TYPE_PREFIXES.values())
             {
                 if(sPropertyString.startsWith(sPrefix))
                 {
                     String sPropertyName = sPropertyString.substring(sPrefix.length());
 
                     return this.properties.get(sPropertyName.toLowerCase());
                 }
             }
         }
         
         return null;
     }
 
     public String getPropertyString(ItemProperty icp, Integer level)
     {
         return this.TYPE_PREFIXES.get(icp.getType())+icp.getName() + " " + RomanNumeral.convertToRoman(level);
     }
   
 //Wrappers  
     public void onArrowHitEntity(Player shooter, ItemStack bow, EntityDamageByEntityEvent e)
     {
         this.ActivatePlayerRareItem(shooter, bow, e, ItemPropertyActions.ARROW_HIT_ENTITY);
     }
 
     public void onDamagedOtherEntity(Player attacker, EntityDamageByEntityEvent e)
     {
         this.ActivatePlayerRareItem(attacker, attacker.getItemInHand(), e, ItemPropertyActions.DAMAGE_OTHER_ENTITY);
     }
 
     public void onInteract(Player player, ItemStack itemInHand, PlayerInteractEvent e)
     {
         this.ActivatePlayerRareItem(player, itemInHand, e, ItemPropertyActions.INTERACT);
     }
 
     public void onInteractEntity(PlayerInteractEntityEvent e)
     {
         this.ActivatePlayerRareItem(e.getPlayer(), e.getPlayer().getItemInHand(), e, ItemPropertyActions.INTERACT_ENTITY);
     }
     
     public void onEquip(InventoryClickEvent e)
     {
         this.ActivatePlayerRareItem((Player) e.getWhoClicked(), e.getCursor(), e, ItemPropertyActions.EQUIP);
     }
     
     public void onJoin(Player p)
     {        
         ItemStack[] armor = p.getInventory().getArmorContents();
         
         if(armor.length > 0)
         {
             for(int i=0;i<armor.length;i++)
             {
                 this.ActivatePlayerRareItem(p, armor[i], null, ItemPropertyActions.EQUIP);
             }
         }
     }
 
     public void onUnequip(InventoryClickEvent e)
     {
         this.ActivatePlayerRareItem((Player) e.getWhoClicked(), e.getCurrentItem(), e, ItemPropertyActions.UNEQUIP);
     }
     
     private void ActivatePlayerRareItem(Player player,ItemStack is,Event event,ItemPropertyActions action)
     {
         if(is == null
         || !is.hasItemMeta()
         || !is.getItemMeta().hasLore())
         {
             return;
         }
         
         List<String> lore = is.getItemMeta().getLore();
         
         if(!lore.get(0).equals(plugin.RAREITEM_HEADER_STRING))
         {
             return;
         }
         
         lore.remove(0);
         
         for(String sLore : lore)
         {
             for(String sPrefix : TYPE_PREFIXES.values())
             {
                 if(sLore.startsWith(sPrefix))
                 {
                     String sPropertyString = sLore.substring(sPrefix.length());
                     
                     int level = 1;
                     
                     String sLevel = sPropertyString.substring(sPropertyString.lastIndexOf(" ")+1);
 
                     try
                     {
                         level = RomanNumeral.valueOf(sLevel);
                         sPropertyString = sPropertyString.substring(0,sPropertyString.lastIndexOf(" "));
                     }
                     catch(Exception ex){}
 
                     ItemProperty property = this.properties.get(sPropertyString.toLowerCase());
                     
                     if(property != null)
                     {
                         int levelIncrement = plugin.COST_LEVEL_INCREMENT;
                         
                         if(levelIncrement > 1)
                         {
                             levelIncrement = level / levelIncrement;
                         }
 
                         int cost = (property.getCost(level) - levelIncrement) * plugin.COST_MULTIPLIER;
                         
                         boolean hasCost = this.hasCost(player, cost);
 
                         if(action == ItemPropertyActions.INTERACT)
                         {
                             if(property.getType() == ItemPropertyTypes.ENCHANTMENT || property.getType() == ItemPropertyTypes.SPELL)
                             {
                                 if(hasCost)
                                 {
                                     if(property.onInteract((PlayerInteractEvent) event, level))
                                     {
                                         this.takeCost(player, cost);
                                     }
                                 }
                                 else
                                 {
                                     this.sendCostMessage(player,property,cost);
                                 }
                             }
                         }
                         else if(action == ItemPropertyActions.ARROW_HIT_ENTITY)
                         {
                             if(property.getType() == ItemPropertyTypes.BOW)
                             {
                                 if(hasCost)
                                 {
                                     if(property.onArrowHitEntity((EntityDamageByEntityEvent) event, player, level))
                                     {
                                         this.takeCost(player, cost);
                                     }
                                 }
                                 else
                                 {
                                     this.sendCostMessage(player,property,cost);
                                 }
                             }
                         }
                         else if(action == ItemPropertyActions.DAMAGE_OTHER_ENTITY)
                         {
                             if(property.getType() == ItemPropertyTypes.SKILL)
                             {
                                 if(hasCost)
                                 {
                                     if(property.onDamageOther((EntityDamageByEntityEvent) event, player, level))
                                     {
                                         this.takeCost(player, cost);
                                     }
                                 }
                                 else
                                 {
                                     this.sendCostMessage(player,property,cost);
                                 }
                             }
                         }
                         else if(action == ItemPropertyActions.INTERACT_ENTITY)
                         {
                             if(property.getType() == ItemPropertyTypes.SPELL)
                             {
                                 if(hasCost)
                                 {
                                     if(property.onInteractEntity((PlayerInteractEntityEvent) event, level))
                                     {
                                         this.takeCost(player, cost);
                                     }
                                 }
                                 else
                                 {
                                     this.sendCostMessage(player,property,cost);
                                 }
                             }
                         }
                         else if(action == ItemPropertyActions.EQUIP)
                         {
                             if(property.getType() == ItemPropertyTypes.ABILITY)
                             {
                                 this.grantPlayerEffect(player,property,level);
         
                                 property.onEquip(player,level);
                             }
                         }
                         else if(action == ItemPropertyActions.UNEQUIP)
                         {
                             if(property.getType() == ItemPropertyTypes.ABILITY)
                             {
                                 this.revokePlayerEffect(player,property,level);
         
                                 property.onUnequip(player,level);
                             }
                         }
                     }
                 }
             }            
         }
     }
 
     public boolean hasCost(Player pShooter, int cost)
     {
         if(plugin.COST_TYPE == ItemPropertyCostTypes.FOOD)
         {
             if(pShooter.getFoodLevel() >= cost)
             {
                 return true;
             }
         }        
         else if(plugin.COST_TYPE == ItemPropertyCostTypes.XP)
         {
            if(pShooter.getTotalExperience() >= cost)
             {
                 return true;
             }
         }        
         else if(plugin.COST_TYPE == ItemPropertyCostTypes.MONEY)
         {
             if(plugin.economy.has(pShooter.getName(), cost))
             {
                 return true;
             }
         }
         return false;
     }
     
     public void takeCost(Player player, int cost)
     {   
         if(plugin.COST_TYPE == ItemPropertyCostTypes.FOOD)
         {
             if(player.getFoodLevel() >= cost)
             {
                 player.setFoodLevel(player.getFoodLevel() - cost);
             }
         }        
         else if(plugin.COST_TYPE == ItemPropertyCostTypes.XP)
         {
            if(player.getTotalExperience() >= cost)
             {
                player.setTotalExperience(player.getTotalExperience() - cost);
             }
         }        
         else if(plugin.COST_TYPE == ItemPropertyCostTypes.MONEY)
         {
             if(plugin.economy.has(player.getName(), cost))
             {
                 plugin.economy.withdrawPlayer(player.getName(), cost);
             }
         }
     }
 
     public ItemProperty getProperty(String property)
     {
         return this.properties.get(property.toLowerCase());
     }
 
     public String getPropertyComponentString(ItemProperty ip)
     {
         return this.TYPE_PREFIXES.get(ip.getType())+ip.getName();
     }
 
     public void sendCostMessage(Player player, ItemProperty property, int cost)
     {
         player.sendMessage(ChatColor.RED+"You need at least "+cost+" "+plugin.COST_TYPE.name().toLowerCase()+" to use "+property.getName()+"!");
     }
 
     public void grantPlayerEffect(Player player, ItemProperty property, int level)
     {
         Map<ItemProperty, Integer> playerEffects;
         
         if(this.activePlayerEffects.containsKey(player.getName()))
         {
             playerEffects = this.activePlayerEffects.get(player.getName());
         }
         else
         {
             playerEffects = new HashMap<ItemProperty,Integer>();
             
             this.activePlayerEffects.put(player.getName(),playerEffects);
         }
         
         playerEffects.put(property, level);
     }
 
     public void revokePlayerEffect(Player player, ItemProperty property, int level)
     {
         this.revokePlayerEffect(player.getName(), property, level);
     }
 
     private void revokePlayerEffect(String sPlayer, ItemProperty property, int level)
     {
         Map<ItemProperty, Integer> playerEffects = this.activePlayerEffects.get(sPlayer);
         
         if(playerEffects != null)
         {
             playerEffects.remove(property);
         
             if(playerEffects.isEmpty())
             {
                 this.activePlayerEffects.remove(sPlayer);
             }
         }
     }
 
     public void revokeAllItemProperties(Player player)
     {
         if(activePlayerEffects.containsKey(player.getName()))
         {
             Map<ItemProperty, Integer> playerEffects = activePlayerEffects.get(player.getName());
             
             for(ItemProperty ip : playerEffects.keySet())
             {
                 this.revokePlayerEffect(player, ip, playerEffects.get(ip));
             }
         }
     }
 
     public void addTemporaryEffect(Player player, final ItemProperty property, final int level, int duration)
     {
         final String sPlayer = player.getName();
         
         Map<ItemProperty,BukkitTask> taskIds;
         
         if(!this.playerTemporaryEffectTaskIds.containsKey(sPlayer))
         {
             taskIds = playerTemporaryEffectTaskIds.put(sPlayer, new HashMap<ItemProperty,BukkitTask>());
         }
         else
         {
             taskIds = playerTemporaryEffectTaskIds.get(sPlayer);
             
             if(taskIds.containsKey(property))
             {
                 taskIds.get(property).cancel();
             }
         }
         
         final PropertyManager pm = this;
         
         taskIds.put(property, plugin.getServer().getScheduler().runTaskLater(plugin,new Runnable()
         {
             @Override
             public void run()
             {
                 pm.revokePlayerEffect(sPlayer, property, level);
             }
         },duration));
     }
 
     public Collection<ItemProperty> getAllProperties()
     {
         return this.properties.values();
     }
 }

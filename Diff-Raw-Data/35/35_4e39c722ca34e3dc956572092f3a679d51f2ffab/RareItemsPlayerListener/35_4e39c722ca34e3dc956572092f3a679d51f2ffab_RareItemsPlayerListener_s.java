 package com.gmail.ne0nx3r0.rareitems.listeners;
 
 import com.gmail.ne0nx3r0.rareitems.RareItems;
 import com.gmail.ne0nx3r0.rareitems.inventory.VirtualChest;
 import com.gmail.ne0nx3r0.rareitems.item.RareItem;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.Bukkit;
 import org.bukkit.EntityEffect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.metadata.Metadatable;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class RareItemsPlayerListener implements Listener
 {
     @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
     public void onPlayerJoin(PlayerJoinEvent e)
     {        
         RareItems.pm.loadPlayerProfile(e.getPlayer());
             
         RareItems.am.addPlayerToQueue(e.getPlayer());
     }
 
     @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerQuit(PlayerQuitEvent e)
     {
         RareItems.pm.removePlayerProfile(e.getPlayer());
             
         RareItems.am.removePlayerFromQueue(e.getPlayer());
     }
     
     @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
     public void onPlayerChangeWorld(PlayerChangedWorldEvent e)
     {
         final String sPlayer = e.getPlayer().getName();
         
         RareItems.self.getServer().getScheduler().scheduleSyncDelayedTask(RareItems.self, new Runnable() {
             @Override 
             public void run()
             {
                 Player p = Bukkit.getPlayer(sPlayer);
                 if(p != null)
                 {
                     RareItems.pm.refreshArmor(p);
                 }
             }
         },20);
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent e)
     {
         if((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
         && e.getPlayer().getItemInHand() != null
         && e.getPlayer().getItemInHand().getType() != Material.AIR)
         {
             RareItem ri = RareItems.pm.getRareItem(e.getPlayer(),e.getPlayer().getItemInHand());
 
             if(ri != null)
             {
                 ri.onInteract(e);
             }
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerInteractedWithEntity(PlayerInteractEntityEvent e)
     {
         RareItem ri = RareItems.pm.getRareItem(e.getPlayer(),e.getPlayer().getItemInHand());
 
         if(ri != null)
         {
             ri.onInteractEntity(e);
         }
     }
     
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerInventoryClick(InventoryClickEvent e)
     {
         if(e.getSlotType() == SlotType.ARMOR)
         {
             if(e.getCursor() != null && e.getCursor().getType() != Material.AIR)//equipped item
             {
                 RareItem riEquipped = RareItems.pm.getRareItem((Player) e.getWhoClicked(),e.getCursor());
                 if(riEquipped != null)
                 {
                     riEquipped.onEquipped(((Player) e.getWhoClicked()));
                 }
             }
             if(e.getCurrentItem().getType() != Material.AIR)//unequipped item
             {                
                 RareItem riUnequipped = RareItems.pm.getRareItem((Player) e.getWhoClicked(),e.getCurrentItem());
                 
                 if(riUnequipped != null)
                 {
                     riUnequipped.onUnequipped(((Player) e.getWhoClicked()));
                 }
             }
         }
         
         if(e.getInventory().getHolder() instanceof VirtualChest)
         {
             if(e.isShiftClick())
             {
                 e.setCancelled(true);
                 return;
             }
             Player p = (Player) e.getWhoClicked();
             
             if(e.getRawSlot() < 54)
             {
                 if(e.getCursor() != null && e.getCursor().getType() != Material.AIR)//putting down
                 {
                     RareItem riPuttingDown = RareItems.pm.getRareItem(((Player) e.getWhoClicked()),e.getCursor(),true);
 
                     if(riPuttingDown == null 
                     || !riPuttingDown.getOwner().equalsIgnoreCase(p.getName()))
                     {
                         p.sendMessage("You can only check in your own RareItems!");
 
                         e.setCancelled(true);
                     }
                     else
                     {
                         if(!RareItems.pm.checkInRareItem(riPuttingDown,p))
                         {
                             p.sendMessage("You cannot check in that item");
 
                             e.setCancelled(true);
                         }
                     }
                      
                 }
 
                 if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR)//picking up
                 {
                     RareItem riPickingUp = RareItems.pm.getRareItem(p,e.getCurrentItem(),true);
 
                     if(riPickingUp == null 
                     || !riPickingUp.getOwner().equalsIgnoreCase(p.getName()))
                     {
                         p.sendMessage("You can only check out your own RareItems!");
 
                         e.setCancelled(true);
                     }
                     else if(!RareItems.pm.checkOutRareItem(riPickingUp,p))
                     {
                         p.sendMessage("You cannot check out that item");
 
                         e.setCancelled(true);
                     }
                     else if(!riPickingUp.isAllowed())
                     {
                         p.sendMessage("You cannot use that rare item on this server!");
 
                         e.setCancelled(true);
                     }
                 }  
             }
         }
     }
     
     @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
     public void onEntityDamaged(EntityDamageEvent e)
     {
         if(e.getEntity() instanceof Player)
         {
            String sPlayerName = ((Player) e.getEntity()).getName();
             Set<Integer> playerActiveItemProperties = RareItems.ipm.getPlayerActiveItemProperties(sPlayerName);
            
             if(playerActiveItemProperties != null)
             {
                 if(e.getCause() == EntityDamageEvent.DamageCause.FALL
                 && playerActiveItemProperties.contains(40))//Cat's Feet
                 {
                     e.setCancelled(true);
                 }
                 else if(e.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)
                 && playerActiveItemProperties.contains(7))//Water Breathing
                 {
                         e.setCancelled(true);
                 }
                 else if(playerActiveItemProperties.contains(5))//Hardy
                 {
                     e.setDamage(e.getDamage()-RareItems.ipm.getPlayerEffectLevel(((Player) e.getEntity()).getName(), 5));
                 }
                 else if(playerActiveItemProperties.contains(47))//Tough Love
                 {
                     Player p = ((Player) e.getEntity());
                     Location l = p.getLocation();
                     l.setY(400);
                     final LivingEntity kludgeE = (LivingEntity) p.getWorld().spawnEntity(l, EntityType.WOLF);
                     kludgeE.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,100,100));
                     kludgeE.teleport(p.getLocation());
 
                     RareItems.self.getServer().getScheduler().runTaskLater(RareItems.self,  new Runnable() {
                         @Override  
                         public void run()
                         {
                             kludgeE.playEffect(EntityEffect.WOLF_HEARTS);
                             kludgeE.remove();
                         }
                     },3);
                 }
             }
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDamagedByEntity(EntityDamageByEntityEvent e)
     {
         //Let the bow hit handler deal with RI arrows
         if(e.getDamager() instanceof Arrow)
         {
             Arrow arrow = (Arrow) e.getDamager();
 
             MetadataValue mdvRid = getRareItemMetaData(arrow,"rid");
             
             if(mdvRid != null)
             {
                 int rid = mdvRid.asInt();
                 
                 Player p = Bukkit.getPlayer(getRareItemMetaData(arrow,"shooter").asString());
 
                 if(p != null)
                 {
                     RareItem ri = RareItems.pm.getRareItem(p, rid);
 
                     if(ri != null)
                     {
                         ri.onArrowHitEntity(e,p);
                     }
                 }
             }
         }
         else if(e.getDamager() instanceof Player)
         {
             Player attacker = (Player) e.getDamager();
 
             //Strength Ability
             if(RareItems.ipm.playerHasActiveItemProperty(attacker.getName(),5))//Strength
             {
                 e.setDamage(e.getDamage()
                     +RareItems.ipm.getPlayerEffectLevel(attacker.getName(),5));//Strength
             }
 
             RareItem ri = RareItems.pm.getRareItem(attacker, attacker.getItemInHand());
 
             if(ri != null)
             {
                 ri.onDamagedOther(e);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onEntityShootBow(EntityShootBowEvent e)
     {
         if ((e.getEntity() instanceof Player))
         {
             Player p = (Player) e.getEntity();
             
             RareItem ri = RareItems.pm.getRareItem(p,p.getItemInHand());
 
             //Save RI info for when the arrow hits
             if(ri != null)
             {
                 Arrow arrow = (Arrow) e.getProjectile();
             
                 arrow.setMetadata("rid", new FixedMetadataValue(RareItems.self, ri.getId()));
                 arrow.setMetadata("shooter", new FixedMetadataValue(RareItems.self, p.getName()));
             }
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onProjectileHit(ProjectileHitEvent e)
     {
         MetadataValue mdvRid = getRareItemMetaData(e.getEntity(), "rid");
         
         if(mdvRid != null)
         {
             int rid = mdvRid.asInt();
             Player p = Bukkit.getPlayer(getRareItemMetaData(e.getEntity(),"shooter").asString());
             
             if(p != null)
             {
                 RareItem ri = RareItems.pm.getRareItem(p, rid);
                 
                 if(ri != null)
                 {
                     ri.onArrowHitGround(e,p);
                 }
             }
         }
     }
         
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent e)
     {
        ArrayList<ItemStack> removeThese = new ArrayList<ItemStack>();
         List<ItemStack> drops = e.getDrops();        
         
        for(int i = 0;i<drops.size();i++)
         {
            if(RareItems.pm.getRareItem(e.getEntity(), drops.get(i)) != null)
             {
                removeThese.add(drops.get(i));
             }
        }
        
        for(ItemStack isRemove : removeThese)
        {
            drops.remove(isRemove);
         }
     }
     
     public MetadataValue getRareItemMetaData(Metadatable holder,String key)
     {
         List<MetadataValue> metadata = holder.getMetadata(key);
         
         for(MetadataValue mdv : metadata)
         {
             if(mdv.getOwningPlugin().equals(RareItems.self))
             {
                 return mdv;
             }
         }
         
         return null;
     }
 }

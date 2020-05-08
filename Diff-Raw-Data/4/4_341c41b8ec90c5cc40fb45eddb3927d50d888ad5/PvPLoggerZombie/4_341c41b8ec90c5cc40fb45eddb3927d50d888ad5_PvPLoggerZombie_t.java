 package com.pwn9.PwnCombatLoggers;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Zombie;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 public class PvPLoggerZombie 
 {
    public static Set<PvPLoggerZombie> zombies = new HashSet<PvPLoggerZombie>();
    public static Set<String> waitingToDie = new HashSet<String>();
    public static Set<Integer> zombieIds = new HashSet<Integer>();
    public static int HEALTH = 50;
    private double hp = 10;
    private Zombie zombie;
    private String player;
    private ItemStack[] contents;
 
    public PvPLoggerZombie(String player) 
    {
       this.player = player;
       Player p = Bukkit.getPlayer(player);
       hp = p.getHealth();
       zombieIds.add((zombie = (Zombie)p.getWorld().spawnEntity(p.getLocation(), EntityType.ZOMBIE)).getEntityId());
       zombie.getWorld().playEffect(zombie.getLocation(), Effect.MOBSPAWNER_FLAMES, 1, 1);
       zombie.setRemoveWhenFarAway(false);
       invFromPlayer(p); // Take player's inventory and apply it to this zombie.
       Iterator<PvPLoggerZombie> it = zombies.iterator();
       while(it.hasNext()) 
       {
          PvPLoggerZombie pz = it.next();
          if(pz.getPlayer().equalsIgnoreCase(player)) 
          {
             despawnDrop(false);
             it.remove();
          }
       }
       zombies.add(this);
    }
 
    public Zombie getZombie() 
    {
       return zombie;
    }
 
    public void setZombie(Zombie zombie) 
    {
       this.zombie = zombie;
    }
 
    public String getPlayer() 
    {
       return player;
    }
 
    public void setPlayer(String player)
    {
       this.player = player;
    }
 
    @SuppressWarnings("deprecation")
    public void invFromPlayer(Player p) 
    {
 	  PlayerInventory pi = p.getInventory(); 	   
      //zombie.setMaxHealth(getHealth());   not sure what they were doing here...  
	  // (args.length > 0) ? 1 : 2;  - or set to 20..  this probably works better
      zombie.setMaxHealth((getHealth() == 0) ? 1 : getHealth());
       zombie.setHealth(getHealth());
       zombie.setRemoveWhenFarAway(false);
       zombie.setCanPickupItems(false);
       zombie.getEquipment().setArmorContents(pi.getArmorContents());
       zombie.getEquipment().setItemInHand(pi.getItemInHand());
       zombie.getEquipment().setBootsDropChance(0);
       zombie.getEquipment().setChestplateDropChance(0);
       zombie.getEquipment().setHelmetDropChance(0);
       zombie.getEquipment().setLeggingsDropChance(0);
       zombie.getEquipment().setItemInHandDropChance(0);
       pi.setArmorContents(new ItemStack[] { null, null, null, null });
       pi.setItemInHand(null);
       this.contents = pi.getContents();
       // We've saved the player's inventory, now let's wipe it from the player, so no dupes. -Sage905
       p.getInventory().clear();
       p.updateInventory();    
    }
 
 
    @SuppressWarnings("deprecation")
    public void invToPlayer(Player p)
    {
 	   PlayerInventory pi = p.getInventory();
        // Give to Player
 	   pi.setContents(this.contents);
 	   pi.setArmorContents(zombie.getEquipment().getArmorContents());
 	   pi.setItemInHand(zombie.getEquipment().getItemInHand());
 	   p.updateInventory();
 	   // Take from Zombie
        zombie.getEquipment().setBootsDropChance(0);
        zombie.getEquipment().setChestplateDropChance(0);
        zombie.getEquipment().setHelmetDropChance(0);
        zombie.getEquipment().setLeggingsDropChance(0);
        zombie.getEquipment().setItemInHandDropChance(0);
        zombie.getEquipment().setArmorContents(new ItemStack[]{});
        zombie.getEquipment().setItemInHand(null);
    } 
 	
    public List<ItemStack> itemsToDrop() 
    {   
       List<ItemStack> itemsToDrop = new ArrayList<ItemStack>();
       for(ItemStack i : contents) 
       {
          if(i != null) itemsToDrop.add(i);
       }
       return itemsToDrop;
    }
 
    public void despawnNoDrop(boolean giveToOwner, boolean iterate) 
    {
       if(giveToOwner)
       {
          Player p = Bukkit.getPlayer(player);
          if(p == null) 
          {
             PwnCombatLoggers.log(Level.WARNING, "Player was null!");
             return;
          }
          invToPlayer(p);
       }
       zombie.getEquipment().setBootsDropChance(0);
       zombie.getEquipment().setChestplateDropChance(0);
       zombie.getEquipment().setHelmetDropChance(0);
       zombie.getEquipment().setLeggingsDropChance(0);
       zombie.getEquipment().setItemInHandDropChance(0);      
       zombie.remove();
       if(iterate)
          despawn();
    }
 
    public void despawn() 
    {
       Iterator<PvPLoggerZombie> it = zombies.iterator();
       while(it.hasNext()) 
       {
          PvPLoggerZombie pz = it.next();
          if(pz.getPlayer().equalsIgnoreCase(player)) it.remove();
       }
       zombie.remove();
    }
 
    public void despawnDrop(boolean iterate) 
    {
       zombie.setCanPickupItems(false);
       for(ItemStack is : contents)
       {
          if(is != null) 
          {
         	 if (is.getType() != Material.AIR) 
         	 {
         		 zombie.getWorld().dropItemNaturally(zombie.getLocation(), is);
         	 }
          }
       }
       
      // Drop armor in same condition.  Allowing it to drop by the zombie will damage it.
      for (ItemStack is: zombie.getEquipment().getArmorContents()) 
      {
         if(is != null) 
         {
         	if (is.getType() != Material.AIR) 
         	{
         		zombie.getWorld().dropItemNaturally(zombie.getLocation(), is);
         	}
         }
      }
      
      // Same with the ItemInHand
   	 if (zombie.getEquipment().getItemInHand() != null) 
   	 {
   		if (zombie.getEquipment().getItemInHand().getType() != Material.AIR) 
   		{
   			zombie.getWorld().dropItemNaturally(zombie.getLocation(), zombie.getEquipment().getItemInHand());
   		}
   	 } 
       
      zombie.getWorld().playEffect(zombie.getLocation(), Effect.ENDER_SIGNAL, 1, 1);
      zombie.setHealth(0);
      zombie.remove();
      if(iterate)
         despawn();
    }
 
    public static PvPLoggerZombie getByOwner(String owner) 
    {
       for(PvPLoggerZombie pz : zombies) 
       {
          if(pz.getPlayer().equalsIgnoreCase(owner)) return pz;
       }
       return null;
    }
 
    public static PvPLoggerZombie getByZombie(Zombie z)
    {
       for(PvPLoggerZombie pz : zombies) 
       {
          if(zombieEquals(pz.getZombie(), z)) return pz;
       }
       return null;
    }
 
    private static boolean zombieEquals(Zombie z1, Zombie z2)
    {
       return (z1.getEntityId() == (z2.getEntityId()));
    }
 
    public static boolean isPvPZombie(Zombie z) 
    {
       return zombieIds.contains(z.getEntityId());
    }
 
    public void killOwner() 
    {
       waitingToDie.add(player);
    }
 
    public double getHealth() 
    {
       if(! PwnCombatLoggers.keepPlayerHealthZomb)
          return HEALTH;
       else
       {
          return hp;
       }
    }
 
    public double getHealthForOwner() 
    {
       if(! PwnCombatLoggers.keepPlayerHealthZomb)
          return hp;
       else
          return zombie.getHealth();
    }
 }

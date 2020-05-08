 package com.herocraftonline.dev.heroes;
 
 import java.util.List;
 
 import net.minecraft.server.EntityPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerBedLeaveEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.Command;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.hero.HeroManager;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Properties;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class HPlayerListener extends PlayerListener {
 
     public final Heroes plugin;
 
     public HPlayerListener(Heroes instance) {
         plugin = instance;
     }
 
     @Override
     public void onItemHeldChange(PlayerItemHeldEvent event) {
         Player player = event.getPlayer();
         Hero hero = plugin.getHeroManager().getHero(player);
         if (hero.hasEffectType(EffectType.DISARM)) {
             Util.disarmCheck(hero, plugin);
         }
         hero.checkInventory();
     }
 
     @Override
     public void onPlayerBedEnter(PlayerBedEnterEvent event) {
         Properties props = plugin.getConfigManager().getProperties();
         if (event.isCancelled() || !props.bedHeal || props.disabledWorlds.contains(event.getPlayer().getWorld().getName()))
             return;
 
         Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
         long period = props.healInterval * 1000;
         double tickHealPercent = props.healPercent / 100.0;
         BedHealEffect bhEffect = new BedHealEffect(period, tickHealPercent);
         hero.addEffect(bhEffect);
     }
 
     @Override
     public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
         if (!plugin.getConfigManager().getProperties().bedHeal)
             return;
 
         // This player is no longer in bed so remove them from the bedHealer set
         Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
         if (hero.hasEffect("BedHeal")) {
             hero.removeEffect(hero.getEffect("BedHeal"));
         }
     }
 
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         if (event.useItemInHand() == Result.DENY)
             return;
 
         Hero hero = plugin.getHeroManager().getHero(player);
         if (hero.hasEffectType(EffectType.DISARM))
             Util.disarmCheck(hero, plugin);
 
 
         if (!hero.canEquipItem(player.getInventory().getHeldItemSlot())) {
             event.setCancelled(true);
             Util.syncInventory(player, plugin);
             return;
         }
 
         Block clickedBlock = event.getClickedBlock();
         if (clickedBlock != null) {
             switch (clickedBlock.getType()) {
             case DISPENSER:
             case BED:
             case FURNACE:
             case BURNING_FURNACE:
             case WOOD_DOOR:
             case LEVER:
             case IRON_DOOR:
             case JUKEBOX:
             case DIODE_BLOCK_OFF:
             case DIODE_BLOCK_ON:
             case CHEST:
             case LOCKED_CHEST:
             case TRAP_DOOR:
                 hero.cancelDelayedSkill();
                 return;
             }
         }
 
         boolean isStealthy = false;
         if (player.getItemInHand() != null) {
             Material material = player.getItemInHand().getType();
             if (hero.hasBind(material)) {
                 if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                     String[] args = hero.getBind(material);
                     plugin.onCommand(player, null, "skill", args);
                     isStealthy = plugin.getSkillManager().getSkill(args[0]).isType(SkillType.STEALTHY);
                 } else {
                     hero.cancelDelayedSkill();
                 }
             } else {
                 hero.cancelDelayedSkill();
             }
         }
         // Remove effects dependant on non-interaction
         if (!isStealthy) {
             for (Effect effect : hero.getEffects()) {
                 if (effect.isType(EffectType.INVIS))
                    effect.remove(hero);
             }
         }
     }
 
     @Override
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         Hero hero = plugin.getHeroManager().getHero(player);
         hero.syncExperience();
         hero.syncHealth();
         hero.checkInventory();
         if (plugin.getConfigManager().getProperties().prefixClassName) {
             player.setDisplayName("[" + hero.getHeroClass().getName() + "]" + player.getName());
         }
     }
 
     @Override
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         if (event.isCancelled() || plugin.getConfigManager().getProperties().disabledWorlds.contains(event.getPlayer().getWorld().getName()))
             return;
 
         final Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
         if (hero.hasEffectType(EffectType.DISARM) && Util.isWeapon(event.getItem().getItemStack().getType())) {
             event.setCancelled(true);
             return;
         }
 
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 hero.checkInventory();
             }
         });
 
         if (!hero.hasParty())
             return;
         HeroParty party = hero.getParty();
         if (!party.updateMapDisplay() && event.getItem().getItemStack().getType().toString().equalsIgnoreCase("MAP")) {
             party.setUpdateMapDisplay(true);
         }
     }
 
     @Override
     public void onPlayerQuit(PlayerQuitEvent event) {
         Player player = event.getPlayer();
         HeroManager heroManager = plugin.getHeroManager();
         Hero hero = heroManager.getHero(player);
         hero.clearEffects();
         heroManager.saveHero(hero);
         heroManager.removeHero(hero);
         for (Command command : plugin.getCommandHandler().getCommands()) {
             if (command.isInteractive()) {
                 command.cancelInteraction(player);
             }
         }
     }
 
     @Override
     public void onPlayerRespawn(PlayerRespawnEvent event) {
         Player player = event.getPlayer();
         final Hero hero = plugin.getHeroManager().getHero(player);
         hero.setHealth(hero.getMaxHealth());
         hero.setMana(0);
         CraftPlayer craftPlayer = (CraftPlayer) player;
         EntityPlayer entityPlayer = craftPlayer.getHandle();
         entityPlayer.exp = 0;
         entityPlayer.expTotal = 0;
         entityPlayer.expLevel = 0;
         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 hero.syncExperience();
             }
         }, 20L);
     }
 
     @Override
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         if (event.isCancelled())
             return;
 
         Player player = event.getPlayer();
         if (event.getFrom().getWorld() != event.getTo().getWorld()) {
             final Hero hero = plugin.getHeroManager().getHero(player);
             HeroClass heroClass = hero.getHeroClass();
 
             List<Command> commands = plugin.getCommandHandler().getCommands();
             for (Command cmd : commands) {
                 if (cmd instanceof OutsourcedSkill) {
                     OutsourcedSkill skill = (OutsourcedSkill) cmd;
                     if (heroClass.hasSkill(skill.getName())) {
                         skill.tryLearningSkill(hero);
                     }
                 }
             }
             if (plugin.getConfigManager().getProperties().disabledWorlds.contains(event.getTo().getWorld().getName())) {
                 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                     @Override
                     public void run() {
                         hero.checkInventory();
                     }
                 });
             }
         }
     }
 
     public class BedHealEffect extends PeriodicEffect {
 
         private final double tickHealPercent;
 
         public BedHealEffect(long period, double tickHealPercent) {
             super(null, "BedHeal", period);
             this.tickHealPercent = tickHealPercent;
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             this.lastTickTime = System.currentTimeMillis();
         }
 
         @Override
         public void tick(Hero hero) {
             super.tick(hero);
             Player player = hero.getPlayer();
             double healAmount = hero.getMaxHealth() * tickHealPercent;
             hero.setHealth(hero.getHealth() + healAmount);
             hero.syncHealth();
             if (hero.isVerbose()) {
                 player.sendMessage(Messaging.createFullHealthBar(hero.getHealth(), hero.getMaxHealth()));
             }
         }
     }
 }

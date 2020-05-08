 
 package net.specialattack.mobdrop;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.specialattack.core.PluginState;
 import net.specialattack.mobdrop.modifiers.IModifier;
 import net.specialattack.mobdrop.modifiers.ModifierBaby;
 import net.specialattack.mobdrop.modifiers.ModifierChargedCreeper;
 import net.specialattack.mobdrop.modifiers.ModifierEntity;
 import net.specialattack.mobdrop.modifiers.ModifierPermission;
 import net.specialattack.mobdrop.modifiers.ModifierSpawner;
 import net.specialattack.mobdrop.modifiers.ModifierTime;
import net.specialattack.mobdrop.modifiers.ModifierWitherSkeleton;
 import net.specialattack.mobdrop.modifiers.ModifierZombieVillager;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SpAMobDrop extends JavaPlugin {
 
     public static SpAMobDrop instance;
     private static PluginState state = PluginState.Unloaded;
     private PluginDescriptionFile pdf;
     private FileConfiguration config;
     private Logger logger;
     public ArrayList<IModifier> moneyModifiers;
     public ArrayList<LivingEntity> spawnedMobs;
 
     public SpAMobDrop() {
         super();
         state = PluginState.Initializing;
 
         instance = this;
 
         state = PluginState.Initialized;
     }
 
     @Override
     public void onLoad() {
         state = PluginState.Loading;
 
         this.logger = this.getLogger();
 
         state = PluginState.Loaded;
     }
 
     @Override
     public void onEnable() {
         state = PluginState.Enabling;
 
         this.pdf = this.getDescription();
         this.config = this.getConfig();
 
         this.moneyModifiers = new ArrayList<IModifier>();
         this.spawnedMobs = new ArrayList<LivingEntity>();
 
         ConfigurationSection baseBounty = this.config.getConfigurationSection("baseBounty");
         if (baseBounty == null) {
             baseBounty = this.config.createSection("baseBounty");
         }
 
         for (EntityType type : EntityType.values()) {
             if (type.getEntityClass() == null || !LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                 continue;
             }
 
             ModifierEntity modifier = new ModifierEntity(type);
 
             if (!baseBounty.contains(type.name())) {
                 baseBounty.set(type.name(), 10.0D);
             }
 
             modifier.baseMoney = baseBounty.getDouble(type.name(), 0.0D);
 
             this.moneyModifiers.add(modifier);
         }
 
         ConfigurationSection permissions = this.config.getConfigurationSection("permissions");
         if (permissions == null) {
             permissions = this.config.createSection("permissions");
         }
 
         Map<String, Object> values = permissions.getValues(false);
 
         for (Entry<String, Object> entry : values.entrySet()) {
             if (entry.getValue() instanceof ConfigurationSection) {
                 ConfigurationSection section = (ConfigurationSection) entry.getValue();
                 for (EntityType type : EntityType.values()) {
                     if (type.getEntityClass() == null || !LivingEntity.class.isAssignableFrom(type.getEntityClass())) {
                         continue;
                     }
 
                     ModifierPermission modifier = new ModifierPermission(type, entry.getKey());
 
                     if (!section.contains(type.name())) {
                         section.set(type.name(), 0.0D);
                     }
 
                     modifier.modifier = section.getDouble(type.name(), 1.0D);
 
                     this.moneyModifiers.add(modifier);
                 }
             }
         }
 
         ModifierTime day = new ModifierTime(0, 12000);
         if (!this.config.contains("modifierDay")) {
             this.config.set("modifierDay", 1.0D);
         }
         day.modifier = this.config.getDouble("modifierDay", 1.0D);
         this.moneyModifiers.add(day);
 
         ModifierTime dusk = new ModifierTime(12000, 13800);
         if (!this.config.contains("modifierDusk")) {
             this.config.set("modifierDusk", 1.0D);
         }
         dusk.modifier = this.config.getDouble("modifierDusk", 1.0D);
         this.moneyModifiers.add(dusk);
 
         ModifierTime night = new ModifierTime(13800, 22200);
         if (!this.config.contains("modifierNight")) {
             this.config.set("modifierNight", 1.0D);
         }
         night.modifier = this.config.getDouble("modifierNight", 1.0D);
         this.moneyModifiers.add(night);
 
         ModifierTime dawn = new ModifierTime(22200, 24000);
         if (!this.config.contains("modifierDawn")) {
             this.config.set("modifierDawn", 1.0D);
         }
         dawn.modifier = this.config.getDouble("modifierDawn", 1.0D);
         this.moneyModifiers.add(dawn);
 
        ModifierBaby babyAnimal = new ModifierBaby(false);
         if (!this.config.contains("modifierBabyAnimal")) {
             this.config.set("modifierBabyAnimal", 1.0D);
         }
         babyAnimal.modifier = this.config.getDouble("modifierBabyAnimal", 1.0D);
         this.moneyModifiers.add(babyAnimal);
 
         ModifierBaby babyMonster = new ModifierBaby(true);
         if (!this.config.contains("modifierBabyMonster")) {
             this.config.set("modifierBabyMonster", 1.0D);
         }
         babyMonster.modifier = this.config.getDouble("modifierBabyMonster", 1.0D);
         this.moneyModifiers.add(babyMonster);
 
         ModifierChargedCreeper chargedCreeper = new ModifierChargedCreeper();
         if (!this.config.contains("modifierChargedCreeper")) {
             this.config.set("modifierChargedCreeper", 1.0D);
         }
         chargedCreeper.modifier = this.config.getDouble("modifierChargedCreeper", 1.0D);
         this.moneyModifiers.add(chargedCreeper);
 
         ModifierZombieVillager zombieVillager = new ModifierZombieVillager();
         if (!this.config.contains("modifierZombieVillager")) {
             this.config.set("modifierZombieVillager", 1.0D);
         }
         zombieVillager.modifier = this.config.getDouble("modifierZombieVillager", 1.0D);
         this.moneyModifiers.add(zombieVillager);
 
        ModifierWitherSkeleton witherSkeleton = new ModifierWitherSkeleton();
         if (!this.config.contains("modifierWitherSkeleton")) {
             this.config.set("modifierWitherSkeleton", 1.0D);
         }
         witherSkeleton.modifier = this.config.getDouble("modifierWitherSkeleton", 1.0D);
         this.moneyModifiers.add(witherSkeleton);
 
         ModifierSpawner spawner = new ModifierSpawner();
         if (!this.config.contains("modifierSpawner")) {
             this.config.set("modifierSpawner", 0.0D);
         }
         spawner.modifier = this.config.getDouble("modifierSpawner", 0.0D);
         this.moneyModifiers.add(spawner);
 
         this.saveConfig();
 
         Bukkit.getPluginManager().registerEvents(new EventListener(), this);
 
         log(this.pdf.getFullName() + " is now enabled!");
 
         state = PluginState.Enabled;
     }
 
     @Override
     public void onDisable() {
         state = PluginState.Disabling;
 
         log(this.pdf.getFullName() + " is now disabled!");
 
         state = PluginState.Disabled;
     }
 
     public static PluginState getState() {
         return state;
     }
 
     public static void log(String message) {
         instance.logger.log(Level.INFO, message);
     }
 
     public static void log(Level level, String message) {
         instance.logger.log(level, message);
     }
 
     public static void log(Level level, String message, Throwable throwable) {
         instance.logger.log(level, message, throwable);
     }
 
 }

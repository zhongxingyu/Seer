 /*
 This file is part of Legends.
 
     Legends is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Legends is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Legends.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.github.legendsdev.legends.library.database;
 
 import com.github.legendsdev.legends.library.BasicInfo;
 import com.github.legendsdev.legends.library.Configuration;
 import com.github.legendsdev.legends.library.StatsInfo;
 import com.github.legendsdev.legends.library.armor.*;
 import com.github.legendsdev.legends.library.database.helper.YAMLHelper;
 import com.github.legendsdev.legends.library.lclass.*;
 import com.github.legendsdev.legends.library.level.LevelRestricted;
 import com.github.legendsdev.legends.library.lplayer.LPlayer;
 import com.github.legendsdev.legends.library.race.Race;
 import com.github.legendsdev.legends.library.restriction.RestrictionHandler;
 import com.github.legendsdev.legends.library.util.FileClassLoader;
 import com.github.legendsdev.legends.library.weapon.*;
 
 import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Thread safe class for accessing YAML database.
  *
  * @author B2OJustin
  */
 @SuppressWarnings("unchecked")
 public class YAMLDataSource implements DataSource {
     private static final Logger logger = Logger.getLogger(YAMLDataSource.class.getName());
     private String configPath;
     private final String RACE_PATH = "races/";
     private final String SKILL_PATH = "skills/";
     private final String PLAYER_PATH = "players/";
     private final String CLASS_PATH = "classes/";
 
     @Override
     public String getName() {
         return "YAML";
     }
 
     public YAMLDataSource(String configPath) {
         this.configPath = configPath;
         if(!configPath.endsWith("/")) configPath.concat("/");
     }
 
     @Override
     public Logger getLogger() {
         return YAMLDataSource.logger;
     }
 
     @Override
     public synchronized LPlayer loadLPlayer(String name) {
         return null; //TODO loadLPlayer method stub
     }
 
     @Override
     public void saveLPlayer(LPlayer lPlayer) {
         //TODO saveLPlayer method stub
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public synchronized Race loadRace(String name) {
         String filePath = configPath + RACE_PATH + name.replace(" ", "_") + ".yml";
         try {
             Race race;
 
             // Load custom race file if exists
             Class raceClass = FileClassLoader.load(Race.class, configPath + RACE_PATH, name);
             if(raceClass != null) {
                 race = (Race)raceClass.newInstance();
             } else race = new Race();
 
             YAMLHelper yml = new YAMLHelper(filePath);
 
             for(String mainKey : yml.getKeys("")) {
                 switch(mainKey) {
                     case "name":
                         race.setName(yml.getString("name"));
                         break;
                     case "description":
                         race.setDescription(yml.getStringList("description"));
                         break;
                     case "Weapons":
                         RestrictionHandler.getInstance().setWeaponRestrictions(race, loadWeaponRestrictions(race, yml));
                         break;
                     case "Armor":
                         RestrictionHandler.getInstance().setArmorRestrictions(race, loadArmorRestrictions(race, yml));
                         break;
                     case "Class":
                         RestrictionHandler.getInstance().setClassRestrictions(race, loadClassRestrictions(race, yml));
                         break;
                     case "Stats":
                         loadStats(race.getDefaultInfo(), yml);
                 }
             }
 
             return race;
 
         } catch (FileNotFoundException e) {
             logger.warning("Could not find file for race '" + name + "' at " + filePath);
         } catch (ClassCastException e) {
             logger.warning("You seem to have an error in your yaml. Could not load race '" + name + "'");
         } catch(IllegalAccessException | InstantiationException ignored) {}
 
         return null;
     }
 
     protected <T extends BasicInfo> T loadBasicInfo(T basicInfo, String path, YAMLHelper yml) throws ClassCastException {
         for(String infoKey : yml.getKeys(path)) {
             switch(infoKey) {
                 case "bonus-defense":
                     int bonusDefense = yml.getInt(path + ".bonus-defense");
                     basicInfo.addBonusDefense(bonusDefense);
                     break;
                 case "bonus-damage":
                     int bonusDamage = yml.getInt(path + ".bonus-damage");
                     basicInfo.addBonusWeaponDamage(bonusDamage);
                     break;
                 case "bonus-stamina":
                     int bonusStamina = yml.getInt(path + ".bonus-stamina");
                     basicInfo.addBonusStamina(bonusStamina);
                     break;
                 case "bonus-health":
                     int bonusHealth = yml.getInt(path + ".bonus-health");
                     basicInfo.addBonusHealth(bonusHealth);
                     break;
                 case "bonus-mana":
                     int bonusMana = yml.getInt(path + ".bonus-mana");
                     basicInfo.addBonusMana(bonusMana);
                     break;
 
                 // Level restricted
                 case "required-level":
                     if(basicInfo instanceof LevelRestricted) {
                         int requiredLevel = yml.getInt(path + ".required-level");
                         RestrictionHandler.getInstance().getLevelRestrictions((LevelRestricted)basicInfo).setMinLevel(requiredLevel);
                     }
                     break;
                 case "maximum-level":
                     if(basicInfo instanceof  LevelRestricted) {
                         int maximumLevel = yml.getInt(path + ".maximum-level");
                         RestrictionHandler.getInstance().getLevelRestrictions((LevelRestricted)basicInfo).setMaxLevel(maximumLevel);
                     }
                     break;
             }
         }
         return basicInfo;
     }
 
     protected synchronized StatsInfo loadStats(StatsInfo stats, YAMLHelper yml) {
         if(stats == null) return null;
         for(String statKey : yml.getKeys("Stats")) {
             switch(statKey) {
                 case "health-per-level":
                         int healthPerLevel = yml.getInt("Stats.health-per-level");
                         stats.setHealthPerLevel(healthPerLevel);
                     break;
                 case "mana-per-level":
                         int manaPerLevel = yml.getInt("Stats.mana-per-level");
                         stats.setManaPerLevel(manaPerLevel);
                     break;
                 case "stamina-per-level":
                         int staminaPerLevel = yml.getInt("Stats.stamina-per-level");
                         stats.setStaminaPerLevel(staminaPerLevel);
                     break;
             }
         }
         // Basic bonuses
         if(stats instanceof BasicInfo) {
             loadBasicInfo((BasicInfo)stats, "Stats", yml);
         }
         return stats;
     }
 
     protected synchronized WeaponRestrictions loadWeaponRestrictions(WeaponRestricted restricted, YAMLHelper yml) {
         WeaponRestrictions restrictions = new WeaponRestrictions();
         for(String wepKey : yml.getKeys("Weapons")) {
             switch(wepKey) {
                 case "default":
                     if(yml.getString("Weapons.default").equals("allow")) {
                         restrictions.setDefault(true);
                     }
                     else restrictions.setDefault(false);
                     break;
                 case "permitted-weapon":
                     for(String wepID : yml.getKeys("Weapons.permitted-weapon")) {
                         Weapon weapon = WeaponHandler.getInstance().get(wepID);
                         if(weapon != null) {
                             restrictions.setAllowed(weapon, true);
                             if(restricted instanceof WeaponUser) {
                                 loadBasicInfo(((WeaponUser)restricted).getWeaponInfo(weapon), String.format("Weapons.permitted-weapon.%s", wepID), yml);
                             }
                         }
                     }
                     break;
                 case "restricted-weapon":
                     for(String wepID : yml.getStringList("Weapons.restricted-weapon")) {
                         Weapon weapon = WeaponHandler.getInstance().get(wepID);
                         restrictions.setAllowed(weapon, false);
                     }
                     break;
             }
         }
         return restrictions;
     }
 
     protected ArmorRestrictions loadArmorRestrictions(ArmorRestricted restricted, YAMLHelper yml) {
         ArmorRestrictions restrictions = new ArmorRestrictions();
         for(String armorKey : yml.getKeys("Armor")) {
             switch(armorKey) {
                 case "default":
                     if(yml.getString("Armor.default").equals("allow")) {
                         restrictions.setDefault(true);
                     }
                     else restrictions.setDefault(false);
                     break;
                 case "permitted-armor":
                     for(String armorID : yml.getKeys("Armor.permitted-armor")) {
                         Armor armor = ArmorHandler.getInstance().get(armorID);
                         if(armor != null) {
                             restrictions.setAllowed(armor, true);
                             if(restricted instanceof ArmorUser) {
                                 loadBasicInfo(((ArmorUser)restricted).getArmorInfo(armor), String.format("Armor.permitted-armor.%s", armorID), yml);
                             }
                         }
                     }
                     break;
                 case "restricted-armor":
                     for(String armorID : yml.getStringList("Armor.restricted-armor")) {
                         Armor armor = ArmorHandler.getInstance().get(armorID);
                         restrictions.setAllowed(armor, false);
                     }
             }
         }
         return restrictions;
     }
 
     protected LClassRestrictions loadClassRestrictions(LClassRestricted restricted, YAMLHelper yml) {
         LClassRestrictions restrictions = new LClassRestrictions();
         for(String classKey : yml.getKeys("Class")) {
             switch(classKey) {
                 case "default":
                     if(yml.getString("Class.default").equals("allow")) {
                         restrictions.setDefault(true);
                     }
                     else restrictions.setDefault(false);
                     break;
                 case "permitted-class":
                     for(String classID : yml.getKeys("Class.permitted-class")) {
                         LClass lClass = LClassHandler.getInstance().get(classID);
                         if(lClass != null) {
                             restrictions.setAllowed(lClass, true);
                             if(restricted instanceof LClassUser) {
                                 loadBasicInfo(((LClassUser)restricted).getLClassInfo(lClass), String.format("Class.permitted-class.%s", classID), yml);
                             }
                         }
                     }
                     break;
                 case "restricted-class":
                     for(String classID : yml.getKeys("Class.restricted-class")) {
                         LClass lClass = LClassHandler.getInstance().get(classID);
                         if(lClass != null) {
                             restrictions.setAllowed(lClass, false);
                         }
                     }
             }
         }
         return restrictions;
     }
 
 
     @Override
     public synchronized LClass loadLClass(String name) {
         String filePath = configPath + CLASS_PATH + name.replace(" ", "_") + ".yml";
         LClass lClass;
         try {
             // Load custom class file if exists
             Class lClassClass = FileClassLoader.load(Race.class, configPath + CLASS_PATH, name);
             if(lClassClass != null) {
                 lClass = (LClass)lClassClass.newInstance();
             } else lClass = new LClass();
 
             YAMLHelper yml = new YAMLHelper(filePath);
 
             for(String mainKey : yml.getKeys("")) {
                 switch(mainKey) {
                     case "name":
                         lClass.setName(yml.getString("name"));
                         break;
                     case "description":
                         lClass.setDescription(yml.getStringList("description"));
                         break;
                     case "Weapons":
                         RestrictionHandler.getInstance().setWeaponRestrictions(lClass, loadWeaponRestrictions(lClass, yml));
                         break;
                     case "Armor":
                         RestrictionHandler.getInstance().setArmorRestrictions(lClass, loadArmorRestrictions(lClass, yml));
                         break;
                     case "Stats":
                         loadStats(lClass.getDefaultInfo(), yml);
                 }
             }
 
             return lClass;
 
         } catch (FileNotFoundException e) {
             logger.warning("Could not find file for class '" + name + "' at " + filePath);
         } catch (ClassCastException e) {
             logger.warning("You seem to have an error in your yaml. Could not load class '" + name + "'");
         } catch(IllegalAccessException | InstantiationException ignored) {}
 
         return null;
     }
 
     public synchronized Configuration loadConfiguration(Configuration config, String file) throws FileNotFoundException {
         YAMLHelper yml = new YAMLHelper(configPath + file);
 
         // Main configuration
         for(String configKey : yml.getKeys("")) {
             switch(configKey) {
                 case "database-type":
                     config.setDatabaseType(yml.getString(configKey));
                     break;
                 case "default-race":
                     config.setDefaultRace(yml.getString(configKey));
                     break;
                 case "default-primary-class":
                     config.setDefaultPrimaryClass(yml.getString(configKey));
                     break;
                 case "default-secondary-class":
                     config.setDefaultSecondaryClass(yml.getString(configKey));
                     break;
             }
         }
 
         // YAML database configuration
         if(config.getDatabaseType().toUpperCase().equals("YAML")) {
             for(String yamlKey : yml.getKeys("YAML")) {
                 switch(yamlKey.toLowerCase()) {
                     case "config-path":
                         config.setYamlConfigPath(yml.getString("YAML.config-path"));
                         break;
                 }
             }
         }
 
         return config;
     }
 
     // TODO implement yaml configuration saving
     public synchronized YAMLDataSource saveConfiguration(String file) {
         return this;
     }
 }

 package com.herocraftonline.dev.heroes.classes;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.permissions.Permission;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ArmorItems;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ArmorType;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.classes.HeroClass.WeaponItems;
 import com.herocraftonline.dev.heroes.classes.HeroClass.WeaponType;
 import com.herocraftonline.dev.heroes.damage.DamageManager.ProjectileType;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class HeroClassManager {
 
     private final Heroes plugin;
     private Set<HeroClass> classes;
     private HeroClass defaultClass;
 
     public HeroClassManager(Heroes plugin) {
         this.plugin = plugin;
         this.classes = new HashSet<HeroClass>();
     }
 
     public boolean addClass(HeroClass c) {
         return classes.add(c);
     }
 
     public HeroClass getClass(String name) {
         for (HeroClass c : classes) {
             if (name.equalsIgnoreCase(c.getName()))
                 return c;
         }
         return null;
     }
 
     public Set<HeroClass> getClasses() {
         return classes;
     }
 
     public HeroClass getDefaultClass() {
         return defaultClass;
     }
 
     public void loadClasses(File file) {
         Configuration config = new Configuration(file);
         config.load();
         List<String> classNames = config.getKeys("classes");
         // Warn console if there are No class definitions
         if (classNames == null) {
             Heroes.log(Level.WARNING, "You have no classes defined in your setup!");
             return;
         }
         for (String className : classNames) {
             HeroClass newClass = new HeroClass(className);
             ConfigurationNode classConfig = config.getNode("classes." + className);
 
             newClass.setDescription(classConfig.getString("description", ""));
             newClass.setExpModifier(classConfig.getDouble("expmodifier", 1.0D));
 
             // Load class allowed Armor + Weapons
 
             loadArmor(newClass, classConfig);
             loadWeapons(newClass, classConfig);
             loadDamages(newClass, classConfig);
             loadPermittedSkills(newClass, classConfig);
             loadPermissionSkills(newClass, classConfig);
             loadExperienceTypes(newClass, classConfig);
 
             Double baseMaxHealth = classConfig.getDouble("base-max-health", 20);
             Double maxHealthPerLevel = classConfig.getDouble("max-health-per-level", 0);
             newClass.setBaseMaxHealth(baseMaxHealth);
             newClass.setMaxHealthPerLevel(maxHealthPerLevel);
 
             // Get the class expLoss
             newClass.setExpLoss(classConfig.getDouble("expLoss", -1));
 
             // Get the maximum level or use the default if it's not specified
             int defaultMaxLevel = plugin.getConfigManager().getProperties().maxLevel;
             int maxLevel = classConfig.getInt("max-level", defaultMaxLevel);
             if (maxLevel < 1) {
                 Heroes.log(Level.WARNING, "Class (" + className + ") max level is too low. Setting max level to 1.");
                 maxLevel = 1;
             } else if (maxLevel > defaultMaxLevel) {
                 Heroes.log(Level.WARNING, "Class (" + className + ") max level is too high. Setting max level to " + defaultMaxLevel + ".");
                 maxLevel = defaultMaxLevel;
             }
             newClass.setMaxLevel(maxLevel);
 
             int defaultCost = plugin.getConfigManager().getProperties().swapCost;
             int cost = classConfig.getInt("cost", defaultCost);
             if (cost < 0) {
                 Heroes.log(Level.WARNING, "Class (" + className + ") cost is too low. Setting cost to 0.");
                 cost = 0;
             }
             newClass.setCost(cost);
 
             // Attempt to add the class
             if (!addClass(newClass)) {
                 Heroes.log(Level.WARNING, "Duplicate class (" + className + ") found. Skipping this class.");
             } else {
                 Heroes.log(Level.INFO, "Loaded class: " + className);
                 if (config.getBoolean("classes." + className + ".default", false)) {
                     Heroes.log(Level.INFO, "Default class found: " + className);
                     defaultClass = newClass;
                 }
             }
         }
 
         // After all classes are loaded we need to link them all together
         checkClassHeirarchy(config);
 
         if (defaultClass == null) {
             Heroes.log(Level.SEVERE, "You are missing a default class, this will cause A LOT of issues!");
         }

        registerClassPermissions();
     }
 
     private void registerClassPermissions() {
         Map<String, Boolean> classPermissions = new HashMap<String, Boolean>();
         for (HeroClass heroClass : classes) {
             classPermissions.put("heroes.classes." + heroClass.getName().toLowerCase(), true);
         }
         Permission wildcardClassPermission = new Permission("heroes.classes.*", "Grants access to all classes.", classPermissions);
         plugin.getServer().getPluginManager().addPermission(wildcardClassPermission);
     }
 
     public void loadDamages(HeroClass newClass, ConfigurationNode config) {
         String className = newClass.getName();
 
         // Load in item/weapon damages for this class
         List<String> itemDamages = config.getKeys("item-damage");
         if (itemDamages == null || itemDamages.isEmpty()) {
             plugin.debugLog(Level.WARNING, className + " has no item damage section");
         } else {
             for (String materialName : itemDamages) {
                 Material material = Material.matchMaterial(materialName);
                 if (material != null) {
                     int damage = config.getInt("item-damage." + materialName, 0);
                     newClass.setItemDamage(material, damage);
                 } else {
                     Heroes.log(Level.WARNING, "Invalid material (" + material + ") defined for " + className);
                 }
             }
         }
 
         // Load in Projectile Damages for the class
         List<String> projectileDamages = config.getKeys("projectile-damage");
         if (projectileDamages == null || projectileDamages.isEmpty()) {
             plugin.debugLog(Level.WARNING, className + " has no projectile damage section");
         } else {
             for (String projectileName : projectileDamages) {
                 try {
                     ProjectileType type = ProjectileType.matchProjectile(projectileName);
 
                     int damage = config.getInt("projectile-damage." + projectileName, 0);
                     newClass.setProjectileDamage(type, damage);
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid projectile type (" + projectileName + ") defined for " + className);
                 }
             }
         }
     }
 
     public void loadWeapons(HeroClass newClass, ConfigurationNode config) {
         StringBuilder wLimits = new StringBuilder();
         String className = newClass.getName();
         // Get the list of allowed weapons for this class
         List<String> weapon = config.getStringList("permitted-weapon", new ArrayList<String>());
         if (weapon.isEmpty()) {
             plugin.debugLog(Level.WARNING, className + " has no permitted-weapon section");
             return;
         }
         for (String w : weapon) {
             if (w.equals("*") || w.equals("ALL")) {
                 newClass.addAllowedWeapon("*");
                 continue;
             }
             // A BOW has no ItemType so we just add it straight away.
             if (w.equalsIgnoreCase("BOW")) {
                 newClass.addAllowedWeapon("BOW");
                 wLimits.append(" BOW");
                 continue;
             }
             // If it's a generic type like 'DIAMOND' or 'LEATHER' we add all the possible entries.
             if (!w.contains("_")) {
                 try {
                     WeaponType wType = WeaponType.valueOf(w);
                     newClass.addAllowedWeapon(wType + "_PICKAXE");
                     wLimits.append(" ").append(wType).append("_PICKAXE");
                     newClass.addAllowedWeapon(wType + "_AXE");
                     wLimits.append(" ").append(wType).append("_AXE");
                     newClass.addAllowedWeapon(wType + "_HOE");
                     wLimits.append(" ").append(wType).append("_HOE");
                     newClass.addAllowedWeapon(wType + "_SPADE");
                     wLimits.append(" ").append(wType).append("_SPADE");
                     newClass.addAllowedWeapon(wType + "_SWORD");
                     wLimits.append(" ").append(wType).append("_SWORD");
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid weapon type (" + w + ") defined for " + className);
                 }
             } else {
                 String type = w.substring(0, w.indexOf("_"));
                 String item = w.substring(w.indexOf("_") + 1, w.length());
                 try {
                     WeaponType wType = WeaponType.valueOf(type);
                     WeaponItems wItem = WeaponItems.valueOf(item);
                     newClass.addAllowedWeapon(wType + "_" + wItem);
                     wLimits.append(" - ").append(wType).append("_").append(wItem);
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid weapon type (" + type + "_" + item + ") defined for " + className);
                 }
             }
         }
         plugin.debugLog(Level.INFO, "Allowed Weapons - " + wLimits.toString());
     }
 
     public boolean removeClass(HeroClass c) {
         return classes.remove(c);
     }
 
     public void setDefaultClass(HeroClass defaultClass) {
         this.defaultClass = defaultClass;
     }
 
     /**
      * Checks the full class Heirarchy and links all classes together properly.
      * 
      * @param config
      */
     private void checkClassHeirarchy(Configuration config) {
         for (HeroClass unlinkedClass : classes) {
             String className = unlinkedClass.getName();
 
             String oldStyleParentName = config.getString("classes." + className + ".parent");
             if (oldStyleParentName != null) {
                 HeroClass parent = getClass(oldStyleParentName);
                 if (parent != null) {
                     parent.addSpecialization(unlinkedClass);
                     unlinkedClass.addStrongParent(parent);
                 } else {
                     Heroes.log(Level.WARNING, "Cannot assign " + className + " a parent class as " + oldStyleParentName + " does not exist.");
                 }
             } else {
                 Set<String> strongParents = new HashSet<String>(config.getStringList("classes." + className + ".parents.strong", new ArrayList<String>()));
                 for (String cName : strongParents) {
                     HeroClass parent = getClass(cName);
                     if (parent != null) {
                         parent.addSpecialization(unlinkedClass);
                         unlinkedClass.addStrongParent(parent);
                     } else {
                         Heroes.log(Level.WARNING, "Cannot assign " + className + " a parent class as " + cName + " does not exist.");
                     }
                 }
 
                 Set<String> weakParents = new HashSet<String>(config.getStringList("classes." + className + ".parents.weak", new ArrayList<String>()));
                 for (String cName : weakParents) {
                     HeroClass parent = getClass(cName);
                     if (parent != null) {
                         parent.addSpecialization(unlinkedClass);
                         unlinkedClass.addWeakParent(parent);
                     }
                 }
             }
         }
     }
 
     private void loadArmor(HeroClass newClass, ConfigurationNode config) {
         StringBuilder aLimits = new StringBuilder();
         String className = newClass.getName();
         // Get the list of Allowed armors for this class
         List<String> armor = config.getStringList("permitted-armor", new ArrayList<String>());
         if (armor.isEmpty()) {
             plugin.debugLog(Level.WARNING, className + " has no permitted-armor section");
             return;
         }
         for (String a : armor) {
             if (a.equals("*") || a.equals("ALL")) {
                 newClass.addAllowedArmor("*");
                 continue;
             }
             // If it's a generic type like 'DIAMOND' or 'LEATHER' we add all the possible entries.
             if (!a.contains("_")) {
                 try {
                     ArmorType aType = ArmorType.valueOf(a);
                     newClass.addAllowedArmor(aType + "_HELMET");
                     aLimits.append(" ").append(aType).append("_HELMET");
                     newClass.addAllowedArmor(aType + "_CHESTPLATE");
                     aLimits.append(" ").append(aType).append("_CHESTPLATE");
                     newClass.addAllowedArmor(aType + "_LEGGINGS");
                     aLimits.append(" ").append(aType).append("_LEGGINGS");
                     newClass.addAllowedArmor(aType + "_BOOTS");
                     aLimits.append(" ").append(aType).append("_BOOTS");
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid armor type (" + a + ") defined for " + className);
                 }
             } else {
                 String type = a.substring(0, a.indexOf("_"));
                 String item = a.substring(a.indexOf("_") + 1, a.length());
                 try {
                     ArmorType aType = ArmorType.valueOf(type);
                     ArmorItems aItem = ArmorItems.valueOf(item);
                     newClass.addAllowedArmor(aType + "_" + aItem);
                     aLimits.append(" ").append(aType).append("_").append(aItem);
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid armor type (" + type + "_" + item + ") defined for " + className);
                 }
             }
         }
         plugin.debugLog(Level.INFO, "Allowed Armor - " + aLimits.toString());
     }
 
     private void loadExperienceTypes(HeroClass newClass, ConfigurationNode config) {
         String className = newClass.getName();
         // Get experience for each class
         List<String> experienceNames = config.getStringList("experience-sources", null);
         Set<ExperienceType> experienceSources = EnumSet.noneOf(ExperienceType.class);
         if (experienceNames == null) {
             plugin.debugLog(Level.WARNING, className + " has no experience-sources section");
         } else {
             for (String experience : experienceNames) {
                 try {
                     boolean added = experienceSources.add(ExperienceType.valueOf(experience));
                     if (!added) {
                         Heroes.log(Level.WARNING, "Duplicate experience source (" + experience + ") defined for " + className + ".");
                     }
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid experience source (" + experience + ") defined for " + className + ". Skipping this source.");
                 }
             }
         }
         newClass.setExperienceSources(experienceSources);
     }
 
     private void loadPermissionSkills(HeroClass newClass, ConfigurationNode config) {
         String className = newClass.getName();
         // Load in the Permission-Skills
         List<String> permissionSkillNames = config.getKeys("permission-skills");
         if (permissionSkillNames != null) {
             for (String skill : permissionSkillNames) {
                 // Ignore Overlapping Skill names that are already loaded as permitted-skills
                 if (newClass.hasSkill(skill)) {
                     Heroes.log(Level.WARNING, "Skill already assigned (" + skill + ") for " + className + ". Skipping this skill");
                     continue;
                 }
                 try {
                     ConfigurationNode skillSettings = Configuration.getEmptyNode();
                     skillSettings.setProperty("level", config.getInt("permission-skills." + skill + ".level", 1));
                     newClass.addSkill(skill, skillSettings);
 
                     String usage = config.getString("permission-skills." + skill + ".usage", "");
                     String[] permissions = config.getStringList("permission-skills." + skill + ".permissions", null).toArray(new String[0]);
                     OutsourcedSkill oSkill = new OutsourcedSkill(plugin, skill, permissions, usage);
                     plugin.getSkillManager().addSkill(oSkill);
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid permission skill (" + skill + ") defined for " + className + ". Skipping this skill.");
                 }
             }
         }
     }
 
     private void loadPermittedSkills(HeroClass newClass, ConfigurationNode config) {
         String className = newClass.getName();
         // Load in Permitted Skills for the class
         if (config.getKeys("permitted-skills") == null) {
             plugin.debugLog(Level.WARNING, className + " has no permitted-skills section");
         } else {
             Set<String> skillNames = new HashSet<String>();
             skillNames.addAll(config.getKeys("permitted-skills"));
             boolean allSkills = false;
             for (String skillName : skillNames) {
                 if (skillName.equals("*") || skillName.toLowerCase().equals("all")) {
                     allSkills = true;
                     continue;
                 }
                 Skill skill = plugin.getSkillManager().getSkill(skillName);
                 if (skill == null) {
                     Heroes.log(Level.WARNING, "Skill " + skillName + " defined for " + className + " not found.");
                     continue;
                 }
 
                 ConfigurationNode skillSettings = Configuration.getEmptyNode();
                 List<String> settings = config.getKeys("permitted-skills." + skillName);
                 if (settings != null) {
                     for (String key : settings) {
                         skillSettings.setProperty(key, config.getProperty("permitted-skills." + skillName + "." + key));
                     }
                 }
                 newClass.addSkill(skillName, skillSettings);
 
             }
 
             // Load all skills onto the Class if we found ALL
             if (allSkills) {
                 // Make sure all the skills are loaded first
                 plugin.getSkillManager().loadSkills();
                 for (Skill skill : plugin.getSkillManager().getSkills()) {
                     // Ignore this skill if it was already loaded onto the class (we don't want to overwrite defined
                     // skills as they have settings)
                     if (newClass.hasSkill(skill.getName())) {
                         continue;
                     }
 
                     ConfigurationNode skillSettings = Configuration.getEmptyNode();
                     List<String> settings = config.getKeys("permitted-skills." + skill.getName());
                     if (settings != null) {
                         for (String key : settings) {
                             skillSettings.setProperty(key, config.getProperty("permitted-skills." + skill.getName() + "." + key));
                         }
                     }
                     newClass.addSkill(skill.getName(), skillSettings);
                 }
             }
         }
     }
 
 }

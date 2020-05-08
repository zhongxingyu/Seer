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
 package net.dawnfirerealms.legends.library.database;
 
 import net.dawnfirerealms.legends.core.LPlayer;
 import net.dawnfirerealms.legends.library.lclass.LClass;
 import net.dawnfirerealms.legends.library.race.Race;
 import net.dawnfirerealms.legends.library.skill.Skill;
 import org.apache.commons.lang.ArrayUtils;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.AbstractConstruct;
 import org.yaml.snakeyaml.constructor.Constructor;
 import org.yaml.snakeyaml.nodes.Node;
 import org.yaml.snakeyaml.nodes.NodeId;
 
 import javax.persistence.MapKey;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 import net.dawnfirerealms.legends.core.LPlayer;
 import net.dawnfirerealms.legends.library.lclass.LClass;
 import net.dawnfirerealms.legends.library.race.Race;
 import org.yaml.snakeyaml.Yaml;
 
 /**
  * @author B2OJustin
  */
 public class YAMLDataSource implements DataSource {
     public static final Logger logger = Logger.getLogger(YAMLDataSource.class.getName());
     private String configPath = "";
     private final String RACE_PATH = "races/";
     private final String SKILL_PATH = "skills/";
     private final String PLAYER_PATH = "players/";
     private final String CLASS_PATH = "classes/";
 
     @Override
     public String getName() {
         return "YAML";
     }
 
     public YAMLDataSource setPath(String filePath) {
         configPath = filePath;
         return this;
     }
 
     @Override
     public Logger getLogger() {
         return YAMLDataSource.logger;
     }
 
     @Override
     public LPlayer loadLPlayer(String name) {
         return null; //TODO loadLPlayer method stub
     }
 

     @Override
    @SuppressWarnings({"LoggerStringConcat", "unchecked"})
     public Race loadRace(String name) {
         name = name.replace(" ", "_"); // Replace spaces for filenames
         Yaml yaml = new Yaml();
         Race race = new Race();
         String filePath = configPath + RACE_PATH + name + ".yml";
         try {
             InputStream fileStream = new FileInputStream(filePath);
             LinkedHashMap<String, Object>  raceMap = (LinkedHashMap) yaml.load(fileStream);
             race.setName( (String) raceMap.get("name"));
             race.setDescription( (ArrayList<String>) raceMap.get("description"));
 
             for(String skillName : ((LinkedHashMap<String, Object>) raceMap.get("permitted-skills")).keySet()) {
                 System.out.println("Permitted skill -" + skillName);
 
 
             }
         } catch (FileNotFoundException e) {
             logger.warning("Could not find file for race '" + name + "' at " + filePath);
         } catch (ClassCastException e) {
             logger.warning("You seem to have an error in your yaml. Could not load race '" + name + "'");
         }
 
 
         /*List<String> description = config.getStringList("description");
         Race race = new Race().
                 setName(config.getString("name")).
                 setDescription(description.toArray(new String[description.size()]));
 
         // Allowed weapons
         WeaponRestrictions weaponRestrictions = race.getWeaponRestrictions();
         for(String name : config.getStringList("permitted-weapon")) {
             Weapon weapon = WeaponHandler.getInstance().get(name);
             weaponRestrictions.setAllowed(weapon, true);
         }
 
         // Allowed armor
         ArmorRestrictions armorRestrictions = race.getArmorRestrictions();
         for(String name : config.getStringList("permitted-armor")) {
             Armor armor = ArmorHandler.getInstance().get(name);
             armorRestrictions.setAllowed(armor, true);
         }
         */
         return race;
     }
 
     @Override
     public LClass loadLClass(String name) {
         return null; //TODO loadLClass method stub
     }
 }

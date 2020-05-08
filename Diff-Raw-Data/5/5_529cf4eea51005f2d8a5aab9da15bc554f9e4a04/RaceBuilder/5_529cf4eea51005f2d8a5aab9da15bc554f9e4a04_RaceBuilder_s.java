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
 package net.dawnfirerealms.legends.core.race;
 
import com.mysql.jdbc.ResultSetRow;
import net.dawnfirerealms.legends.core.utils.BasicBuilder;
 import net.dawnfirerealms.legends.library.race.Race;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import java.util.List;
 
 /**
  * @author B2OJustin
  */
 public class RaceBuilder {
     public static Race load(YamlConfiguration config) {
         List<String> description = config.getStringList("description");
         Race race = new Race().
             setName(config.getString("name")).
             setDescription(description.toArray(new String[description.size()]));
 
         // Allowed weapons
         for(String name : config.getStringList("permitted-weapon")) {
 
         }
         // TODO ...
 
         return race;
     }
 }

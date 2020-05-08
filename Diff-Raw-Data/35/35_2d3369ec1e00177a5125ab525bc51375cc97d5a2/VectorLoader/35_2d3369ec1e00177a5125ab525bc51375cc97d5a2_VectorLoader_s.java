 package com.vartala.soulofw0lf.rpgapi.loaders;
 
 import com.vartala.soulofw0lf.rpgapi.RpgAPI;
 import com.vartala.soulofw0lf.rpgapi.playerapi.RpgPlayer;
 import com.vartala.soulofw0lf.rpgapi.util.ChatColors;
 import com.vartala.soulofw0lf.rpgapi.util.Misc;
 import com.vartala.soulofw0lf.rpgapi.vectorapi.RpgVectorBlocks;
 import com.vartala.soulofw0lf.rpgapi.vectorapi.VecBlockListener;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created by: soulofw0lf
  * Date: 7/25/13
  * Time: 4:12 PM
  * <p/>
  * This file is part of the Rpg Suite Created by Soulofw0lf and Linksy.
  * <p/>
  * The Rpg Suite is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * The Rpg Suite is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with The Rpg Suite Plugin you have downloaded.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class VectorLoader {
     RpgAPI rpg;
     public VectorLoader(RpgAPI Rpg){
      this.rpg = Rpg;
 
     new VecBlockListener(this.rpg);
         YamlConfiguration vecConfig = YamlConfiguration.loadConfiguration(new File("plugins/RpgVectorBlocks/config.yml"));
         if (vecConfig.get("Vector Blocks") == null){
             vecConfig.set("Vector Command", "vec");
             vecConfig.set("Vector Message", "&f[&2Rpg Vector Blocks&f] &eVector @n Created!");
             vecConfig.set("Vector Exists", "&f[&2Rpg Vector Blocks&f] &eA vector named @n already exists!");
             vecConfig.set("Vector Permission", "vector.make");
             vecConfig.set("Vector Permission Denied", "&f[&2Rpg Vector Blocks&f] &4You do not have permission to set new vectors!");
             vecConfig.set("Vector Error", "&f[&2Rpg Vector Blocks&f] &4Error please use /vec name immunetime vecx vecy vecz");
             vecConfig.set("Vector Blocks.1.Location", "world|0|0|0");
            vecConfig.set("Vector Blocks.1.Vector", "-.8|1.6|-2.0");
             vecConfig.set("Vector Blocks.1.Vector Immune", "40");
             try {
                 vecConfig.save(new File("plugins/RpgVectorBlocks/config.yml"));
             } catch (IOException e){
                 e.printStackTrace();
             }
         }
         RpgAPI.commands.add(vecConfig.getString("Vector Command"));
         RpgAPI.commandSettings.put("Vector Command", vecConfig.getString("Vector Command"));
         RpgAPI.localeSettings.put("Vector Message", vecConfig.getString("Vector Message"));
         RpgAPI.localeSettings.put("Vector Exists", vecConfig.getString("Vector Exists"));
         RpgAPI.localeSettings.put("Vector Permission Denied", vecConfig.getString("Vector Permission Denied"));
         RpgAPI.localeSettings.put("Vector Error", vecConfig.getString("Vector Error"));
         RpgAPI.permissionSettings.put("Vector Permission", vecConfig.getString("Vector Permission"));
         for (String key : vecConfig.getConfigurationSection("Vector Blocks").getKeys(false)){
             if (key == null){
                 continue;
             }
             Location loc = Misc.stringToLoc(vecConfig.getString("Vector Blocks." + key + ".Location"));
             Block b = loc.getBlock();
             RpgVectorBlocks rV = new RpgVectorBlocks(key, b, vecConfig.getInt("Vector Blocks." + key + ".Vector Immune"), Misc.stringToVec(vecConfig.getString("Vector Blocks." + key + ".Vector")));
             RpgAPI.vecBlocks.add(b);
             RpgAPI.vecBlockMap.put(b, rV);
         }
     }
     public static boolean vectorCommands(String[] cmd, Player p){
         if (cmd[0].equalsIgnoreCase(RpgAPI.commandSettings.get("Vector Command"))){
             RpgPlayer rp = RpgAPI.getRp(p);
             if (!rp.hasPermission(RpgAPI.permissionSettings.get("Vector Permission"))){
                  p.sendMessage(ChatColors.ChatString(RpgAPI.localeSettings.get("Vector Permission Denied")));
                 return true;
             }
             if (cmd.length != 6){
                 p.sendMessage(ChatColors.ChatString(RpgAPI.localeSettings.get("Vector Error")));
                 return true;
             }
             for (Block b : RpgAPI.vecBlocks){
                 RpgVectorBlocks rv = RpgAPI.vecBlockMap.get(b);
                 if (rv.getName().equalsIgnoreCase(cmd[1])){
                     p.sendMessage(ChatColors.ChatString(RpgAPI.localeSettings.get("Vector Exists").replace("@n", cmd[1])));
                     return true;
                 }
             }
             Block bl = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
             RpgVectorBlocks rVb = new RpgVectorBlocks(cmd[1], bl, Integer.parseInt(cmd[2]), new Vector(Double.parseDouble(cmd[3]), Double.parseDouble(cmd[4]), Double.parseDouble(cmd[5])));
             RpgAPI.vecBlocks.add(bl);
             RpgAPI.vecBlockMap.put(bl, rVb);
             p.sendMessage(ChatColors.ChatString(RpgAPI.localeSettings.get("Vector Message").replace("@n", rVb.getName())));
             return true;
         }
 
         return false;
     }
 }

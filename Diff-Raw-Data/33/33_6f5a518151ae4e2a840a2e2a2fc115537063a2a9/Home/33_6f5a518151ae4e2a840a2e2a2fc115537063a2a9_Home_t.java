 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.data;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class Home {
 
     private int id;
     private String owner;
     private Location location;
 
     // CONSTRUCTOR WHEN PLAYER CREATES A HOME
     public Home(Player player) {
         this.owner = player.getName();
         this.location = player.getLocation();
     }
 
     // CONSTUCTOR WHEN HOME IS LOADED FROM DATABASE
     public Home(int id, String owner, double x, double y, double z, float yaw, float pitch, String worldName) {
         this.id = id;
         this.owner = owner;
         setLocation(x, y, z, yaw, pitch, worldName);
     }
 
     private void setLocation(double x, double y, double z, float yaw, float pitch, String worldName) {
         World w = Bukkit.getWorld(worldName);
         if (w != null)
             this.location = new Location(w, x, y, z, yaw, pitch);
         else
             ConsoleUtils.printError(Core.NAME, "Can't create a Home for '" + this.owner + "' because the world '" + worldName + "' doesn't exist!");
     }
 
     public Location getLocation() {
         return location;
     }
 
     public String getOwner() {
         return owner;
     }
 
     public int getId() {
         return id;
     }
 
     public void updateLocation(Location loc) {
         this.location = loc;
     }
 
     public void setId(int id) {
        if (this.id == 0)
             this.id = id;
         else
             ConsoleUtils.printError(Core.NAME, "Home of '" + owner + "' has already an database id!");
     }
 
     @Override
     public String toString() {
 
         StringBuilder sBuilder = new StringBuilder("Home: ");
 
         sBuilder.append("owner= ");
         sBuilder.append(owner);
         sBuilder.append(", location=");
         sBuilder.append(location);
         sBuilder.append(", id=");
         sBuilder.append(id);
         return sBuilder.toString();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null)
             return false;
         if (this == obj)
             return true;
         if (!(obj instanceof Home))
             return false;
 
         Home that = (Home) obj;
         return this.owner.equals(that.owner) && this.location.equals(that.location);
     }
 
     @Override
     public int hashCode() {
         return this.owner.hashCode() * this.location.hashCode() << 5;
     }
 }

 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence.storage.persistent;
 
 import com.avaje.ebean.validation.Length;
 import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
 import org.bukkit.Chunk;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  * Represents a chunk reserved by town.
  *
  * @author Michael Hohl
  */
 @Entity
 @Table(name = "res_chunks")
 public class TownChunk {
     @Id
     private int id;
 
     @NotNull
     private int townId;
 
     @NotEmpty
     @Length(max = 32)
     private String world;
 
     @NotNull
     private int x;
 
     @NotNull
     private int z;
 
     /** Creates a new chunk reserved by a town. */
     public TownChunk() {
     }
 
     /**
      * Creates a new chunk reserved by a town.
      *
      * @param town  the town which reserved the chunk.
      * @param chunk the chunk to reserve.
      */
     public TownChunk(Town town, Chunk chunk) {
         this.townId = town.getId();
 
         this.setWorld(chunk.getWorld().getName());
         this.setX(chunk.getX());
         this.setZ(chunk.getZ());
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public int getTownId() {
         return townId;
     }
 
     public void setTownId(int townId) {
         this.townId = townId;
     }
 
     public String getWorld() {
         return world;
     }
 
     public void setWorld(String world) {
         this.world = world;
     }
 
     public int getX() {
         return x;
     }
 
     public void setX(int x) {
         this.x = x;
     }
 
     public int getZ() {
         return z;
     }
 
     public void setZ(int z) {
         this.z = z;
     }
 }

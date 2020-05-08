 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of DirectorPlugin.
  * 
  * DirectorPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * DirectorPlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with DirectorPlugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.director.area;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import org.bukkit.Chunk;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 public class Area {
     private String areaOwner = "";
     private String areaName = "";
     private String worldName = "";
     private Point minChunk = null;
     private Point maxChunk = null;
     private Rectangle rectangle = null;
 
     // ///////////////////////////
     // CONSTRUCTOR
     // ///////////////////////////
     public Area(final String areaName, final String areaOwner, final String worldName, Chunk chunk1, Chunk chunk2) {
         this.areaName = areaName;
         this.areaOwner = areaOwner;
         this.worldName = worldName;
        this.minChunk = new Point(Math.min(chunk1.getX(), chunk2.getX()), Math.min(chunk1.getZ(), chunk2.getZ()));
        this.maxChunk = new Point(Math.max(chunk1.getX(), chunk2.getX()), Math.max(chunk1.getZ(), chunk2.getZ()));
         this.rectangle = new Rectangle(this.minChunk.x, this.minChunk.y, this.maxChunk.x, this.maxChunk.y);
     }
 
     // ///////////////////////////
     // IS BLOCK IN AREA
     // ///////////////////////////
     public boolean isBlockInArea(final Block block) {
         return rectangle.contains(block.getX(), block.getZ());
     }
 
     // ///////////////////////////
     // IS AREA IN AREA
     // ///////////////////////////
     public boolean intersectsArea(final Area otherArea) {
         return rectangle.intersects(otherArea.getRectangle());
     }
     
     // ///////////////////////////
     // IS AREAOWNER
     // ///////////////////////////   
     public boolean isOwner(Player player) {
         return this.isOwner(player.getName());
     }
 
     public boolean isOwner(String playerName) {
         return this.getAreaOwner().equalsIgnoreCase(playerName);
     }
     
     // ///////////////////////////
     // GETTER
     // ///////////////////////////
     public String getAreaName() {
         return this.areaName;
     }
     
     private String getAreaOwner() {
         return this.areaOwner;
     }
 
     public String getWorldName() {
         return this.worldName;
     }
 
     public Point getMinChunk() {
         return this.minChunk;
     }
 
     public Point getMaxChunk() {
         return this.maxChunk;
     }
 
     public Rectangle getRectangle() {
         return this.rectangle;
     }
 }

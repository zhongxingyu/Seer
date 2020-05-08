 /*
  * Copyright (c) 2012 GuntherDW
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package com.guntherdw.bukkit.TweakWarp;
 
 import com.avaje.ebean.validation.Length;
 import com.avaje.ebean.validation.NotEmpty;
 import com.avaje.ebean.validation.NotNull;
 import org.bukkit.Location;
 import org.bukkit.Server;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  * @author GuntherDW
  */
 @Entity()
 @Table(name="warps")
 public class Warp {
 
     @Id
     private int id;
 
     @Length(max=45)
     @NotNull
     private String name;
 
     @NotNull
     private double x;
 
     @NotNull
     private double y;
 
     @NotNull
     private double z;
 
     @NotNull
     private float pitch;
 
     @NotNull
     private float yaw;
 
     @Length(max=45)
     @NotEmpty
     private String world;
 
     @Length(max=100)
     @NotNull
     private String warpgroup;
 
     @Length(max=100)
     @NotNull
     private String accessgroup;
 
     /**
      *  Default constructor for persistence manager.
      */
     public Warp() {
 
     }
 
     public Warp(double x, double y, double z, float pitch, float yaw, String name, String world, String warpgroup, String accessgroup) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.pitch = pitch;
         this.yaw = yaw;
         this.name = name;
         this.world = world;
         this.warpgroup = warpgroup.toLowerCase();
         if(this.warpgroup.trim().equals("")) this.warpgroup = TweakWarp.DEFAULT_WARP_GROUP;
         this.accessgroup = accessgroup.toLowerCase();
     }
 
     public Warp(Location location, String name, String warpgroup, String accessgroup) {
         this(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw(), name, location.getWorld().getName(), warpgroup, accessgroup);
     }
 
     public Location getLocation(Server server) {
         return new Location(server.getWorld(getWorld()),getX(), getY() + 1, getZ(), getYaw(), getPitch());
     }
 
     public boolean delete(TweakWarp plugin) {
         if(plugin.forgetWarp(this)) {
             // plugin.getDatabase().delete(Warp.class, this.id);
            plugin.getDataSource().deleteWarp(name, accessgroup);
             return true;
         }
         return false;
     }
 
     public boolean save(TweakWarp plugin) {
         Warp w = plugin.getWarp(getWarpgroup(), getName());
         if(w != null) w.delete(plugin);
         if(plugin.registerWarp(this)) {
             // plugin.getDatabase().save(this);
             int id = plugin.getDataSource().addWarp(this);
             if(id!=-1) this.id = id;
             return true;
         }
         return false;
     }
     
     public boolean updateGroup(TweakWarp plugin, String newGroup) {
         Warp w = plugin.getWarp(getWarpgroup(), getName());
         if(w==null) return false;
         plugin.forgetWarp(this);
         this.setWarpgroup(newGroup);
         if(plugin.registerWarp(this)) {
             int amt = plugin.getDataSource().updateGroup(this.id, newGroup);
             if(amt == 0) return false;
             return true;
         }
         return false;
     }
 
     public int getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public double getX() {
         return x;
     }
 
     public double getY() {
         return y;
     }
 
     public double getZ() {
         return z;
     }
 
     public float getPitch() {
         return pitch;
     }
 
     public float getYaw() {
         return yaw;
     }
 
     public String getWorld() {
         return world;
     }
 
     public String getWarpgroup() {
         return warpgroup;
     }
 
     public String getAccessgroup() {
         return accessgroup;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setX(double x) {
         this.x = x;
     }
 
     public void setY(double y) {
         this.y = y;
     }
 
     public void setZ(double z) {
         this.z = z;
     }
 
     public void setPitch(float pitch) {
         this.pitch = pitch;
     }
 
     public void setYaw(float yaw) {
         this.yaw = yaw;
     }
 
     public void setWorld(String world) {
         this.world = world;
     }
 
     public void setWarpgroup(String warpgroup) {
         this.warpgroup = warpgroup;
     }
 
     public void setAccessgroup(String accessgroup) {
         this.accessgroup = accessgroup;
     }
 }

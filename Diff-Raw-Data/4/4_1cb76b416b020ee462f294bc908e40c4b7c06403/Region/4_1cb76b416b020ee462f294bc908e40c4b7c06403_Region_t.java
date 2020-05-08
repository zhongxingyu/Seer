 package com.undeadscythes.udsplugin;
 
 import com.undeadscythes.udsplugin.Bearing.Direction;
 import com.undeadscythes.udsplugin.SaveablePlayer.PlayerRank;
 import java.io.*;
 import java.util.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 import org.bukkit.util.Vector;
 
 /**
  * An area of blocks with in a world.
  * @author UndeadScythes
  */
 public class Region implements Saveable {
     /**
      * Region flags.
      */
     public enum RegionFlag {
         /**
          * This region is protected from other players building.
          */
         PROTECTION(true),
         /**
          * Mobs can spawn here.
          */
         MOBS(false),
         /**
          * Players can damage passive mobs here.
          */
         PVE(true),
         /**
          * Players cannot interact with anything here.
          */
         LOCK(true),
         /**
          * Vines will grow here.
          */
         VINES(true),
         /**
          * Food items will grow here.
          */
         FOOD(true),
         /**
          * Fire will burn and spread here.
          */
         FIRE(false),
         /**
          * Snow will fall and ice will melt here.
          */
         SNOW(false),
         /**
          * Players can attack other players here.
          */
         PVP(false);
 
         private boolean defaultValue;
 
         RegionFlag(boolean value) {
             this.defaultValue = value;
         }
 
         /**
          * Get this flag's default value.
          * @return
          */
         public boolean getDefault() {
             return defaultValue;
         }
 
         @Override
         public String toString() {
             return name().toLowerCase();
         }
 
         /**
          * Get a flag by name.
          * @param string The name of the flag.
          * @return The flag or <code>null</code> if there was no match.
          */
         public static RegionFlag getByName(String string) {
             for(RegionFlag flag : values()) {
                 if(flag.name().equals(string.toUpperCase())) {
                     return flag;
                 }
             }
             return null;
         }
     }
 
     /**
      * Region type.
      */
     public enum RegionType {
         /**
          * A standard region.
          */
         NORMAL,
         /**
          * A player ownable shop.
          */
         SHOP,
         /**
          * A clan base.
          */
         BASE,
         /**
          * A respawning quarry.
          */
         QUARRY,
         /**
          * A player owned home.
          */
         HOME,
         /**
          * A PVP arena.
          */
         ARENA,
         /**
          * A player owned city.
          */
         CITY;
 
         /**
          * Get a region type by name.
          * @param string Name of region type.
          * @return The region type or <code>null</code> if there was no match.
          */
         public static RegionType getByName(String string) {
             for(RegionType type : values()) {
                 if(type.name().equals(string.toUpperCase())) {
                     return type;
                 }
             }
             return null;
         }
     }
 
     /**
      * File name of region storage file.
      */
     public static final String PATH = "regions.csv";
 
     private String name;
     private Vector v1;
     private Vector v2;
     private Location warp;
     private SaveablePlayer owner;
     private HashSet<SaveablePlayer> members = new HashSet<SaveablePlayer>();
     private String data;
     private HashSet<RegionFlag> flags;
     private RegionType type;
     private PlayerRank rank = PlayerRank.NONE;
 
     /**
      * Initialise a brand new region.
      * @param name Name of the region.
      * @param v1 Minimum block position.
      * @param v2 Maximum block position.
      * @param warp Warp location of the region.
      * @param owner Owner of the region.
      * @param data Data of region, if any.
      * @param type Region type.
      */
     public Region(String name, Vector v1, Vector v2, Location warp, SaveablePlayer owner, String data, RegionType type) {
         this.name = name;
         this.v1 = v1;
         this.v2 = v2;
         this.warp = warp;
         this.owner = owner;
         this.data = data;
         flags = new HashSet<RegionFlag>();
         for(RegionFlag flag : RegionFlag.values()) {
             if(flag.getDefault()) {
                 flags.add(flag);
             }
         }
         this.type = type;
     }
 
     /**
      * Initialise a region from a string record.
      * @param record A line from a save file.
      * @throws IOException Thrown when vectors can't be loaded.
      */
     public Region(String record) throws IOException {
         String[] recordSplit = record.split("\t");
         name = recordSplit[0];
         v1 = getBlockPos(recordSplit[1]);
         v2 = getBlockPos(recordSplit[2]);
         warp = (Location)(new Bearing(recordSplit[3]));
         owner = UDSPlugin.getPlayers().get(recordSplit[4]);
         members = new HashSet<SaveablePlayer>();
         if(!recordSplit[5].equals("")) {
             for(String member : recordSplit[5].split(",")) {
                 members.add(UDSPlugin.getPlayers().get(member));
             }
         }
         data = recordSplit[6];
         flags = new HashSet<RegionFlag>();
         for(String flag : recordSplit[7].split(",")) {
             flags.add(RegionFlag.getByName(flag));
         }
         type = RegionType.getByName(recordSplit[8]);
         rank = PlayerRank.getByName(recordSplit[9]);
     }
 
     /**
      * Helper function to build a new block position from a string.
      * @param string String containing coded block position.
      * @return The corresponding new block position.
      */
     private Vector getBlockPos(String string) {
         String[] split = string.replace("(", "").replace(")", "").split(",");
         double x = Double.parseDouble(split[0]);
         double y = Double.parseDouble(split[1]);
         double z = Double.parseDouble(split[2]);
         return new Vector(x, y, z);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public String getRecord() {
         ArrayList<String> record = new ArrayList<String>();
         record.add(name);
         record.add(v1.toString());
         record.add(v2.toString());
         record.add(new Bearing(warp).toString());
         record.add(owner == null ? "" : owner.getName());
         ArrayList<String> memberList = new ArrayList<String>();
         for(SaveablePlayer member : members) {
             memberList.add(member.getName());
         }
         record.add(StringUtils.join(memberList, ","));
         record.add(data);
         record.add(StringUtils.join(flags.toArray(), ","));
         record.add(type.toString());
         record.add(rank.toString());
         return StringUtils.join(record.toArray(), "\t");
     }
 
     /**
      * Clear the members of the region.
      */
     public void clearMembers() {
         members = new HashSet<SaveablePlayer>();
     }
 
     @Override
     public String toString() {
         return name;
     }
 
     /**
      * Change the owner of the region.
      * @param owner New owner name.
      */
     public void changeOwner(SaveablePlayer owner) {
         this.owner = owner;
     }
 
     /**
      * Get the region members.
      * @return Region members.
      */
     public HashSet<SaveablePlayer> getMembers() {
         return members;
     }
 
     /**
      * Change the name of the region.
      * @param newName New region name.
      */
     public void changeName(String newName) {
         name = newName;
     }
 
     public String getOwnerName() {
         return owner != null ? owner.getDisplayName() : "";
     }
 
     public boolean isOwner(SaveablePlayer player) {
         return player != null && player.equals(owner);
     }
 
     /**
      * Expand this region in some direction.
      * @param direction Direction to expand.
      * @param distance Distance to expand.
      */
     public void expand(Direction direction, int distance) {
         if(direction.equals(Direction.NORTH)) {
             v1.add(new Vector(0, 0, -distance));
         } else if(direction.equals(Direction.SOUTH)) {
             v2.add(new Vector(0, 0, distance));
         } else if(direction.equals(Direction.EAST)) {
             v2.add(new Vector(distance, 0, 0));
         } else if(direction.equals(Direction.WEST)) {
             v1.add(new Vector(-distance, 0, 0));
         } else if(direction.equals(Direction.UP)) {
             v2.add(new Vector(0, distance, 0));
         } else if(direction.equals(Direction.DOWN)) {
             v1.add(new Vector(0, -distance, 0));
         }
     }
 
     public PlayerRank getRank() {
         return rank;
     }
 
     public void setRank(PlayerRank rank) {
         this.rank = rank;
     }
 
     /**
      * Get this regions flags.
      * @return The region flags.
      */
     public HashSet<RegionFlag> getFlags() {
         return flags;
     }
 
     /**
      * Change this regions defining points.
      * @param v1 New v1.
      * @param v2 New v2.
      */
     public void changeV(Vector v1, Vector v2) {
         this.v1 = v1;
         this.v2 = v2;
     }
 
     /**
      * Place single corner markers around the region.
      */
     public void placeCornerMarkers() {
         WEWorld world = new WEWorld(getWorld());
         world.buildTower(v1.getBlockX(), v1.getBlockZ(), 1, Material.FENCE, Material.TORCH);
         world.buildTower(v2.getBlockX(), v1.getBlockZ(), 1, Material.FENCE, Material.TORCH);
         world.buildTower(v1.getBlockX(), v2.getBlockZ(), 1, Material.FENCE, Material.TORCH);
         world.buildTower(v2.getBlockX(), v2.getBlockZ(), 1, Material.FENCE, Material.TORCH);
     }
 
     /**
      * Place 3 wide side markers around the region.
      */
     public void placeMoreMarkers() {
         WEWorld world = new WEWorld(getWorld());
         world.buildLine(v1.getBlockX(), (v1.getBlockZ() + v2.getBlockZ()) / 2 - 3, 0, 6, Material.FENCE, Material.TORCH);
         world.buildLine(v2.getBlockX(), (v1.getBlockZ() + v2.getBlockZ()) / 2 - 3, 0, 6, Material.FENCE, Material.TORCH);
        world.buildLine((v1.getBlockX() + v2.getBlockX()) / 2 - 3, v1.getBlockZ(), 6, 0, Material.FENCE, Material.TORCH);
        world.buildLine((v1.getBlockX() + v2.getBlockX()) / 2 - 3, v2.getBlockZ(), 6, 0, Material.FENCE, Material.TORCH);
     }
 
     /**
      * Place 10 block high towers in each corner of the region.
      */
     public void placeTowers() {
         WEWorld world = new WEWorld(getWorld());
         world.buildTower(v1.getBlockX(), v1.getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
         world.buildTower(v1.getBlockX(), v2.getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
         world.buildTower(v2.getBlockX(), v1.getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
         world.buildTower(v2.getBlockX(), v2.getBlockZ(), 10, Material.FENCE, Material.GLOWSTONE);
     }
 
     /**
      * Get the number of members this region has.
      * @return The number of members of this region.
      */
     public int getMemberNo() {
         return members.size();
     }
 
     /**
      * Checks if this region overlaps another region.
      * @param region Region to check.
      * @return <code>true</code> if this region overlaps with the other, <code>false</code> otherwise.
      */
     public boolean hasOverlap(Region region) {
         return !(v1.getX() > region.getV2().getX() || v2.getX() < region.getV1().getX() || v1.getZ() > region.getV2().getZ() || v2.getZ() < region.getV1().getZ() || v1.getY() > region.getV2().getY() || v2.getY() < region.getV1().getY());
     }
 
     /**
      * Set the warp point of a region.
      * @param location Location to set as warp.
      */
     public void setWarp(Location location) {
         warp = location;
     }
 
     /**
      * Get the name of the owner of this region.
      * @return Regions owner's name.
      */
     public SaveablePlayer getOwner() {
         return owner;
     }
 
     /**
      * Get the name of the region.
      * @return Name of the region.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Change the name of the region.
      * @param name New name of the region.
      */
     public void rename(String name) {
         this.name = name;
     }
 
     /**
      * Check if a location is contained within the region.
      * @param location Location to check.
      * @return <code>true</code> if the location is contained within the region, <code>false</code> otherwise.
      */
     public boolean contains(Location location) {
         return contains(location.getWorld(), location.getX(), location.getY(), location.getZ());
     }
 
     /**
      * Check if coordinates are contained within the region.
      * @param world World of coordinates.
      * @param x X-Coordinate.
      * @param y Y-Coordinate.
      * @param z Z-Coordinate.
      * @return <code>true</code> if the location is contained within the region, <code>false</code> otherwise.
      */
     public boolean contains(World world, double x, double y, double z) {
         if(warp.getWorld().equals(world) && x > v1.getX() && x < v2.getX() + 1 && z > v1.getZ() && z < v2.getZ() + 1 && y > v1.getY() && y < v2.getY() + 1) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Get the type of this region.
      * @return Type of the region.
      */
     public RegionType getType() {
         return type;
     }
 
     /**
      * Get the warp location of this region.
      * @return Location of region warp point.
      */
     public Location getWarp() {
         return warp;
     }
 
     /**
      * Check if a player is a member of a region.
      * @param player Player name.
      * @return <code>true</code> if player is a member, <code>false</code> otherwise.
      */
     public boolean hasMember(SaveablePlayer player) {
         return members.contains(player);
     }
 
     /**
      * Add a player as a member of the region.
      * @param player Player name.
      * @return <code>true</code> if the player was not already a member of the region, <code>false</code> otherwise.
      */
     public boolean addMember(SaveablePlayer player) {
         return members.add(player);
     }
 
     /**
      * Remove a player as a member of a region.
      * @param player Player name.
      * @return <code>true</code> if the player was a member of the region, <code>false</code> otherwise.
      */
     public boolean delMember(SaveablePlayer player) {
         return members.remove(player);
     }
 
     /**
      * Get the regions minimum vector.
      * @return Vector 1.
      */
     public Vector getV1() {
         return v1;
     }
 
     /**
      * Get the regions maximum vector.
      * @return Vector 2.
      */
     public Vector getV2() {
         return v2;
     }
 
     /**
      * Get the world this region is in.
      * @return The regions world.
      */
     public World getWorld() {
         return warp.getWorld();
     }
 
     /**
      * Get the data stored with this region.
      * @return The region data.
      */
     public String getData() {
         return data;
     }
 
     /**
      * Check if a region has a certain flag set.
      * @param flag Flag to check.
      * @return <code>true</code> if this flag is set, <code>false</code> otherwise.
      */
     public boolean hasFlag(RegionFlag flag) {
         return flags.contains(flag);
     }
 
     /**
      * Set a flag in this region.
      * @param flag Flag to set.
      * @return <code>true</code> if this flag was not already set, <code>false</code> otherwise.
      */
     public boolean setFlag(RegionFlag flag) {
         return flags.add(flag);
     }
 
     /**
      * Toggle a flag in this region.
      * @param flag Flag to toggle.
      * @return <code>true</code> if this flag is now set, <code>false</code> otherwise.
      */
     public boolean toggleFlag(RegionFlag flag) {
         if(!flags.add(flag)) {
             return !flags.remove(flag);
         }
         return true;
     }
 }

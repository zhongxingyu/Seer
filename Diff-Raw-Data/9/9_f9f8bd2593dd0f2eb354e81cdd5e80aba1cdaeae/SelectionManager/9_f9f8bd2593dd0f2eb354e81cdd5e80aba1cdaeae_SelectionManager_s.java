 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.bekvon.bukkit.residence.selection;
 import com.bekvon.bukkit.residence.Residence;
 import com.bekvon.bukkit.residence.permissions.PermissionGroup;
 import com.bekvon.bukkit.residence.protection.CuboidArea;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Administrator
  */
 public class SelectionManager {
     protected Map<String,Location> mins;
     protected Map<String,Location> maxs;
     protected Server server;
 
     public static final int MAX_HEIGHT = 255,MIN_HEIGHT = 0;
     
     public enum Direction
     {
         UP,DOWN,PLUSX,PLUSZ,MINUSX,MINUSZ
     }
 
     public SelectionManager(Server server)
     {
     	this.server = server;
         mins = Collections.synchronizedMap(new HashMap<String,Location>());
         maxs = Collections.synchronizedMap(new HashMap<String,Location>());
     }
 
     public void placeLoc1(Player player, Location loc)
     {
         if(loc!=null)
         {
             this.placeLoc(player, loc);
         }
     }
 
     public void placeLoc2(Player player, Location loc)
     {
         if(loc!=null)
         {
             this.placeLoc(player, loc);
         }
     }
     
     private void placeLoc(Player player, Location loc)
     {
         Location min = mins.get(player.getName());
         if(min!=null)
         {
             CuboidArea area = new CuboidArea(min,loc);
             mins.put(player.getName(), area.getLowLoc());
         }
         Location max = mins.get(player.getName());
         if(max!=null)
         {
             CuboidArea area = new CuboidArea(max,loc);
             maxs.put(player.getName(), area.getHighLoc());
         }
     }
     
     @Deprecated
     public Location getPlayerLoc1(String player)
     {
         return mins.get(player);
     }
 
     @Deprecated
     public Location getPlayerLoc2(String player)
     {
         return maxs.get(player);
     }
     
     public Location getMinimumLocation(String player)
     {
         return mins.get(player);
     }
 
     public Location getMaximumLocation(String player)
     {
         return maxs.get(player);
     }
 
     public boolean hasPlacedBoth(String player)
     {
         return (mins.containsKey(player) && maxs.containsKey(player));
     }
 
     public void showSelectionInfo(Player player) {
         String pname = player.getName();
         if (this.hasPlacedBoth(pname)) {
             CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(pname), getPlayerLoc2(pname));
             player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Selection.Total.Size")+":"+ChatColor.DARK_AQUA+" " + cuboidArea.getSize());
             PermissionGroup group = Residence.getPermissionManager().getGroup(player);
             if(Residence.getConfigManager().enableEconomy())
             player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Land.Cost")+":"+ChatColor.DARK_AQUA+" " + ((int)Math.ceil((double)cuboidArea.getSize()*group.getCostPerBlock())));
             player.sendMessage(ChatColor.YELLOW+"X"+Residence.getLanguage().getPhrase("Size")+":"+ChatColor.DARK_AQUA+" " + cuboidArea.getXSize());
             player.sendMessage(ChatColor.YELLOW+"Y"+Residence.getLanguage().getPhrase("Size")+":"+ChatColor.DARK_AQUA+" " + cuboidArea.getYSize());
             player.sendMessage(ChatColor.YELLOW+"Z"+Residence.getLanguage().getPhrase("Size")+":"+ChatColor.DARK_AQUA+" " + cuboidArea.getZSize());
         }
         else
             player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SelectPoints"));
     }
 
     public void vert(Player player, boolean resadmin)
     {
         if(hasPlacedBoth(player.getName()))
         {
             this.sky(player, resadmin);
             this.bedrock(player, resadmin);
         }
         else
         {
             player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
         }
     }
 
     public void sky(Player player, boolean resadmin)
     {
         if(hasPlacedBoth(player.getName()))
         {
             PermissionGroup group = Residence.getPermissionManager().getGroup(player);
             int min = mins.get(player.getName()).getBlockY();
             int newy = MAX_HEIGHT;
             if(!resadmin)
             {
                 if(group.getMaxHeight()<newy)
                     newy = group.getMaxHeight();
                 if(newy - min > (group.getMaxY()-1))
                     newy = min + (group.getMaxY()-1);
             }
             maxs.get(player.getName()).setY(newy);
             player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SelectionSky"));
         }
         else
         {
             player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SelectPoints"));
         }
     }
 
     public void bedrock(Player player, boolean resadmin)
     {
         if(hasPlacedBoth(player.getName()))
         {
             PermissionGroup group = Residence.getPermissionManager().getGroup(player);
             int max = maxs.get(player.getName()).getBlockY();
             int newy = MIN_HEIGHT;
             if(!resadmin)
             {
                 if(newy<group.getMinHeight())
                     newy = group.getMinHeight();
                 if(max - newy > (group.getMaxY()-1))
                     newy = max - (group.getMaxY()-1);
             }
             mins.get(player.getName()).setY(newy);
             player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SelectionBedrock"));
         }
         else
         {
             player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SelectPoints"));
         }
     }
 
     public void clearSelection(Player player)
     {
         mins.remove(player.getName());
         maxs.remove(player.getName());
     }
 
     public void selectChunk(Player player)
     {
         Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
         int xcoord = chunk.getX() * 16;
         int zcoord = chunk.getZ() * 16;
         int ycoord = MIN_HEIGHT;
         int xmax = xcoord + 15;
         int zmax = zcoord + 15;
         int ymax = MAX_HEIGHT;
         this.mins.put(player.getName(), new Location(player.getWorld(), xcoord, ycoord, zcoord));
         this.maxs.put(player.getName(), new Location(player.getWorld(), xmax,ymax,zmax));
         player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SelectionSuccess"));
     }
 
     public boolean worldEdit(Player player) {
         player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("WorldEditNotFound"));
         return false;
     }
 
     public void selectBySize(Player player, int xsize, int ysize, int zsize) {
         Location myloc = player.getLocation();
         Location loc1 = new Location(myloc.getWorld(), myloc.getBlockX() - xsize, myloc.getBlockY() - ysize, myloc.getBlockZ() - zsize);
         Location loc2 = new Location(myloc.getWorld(), myloc.getBlockX() + xsize, myloc.getBlockY() + ysize, myloc.getBlockZ() + zsize);
         placeLoc1(player, loc1);
         placeLoc2(player, loc2);
         player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("SelectionSuccess"));
         showSelectionInfo(player);
     }
 
     public void modify(Player player, boolean shift, int amount)
     {
         if(!this.hasPlacedBoth(player.getName()))
         {
             player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SelectPoints"));
             return;
         }
         Direction d = this.getDirection(player);
         if(d==null)
         {
             player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidDirection"));
         }
         CuboidArea area = new CuboidArea (mins.get(player.getName()),maxs.get(player.getName()));
         if(d == Direction.UP)
         {
             int oldy = area.getHighLoc().getBlockY();
             oldy = oldy + amount;
             if(oldy>MAX_HEIGHT)
             {
                 player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("SelectTooHigh"));
                 oldy = MAX_HEIGHT;
             }
             area.getHighLoc().setY(oldy);
             if(shift)
             {
                 int oldy2 = area.getLowLoc().getBlockY();
                 oldy2 = oldy2 + amount;
                 area.getLowLoc().setY(oldy2);
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Shifting.Up")+"...");
             }
             else
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Expanding.Up")+"...");
         }
         if(d == Direction.DOWN)
         {
             int oldy = area.getLowLoc().getBlockY();
             oldy = oldy - amount;
             if(oldy<MIN_HEIGHT)
             {
                 player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectTooLow"));
                 oldy = MIN_HEIGHT;
             }
             area.getLowLoc().setY(oldy);
             if(shift)
             {
                 int oldy2 = area.getHighLoc().getBlockY();
                 oldy2 = oldy2 - amount;
                 area.getHighLoc().setY(oldy2);
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Shifting.Down")+"...");
             }
             else
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Expanding.Down")+"...");
         }
         if(d == Direction.MINUSX)
         {
             int oldx = area.getLowLoc().getBlockX();
             oldx = oldx - amount;
             area.getLowLoc().setX(oldx);
             if(shift)
             {
                 int oldx2 = area.getHighLoc().getBlockX();
                 oldx2 = oldx2 - amount;
                 area.getHighLoc().setX(oldx2);
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Shifting")+" -X...");
             }
             else
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Expanding")+" -X...");
         }
         if(d == Direction.PLUSX)
         {
             int oldx = area.getHighLoc().getBlockX();
             oldx = oldx + amount;
             area.getHighLoc().setX(oldx);
             if(shift)
             {
                 int oldx2 = area.getLowLoc().getBlockX();
                 oldx2 = oldx2 + amount;
                 area.getLowLoc().setX(oldx2);
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Shifting")+" +X...");
             }
             else
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Expanding")+" +X...");
         }
         if(d == Direction.MINUSZ)
         {
             int oldz = area.getLowLoc().getBlockZ();
             oldz = oldz - amount;
             area.getLowLoc().setZ(oldz);
             if(shift)
             {
                 int oldz2 = area.getHighLoc().getBlockZ();
                 oldz2 = oldz2 - amount;
                 area.getHighLoc().setZ(oldz2);
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Shifting")+" -Z...");
             }
             else
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Expanding")+" -Z...");
         }
         if(d == Direction.PLUSZ)
         {
             int oldz = area.getHighLoc().getBlockZ();
             oldz = oldz + amount;
             area.getHighLoc().setZ(oldz);
             if(shift)
             {
                 int oldz2 = area.getLowLoc().getBlockZ();
                 oldz2 = oldz2 + amount;
                 area.getLowLoc().setZ(oldz2);
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Shifting")+" +Z...");
             }
             else
                 player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Expanding")+" +Z...");
         }
         maxs.put(player.getName(), area.getHighLoc());
         mins.put(player.getName(), area.getLowLoc());
     }
 
     private Direction getDirection(Player player)
     {
         float pitch = player.getLocation().getPitch();
         float yaw = player.getLocation().getYaw();
         if(pitch<-50)
             return Direction.UP;
         if(pitch > 50)
             return Direction.DOWN;
         if((yaw>45 && yaw<135) || (yaw<-45 && yaw>-135))
             return Direction.PLUSX;
         if((yaw>225 && yaw<315) ||  (yaw<-225 && yaw>-315))
             return Direction.MINUSX;
         if((yaw>135 && yaw<225) || (yaw<-135 && yaw>-225))
             return Direction.MINUSZ;
         if((yaw<45 || yaw>315) || (yaw>-45 || yaw<-315))
             return Direction.PLUSZ;
         return null;
     }
 
 }

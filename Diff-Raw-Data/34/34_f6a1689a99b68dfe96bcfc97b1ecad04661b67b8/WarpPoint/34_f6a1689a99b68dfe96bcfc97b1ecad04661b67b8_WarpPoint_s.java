 package it.masch.bukkit.awp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 
 class WarpPoint
 {
     private Location loc;
     private boolean isPublic;
     private List<String> players = null;
 
     public WarpPoint(Location loc)
     {
         this(loc, true);
     }
 
     public WarpPoint(Location loc, boolean isPublic)
     {
         this.loc = loc;
         this.setPublic(isPublic);
         if (!isPublic) {
             players = new ArrayList<String>();
         }
     }
 
     public WarpPoint(Server srv, String str) throws Exception
     {
         String[] params = str.split(",");
         if (params.length < 6) {
             throw new Exception("Invalid data format.");
         }
         this.loc = new Location(srv.getWorld(params[0]),
                 Double.parseDouble(params[1]), // X
                 Double.parseDouble(params[2]), // Y
                 Double.parseDouble(params[3]), // Z
                 Float.valueOf(params[4]).floatValue(), // yaw
                 Float.valueOf(params[5]).floatValue() // pitch
         );
         boolean isPublic = params.length == 6 || Boolean.valueOf(params[6]);
         this.setPublic(isPublic);
         if (!this.isPublic()) {
             players = new ArrayList<String>();
             for (int i = 7; i < params.length; i++) {
                 players.add(params[i]);
             }
         }
     }
 
     @Override
     public String toString()
     {
         String ret = this.loc.getWorld().getName() + "," + this.loc.getX()
                 + "," + this.loc.getY() + "," + this.loc.getZ() + ","
                 + this.loc.getYaw() + "," + this.loc.getPitch();
         if (!this.isPublic()) {
             ret += "," + this.isPublic();
             for (String name : players) {
                 ret += "," + name;
             }
         }
         return ret;
     }
 
     public boolean isPublic()
     {
         return isPublic;
     }
 
     public void setPublic(boolean isPublic)
     {
         if (this.isPublic == isPublic)
             return;
         this.isPublic = isPublic;
         if (isPublic) {
             players = null;
         } else {
             players = new ArrayList<String>();
         }
     }
 
     public List<String> getPlayers()
     {
         return players;
     }
 
     public void addPlayer(String playerName)
     {
         if (this.isPublic())
             return;
         if (this.getPlayers().contains(playerName))
             return;
         this.players.add(playerName);
     }
 
     public Location getLocation()
     {
         return this.loc;
     }
 
     public void setLocation(Location loc)
     {
         this.loc = loc;
     }
 }

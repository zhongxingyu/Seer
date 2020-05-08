 package dungeon.models;
 
 public class TeleporterTile extends Tile {
   public Target target;
 
   public static class Target {
    public String roomId;
 
     public int x;
 
     public int y;
   }
 }

 package dungeon.models;
 
 import dungeon.models.messages.Transform;
 
 import java.io.Serializable;
 import java.util.*;
 
 public class World implements Serializable {
   private final List<Level> levels;
 
   private final List<Player> players;
 
   public World (List<Level> levels, List<Player> players) {
     this.levels = Collections.unmodifiableList(new ArrayList<>(levels));
     this.players = Collections.unmodifiableList(new ArrayList<>(players));
   }
 
   public List<Level> getLevels () {
     return this.levels;
   }
 
   public List<Player> getPlayers () {
     return this.players;
   }
 
   public List<Player> getPlayersInRoom (Room room) {
     return this.getPlayersInRoom(room.getId());
   }
 
   public List<Player> getPlayersInRoom (String roomId) {
     List<Player> players = new ArrayList<>();
 
     for (Player player : this.players) {
       if (player.getRoomId().equals(roomId)) {
         players.add(player);
       }
     }
 
     return players;
   }
 
   public Room getCurrentRoom (Player player) {
     Level currentLevel = this.getCurrentLevel(player);
 
     if (currentLevel == null) {
       return null;
     }
 
     for (Room room : currentLevel.getRooms()) {
       if (room.getId().equals(player.getRoomId())) {
         return room;
       }
     }
 
     return null;
   }
 
   public List<Room> getCurrentRooms () {
     List<Room> rooms = new ArrayList<>();
 
     for (Player player : this.players) {
       rooms.add(this.getCurrentRoom(player));
     }
 
     return rooms;
   }
 
   public Player getPlayer (int id) {
     Player player = null;
 
     for (Player a : this.players) {
       if (a.getId() == id) {
         player = a;
       }
     }
 
     return player;
   }
 
   private Level getCurrentLevel (Player player) {
     for (Level level : this.levels) {
       if (!level.getId().equals(player.getLevelId())) {
         continue;
       }
 
       return level;
     }
 
     return null;
   }
 
   private Set<Level> getCurrentLevels () {
     Set<Level> levels = new HashSet<>();
 
     for (Player player : this.players) {
       levels.add(this.getCurrentLevel(player));
     }
 
     return levels;
   }
 
   /**
    * Applies transforms only to the current levels for performance reasons.
    */
   public World apply (Transform transform) {
     if (transform instanceof WorldTransform) {
       return ((WorldTransform)transform).apply(this);
     } else {
       List<Level> levels = new ArrayList<>();
       List<Player> players = new ArrayList<>();
 
       for (Player player : this.players) {
         players.add(player.apply(transform));
       }
 
       Set<Level> currentLevels = this.getCurrentLevels();
 
       for (Level level : this.levels) {
         if (currentLevels.contains(level)) {
           levels.add(level.apply(transform));
         } else {
           levels.add(level);
         }
       }
 
       return new World(levels, players);
     }
   }
 
   private static class WorldTransform implements Transform {
     public World apply (World world) {
       return new World(
         this.levels(world),
         this.players(world)
       );
     }
 
     protected List<Level> levels (World world) {
       return world.levels;
     }
 
     protected List<Player> players (World world) {
       return world.players;
     }
   }
 
   public static class AddPlayerTransform extends WorldTransform {
     public final Player player;
 
     public AddPlayerTransform (Player player) {
       this.player = player;
     }
 
     @Override
     protected List<Player> players (World world) {
       List<Player> players = new ArrayList<>(world.players);
      players.add(player);
       return players;
     }
   }
 
   public static class RemovePlayerTransform extends WorldTransform {
     public final int playerId;
 
     public RemovePlayerTransform (int playerId) {
       this.playerId = playerId;
     }
 
     @Override
     protected List<Player> players (World world) {
       List<Player> players = new ArrayList<>();
 
       for (Player player : world.players) {
         if (this.playerId != player.getId()) {
           players.add(player);
         }
       }
 
       return players;
     }
   }
 }

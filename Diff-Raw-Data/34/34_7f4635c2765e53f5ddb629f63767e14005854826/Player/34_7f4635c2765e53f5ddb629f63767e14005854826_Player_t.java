 package lt.ltech.numbers.player;
 
 import java.io.Serializable;
import java.util.UUID;
 
 import lt.ltech.numbers.game.Number;
 
 @SuppressWarnings("serial")
 public class Player implements Serializable {
     /**
      * The player's index in a game in progress. For any given game this will be
      * unique for all players in the game
      */
     private Byte index;
 
     /**
      * The player's ID on a single phone. This should be unique for all players
      * stored on a given phone
      */
     private Long id;
 
     /**
     * The player's GUID. Used to identify players in other player's phones.
     */
    private final UUID guid;

    /**
      * The player's name
      */
     private final String name;
 
     /**
      * The number picked by the player in a game in progress
      */
     private Number number;
 
    public Player(UUID guid, String name) {
        this.guid = guid;
         this.name = name;
     }
 
     public Byte getIndex() {
         return this.index;
     }
 
     public void setIndex(Byte index) {
         this.index = index;
     }
 
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
    public UUID getGuid() {
        return guid;
    }

     public String getName() {
         return this.name;
     }
 
     public Number getNumber() {
         return this.number;
     }
 
     public void setNumber(Number number) {
         this.number = number;
     }
 
     public String toString() {
         return String.format("Player no. %d: %s", id, name);
     }
 
     public int hashCode() {
         return this.id.hashCode();
     }
 
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (!(o instanceof Player)) {
             return false;
         }
         Player p = (Player) o;
         return (this.id.equals(p.id) && this.index.equals(p.index));
     }
 }

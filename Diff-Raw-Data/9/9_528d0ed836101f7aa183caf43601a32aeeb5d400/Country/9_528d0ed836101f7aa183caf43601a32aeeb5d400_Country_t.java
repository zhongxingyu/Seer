 package risk.game;
 
 import java.io.Serializable;
 
 import risk.protocol.command.Command;
 
 public class Country implements Serializable {
     private static final long serialVersionUID = Command.serialVersionUID;
 
     /**
      * The name of the country.
      */
     private String name;
     
     /**
      * The player who controls this country.
      */
     private Player owner;
 
     public Player getOwner() {
         return owner;
     }
 
     public int getTroops() {
         return troops;
     }
 
     /**
      * The number of units of soldiers stationed in this country.
      */
     private int troops;
     
     public Country(String name) {
         this.name = name;
     }
     
     public void setOwner(Player owner) {
         this.owner = owner;
     }
     
     public String getName() {
         return name;
     }
 
     public void setTroops(int i) {
         troops = i;
     }
 }

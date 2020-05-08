 package cx.ath.fota.dangerousFlight.model;
 
 
 import org.bukkit.entity.Player;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 
 @Entity
 @Table(name = "dangerousFlight")
 @SuppressWarnings("UnusedDeclaration")
 public class DFlier implements Serializable {
     private static final long serialVersionUID = 3952001110315770131L;
     Long id;
     String playerName;
     Boolean flightEnabled;
     Boolean flying;
 
     public DFlier(Player player) {
         this.flying = player.isFlying();
         this.flightEnabled = player.getAllowFlight();
         this.playerName = player.getName();
     }
 
    public DFlier() {
        this.flying = false;
        this.flightEnabled = false;
        this.playerName = null;
    }

     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Column(unique = true)
     public String getPlayerName() {
         return this.playerName;
     }
 
     public void setPlayerName(String playerName) {
         this.playerName = playerName;
     }
 
     @Column
     public Boolean getFlightEnabled() {
         return this.flightEnabled;
     }
 
     public void setFlightEnabled(Boolean flightEnabled) {
         this.flightEnabled = flightEnabled;
     }
 
     @Column
     public Boolean getFlying() {
         return this.flying;
     }
 
     public void setFlying(Boolean flying) {
         this.flying = flying;
     }
 }
 

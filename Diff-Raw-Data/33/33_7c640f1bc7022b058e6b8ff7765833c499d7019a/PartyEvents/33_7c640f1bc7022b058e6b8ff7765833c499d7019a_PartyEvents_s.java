 package ca.gibstick.discosheep;
 
import java.util.logging.Level;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Sheep;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 
 /**
  *
  * @author Charlie
  */
 public class PartyEvents implements Listener {
 
 	DiscoSheep parent;
 	DiscoParty party;
 
 	public PartyEvents(DiscoSheep parent, DiscoParty party) {
 		this.parent = parent;
 		this.party = party;
 	}
 
 	// prevent sheep shearing
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerShear(PlayerShearEntityEvent e) {
 		if (e.getEntity() instanceof Sheep) {
 
 			if (party.getSheepList().contains((Sheep) e.getEntity())) {
 				e.setCancelled(true);
 			}
 
 		}
 	}
 
 	// actually make sheep and other guests invincible
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onLivingEntityDamageEvent(EntityDamageEvent e) {
 		if (e.getEntity() instanceof Sheep) {
 			if (party.getSheepList().contains((Sheep) e.getEntity())) {
 				{
 					party.jump((LivingEntity) e.getEntity()); // for kicks
 					e.setCancelled(true);
 				}
 			}
 
 		}
 
 
 		if (party.getGuestList().contains(e.getEntity())) {
 			party.jump((LivingEntity) e.getEntity());
 			e.setCancelled(true);
 		}

		parent.getLogger().log(Level.INFO, "Debug: EVENT TRIGGERED");
 	}
 
 	// prevent uninvited guests from targetting players
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEntityTargetLivingEntityEvent(EntityTargetEvent e) {
 
 		if (party.getGuestList().contains(e.getEntity())) { // safe; event is only triggered by LivingEntity targetting LivingEntity
 			e.setCancelled(true);
 		}
 
 	}
 }

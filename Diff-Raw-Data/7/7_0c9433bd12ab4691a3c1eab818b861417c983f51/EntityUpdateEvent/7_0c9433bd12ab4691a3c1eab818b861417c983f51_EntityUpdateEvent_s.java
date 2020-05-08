 package org.glydar.paraglydar.event.events;
 
 import java.util.Collection;
 
 import org.glydar.paraglydar.data.EntityData;
 import org.glydar.paraglydar.event.Event;
 import org.glydar.paraglydar.models.BaseTarget;
 import org.glydar.paraglydar.models.CustomTarget;
import org.glydar.paraglydar.models.EveryoneTarget;
 import org.glydar.paraglydar.models.Player;
 import org.glydar.paraglydar.models.WorldTarget;
 
 public class EntityUpdateEvent extends Event {
 
 	private boolean cancelled = false;
 	private Player player;
 	private EntityData ed;
 	private BaseTarget recievers;
 
 	public EntityUpdateEvent(final Player player, final EntityData ed) {
 		this.setPlayer(player);
 		this.setEntityData(ed);
 		recievers = new WorldTarget(player.getWorld());
 	}
 
 	public boolean isCancelled() {
 		return cancelled;
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	public EntityData getEntityData() {
 		return ed;
 	}
 
 	public void setEntityData(EntityData ed) {
 		this.ed = ed;
 	}
 
 	public BaseTarget getTarget() {
 		return recievers;
 	}
 
 	public Collection<Player> getRecievers() {
 		return recievers.getPlayers();
 	}
 
 	public void setRecievers(Collection<Player> c) {
 		recievers = new CustomTarget(c);
 	}
 }

 package gt.plugin.helloeditor;
 
 import gt.general.trigger.Trigger;
 import gt.general.trigger.TriggerContext;
 import gt.general.trigger.persistance.YamlSerializable;
 import gt.general.trigger.response.Response;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 import org.getspout.spoutapi.particle.Particle;
 import org.getspout.spoutapi.particle.Particle.ParticleType;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 
 
 public class ParticleManager implements Runnable {
 
 	private Multimap<Player, Block> activeBlocks;
 	
	/**
	 * generates a new particle manager
	 */
 	public ParticleManager() {
 		activeBlocks = HashMultimap.create();
 	}
 	
 	/**
 	 * highlight all blocks of a context
 	 * @param context	TriggerContext
 	 * @param type		Particle type
 	 * @param player	player that can see particles
 	 */
 	public void addContext(final TriggerContext context, final ParticleType type, final Player player) {
 		for(Trigger trigger : context.getTriggers()) {
 			addSerializable(trigger, type, player);
 		}
 		for(Response response : context.getResponses()) {
 			addSerializable(response, type, player);
 		}
 		player.sendMessage(ChatColor.YELLOW + "Highlighting " + context.getLabel());
 	}
 	
 	/**
 	 * despawns all particles of a context
 	 * @param context	a TriggerContext
 	 * @param player	the player which could see the particles
 	 */
 	public void removeHighlight(final TriggerContext context, final Player player) {
 		for(Trigger trigger : context.getTriggers()) {
 			removeSerializable(trigger, player);
 		}
 		for(Response response : context.getResponses()) {
 			removeSerializable(response, player);
 		}
 	}
 	
 	/**
 	 * highlights the edges of a block
 	 * @param block the block to highlight
 	 * @param player a bukkit player
 	 */
 	private void highlight(final Block block, final Player player) {
 		//TODO: variable effect?
 		ParticleType type = ParticleType.DRIPLAVA;
 		Location loc = block.getLocation();
 		
 		// yep, that's the 8 edges
 		paintParticle(type, loc, new Vector(0, 0, 0), player);
 		paintParticle(type, loc, new Vector(0, 0, 1), player);
 		paintParticle(type, loc, new Vector(0, 1, 0), player);
 		paintParticle(type, loc, new Vector(0, 1, 1), player);
 		paintParticle(type, loc, new Vector(1, 0, 0), player);
 		paintParticle(type, loc, new Vector(1, 0, 1), player);
 		paintParticle(type, loc, new Vector(1, 1, 0), player);
 		paintParticle(type, loc, new Vector(1, 1, 1), player);
 		
 	}
 	
 	/**
 	 * despawns all particles of a trigger/response
 	 * @param serializable	a trigger or response
 	 * @param player a bukkit player
 	 */
 	public void removeSerializable(final YamlSerializable serializable, final Player player) {
		for(Block block : serializable.getBlocks()) {
 			activeBlocks.remove(player, block);
 		}
 	}
 	
 	/**
 	 * highlight all blocks of a serializable object
 	 * @param serializable	trigger or response
 	 * @param type			particle type
 	 * @param player		player that can see particles
 	 */
 	public void addSerializable(final YamlSerializable serializable, final ParticleType type, final Player player) {
 		
 		activeBlocks.putAll(player, serializable.getBlocks());
 	}
 
 	/**
 	 * spawns a single particle
 	 * @param type	particle type
 	 * @param origin	location of the block that is highlighted
 	 * @param offset	an offset that specifies which edge of the block is highlighted
 	 * @param player	player that can see the particle
 	 * @return the particle
 	 */
 	private Particle paintParticle(final ParticleType type, final Location origin, final Vector offset, final Player player) {
 
 		Location newLoc = origin.clone().add(offset);
 		newLoc.subtract(0.04, 0.1, 0.04);	// looks much better with this additional offset
 		Particle particle = new Particle(type, newLoc, new Vector(0,0,0));
 		
 		particle.setScale(7);		// particle size
 		particle.setAmount(1);
 		// view range: this one is overwritten by client settings!
 		particle.setRange(100.0);	
 		particle.setMaxAge(20);		// lifetime in ticks
 		particle.setGravity(0);
 		
 		particle.spawn((SpoutPlayer) player);
 		
 		return particle;
 	}
 
 	@Override
 	public void run() {
 		for (Player player : activeBlocks.keySet()) {
 			for (Block block : activeBlocks.get(player)) {
 				highlight(block, player);
 			}
 		}
 		
 	}
 }

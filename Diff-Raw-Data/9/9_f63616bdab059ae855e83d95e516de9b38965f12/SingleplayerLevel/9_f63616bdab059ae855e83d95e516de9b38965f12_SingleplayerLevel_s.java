 package pl.spaceshooters.level;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pl.spaceshooters.entity.PlayerSP;
 import spaceshooters.aurora.entity.IEntity;
 import spaceshooters.aurora.particle.IParticle;
 
 public abstract class SingleplayerLevel extends Level {
 	
 	protected PlayerSP player = new PlayerSP(this, 400, 300);
 	
 	public SingleplayerLevel() {
 		this.spawnEntity(player);
 	}
 	
 	@Override
 	public void render() {
 		// Render effects.
 		List<IParticle> renderAfter = new ArrayList<>();
 		for (IParticle e : particles) {
 			if (e.getRenderAfterEntities()) {
 				renderAfter.add(e);
 				continue;
 			}
 			
 			if (e.isAlive())
 				e.render();
 		}
 		
 		// Render entities.
 		for (IEntity e : entities)
 			if (e.isAlive())
 				e.render();
 		
 		// Render special effects, for example explosions.
 		for (IParticle e : renderAfter)
 			if (e.isAlive())
 				e.render();
 	}
 	
 	@Override
 	public void update(int delta) {
 		// Entity updates.
 		for (IEntity e : this.getEntities())
 			if (e.isAlive())
 				e.update(delta);
 			else
 				this.removeEntity(e);
 		
 		// Effect updates.
 		for (IParticle e : this.getParticles())
 			if (e.isAlive())
 				e.update(delta);
 			else
 				this.removeParticle(e);
 	}
 	
 	@Override
 	public PlayerSP getPlayer() {
 		return player;
 	}
 	
 	@Override
 	public void spawnEntity(IEntity entity) {
 		entities.add(entity);
 	}
 	
 	@Override
 	public void removeEntity(IEntity entity) {
 		entities.remove(entity);
 	}
 	
 	@Override
 	public void spawnParticle(IParticle particle) {
 		particles.add(particle);
 	}
 	
 	@Override
 	public void removeParticle(IParticle particle) {
 		particles.remove(particle);
 	}
 }

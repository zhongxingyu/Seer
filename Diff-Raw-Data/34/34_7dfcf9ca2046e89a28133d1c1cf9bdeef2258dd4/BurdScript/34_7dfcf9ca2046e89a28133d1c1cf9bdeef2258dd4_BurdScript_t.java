 package burd.customscripts;
 
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import toritools.entity.Entity;
 import toritools.entity.Level;
 import toritools.entity.physics.PhysicsModule;
 import toritools.math.Vector2;
 import toritools.scripting.EntityScript;
 import toritools.scripting.ScriptUtils;
 import burd.BurdGame;
 
 public class BurdScript implements EntityScript {
 
 	PhysicsModule physicsModule;
 
 	/**
 	 * defaults overwritten by player.entity settings.
 	 */
 	private float hSpeed = .2f, vSpeed = .5f;
 
 	private Vector2 startPos;
 
 	private List<Entity> breadsEaten = new ArrayList<Entity>();
 
 	public void onSpawn(Level level, Entity self) {
 
 		// hSpeed = self.variables.getFloat("hSpeed");
 		//
 		// vSpeed = self.variables.getFloat("vSpeed");
 
 		startPos = self.pos.clone();
 
 		physicsModule = new PhysicsModule(new Vector2(0, 0.2f), 1f, self);
 
 		physicsModule.onStart();
 	}
 
 	public void onUpdate(Level level, Entity self) {
 
		boolean flapped = false;

 		self.sprite.setCycle(0);
 		if (BurdGame.keys.isPressed(KeyEvent.VK_SPACE)) {
 			physicsModule.addVelocity(new Vector2(0, -vSpeed));
			flapped = true;
 		}

 		if (BurdGame.keys.isPressed(KeyEvent.VK_A)) {
 			physicsModule.addVelocity(new Vector2(-hSpeed, 0));
 			self.sprite.setCycle(1);
 		}
 		if (BurdGame.keys.isPressed(KeyEvent.VK_D)) {
 			physicsModule.addVelocity(new Vector2(hSpeed, 0));
 			self.sprite.setCycle(2);
 		}
 
		Vector2 delta = physicsModule.onUpdate();

		ScriptUtils.safeMove(self, delta, level.solids.toArray(new Entity[0]));

		if (flapped) {
			self.sprite.nextFrame();
		} else {
			// make the wing match the motion
			if (Math.abs(delta.y) < 5)
				self.sprite.setFrame(1);
			else if (delta.y < 0)
				self.sprite.setFrame(2);
			else
				self.sprite.setFrame(0);
		}
		
		BurdGame.setDisplayPrompt(delta.y + "");
 
 		for (Entity spike : level.getEntitiesWithType("spike")) {
 			if (ScriptUtils.isColliding(spike, self)) {
 				for (int i = 0; i < 10; i++) {
 					Entity blood = VolcanoParticleScript.getBlood();
 					blood.pos = self.pos.clone();
 					level.spawnEntity(blood);
 				}
 				if (!breadsEaten.isEmpty()) {
 					for (Entity bread : breadsEaten) {
 						bread.pos = self.pos.clone();
 						level.spawnEntity(bread);
 					}
 					breadsEaten.clear();
 				}
 				self.pos = startPos.clone();
 				physicsModule.clearVelocity();
 				break;
 			}
 
 		}
 
 		for (Entity flag : level.getEntitiesWithType("flag"))
 			if (ScriptUtils.isColliding(flag, self)
 					&& (flag != latestFlag || !breadsEaten.isEmpty())) {
 				latestFlag = flag;
 				if (!breadsEaten.isEmpty())
 					breadsEaten.clear();
 				startPos = flag.pos.clone();
 				for (int i = 0; i < 10; i++) {
 					Entity sparkle = VolcanoParticleScript.getSparkle();
 					sparkle.pos = self.pos.clone();
 					level.spawnEntity(sparkle);
 				}
 			}
 
 		List<Entity> breads = level.getEntitiesWithType("bread");
 		if (breads == null || breads.isEmpty()) {
 			BurdGame.nextLevel();
 		} else {
 			for (Entity bread : breads) {
 				if (bread.active && ScriptUtils.isColliding(bread, self)) {
 					level.killEntity(bread);
 					breadsEaten.add(bread);
 				}
 			}
 		}
 	}
 
 	private Entity latestFlag;
 
 	public void onDeath(Level level, Entity self, boolean isRoomExit) {
 	}
 }

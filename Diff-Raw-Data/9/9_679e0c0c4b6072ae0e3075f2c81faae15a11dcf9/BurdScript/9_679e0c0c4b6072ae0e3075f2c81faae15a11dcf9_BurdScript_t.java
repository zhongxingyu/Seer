 package burd.customscripts;
 
 import java.awt.event.KeyEvent;
 
 import toritools.entity.Entity;
 import toritools.entity.Level;
 import toritools.entity.physics.PhysicsModule;
 import toritools.math.Vector2;
 import toritools.scripting.EntityScript;
 import toritools.scripting.ScriptUtils;
 import burd.BurdGame;
 
 public class BurdScript implements EntityScript {
 
 	private PhysicsModule physicsModule;
 
 	private Entity latestFlag;
 
 	private float hSpeed = .2f, vSpeed = .5f;
 
 	private Vector2 startPos;
 
 	public boolean inAir = true;
 
 	public void onSpawn(Level level, Entity self) {
 
 		startPos = self.pos.clone();
 
 		physicsModule = new PhysicsModule(new Vector2(0, 0.2f), 1f, self);
 	}
 
 	public void onUpdate(Level level, Entity self) {
 
 		boolean flapped = false;
 
 		if (BurdGame.keys.isPressed(KeyEvent.VK_SPACE)) {
 			physicsModule.addVelocity(new Vector2(0, -vSpeed));
 			flapped = true;
 		}
 
 		if (BurdGame.keys.isPressed(KeyEvent.VK_A)) {
 			physicsModule.addVelocity(new Vector2(-hSpeed, 0));
 		}
 		if (BurdGame.keys.isPressed(KeyEvent.VK_D)) {
 			physicsModule.addVelocity(new Vector2(hSpeed, 0));
 
 		}
 
 		Vector2 delta = physicsModule.onUpdate();
 
 		boolean onGround = ScriptUtils.safeMove(self, delta, true,
 				level.solids.toArray(new Entity[0])).mag() != 0;
 
		if (onGround || !inAir)
 			physicsModule.setgDrag(.95f);
 		else
 			physicsModule.setgDrag(1f);
 
 		if (Math.abs(delta.x) < 1)
 			self.sprite.setCycle(0);
 		else if (delta.x < 0)
 			self.sprite.setCycle(1);
 		else
 			self.sprite.setCycle(2);
 
 		if (flapped || (onGround && Math.abs(delta.x) > 1)) {
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
 
 		if (!BurdGame.debug) {
 			for (Entity spike : level.getEntitiesWithType("spike")) {
 				if (spike.inView && ScriptUtils.isColliding(spike, self)) {
 					for (Entity bread : level.getEntitiesWithType("bread")) {
 						bread.active = true;
 					}
 					for (int i = 0; i < 15; i++) {
 						Entity blood = VolcanoParticleScript.getBlood();
 						blood.pos = self.pos.clone();
 						level.spawnEntity(blood);
 					}
 					self.pos = startPos.clone();
 					physicsModule.clearVelocity();
 					break;
 				}
 
 			}
 		}
 
 		for (Entity flag : level.getEntitiesWithType("flag"))
 			if (flag.inView && ScriptUtils.isColliding(flag, self)
 					&& (flag != latestFlag)) {
 				if (latestFlag != null)
 					latestFlag.sprite.setFrame(0);
 				latestFlag = flag;
 				latestFlag.sprite.setFrame(1);
 				startPos = flag.pos.clone();
 			}
 
 		for (Entity air : level.getEntitiesWithType("inAir"))
 			if (air.inView && ScriptUtils.isColliding(air, self)) {
 				inAir = true;
 			}
 		for (Entity water : level.getEntitiesWithType("inWater"))
 			if (water.inView && ScriptUtils.isColliding(water, self)) {
 				inAir = false;
 			}
 	}
 
 	public void onDeath(Level level, Entity self, boolean isRoomExit) {
 	}
 }

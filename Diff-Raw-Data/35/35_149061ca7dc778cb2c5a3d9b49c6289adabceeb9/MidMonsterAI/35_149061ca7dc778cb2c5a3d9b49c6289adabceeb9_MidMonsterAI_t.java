 package com.soc.ai.bosses;
 
 import java.util.Random;
 
 import com.artemis.Entity;
 import com.artemis.utils.Bag;
 import com.soc.ai.AI;
 import com.soc.core.Constants;
 import com.soc.core.EntityFactory;
 import com.soc.core.SoC;
 import com.soc.game.attacks.spells.Spell;
 import com.soc.game.components.Buff;
 import com.soc.game.components.Delay;
 import com.soc.game.components.Position;
 import com.soc.game.components.State;
import com.soc.game.components.Stats;
 import com.soc.game.components.Velocity;
 import com.soc.game.states.benefits.Casting;
 import com.soc.game.states.benefits.Teleport;
 
 public class MidMonsterAI extends AI{
 
 
 	public float timer;
 	public Random r;
 	float limitXLeft;
 	float limitXRight;
 	float limitYBottom;
 	float limitYUp;
 	boolean monsterSpawned;
 	boolean characterInside;
 	boolean flameWallSpawned;
 	Bag<Entity>flameWallsLeft;
 	Bag<Entity>flameWallsRight;
 	public float teleportTimer;
 	public boolean fireBreath;
 	public MidMonsterAI(){
 		timer = 0;
 		r = new Random();
 		limitXLeft=37*Constants.World.TILE_SIZE;
 		limitXRight=61*Constants.World.TILE_SIZE;
 		limitYBottom=54*Constants.World.TILE_SIZE;
 		limitYUp=83*Constants.World.TILE_SIZE;
 		monsterSpawned=false;
 		characterInside=false;
 		flameWallSpawned=false;
 		flameWallsLeft=new Bag<Entity>();
 		flameWallsRight=new Bag<Entity>();
 		fireBreath=false;
 		teleportTimer=10f;
 	}
 	
 	@Override
 	public void process(Entity e) {
 		Position pos = SoC.game.positionmapper.get(e);
 		Velocity vel = SoC.game.velocitymapper.get(e);
 		State state = SoC.game.statemapper.get(e);
 		Entity player = SoC.game.player;
 		Position playerPos = SoC.game.positionmapper.get(player);
 		if(state.state == State.DYING) return;
 		if(state.state >= State.BLOCKED){return;}		
 		if(fireBreath){
 			fireBreath=false;
 			teleportTimer=10f;
 			for(int i=0;i<flameWallsLeft.size();i++){
 				Position pFlame=SoC.game.positionmapper.get(flameWallsLeft.get(i));
 				pFlame.x=38*Constants.World.TILE_SIZE;
 			}
 			for(int i=0;i<flameWallsRight.size();i++){
 				Position pFlame=SoC.game.positionmapper.get(flameWallsRight.get(i));
 				pFlame.x=59*Constants.World.TILE_SIZE;
 			}
 
 		}
 		if(!monsterSpawned ){
 			EntityFactory.createWall(e, 54, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 55, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 56, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 57, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 58, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 59, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 60, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 45, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 44, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 43, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 42, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 41, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 40, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 39, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 38, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 82, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 81, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 80, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 79, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 78, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 77, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 76, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 75, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 74, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 73, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 72, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 71, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 70, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 69, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 68, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 67, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 66, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 65, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 64, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 63, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 62, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 61, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 60, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 59, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 58, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 57, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 56, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 55, 0).addToWorld();
 			EntityFactory.createWall(e, 37, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 38, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 39, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 40, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 41, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 42, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 43, 54, 0).addToWorld();
 //			EntityFactory.createWall(e, 45, 54, 0).addToWorld();
 //			EntityFactory.createWall(e, 46, 54, 0).addToWorld();
 //			EntityFactory.createWall(e, 47, 54, 0).addToWorld();
 //			EntityFactory.createWall(e, 48, 54, 0).addToWorld();
 //			EntityFactory.createWall(e, 49, 54, 0).addToWorld();
 //			EntityFactory.createWall(e, 50, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 52, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 53, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 54, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 55, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 56, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 57, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 58, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 59, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 60, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 81, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 80, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 79, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 78, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 77, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 76, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 75, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 74, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 73, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 72, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 71, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 70, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 69, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 68, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 67, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 66, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 65, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 64, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 63, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 62, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 61, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 60, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 59, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 58, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 57, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 56, 0).addToWorld();
 			EntityFactory.createWall(e, 61, 55, 0).addToWorld();
 			monsterSpawned=true;
 		}
 		if(!flameWallSpawned){
 			Entity flame=null;
 			for(int i=54;i<=79;i++){
 				flame=EntityFactory.createFlameWall(e, 38, i, 0);
 				SoC.game.groupmanager.add(flame, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(flame, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager.setLevel(flame, Constants.Groups.LEVEL +pos.z);
 				flame.addToWorld();
 				flameWallsLeft.add(flame);
 			}
 			for(int i=54;i<=79;i++){
 				flame=EntityFactory.createFlameWall(e, 59, i, 0);
 				SoC.game.groupmanager.add(flame, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(flame, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager
 				.setLevel(flame, Constants.Groups.LEVEL +pos.z);
 				flame.addToWorld();
 				flameWallsRight.add(flame);
 			}
 			flameWallSpawned=true;
 		}
 		
 		if(!characterInside && (playerPos.x>limitXLeft && playerPos.x<limitXRight) && (playerPos.y>limitYBottom && playerPos.y<limitYUp)){
 			EntityFactory.createWall(e, 45, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 46, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 47, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 48, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 49, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 50, 54, 0).addToWorld();
			
			SoC.game.musicmanager.play(e, "dark-descent.ogg");

 			characterInside=true;
 		}
 
 		if((playerPos.x<limitXLeft || playerPos.x>limitXRight) || (playerPos.y<limitYBottom || playerPos.y>limitYUp)){
 			state.state=State.IDLE;
 			vel.vx=0;
 			vel.vy=0;
 			return;
 		}
 		
 		
 		timer-=SoC.game.world.delta;
 		teleportTimer-=SoC.game.world.delta;
 		
 		float dsty = playerPos.y - pos.y;
 		float dstx = playerPos.x - pos.x;
 		
 		pos.direction.x = Math.signum(dstx);
 		pos.direction.y = Math.signum(dsty); 
 		
 		vel.vx = vel.speed * pos.direction.x;
 		vel.vy = vel.speed * pos.direction.y;
 		
 		
 			if(timer<=0){
 				timer=1f;
 				Entity spawned=EntityFactory.createFlame((playerPos.x-(Constants.Characters.WIDTH/2)), playerPos.y, playerPos.z,SoC.game.statsmapper.get(player).intelligence);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager.setLevel(spawned, Constants.Groups.LEVEL +pos.z);
 				spawned.addToWorld();
 				spawned=EntityFactory.createFlame(playerPos.x+(Constants.Characters.WIDTH/2), playerPos.y, playerPos.z,SoC.game.statsmapper.get(player).intelligence);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager.setLevel(spawned, Constants.Groups.LEVEL +pos.z);
 				spawned.addToWorld();
 				spawned=EntityFactory.createFlame(playerPos.x-(Constants.Characters.WIDTH), playerPos.y+(Constants.Characters.HEIGHT), playerPos.z,SoC.game.statsmapper.get(player).intelligence);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager.setLevel(spawned, Constants.Groups.LEVEL +pos.z);
 				spawned.addToWorld();
 				spawned=EntityFactory.createFlame(playerPos.x+(Constants.Characters.WIDTH), playerPos.y+(Constants.Characters.HEIGHT), playerPos.z,SoC.game.statsmapper.get(player).intelligence);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager.setLevel(spawned, Constants.Groups.LEVEL +pos.z);
 				spawned.addToWorld();
 				spawned=EntityFactory.createFlame((playerPos.x-(Constants.Characters.WIDTH/2)), playerPos.y+(Constants.Characters.HEIGHT), playerPos.z,SoC.game.statsmapper.get(player).intelligence);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager.setLevel(spawned, Constants.Groups.LEVEL +pos.z);
 				spawned.addToWorld();
 				spawned=EntityFactory.createFlame(playerPos.x+(Constants.Characters.WIDTH/2), playerPos.y+(Constants.Characters.HEIGHT), playerPos.z,SoC.game.statsmapper.get(player).intelligence);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(spawned, Constants.Groups.MAP_BOUND);
 				SoC.game.levelmanager.setLevel(spawned, Constants.Groups.LEVEL +pos.z);
 				spawned.addToWorld();
 			}
 
 			state.state = State.WALK;
 			if(Math.abs(dstx) < 32) pos.direction.x = 0;
 			else if(Math.abs(dsty) < 10) pos.direction.y = 0;
 			
 			if(teleportTimer<=0 ){ 
 				//The bounds are 37 and 57. So, a random number between 0-19->+1(1-20)-->+37
 				//int tileX=r.nextInt(20)+1+37;
 				int tileX=(int)(playerPos.x*Constants.World.TILE_FACTOR)-4+r.nextInt(9);
 				Buff.addbuff(e, new Teleport(tileX*Constants.World.TILE_SIZE, 78*Constants.World.TILE_SIZE, 0));
 				for(int i=0;i<flameWallsLeft.size();i++){
 					Position pFlame=SoC.game.positionmapper.get(flameWallsLeft.get(i));
 					pFlame.x=(tileX-4)*Constants.World.TILE_SIZE;
 				}
 				for(int i=0;i<flameWallsRight.size();i++){
 					Position pFlame=SoC.game.positionmapper.get(flameWallsRight.get(i));
 					pFlame.x=(tileX+4)*Constants.World.TILE_SIZE;
 				}
 				Buff.addbuff(e, new Casting(2f,Constants.BuffColors.RED));
 				pos.direction.x=0;
 				pos.direction.y=-1;
 				fireBreath=true;
 				
 				Spell spell = SoC.game.spells[Constants.Spells.FIREBREATH];
 				state.state = spell.state;
 				e.addComponent(new Delay(Constants.Groups.ENEMY_ATTACKS,spell.cast, spell.blocking, Constants.Spells.FIREBREATH));
 				vel.vx = 0;
 				vel.vy = 0;
 				e.changedInWorld();
 			}
 
 	}
 
 	@Override
 	public void death(Entity e) {
 		SoC.game.progress.midMonsterDefeated=true;
		Position pos = SoC.game.positionmapper.get(e);
		EntityFactory.createItem(Constants.Items.GOLD_SHIELD, pos.x, pos.y, pos.z);
		
		Stats s = SoC.game.statsmapper.get(SoC.game.player);
		System.out.println(s.clazz);
		if(s.clazz.equals(Constants.Characters.WARRIOR)){
			EntityFactory.createItem(Constants.Items.GOLD_SWORD, pos.x, pos.y, pos.z);
		} else if(s.clazz.equals(Constants.Characters.MAGE)){
			EntityFactory.createItem(Constants.Items.DIVINE_WAND, pos.x, pos.y, pos.z);
		}
 	}
 
 }

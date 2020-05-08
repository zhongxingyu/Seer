 package com.soc.ai.bosses;
 
 import com.artemis.Entity;
 import com.badlogic.gdx.math.Vector2;
 import com.soc.ai.AI;
 import com.soc.core.Constants;
 import com.soc.core.EntityFactory;
 import com.soc.core.SoC;
 import com.soc.core.Constants.Characters;
 import com.soc.game.components.Buff;
 import com.soc.game.components.Feet;
 import com.soc.game.components.Position;
 import com.soc.game.components.State;
 import com.soc.game.components.Stats;
 import com.soc.game.components.Velocity;
 import com.soc.game.states.benefits.Inmune;
 
 public class RightMonsterAI extends AI{
 	float timerCast;
 	float limitXLeft;
 	float limitXRight;
 	float limitYBottom;
 	float limitYUp;
 	boolean rightSpawned;
 	boolean playerInside;
 	float timerStomp;
 	boolean stompMode;
 	boolean inmune;
 	float timerCharge;
 	float timerQuaqueBlade;
 	boolean stomp;
 	
 	public RightMonsterAI(){
 		timerCast=0;
 		limitXLeft=63*Constants.World.TILE_SIZE;
 		limitXRight=99*Constants.World.TILE_SIZE;
 		limitYBottom=55*Constants.World.TILE_SIZE;
 		limitYUp=83*Constants.World.TILE_SIZE;
 		playerInside=false;
 		rightSpawned=false;
 		timerStomp=10f;
 		stompMode=false;
 		inmune=false;
 		timerCharge=3f;
 		timerQuaqueBlade=6f;
 		stomp=false;
 	}
 	@Override
 	public void process(Entity e) {
 		Position pos = SoC.game.positionmapper.get(e);
 		Velocity vel = SoC.game.velocitymapper.get(e);
 		Stats stats=SoC.game.statsmapper.get(e);
 		State state = SoC.game.statemapper.get(e);
 		Entity player = SoC.game.player;
 		Position playerPos = SoC.game.positionmapper.get(player);
 		if(state.state==State.FALLING) return;
 		if(!rightSpawned){
 			EntityFactory.createWall(e, 45, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 46, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 47, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 48, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 49, 54, 0).addToWorld();
 			EntityFactory.createWall(e, 50, 54, 0).addToWorld();
 			rightSpawned=true;
 		}
 		if( !playerInside && playerPos.x>limitXLeft && playerPos.x<limitXRight && playerPos.y>limitYBottom && playerPos.y<limitYUp){
 			EntityFactory.createWall(e, 77, 21, 0).addToWorld();
 			EntityFactory.createWall(e, 78, 21, 0).addToWorld();
 			EntityFactory.createWall(e, 79, 21, 0).addToWorld();
 			EntityFactory.createWall(e, 80, 21, 0).addToWorld();
 			EntityFactory.createWall(e, 81, 21, 0).addToWorld();
 			EntityFactory.createWall(e, 82, 21, 0).addToWorld();
 			EntityFactory.createWall(e, 83, 21, 0).addToWorld();
 			EntityFactory.createWall(e, 84, 21, 0).addToWorld();
 			playerInside=true;
 		}
 		
 		float dsty = playerPos.y - pos.y;
 		float dstx = playerPos.x - pos.x;
 		
 		if( stats.health<stats.maxHealth*0.20 || (SoC.game.damagemapper.has(e) && stats.health < SoC.game.damagemapper.get(e).damage-stats.armor)){
 
 			if(!inmune){
 				if(SoC.game.damagemapper.has(e)){
 					SoC.game.damagemapper.get(e).damage=0;
 					stats.health=1;
 				}
 				Buff.addbuff(e, new Inmune());
 				inmune=true;
 				EntityFactory.createWall(e, 99, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 98, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 97, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 96, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 95, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 94, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 93, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 92, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 91, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 90, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 89, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 88, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 87, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 86, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 85, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 84, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 83, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 82, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 81, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 80, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 79, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 78, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 77, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 76, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 75, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 74, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 73, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 72, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 71, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 70, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 69, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 68, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 67, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 82, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 81, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 80, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 79, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 78, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 77, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 76, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 75, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 74, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 73, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 72, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 71, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 70, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 69, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 68, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 67, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 66, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 65, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 64, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 63, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 62, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 61, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 60, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 59, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 58, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 57, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 56, 0).addToWorld();
 				EntityFactory.createWall(e, 66, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 67, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 68, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 69, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 70, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 71, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 72, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 73, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 74, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 75, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 76, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 85, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 86, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 87, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 88, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 89, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 90, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 91, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 92, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 93, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 94, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 95, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 96, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 97, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 98, 55, 0).addToWorld();
 				EntityFactory.createWall(e, 99, 55, 0).addToWorld();
 			}
 			if(state.state == State.CHARGING) return;
 			timerCharge-=SoC.game.world.delta;			
 			pos.direction.x = Math.signum(dstx);
 			pos.direction.y = Math.signum(dsty); 		
 			vel.vx = vel.speed * pos.direction.x;
 			vel.vy = vel.speed * pos.direction.y;
 			state.state = State.WALK;
 			if(Math.abs(dstx) < 32) pos.direction.x = 0;
 			else if(Math.abs(dsty) < 10) pos.direction.y = 0;
 			if(timerCharge<=0){
 				Vector2 chargeDirection=new Vector2();
 				if(Math.abs(dstx) > Math.abs(dsty)){
 					chargeDirection.x = Math.signum(dstx);
 					chargeDirection.y = Math.signum(dsty)*(Math.abs(dsty)/Math.abs(dstx));
 				} else {
 					chargeDirection.y = Math.signum(dsty);
 					chargeDirection.x = Math.signum(dstx)*(Math.abs(dstx)/Math.abs(dsty));
 				}
 				SoC.game.statemapper.get(e).state=State.CHARGING;
 				Entity charge=EntityFactory.createChargeBoss(e, Constants.Groups.ENEMY_ATTACKS, pos, stats.strength,chargeDirection);
 				SoC.game.groupmanager.add(charge, Constants.Groups.PROJECTILES);
 				SoC.game.groupmanager.add(charge, Constants.Groups.ENEMY_ATTACKS);
 				SoC.game.groupmanager.add(charge, Constants.Groups.MAP_BOUND);
 				charge.addToWorld();
 				timerCharge=3f;
 				return;
 			}
 			
 			
 		}else{
 			if((playerPos.x<limitXLeft || playerPos.x>limitXRight) || (playerPos.y<limitYBottom || playerPos.y>limitYUp)){
 				state.state=State.IDLE;
 				vel.vx=0;
 				vel.vy=0;
 				return;
 			}
 			timerStomp-=SoC.game.world.delta;
 			timerQuaqueBlade-=SoC.game.world.delta;
 			if(state.state==State.ATTACK){
 				if(stomp){
 				timerCast+=SoC.game.world.delta;
 				if(timerCast>=2f){
 					Feet feet=SoC.game.feetmapper.get(e);
 					Entity stomp=null;
 					if(pos.direction.x==0 && pos.direction.y==1)
 						stomp = EntityFactory.createStomp(pos.x+feet.width*0.5f, pos.y+feet.heigth*0.5f, pos.z,stats.strength);
 					else if(pos.direction.x==1 && pos.direction.y==0)
 						stomp = EntityFactory.createStomp(pos.x+feet.width*0.5f+Characters.WIDTH, pos.y+feet.heigth*0.5f-Characters.HEIGHT*0.5f, pos.z,stats.strength);
 					else if (pos.direction.x==-1 && pos.direction.y==0)
 						stomp = EntityFactory.createStomp(pos.x+feet.width*0.5f-Characters.WIDTH, pos.y+feet.heigth*0.5f-Characters.HEIGHT*0.5f, pos.z,stats.strength);
 					else if(pos.direction.x==0 && pos.direction.y==-1)
 						stomp = EntityFactory.createStomp(pos.x+feet.width*0.5f, pos.y+feet.heigth*0.5f-Characters.HEIGHT, pos.z,stats.strength);
 					else 
 						stomp = EntityFactory.createStomp(pos.x+feet.width*0.5f, pos.y+feet.heigth*0.5f, pos.z,stats.strength);
 					SoC.game.groupmanager.add(stomp, Constants.Groups.ENEMY_ATTACKS);
 				    SoC.game.groupmanager.add(stomp, Constants.Groups.MAP_BOUND);
 				    SoC.game.levelmanager.setLevel(stomp, Constants.Groups.LEVEL+pos.z);
 				    stomp.addToWorld();
 				    timerCast=0f;
 				    state.state=State.IDLE;
 				    SoC.game.charactermapper.get(e).renderers[State.ATTACK].time=0;
 				    
 				    return;
 				}
 				}else{
 					Feet feet=SoC.game.feetmapper.get(e);
 					Entity quakeBlade=EntityFactory.createQuake(pos, feet, stats.strength);
 					SoC.game.groupmanager.add(quakeBlade, Constants.Groups.ENEMY_ATTACKS);
 				    SoC.game.groupmanager.add(quakeBlade, Constants.Groups.MAP_BOUND);
 				    SoC.game.levelmanager.setLevel(quakeBlade, Constants.Groups.LEVEL+pos.z);
 				    quakeBlade.addToWorld();
 				    state.state=State.IDLE;
 				    SoC.game.charactermapper.get(e).renderers[State.ATTACK].time=0;
 				}
 				return;
 			}			
 			
 			pos.direction.x = Math.signum(dstx);
 			pos.direction.y = Math.signum(dsty); 		
 			vel.vx = vel.speed * pos.direction.x;
 			vel.vy = vel.speed * pos.direction.y;
 			state.state = State.WALK;
 			if(Math.abs(dstx) < 32) pos.direction.x = 0;
 			else if(Math.abs(dsty) < 10) pos.direction.y = 0;
 			
 			if(timerQuaqueBlade<=0){
 				timerQuaqueBlade=6f;
 				state.state=State.ATTACK;
 				vel.vx=0;
 				vel.vy=0;
 				stomp=false;
 			}
 			if(timerStomp<=0){
 				timerStomp=10f;
 				state.state=State.ATTACK;
 				vel.vx=0;
 				vel.vy=0;
 				stomp=true;
 				SoC.game.buffmapper.get(e).removebuff(Inmune.class, e);
 				inmune=false;
 				return;
 			}
 			
 			if(!inmune){
 				Buff.addbuff(e, new Inmune());
 				inmune=true;
 			}
 
 		}
 
 	}
 
 	@Override
 	public void death(Entity e) {
 		SoC.game.progress.rightMonsterDefetead=true;
		Position pos = SoC.game.positionmapper.get(e);
		EntityFactory.createItem(Constants.Items.MIX_ULTRAPOTION, pos.x, pos.y, pos.z);
		EntityFactory.createItem(Constants.Items.MIX_ULTRAPOTION, pos.x, pos.y, pos.z);
		EntityFactory.createItem(Constants.Items.MIX_ULTRAPOTION, pos.x, pos.y, pos.z);
 	}
 
 }

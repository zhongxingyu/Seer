 package com.first.tribes.core;
 
 import pythagoras.f.Point;
 
 import com.first.tribes.core.being.Village;
 import com.first.tribes.core.ui.toolbar.*;
 import com.first.tribes.core.util.Timer.TimerTask;
 import static playn.core.PlayN.*;
 
 
 public class EnemyGod implements TimerTask {
 	private static final int FLOOD_POSITION=5;//Flood tool Position in toolbar
 	private static final int FOOD_POSITION=3;//Food Tool Position in toolbar
 	private static final int IRRIGATION_POSITION=6;//Irrigation tool
 	private static final int PUSHPULL_POSITION=1;//push
 	private static final int SPAWN_AVATAR_POSITION=2;
 	private static final int SPAWN_POSITION=4;
 	private static final int SPAWN_MONSTER_POSITION=7;
 	
 	private static final int numberOfPowers=7;
 	private static final int REPEATAMOUNT = 4;
 	private TribesWorld world;
 	private Village enemyVillage, ownVillage;
 	
 	
 	public EnemyGod(TribesWorld world){
 		this.world=world;
 		enemyVillage=world.villages().get(0);
 		ownVillage=world.villages().get(0);
 	}
 	
 	
 	public Point safeCoordinates(){
 		Point p = new Point(enemyVillage.xPos(),enemyVillage.yPos());
 		while(ownVillage.isUnsafe(p.x, p.y)){
 			p= randomPoint();}
 		return p;
 	}
 	
 	public Point randomPoint(){
 		return new Point(random()*world.getAbsoluteSize().width,random()*world.getAbsoluteSize().height);
 	}
 	
 	@Override
 	public void run() {
		switch((int) random()*10){
 		case 0://flood
 			FloodTool flTool = (FloodTool) world.toolbar().getTools().get(FLOOD_POSITION);
 			if(ownVillage.manna()>flTool.MANNA_COST_PER_DELTA()){
 				boolean up = random()<.6f;
 				for(int i=0;i<REPEATAMOUNT;i++){
 				flTool.flood(up);
 				}
 				ownVillage.costManna(flTool.MANNA_COST_PER_DELTA());
 				flTool.release(0,0);
 			
 			world.ping(randomPoint());
 			}
 			break;
 		case 1://food
 			FoodTool fTool =(FoodTool) world.toolbar().getTools().get(FOOD_POSITION);
 			if(ownVillage.manna()>FoodTool.MANNA_COST_PER_DROP){
 				Point p = randomPoint();
 				fTool.dropFood(p.x,p.y);
 				ownVillage.costManna(FoodTool.MANNA_COST_PER_DROP);
 			world.ping(p);
 			}
 			break;
 		case 2://irrigation
 			IrrigationTool iTool = (IrrigationTool) world.toolbar().getTools().get(IRRIGATION_POSITION);
 			if(ownVillage.manna()>IrrigationTool.MANNA_COST_PER_DROP){
 				Point p = safeCoordinates();
 				iTool.dropIrrigation(p);
 				ownVillage.costManna(IrrigationTool.MANNA_COST_PER_DROP);
 				world.ping(p);
 			}
 			break;
 		case 3://push pull
 			PushPullTool pTool = (PushPullTool) world.toolbar().getTools().get(PUSHPULL_POSITION);
 			if(ownVillage.manna()>PushPullTool.MANNA_COST_PER_DELTA){
 				Point p = randomPoint();
 				for(int i=0;i<REPEATAMOUNT;i++){
 					pTool.bulldoze(p, random()<0.5f);
 					}
 				ownVillage.costManna(PushPullTool.MANNA_COST_PER_DELTA);
 				world.ping(p);
 			}
 			break;
 		case 4://spawn avatar of aggression
 			SpawnAvatarTool saTool =(SpawnAvatarTool) world.toolbar().getTools().get(SPAWN_AVATAR_POSITION);
 			if(ownVillage.manna()>SpawnAvatarTool.MANNA_COST_PER_DROP){
 				Point p = safeCoordinates();
 				saTool.spawn(p.x, p.y,1, 0);
 				ownVillage.costManna(SpawnAvatarTool.MANNA_COST_PER_DROP);
 				world.ping(p);
 			}
 			break;
 		case 5://spawn monster
 			SpawnMonsterTool smTool = (SpawnMonsterTool) world.toolbar().getTools().get(SPAWN_MONSTER_POSITION);
 			if(ownVillage.manna()>SpawnMonsterTool.MANNA_COST_PER_DROP){
 				Point p = safeCoordinates();
 				smTool.spawn(p.x, p.y);
 				ownVillage.costManna(SpawnMonsterTool.MANNA_COST_PER_DROP);
 				world.ping(p);
 			}
 			break;
 		default://spawn tool
 				SpawnTool sTool =(SpawnTool) world.toolbar().getTools().get(SPAWN_POSITION);
 				if(ownVillage.manna()>SpawnTool.MANNA_COST_PER_DROP){
 					Point p = safeCoordinates();
 					sTool.spawn(p.x, p.y,1);
 					ownVillage.costManna(SpawnTool.MANNA_COST_PER_DROP);
 					world.ping(p);
 				}
 			break;
 			
 		}
 
 	}
 
 }

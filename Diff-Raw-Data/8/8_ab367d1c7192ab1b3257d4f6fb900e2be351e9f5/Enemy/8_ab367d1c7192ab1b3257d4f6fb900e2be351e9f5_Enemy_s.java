 package object;
 
 import engine.open2d.draw.Plane;
 import engine.open2d.renderer.WorldRenderer;
 import game.GameTools;
 import game.GameTools.Gesture;
 import game.open2d.R;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 
 import android.util.Log;
 import android.view.MotionEvent;
 import object.GameObject.Direction;
 import object.GameObject.GameObjectState;
 import object.Player.PlayerState;
 
 public class Enemy extends GameObject {
 	public static enum EnemyState implements GameObjectState{
 		STAND("stand"),
 		STRIKE1("strike"),
 		STRUCK1("struck1"),
 		RUN("run"),
 		WALK("walk"),
 		JUMP_BACK("jump_back"),
 		CROSS_ROLL("cross_roll"),
 		DODGE("dodge"),
 		FREEZE("freeze"),
 		DEAD("dead");
 		
 		String name;
 		EnemyState(String n){
 			name = n;
 		}
 		
 		public String getName(){
 			return name;
 		}
 	}
 	
 	private static String OBJNAME = "enemy";
 	private static float RUN_SPEED = 0.16f;
 	private static float WALK_SPEED = 0.08f;
 	private static float JUMP_BACK_SPEED = 0.15f;
 	private static float CROSS_ROLL_SPEED = 0.17f;
 	private static float FAR_DIST_TO_PLAYER = 2.5f;
 	private static float CLOSE_DIST_TO_PLAYER = 1.5f;
 	private static float COLLISION_BUFFER = 1.0f;
 	private static float KNOCK_BACK = 0.0f;
 	
 	Player playerRef;
 	EnemyState enemyState;
 	public int struck;
 	public int unfreezeTimeCount;
 	
 	public Enemy(LinkedHashMap<String,GameObject> gameObjects, Player player, int index, float x, float y, float width, float height){
 		super(gameObjects,x,y,width,height);
 		this.name = OBJNAME+index;
 		this.z = -0.9f+index*0.01f;
 		this.playerRef = player;
 		animations = new HashMap<GameObjectState, Plane>();
 		animations.put(EnemyState.STAND, new Plane(R.drawable.enemy_stance, name+"_"+EnemyState.STAND.getName(), width, height, x, y, z, 4, 7));
 		animations.put(EnemyState.FREEZE, new Plane(R.drawable.enemy_stance, name+"_"+EnemyState.STAND.getName(), width, height, x, y, z, 4, 7));
 		animations.put(EnemyState.RUN, new Plane(R.drawable.enemy_run, name+"_"+EnemyState.RUN.getName(), width, height, x, y, z, 11, 3));
 		animations.put(EnemyState.WALK, new Plane(R.drawable.enemy_run, name+"_"+EnemyState.WALK.getName(), width, height, x, y, z, 11, 3));
 		animations.put(EnemyState.JUMP_BACK, new Plane(R.drawable.enemy_jump_back, name+"_"+EnemyState.JUMP_BACK.getName(), width, height, x, y, z, 2, 13));
 		animations.put(EnemyState.CROSS_ROLL, new Plane(R.drawable.enemy_cross_roll, name+"_"+EnemyState.CROSS_ROLL.getName(), width, height, x, y, z, 4, 5));
 		animations.put(EnemyState.DEAD, new Plane(R.drawable.enemy_stance, name+"_"+EnemyState.DEAD.getName(), width, height, x, y, z, 4, 7));
 		animations.put(EnemyState.STRIKE1, new Plane(R.drawable.enemy_strike1, name+"_"+EnemyState.STRIKE1.getName(), width, height, x, y, z, 4, 6));
 		animations.put(EnemyState.STRUCK1, new Plane(R.drawable.enemy_struck1, name+"_"+EnemyState.STRUCK1.getName(), width, height, x, y, z, 2, 7));
 		
 		struck = 3;
 		unfreezeTimeCount = 0;
 		
 		display = animations.get(EnemyState.STAND);
 		enemyState = EnemyState.STAND;
 		display.drawEnable();
 	}
 
 	@Override
 	public void updateState() {
 		float checkX = getMidX();
 		
 		if(!isEnemyStriking() && !isDodging()){
 			if(playerRef.getMidX() > checkX){
 				direction = Direction.RIGHT;
 			} else if(playerRef.getMidX() < checkX){
 				direction = Direction.LEFT;
 			}
 		}
 
 		if(enemyState == EnemyState.STAND){
 			if(Math.abs(checkX - playerRef.getMidX()) > CLOSE_DIST_TO_PLAYER){
 				enemyState = EnemyState.WALK;
 			}
 			
 			if(Math.abs(checkX - playerRef.getMidX()) > FAR_DIST_TO_PLAYER){
 				enemyState = EnemyState.RUN;
 			}
 			
 			if(GameTools.boxColDetect(this, playerRef, COLLISION_BUFFER) && Math.random() > 0.90){
 				enemyState = EnemyState.STRIKE1;
 			}
 		} else if(enemyState == EnemyState.RUN || enemyState == EnemyState.WALK){
 			executeMovement();
 		}
 		
 		otherAIMoveInteration();
 		
 		if(selected && GameTools.boxColDetect(this, playerRef, COLLISION_BUFFER) && !isDodging()){
 			if(playerRef.isFinishState()){
 				enemyState = EnemyState.DEAD;
 			}else if(playerRef.getPlayerState() == PlayerState.STRIKE1){
 				enemyState = EnemyState.STRUCK1;
 			} else if(playerRef.getPlayerState() == PlayerState.STRIKE2){
 				enemyState = EnemyState.STRUCK1;
 			} else if(playerRef.getPlayerState() == PlayerState.STRIKE3){
 				enemyState = EnemyState.STRUCK1;
 			}
 		}
 		
 		if(!selected){
 			if(playerRef.isFinishState() && enemyState != EnemyState.FREEZE){
 				enemyState = EnemyState.FREEZE;
 				unfreezeTimeCount = playerRef.getDisplay().getTotalFrame();
 			}
 		}
 	}
 
 	@Override
 	public void updateLogic() {
 		if(enemyState == EnemyState.STRUCK1){
 			if(display.getFrame() < Player.CANCEL_STRIKE_FRAMES){
 				selected = false;
 			}
 			if(direction == Direction.RIGHT)
 				x -= KNOCK_BACK;
 			else if(direction == Direction.LEFT)
 				x += KNOCK_BACK;
 		}else if(enemyState == EnemyState.RUN){
 			if(direction == Direction.RIGHT)
 				x += RUN_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= RUN_SPEED;
 		}else if(enemyState == EnemyState.WALK){
 			if(direction == Direction.RIGHT)
 				x += WALK_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= WALK_SPEED;
 		}else if(enemyState == EnemyState.JUMP_BACK){
 			if(direction == Direction.RIGHT)
 				x -= JUMP_BACK_SPEED;
 			else if(direction == Direction.LEFT)
 				x += JUMP_BACK_SPEED;
 		}else if(enemyState == EnemyState.CROSS_ROLL){
 			if(direction == Direction.RIGHT)
 				x += CROSS_ROLL_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= CROSS_ROLL_SPEED;
 		}else if(enemyState == EnemyState.FREEZE){
 			unfreezeTimeCount--;
 		}
 	}
 
 	@Override
 	public void updateDisplay() {
 		if(display != animations.get(enemyState))
 			switchAnimation(enemyState);
 		
 		if(direction==Direction.RIGHT){
 			display.flipTexture(false);
 		} else if(direction==Direction.LEFT){
 			display.flipTexture(true);
 		}
 		
 		if(enemyState == EnemyState.DEAD){
 			display.drawDisable();
 		}
 		
 		if(enemyState == EnemyState.FREEZE){
 			display.drawDisable();
 		}
 	}
 
 	@Override
 	public void updateAfterDisplay() {
 		if(!display.isPlayed())
 			return;
 		
 		if(isEnemyStriking()){
 			display.resetAnimation();
 			enemyState = EnemyState.STAND;
 		} else if(enemyState == EnemyState.JUMP_BACK){
 			display.resetAnimation();
 			enemyState = EnemyState.STAND;
 		} else if(enemyState == EnemyState.CROSS_ROLL){
 			display.resetAnimation();
 			enemyState = EnemyState.STAND;
 		} else if(enemyState == EnemyState.STRUCK1){
 			display.resetAnimation();
 			struck -= 1;
 			enemyState = EnemyState.STAND;
 		} else if(enemyState == EnemyState.FREEZE){
 			if(unfreezeTimeCount <= 0){
 				display.resetAnimation();
 				enemyState = EnemyState.STAND;
 				unfreezeTimeCount = 0;
 			}
 			
 		}
 	}
 	
 	@Override
 	public void passTouchEvent(MotionEvent e, WorldRenderer worldRenderer) {
 		Plane selectedPlane = worldRenderer.getSelectedPlane(e.getX(), e.getY());
 
 		float[] points = worldRenderer.getUnprojectedPoints(e.getX(), e.getY(), this.display);
		selected = checkEnemySelection(points[0],points[1]);
 		
 //		if(animations.containsValue(selectedPlane)){
 //			selected = true;
 //		}
 		
 		if(gesture != Gesture.NONE){
 			selected = false;
 		}
 	}
 
 	public void executeMovement(){
 		float checkX = getMidX();
 		
 		if(Math.abs(checkX - playerRef.getMidX()) > CLOSE_DIST_TO_PLAYER){
 			enemyState = EnemyState.WALK;
 		}
 		
 		if(Math.abs(checkX - playerRef.getMidX()) > FAR_DIST_TO_PLAYER){
 			enemyState = EnemyState.RUN;
 		}
 		
 		if(GameTools.boxColDetect(this, playerRef, COLLISION_BUFFER)){
 			enemyState = EnemyState.STAND;
 		}
 	}
 	
 	public void otherAIMoveInteration(){
 		if(isDodging())
 			return;
 		
 		int counter = 0;
 		for(GameObject gameObject : gameObjects.values()){
 			counter++;
 			if(gameObject instanceof Enemy){
 				Enemy enemy = (Enemy)gameObject;
 				if(enemy != this && GameTools.boxColDetect(this, enemy, COLLISION_BUFFER)){
 					if(	enemy.getEnemyState() != EnemyState.WALK &&
 						enemy.getEnemyState() != EnemyState.RUN &&
 						!enemy.isDodging()){
 						if(counter % 2 == 1){
 							enemyState = EnemyState.JUMP_BACK;
 						} else {
 							enemyState = EnemyState.CROSS_ROLL;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private boolean checkEnemySelection(float xPoint,float yPoint){
 		float top = y + height - COLLISION_BUFFER;
 		float bottom = y + COLLISION_BUFFER;
 		float left = x + COLLISION_BUFFER;
 		float right = x + width - COLLISION_BUFFER;
 
		return(xPoint > left && xPoint < right && yPoint > y && yPoint < top);
 	}
 	
 	public boolean isEnemyStriking(){
 		if(enemyState == EnemyState.STRIKE1){
 			return true;
 		} 
 		
 		return false;
 		
 	}
 	
 	public boolean isDodging(){
 		if(	enemyState == EnemyState.JUMP_BACK ||
 			enemyState == EnemyState.CROSS_ROLL
 		){
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public EnemyState getEnemyState() {
 		return enemyState;
 	}
 
 	public void setEnemyState(EnemyState enemyState) {
 		this.enemyState = enemyState;
 	}
 	
 	public int getStruck(){
 		return struck;
 	}
 }

 package com.yad.harpseal.gameobj.stage;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
 import com.yad.harpseal.constant.Direction;
 import com.yad.harpseal.constant.Layer;
 import com.yad.harpseal.constant.Screen;
 import com.yad.harpseal.constant.TileType;
 import com.yad.harpseal.gameobj.GameObject;
 import com.yad.harpseal.gameobj.character.Fish;
 import com.yad.harpseal.gameobj.character.GoalFlag;
 import com.yad.harpseal.gameobj.character.PlayerSeal;
 import com.yad.harpseal.gameobj.field.TheArcticOcean;
 import com.yad.harpseal.gameobj.tile.BrakingTile;
 import com.yad.harpseal.gameobj.tile.NormalTile;
 import com.yad.harpseal.gameobj.tile.RotatableTile;
 import com.yad.harpseal.gameobj.window.Joystick;
 import com.yad.harpseal.gameobj.window.ScoreCounter;
 import com.yad.harpseal.util.Communicable;
 import com.yad.harpseal.util.Func;
 import com.yad.harpseal.util.HarpEvent;
 import com.yad.harpseal.util.HarpLog;
 
 public class GameStage extends GameObject {
 	
 	// sample map structure
 	private final static String[] tileSample= {
 		"11252234",
 		"41346235",
 		"56664123",
 		"01423634",
 		"01561632",
 		"02346551",
 		"13236262",
 		"31216112",
 		"02314641",
 	};
 	private final static String[] charSample= {
 		"10000000",
 		"00000000",
 		"03000000",
 		"00000000",
 		"00000000",
 		"00000000",
 		"00030030",
 		"00000000",
 		"00000002"
 	};
 	
 	// map
 	private int stageGroup,stageNumber;
 	private String[] tileString;
 	private String[] charString;
 	private int mapWidth,mapHeight;
 
 	// camera
 	private float cameraX,cameraY;
 	private int cameraMode;
 	private float targetX,targetY;
 	private final static int CAM_MENUAL=1;
 	private final static int CAM_TARGET=2;
 	private final static float TARGET_MOTION_MIN=1f;
 	private final static float TARGET_MOTION_SPD=0.075f;
 
 	// tiles, characters
 	private ArrayList<GameObject> tiles;
 	private ArrayList<GameObject> characters;
 	private Queue<GameObject> removed;
 	private PlayerSeal player;	// also exists in 'characters', sometimes null.
 	
 	// field, user interfaces
 	private TheArcticOcean field;
 	private Joystick stick;
 	private ScoreCounter counter;
 	
 	// score data
 	private int stepCount;
 	private int fishCount;
 	private final static int FISH_MAX=3;
 	
 	
 	//// public method
 	
 	
 	public GameStage(Communicable con, int stageGroup, int stageNumber) {
 		super(con);
 		
 		// map vaildity check... later
 		this.stageGroup=stageGroup;
 		this.stageNumber=stageNumber;
 		tileString=tileSample;
 		charString=charSample;
 		////
 		
 		// read map
 		mapWidth=tileString[0].length();
 		mapHeight=tileString.length;
 		tiles=new ArrayList<GameObject>();
 		characters=new ArrayList<GameObject>();
 		removed=new LinkedList<GameObject>();
 		for(int y=0;y<mapHeight;y++) {
 			for(int x=0;x<mapWidth;x++) {
 				
 				// create tiles
 				switch(tileString[y].charAt(x)) {
 				case '0': break;
 				case '1': tiles.add(new NormalTile(this,x,y)); break;
 				case '2': tiles.add(new RotatableTile(this,x,y,TileType.RT_BRIDGE)); break;
 				case '3': tiles.add(new RotatableTile(this,x,y,TileType.RT_CORNER)); break;
 				case '4': tiles.add(new RotatableTile(this,x,y,TileType.RT_FORK)); break;
 				case '5': tiles.add(new RotatableTile(this,x,y,TileType.RT_INTERSECTION)); break;
 				case '6': tiles.add(new BrakingTile(this,x,y)); break;
 				default: HarpLog.danger("Invalid tile type"); break;
 				}
 				
 				// create characters
 				switch(charString[y].charAt(x)) {
 				case '0': break;
 				case '1':
 					player=new PlayerSeal(this,x,y);
 					characters.add(player);
 					break;
 					
 				case '2': characters.add(new GoalFlag(this,x,y)); break;
 				case '3': characters.add(new Fish(this,x,y)); break;
 				default: HarpLog.danger("Invalid character type"); break;
 				}
 			}
 		}
 		
 		// user interface, etc.
 		field=new TheArcticOcean(this, mapWidth, mapHeight);
 		stick=new Joystick(this);
 		counter=new ScoreCounter(this);
 		
 		// camera point init
 		cameraX=0;
 		cameraY=0;
 		cameraMode=CAM_MENUAL;
 		targetX=0;
 		targetY=0;
 		setCameraTarget(player);
 		regulateCamera();
 		
 		// score data init
 		stepCount=0;
 		fishCount=0;
 	}
 
 	@Override
 	public void playGame(int ms) {
 
 		// objects
 		for(GameObject o : tiles)
 			o.playGame(ms);
 		for(GameObject o : characters)
 			o.playGame(ms);
 		field.playGame(ms);
 		stick.playGame(ms);
 		counter.playGame(ms);
 		
 		// camera
 		regulateCamera(ms);
 		
 		// restore
 		tiles.remove(removed.peek());
 		characters.remove(removed.poll());
 	}
 
 	@Override
 	public void receiveMotion(HarpEvent ev, int layer) {
 
 		// camera
 		if(layer==Layer.LAYER_FIELD) ev.setCamera(cameraX, cameraY);
 		else if(layer==Layer.LAYER_WINDOW) ev.setCamera(0, 0);
 		
 		// objects
 		for(GameObject o : tiles) {
 			o.receiveMotion(ev, layer);
 			if(ev.isProcessed()) return;
 		}
 		for(GameObject o : characters) {
 			o.receiveMotion(ev, layer);
 			if(ev.isProcessed()) return;
 		}
 		field.receiveMotion(ev, layer);
 		if(ev.isProcessed()) return;
 		stick.receiveMotion(ev, layer);
 		if(ev.isProcessed()) return;
 		counter.receiveMotion(ev, layer);
 	}
 
 	@Override
 	public void drawScreen(Canvas c, Paint p, int layer) {
 		
 		// camera
 		if(layer==Layer.LAYER_FIELD) c.translate(-cameraX,-cameraY);
 		else if(layer==Layer.LAYER_WINDOW) c.translate(cameraX,cameraY);
 		
 		// objects
 		for(GameObject o : tiles)
 			o.drawScreen(c, p, layer);
 		for(GameObject o : characters)
 			o.drawScreen(c, p, layer);
 		field.drawScreen(c, p, layer);
 		stick.drawScreen(c, p, layer);
 		counter.drawScreen(c, p, layer);
 	}
 
 	@Override
 	public void restoreData() {
 		for(GameObject o : tiles)
 			o.restoreData();
 		for(GameObject o : characters)
 			o.restoreData();
 		field.restoreData();
 		stick.restoreData();
 		counter.restoreData();
 	}
 
 	@Override
 	public int send(String msg) {
 		String[] msgs=msg.split("/");
 
 		if(msgs[0].equals("stickAction")) {
 
 			if(player==null) return 0;
 			else if(movableCheck(player,Integer.parseInt(msgs[1]))==true) {
 				breakTileStart( (Integer)player.get("mapX"), (Integer)player.get("mapY") );
 				player.send("move/"+msgs[1]);
 				if(autoCameraRegulateCheck(player))
 					setCameraTarget(player);
 				return 1;
 			} else return 0;
 		}
 		else if(msgs[0].equals("rotatableCheck")) {
 			
 			if(rotatableCheck(Integer.parseInt(msgs[1]),Integer.parseInt(msgs[2]))==true) return 1;
 			else return 0;
 		}
 		else if(msgs[0].equals("moved")) {
 			if(msgs[1].equals("PlayerSeal")) {
 				stepCount+=1;
 				eatFish(Integer.parseInt(msgs[2]), Integer.parseInt(msgs[3]));
 				if(goalCheck(Integer.parseInt(msgs[2]), Integer.parseInt(msgs[3]))==true)
 					con.send("gameEnd");
 			}
 			return 1;
 		} else if(msgs[0].equals("broken")) {
 			breakTileEnd( Integer.parseInt(msgs[1]), Integer.parseInt(msgs[2]) );
 			return 1;
 		} else if(msgs[0].equals("scroll")) {
 			if(cameraMode==CAM_MENUAL) {
 				cameraX+=Float.parseFloat(msgs[1]);
 				cameraY+=Float.parseFloat(msgs[2]);
 				regulateCamera();
 				return 1;
 			} else return 0;
 		}
 		return con.send(msg);
 	}
 
 	@Override
 	public Object get(String name) {
 		if(name.equals("stepCount"))
 			return stepCount;
 		else if(name.equals("fishCount"))
 			return fishCount;
 		else if(name.equals("fishMax"))
 			return FISH_MAX;
 		else
 			return con.get(name);
 	}
 	
 	
 	//// private method (game play)
 	
 	private void regulateCamera() { regulateCamera(0); }
 	private void regulateCamera(int ms) {
 		
 		switch(cameraMode) {
 		
 		case CAM_MENUAL:
 			cameraX=Func.limit(cameraX, 0, mapWidth*Screen.TILE_LENGTH+Screen.FIELD_MARGIN_LEFT*2-Screen.SCREEN_X);
 			cameraY=Func.limit(cameraY, 0,  mapHeight*Screen.TILE_LENGTH+Screen.FIELD_MARGIN_TOP*2-Screen.SCREEN_Y);
 			break;
 		
 		case CAM_TARGET:
			if(targetX!=cameraX || targetY!=cameraY) {
 				float moveX=(targetX-cameraX)*TARGET_MOTION_SPD*ms/15;
 				float moveY=(targetY-cameraY)*TARGET_MOTION_SPD*ms/15;
 				float minPer=Func.distan(0, 0, moveX, moveY)/TARGET_MOTION_MIN;
 
 				if(minPer<1) {
 					moveX/=minPer;
 					moveY/=minPer;
 				}
 				
 				if(Func.distan(targetX,targetY,cameraX,cameraY)<TARGET_MOTION_MIN) {
 					cameraX=targetX;
 					cameraY=targetY;
 				}
 				else {
 					cameraX+=moveX;
 					cameraY+=moveY;
 				}
 			}
 			if(targetX==cameraX && targetY==cameraY) {
 				cameraMode=CAM_MENUAL;
 				HarpLog.info("Camera mode change : "+cameraMode);
 			}
 			break;
 		
 		}		
 	}
 	
 	private void setCameraTarget(GameObject target) {
 		
 		if(target != null) {
 			cameraMode=CAM_TARGET;
 			targetX=(Integer)target.get("mapX")*Screen.TILE_LENGTH+Screen.TILE_LENGTH/2+Screen.FIELD_MARGIN_LEFT-Screen.SCREEN_X/2;
 			targetY=(Integer)target.get("mapY")*Screen.TILE_LENGTH+Screen.TILE_LENGTH/2+Screen.FIELD_MARGIN_TOP-Screen.SCREEN_Y/2;
 			targetX=Func.limit(targetX, 0, mapWidth*Screen.TILE_LENGTH+Screen.FIELD_MARGIN_LEFT*2-Screen.SCREEN_X);
 			targetY=Func.limit(targetY, 0,  mapHeight*Screen.TILE_LENGTH+Screen.FIELD_MARGIN_TOP*2-Screen.SCREEN_Y);
 			HarpLog.info("Camera mode change : "+cameraMode);
 		}
 	}
 	
 	private boolean autoCameraRegulateCheck(GameObject target) {
 		float tx=(Integer)target.get("mapX")*Screen.TILE_LENGTH+Screen.TILE_LENGTH/2+Screen.FIELD_MARGIN_LEFT;
 		float ty=(Integer)target.get("mapY")*Screen.TILE_LENGTH+Screen.TILE_LENGTH/2+Screen.FIELD_MARGIN_TOP;
 		return (tx < cameraX+Screen.TILE_LENGTH || tx > cameraX+Screen.SCREEN_X-Screen.TILE_LENGTH ||
 				ty < cameraY+Screen.TILE_LENGTH || ty > cameraY+Screen.SCREEN_Y-Screen.TILE_LENGTH);
 			
 	}
 	
 	private boolean movableCheck(GameObject target,int direction) {
 		
 		// action check
 		if((Integer)target.get("moveDirection")!=Direction.NONE) return false;
 		
 		// start point check
 		int mapX=(Integer)target.get("mapX");
 		int mapY=(Integer)target.get("mapY");
 		for(GameObject o : tiles) {
 			if( (Integer)o.get("mapX")==mapX &&
 				(Integer)o.get("mapY")==mapY) {
 				
 				// tile type
 				if(o.getClass()==NormalTile.class)
 					continue;
 				else if(o.getClass()==RotatableTile.class) {
 					int tileType=(Integer)o.get("type");
 					int tileDirection=(Integer)o.get("direction");
 					if(direction==tileDirection) continue;
 					switch(tileType) {
 					case TileType.RT_BRIDGE: if(direction!=Direction.reverse(tileDirection)) return false; break;
 					case TileType.RT_CORNER: if(direction!=Direction.clockwise(tileDirection)) return false; break;
 					case TileType.RT_FORK: if(direction!=Direction.clockwise(tileDirection) && direction!=Direction.clockwiseR(tileDirection)) return false; break;
 					case TileType.RT_INTERSECTION: break;
 					default: HarpLog.error("Invalid Tile Type : "+tileType); return false;
 					}
 				} else if(o.getClass()==BrakingTile.class) {
 					if((Boolean)o.get("braking")==true) return false;
 					else break;
 				}
 			}
 		}
 		
 		// destination check
 		switch(direction) {
 		case Direction.UP: mapY-=1; break;
 		case Direction.LEFT: mapX-=1; break;
 		case Direction.RIGHT: mapX+=1; break;
 		case Direction.DOWN: mapY+=1; break;
 		default: HarpLog.error("Invalid Direction : "+direction); return false;
 		}
 		for(GameObject o : tiles) {
 			if( (Integer)o.get("mapX")==mapX &&
 				(Integer)o.get("mapY")==mapY) {
 
 				// tile type
 				if(o.getClass()==NormalTile.class)
 					return true;
 				else if(o.getClass()==RotatableTile.class) {
 					int tileType=(Integer)o.get("type");
 					int tileDirection=(Integer)o.get("direction");
 					if(direction==Direction.reverse(tileDirection)) return true;
 					switch(tileType) {
 					case TileType.RT_BRIDGE: return (direction==tileDirection);
 					case TileType.RT_CORNER: return (direction==Direction.clockwiseR(tileDirection));
 					case TileType.RT_FORK: return (direction==Direction.clockwiseR(tileDirection) || direction==Direction.clockwise(tileDirection));
 					case TileType.RT_INTERSECTION: return true;
 					default: HarpLog.error("Invalid Tile Type : "+tileType); return false;
 					}
 				} else if(o.getClass()==BrakingTile.class) {
 					return (Boolean)o.get("braking")==false;
 				}
 				
 			}
 		}
 		return false;
 	}
 	
 	private boolean rotatableCheck(int x,int y) {
 		
 		for(GameObject o : characters)
 			if(o.getClass()==PlayerSeal.class && (Integer)o.get("mapX")==x && (Integer)o.get("mapY")==y)
 				return false;
 		return true;
 	}
 	
 	private void eatFish(int x,int y) {
 
 		for(GameObject o : characters) {
 			if(o.getClass()==Fish.class && (Integer)o.get("mapX")==x && (Integer)o.get("mapY")==y) {
 				o.send("eaten");
 				fishCount+=1;
 				if(fishCount>FISH_MAX)
 					HarpLog.danger("FishCount value is more than FISH_MAX value!");
 				preRemoveObject(o);
 				break;
 			}
 		}
 	}
 	
 	private boolean goalCheck(int x,int y) {
 		
 		for(GameObject o : characters) {
 			if(o.getClass()==GoalFlag.class && (Integer)o.get("mapX")==x && (Integer)o.get("mapY")==y) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private void breakTileStart(int x,int y) {
 		
 		for(GameObject o : tiles) {
 			if(o.getClass()==BrakingTile.class && (Integer)o.get("mapX")==x && (Integer)o.get("mapY")==y) {
 				o.send("break");
 				break;
 			}
 		}
 		
 	}
 	
 	private void breakTileEnd(int x,int y) {
 		
 		for(GameObject o : tiles) {
 			if(o.getClass()==BrakingTile.class && (Integer)o.get("mapX")==x && (Integer)o.get("mapY")==y) {
 				preRemoveObject(o);
 				break;
 			}
 		}
 	}
 	
 	
 	//// private (data control)
 	
 	private void preRemoveObject(GameObject o) {
 		o.restoreData();
 		removed.add(o);
 	}
 
 
 
 
 
 }

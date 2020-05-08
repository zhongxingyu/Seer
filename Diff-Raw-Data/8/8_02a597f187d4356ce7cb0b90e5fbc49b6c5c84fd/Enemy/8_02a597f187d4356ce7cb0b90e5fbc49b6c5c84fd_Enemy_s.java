 package com.gamelab.mmi;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Circle;
 import com.badlogic.gdx.math.Vector2;
 import com.sun.xml.internal.bind.v2.runtime.reflect.Accessor.SetterOnlyReflection;
 
 public class Enemy {
 	
 	private static final int numberOfEnemies = 4;
 	private static final int numberOfEnemyTextures = 2*numberOfEnemies;
 	public static final int Hipster1Enemy = 0;
 	public static final int Hipster2Enemy = 1;
 	public static final int SpiesserClnEnemy = 2;
 	public static final int SpiesserFlwEnemy = 3;
 	
 	public static final int aiDefault = 0;
 	public static final int aiMove = 1;
 	
 	public static final int enemyEraseTool = 0;
 	
 	private Vector2 pos;
 	private Vector2 lockAt = new Vector2(0, 1);
 
 	private float length;
 	private int tool;
 	private float toolSize;
 	private double rotation;
 	private float speed;
 	private Tool[] tools;
 	private Vector2 origin;
 	private Circle hitbox;
 	private PlayerTexture[] playerTextures;
 	private int currentPlayerTexture;
 	private Map map;
 	private int aiPhase;
 	private Player player;
 	
 	public void move(Vector2 wc) {
 		this.lockAt = wc.cpy().sub(pos);
 		this.length = this.lockAt.len();
 		this.lockAt = this.lockAt.nor();
 		this.rotation = lockAt.angle();
 	}
 	
 	public void update(float delta) {
 		ai();
 		
 		Vector2 oldPos = pos;	
 		
 		if (delta * speed < length) {
 			pos.add(this.lockAt.cpy().mul(delta * speed));
 			this.length = this.length - delta * speed;
 			
 			Vector2 headPos = new Vector2(28, 32);
 			
 			Vector2 deltaBrush = new Vector2(44, 32).sub(headPos);
 			
 			deltaBrush.rotate((float) this.rotation + 180);
 					
 			tools[tool].draw(pos.cpy().add(deltaBrush), oldPos, toolSize, delta * speed);
 		} else {
 			pos.add(this.lockAt.cpy().mul(this.length));
 			this.length = 0;
 		}
 		
 		if (this.length > 0) {
 			currentPlayerTexture = 2 * tool;			
 		} else {
 			currentPlayerTexture = 2 * tool + 1;
 			playerTextures[currentPlayerTexture].resetAnimationTime();
 		}
 		
 		playerTextures[currentPlayerTexture].update(delta);
 		this.hitbox.set(origin.x, origin.y,
 				this.playerTextures[currentPlayerTexture].getFrameHeight());		
 	}
 
 	public Circle getHitbox() {
 		return hitbox;
 	}
 
 	public void render() {		
 		playerTextures[currentPlayerTexture].render((float) rotation, pos.x,
 				pos.y, 1.0f, 28, 32);
 		
 	}
 
 	public Enemy(Vector2 pos, int tool, Map map, Player player) {
 		this.map = map;
 		this.player = player;
 		this.pos = pos;
 		this.origin = pos;
 		this.tool = tool;
 		currentPlayerTexture = 2 * tool + 1;
 		aiPhase = aiDefault;
 
 		playerTextures = new PlayerTexture[numberOfEnemyTextures];
 
		createTextureForTool(Hipster1Enemy, "data/Hipster-gro-w.png");
		createTextureForTool(Hipster2Enemy, "data/Kunststudentin-gro-w.png");
		createTextureForTool(SpiesserFlwEnemy, "data/Spiesser-gro-w.png");
		createTextureForTool(SpiesserClnEnemy, "data/Spiesser-gro-w.png");
 	
 		this.hitbox = new Circle(origin,
 				this.playerTextures[currentPlayerTexture].getFrameHeight()/2);
 		
 		tools = new Tool[numberOfEnemies];
 		tools[Hipster1Enemy] = new EnemyEraseTool(map);
 		tools[Hipster2Enemy] = new WalkTool(map);
 		tools[SpiesserFlwEnemy] = new WalkTool(map);
 		tools[SpiesserClnEnemy] = new EnemyEraseTool(map);
 	
 		speed = 70.0f;
 		toolSize = 40.0f;
 		length = 0.0f;
 		rotation = 0.0f;
 	}
 	
 	private void ai() {
 		switch (tool) {
 		case Hipster1Enemy:
 			hipster1Ai();
 			break;
 
 		case Hipster2Enemy:
 			hipster2Ai();
 			break;
 
 		case SpiesserFlwEnemy:
 			spiesserFlwAi();
 			break;
 
 		case SpiesserClnEnemy:
 			spiesserClnAi();
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 	private Vector2 searchBlock(int radius) {
 		Vector2 out = new Vector2();
 		int w = Gdx.graphics.getWidth();
 		int h = Gdx.graphics.getHeight();
 		int t = 0; 
 		int sqDist = w*w + h*h;
 		int minY = 0;
 		int maxY = h-1;
 		int minX = 0;
 		int maxX = w-1;
 		boolean found = false;
 
 		if(radius>0) {
 			t = (int)pos.y-radius;
 			if(t>0) {
 				minY = t;
 			}
 			t = (int)pos.y+radius;
 			if(t<h-1) {
 				maxY = t;
 			}
 			t = (int)pos.x-radius;
 			if(t>0) {
 				minX = t;
 			}
 			t = (int)pos.x+radius;
 			if(t<w-1) {
 				maxX = t;
 			}
 		}
 		
 		for(int i=minY; i<maxY; i++) {
 			for(int j=minX; j<maxX; j++) {
 				t = i*i+j*j; 
 				if((t<sqDist)&&map.getEverTouched(j, i)) {
 					out.x = j;
 					out.y = i;
 					sqDist = t;
 					found = true;
 				}
 			}
 		}
 		return found?out:null;
 	}
 	
 	private Vector2 searchTouched() {
 		Vector2 out = searchBlock(80);
 		if(out!=null) {
 			return out;
 		}
 		out = searchBlock(200);
 		if(out!=null) {
 			return out;
 		}
 		return searchBlock(0);
 	}
 	
 	private void hipster1Ai() {
 		switch (aiPhase) {
 		case aiDefault:
 			Vector2 found = searchTouched();
 			if(found!=null) {
 				move(found);
 				aiPhase = aiMove;
 			}
 			break;
 		case aiMove:
 			if(length==0.0f) {
 				aiPhase = aiDefault;
 			}
 			break;
 		default:
 			aiPhase = aiDefault;
 			break;
 		}
 	}
 	
 	private void hipster2Ai() {
 		switch (aiPhase) {
 		case aiDefault:
 			move(player.getPos());
 			aiPhase = aiMove;
 			break;
 		case aiMove:
 			if(length>200.0f||length==0.0f) {
 				aiPhase = aiDefault;
 			}
 			break;
 		default:
 			aiPhase = aiDefault;
 			break;
 		}
 	}
 	
 	private void spiesserFlwAi() {
 		Vector2 diff = player.getPos().cpy().sub(pos);
 		if(diff.x*diff.x+diff.y*diff.y>300.0f*300.0f) {
 			setTool(SpiesserClnEnemy);
 			aiPhase = aiDefault;
 			hipster1Ai();
 			return;
 		}
 		hipster2Ai();
 	}
 	
 	private void spiesserClnAi() {
 		Vector2 diff = player.getPos().cpy().sub(pos);
 		if(diff.x*diff.x+diff.y*diff.y<300.0f*300.0f) {
 			setTool(SpiesserFlwEnemy);
 			aiPhase = aiDefault;
 			hipster2Ai();
 			return;
 		}
 		hipster1Ai();
 	}
 
 	public void setTool(int tool) {
 		this.tool = tool;
 		currentPlayerTexture = 2 * tool + 1;
 	}
 
 	private void createTextureForTool(int _tool, String texture) {
 		playerTextures[2 * _tool] = new PlayerTexture(
 				texture, 1, 2, 0.2f);
 		playerTextures[2 * _tool + 1] = new PlayerTexture(
 				texture, 1, 2, 10.0f);		
 	}
 	
 	public void dispose() {
 		for (int i = 0; i < playerTextures.length; i++) {
 			playerTextures[i].dispose();
 		}
 	}
 
 
 }

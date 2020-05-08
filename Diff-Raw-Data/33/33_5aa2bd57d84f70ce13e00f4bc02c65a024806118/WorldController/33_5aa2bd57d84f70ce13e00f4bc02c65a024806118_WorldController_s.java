 package com.dat255_group3.controller;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.dat255_group3.model.Character;
 import com.dat255_group3.model.Cookie;
 import com.dat255_group3.model.World;
 import com.dat255_group3.utils.CoordinateConverter;
 import com.dat255_group3.utils.PhysBodyFactory;
 import com.dat255_group3.utils.WorldUtil;
 import com.dat255_group3.view.WorldView;
 
 public class WorldController {
 
 	private World world;
 	private WorldView worldView;
 	private CharacterController characterController;
 	private Vector2 gravity;
 	final boolean doSleep;
 	private ArrayList<Body> solidBodyList;
 	private ArrayList<Body> obstacleBodyList;
 	private ArrayList<Cookie> cookieList;
 	private float finishLineX;
 	private Body charBody;
 	private com.badlogic.gdx.physics.box2d.World physicsWorld;
 	private WorldUtil worldUtil;
 	private CookieController cookieController;
 	private int cookieIndex;
 	private int cookieCounter;
 
 	public WorldController(InGameController inGameController, float speedM){
 		this.world = new World();
 		this.worldView = new WorldView();
 		this.worldUtil = new WorldUtil(inGameController.getMap());
 		finishLineX = worldUtil.finishLineX();
 
 		// create the physics world
 		this.setGravity(new Vector2(0.0f, -10f));
 		this.doSleep = true;
 		this.physicsWorld = new com.badlogic.gdx.physics.box2d.World(gravity, doSleep);
 
 
 		this.worldView = new WorldView();
 		this.characterController = new CharacterController(this, inGameController.getCamera());
 
 		// create character body
 		this.charBody = PhysBodyFactory.createRoundCharacter(this.characterController.getCharacter(), this.physicsWorld);
		
 
 		// create the ground
 		solidBodyList = new ArrayList<Body>();
 		ArrayList<Vector2> solidList = WorldUtil.getGroundList().getMapList();
 		for(int i=0; i<solidList.size(); i++) {
 			solidBodyList.add(PhysBodyFactory.addSolidGround(new Vector2(solidList.get(i).x, solidList.get(i).y),
 					worldUtil.getTileSize(), 0.8f, 0f, this.physicsWorld));
 		}
 
 				//create the obstacles
 				obstacleBodyList = new ArrayList<Body>();
 				ArrayList<Vector2> obstacleList = worldUtil.getObstacleList().getMapList();
 				for(int i=0; i<obstacleList.size(); i++) {
 					obstacleBodyList.add(PhysBodyFactory.addObstacle(new Vector2(obstacleList.get(i).x, obstacleList.get(i).y),
 							worldUtil.getTileSize(), 0.8f, 0f, this.physicsWorld));
 				}
 				
 
 
 		// create cookies
 		cookieList = new ArrayList<Cookie>();
 		ArrayList<Vector2> cookiePosList = worldUtil.getCookieList().getMapList();
 		for(int i=0; i<cookiePosList.size(); i++) {
 			cookieList.add(new Cookie(new Vector2(cookiePosList.get(i).x, cookiePosList.get(i).y)));
 		}
 		cookieIndex = 0;
 		cookieCounter = 0;
 
 		// create cookieController
 		cookieController = new CookieController(cookieList, inGameController.getCamera());
 
 		this.worldView = new WorldView();
 
 	}
 	public void uppdatePositions(Body body, Character character){
 		Vector2 posInPixels = CoordinateConverter.meterToPixel(body.getPosition());
 		character.setPosition(new Vector2 (posInPixels.x, posInPixels.y) );
 	}
 
 	public World getWorld() {
 		return world;
 	}
 
 	public WorldView getWorldView() {
 		return worldView;
 	}
 
 	public CharacterController getCharacterController(){
 		return characterController;
 	}
 
 	public CookieController getCookieController() {
 		return cookieController;
 	}
 
 	public com.badlogic.gdx.physics.box2d.World getPhysicsWorld() {
 		return physicsWorld;
 	}
 
 	public Body getCharBody() {
 		return charBody;
 	}
 
 	public ArrayList<Body> getSolidBodyList() {
 		return solidBodyList;
 	}
 
 	public ArrayList<Body> getObstacleBodyList() {
 		return obstacleBodyList;
 	}
 
 	public ArrayList<Cookie> getCookieList() {
 		return cookieList;
 	}
 
 	public float getFinishLineX() {
 		return finishLineX;
 	}
 
 	public Vector2 getGravity() {
 		return gravity;
 	}
 
 	public void setGravity(Vector2 gravity) {
 		this.gravity = gravity;
 	}
 
 
 	public void moveFinishLine(float speedP) {
 		finishLineX = finishLineX - speedP/10;
 	}
 
 	public Vector2 getStartPos() {
 		return WorldUtil.getStartPos();
 	}
 
 	public void checkNextCookie() {
		if(cookieList.get(cookieIndex).getPosition().x + 32 > characterController.getCharacter().getPosition().x) {
			checkCookieCollision();
		}else if(cookieIndex < cookieList.size()){
			cookieIndex++;
 		}
 	}
 
 	public void checkCookieCollision() {
		characterController.getCharacter();
 		if(cookieList.get(cookieIndex).getPosition().x - characterController.getCharacter().getPosition().x 
 				< Character.getRadius()) {
			characterController.getCharacter();
			if(cookieList.get(cookieIndex).getPosition().y + 32 - characterController.getCharacter().getPosition().y
					< Character.getRadius()) {
 				collision();
 			}
 		}
 	}
 
 	public void collision() {
 		cookieList.remove(cookieIndex);
 		cookieCounter++;
 	}
 	public int getCookieCounter() {
 		return cookieCounter;
 	}
 
 }

 package cargame.elements;
 
 import cargame.core.MovingPosition;
 import cargame.core.Player;
 import cargame.screens.GameScreen;
 import cargame.utils.Box2DUtils;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 
 public class Car implements Element,Comparable<Car>{
 	
 	public static final double MAX_STEER_ANGLE = Math.PI*2.5;
 	public static final float HORSEPOWERS = 1800;
 	private static final Vector2 CAR_STARTING_POS = new Vector2(115,115);
 	 
 	// Sprites
	public static final String SPRITE_1 = "img\\car2.png";
 	public static final String SPRITE_2 = "img\\car3.png";
 	public static final String SPRITE_3 = "img\\car4.png";
	public static final String SPRITE_4 = "img\\coche.png";
 	
 	public static final int TYPE_1 = 0;
 	public static final int TYPE_2 = 1;
 	public static final int TYPE_3 = 2;
 	public static final int TYPE_4 = 3;
 	
 	
 	private GameScreen game;
 	
 	private Body body;
 	
 	private String spritePath; 
 	
 	private Player player;
 	
 	private boolean wrongDirection;
 	
 	public Car(Player player,GameScreen game,int carType) {
 		this.game = game;
 		player.trackTime = 0;
 		this.player =player;
 		this.wrongDirection = false;
 		
 		switch(carType){
 			case TYPE_1:
 				this.spritePath = SPRITE_1;
 			break;
 			case TYPE_2:
 				this.spritePath = SPRITE_2;
 			break;
 			case TYPE_3:
 				this.spritePath = SPRITE_3;
 			break;
 			case TYPE_4:
 				this.spritePath = SPRITE_4;
 			break;
 		}
 	}
 	
 	public void setEngineSpeed(float engineSpeed){
 		Vector2 vector = (new Vector2(engineSpeed,engineSpeed)); 
 		body.applyForce(Box2DUtils.rotateVector(vector,body.getAngle()), body.getPosition(),true);
 	}
 	
 	public void setSteeringAngle(float steeringAngle){
 		if(body.getLinearVelocity().len() < 20) return;
 		body.setAngularVelocity(steeringAngle);
 	}
 	
 	public void createCarObject(){
 		body = Box2DUtils.createPolygonBody(game.getWorld(), Car.CAR_STARTING_POS, 2f, 7f, 0.1f, 10f, 0.1f,true,false);
 		
 		// Car image
 		Sprite sprite = new Sprite(new Texture(this.spritePath));
 		sprite.setSize(15f, 15f);
 		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
 		body.setUserData(sprite);
 	}
 
 	public Body getBody(){
 		return body;
 	}
 	
 	public void addLap(long time){
 		this.player.addLap(time);
 	}
 	
 	public float getSpeed(){
 		return getBody().getLinearVelocity().len()*1.5f;
 	}
 	
 	public void setPosition(MovingPosition movingPosition){
 		if(movingPosition == null) return;
 		this.getBody().setTransform(movingPosition.xPos, movingPosition.yPos, movingPosition.angle);
 		this.getBody().setLinearVelocity(new Vector2(movingPosition.linearSpeedX, movingPosition.linearSpeedY));
 		this.getBody().setAngularVelocity(movingPosition.angularSpeed);
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(Player player) {
 //		if(player.time <= this.player.time) return;
 		this.player = player;
 		this.setPosition(player.movingPosition);
 	}
 	
 	public void updatePosition(){
 		if(player.movingPosition == null){
 			player.movingPosition = new MovingPosition();
 		}
 		
 		player.movingPosition.xPos = getBody().getPosition().x;
 		player.movingPosition.yPos = getBody().getPosition().y;
 		player.movingPosition.angle = getBody().getAngle();
 		player.movingPosition.linearSpeedX = getBody().getLinearVelocity().x;
 		player.movingPosition.linearSpeedY = getBody().getLinearVelocity().y;
 		player.movingPosition.angularSpeed = getBody().getAngularVelocity();
 	}
 
 	public void setWrongDirection(boolean wrongDirection) {
 		this.wrongDirection = wrongDirection;
 	}
 
 	public boolean isWrongDirection() {
 		return wrongDirection;
 	}
 
 	@Override
 	public int compareTo(Car o) {
 		if(o.getPlayer().getLaps() == this.getPlayer().getLaps() && o.getPlayer().getBestLapTime() == this.getPlayer().getBestLapTime()){
 			return 0;
 		}
 		if(o.getPlayer().getLaps() > this.getPlayer().getLaps()){
 			return 1;
 		}else if(o.getPlayer().getBestLapTime() < this.getPlayer().getBestLapTime()){
 			return 1;
 		}else{
 			return -1;
 		}
 	}
 	
 }

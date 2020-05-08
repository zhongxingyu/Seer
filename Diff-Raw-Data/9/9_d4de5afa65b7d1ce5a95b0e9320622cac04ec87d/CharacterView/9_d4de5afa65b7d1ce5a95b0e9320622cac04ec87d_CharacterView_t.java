 package view;
 
 import org.jbox2d.dynamics.Body;
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Circle;
 import org.newdawn.slick.geom.Shape;
 
 import utils.WorldBodyFactory;
 import utils.WorldObjects;
 import model.Character;
 
 public class CharacterView {
 	private Character character;
 	private Shape slickShape;
 	private Body characterBody;
 	private Animation nelson, walkRight, walkLeft, blinkLeft, blinkRight, standRight, standLeft;
 	
 	public CharacterView(Character character) throws SlickException {
 		this.character = character;
 		this.slickShape = new Circle(this.character.getX()-(Character.RADIUS/2f), 
 		this.character.getY()-(Character.RADIUS/2f), Character.RADIUS);
 		
 		Image imageRight1 = new Image("pics/nelson1.png");
 		Image imageRight2 = new Image("pics/nelson2.png");
 		Image invisibleNelson = new Image("pics/invisibleNelson.png");
 		Image imageLeft1 = imageRight1.getFlippedCopy(true, false);
 		Image imageLeft2 = imageRight2.getFlippedCopy(true, false);
 		Image[] walkingRight = {imageRight1, imageRight2};
 		Image[] walkingLeft = {imageLeft1, imageLeft2};
 		Image[] blinkingRight = {invisibleNelson, imageRight1, imageRight2};
 		Image[] blinkingLeft = {invisibleNelson, imageLeft1, imageLeft2};
 		Image[] standingRight = {imageRight1, imageRight1};
 		Image[] standingLeft = {imageLeft1, imageLeft1};
 		
 		int duration = 300;		
 		walkRight = new Animation(walkingRight, duration);
 		walkLeft = new Animation(walkingLeft, duration);
 		blinkLeft = new Animation(blinkingLeft, duration);
 		blinkRight = new Animation(blinkingRight, duration);
 		standRight = new Animation(standingRight, duration);
 		standLeft = new Animation(standingLeft, duration);
 		nelson = walkLeft;
 	}
 
 	public Character getCharacter() {
 		return this.character;
 	}
 	
 	public Shape getSlickShape() {
 		return this.slickShape;
 	}
 	
 	public Body getCharacterBody() {
 		return characterBody;
 	}
 
 	public void setCharacterBody(Body characterBody) {
 		this.characterBody = characterBody;
 	}
 
 	public Animation getAnimation() {
 		return this.nelson;
 	}
 	
 	public void setAnimation(Animation animation) {
 		this.nelson = animation;
 	}
 
 	public boolean isBlinking() {
 		return this.nelson == blinkLeft || nelson == blinkRight;
 	}
 
 	public void animateBlinkingLeft() {
 		this.nelson = blinkLeft;
 	}
 	
 	public void animateBlinkingRight() {
 		this.nelson = blinkRight;
 	}
 	
 	public void animateWalkingRight() {
 		this.nelson = walkRight;
 		walkRight.start();
 	}
 	
 	public void animateWalkingLeft() {
 		this.nelson = walkLeft;
 	}
 
 	public boolean isWalkingLeft() {
 		return this.nelson == walkLeft;
 	}
	
 	public boolean isBlinkingLeft() {
 		return this.nelson == blinkLeft;
 	}
 
 	public void animateStandingRight() {
 		this.nelson = standRight;
 	}
 	
 	public void animateStandingLeft() {
 		this.nelson = standLeft;
 	}
 
 	public boolean isStandingLeft() {
 		return this.nelson == standLeft;
 	}
 
 	
 }

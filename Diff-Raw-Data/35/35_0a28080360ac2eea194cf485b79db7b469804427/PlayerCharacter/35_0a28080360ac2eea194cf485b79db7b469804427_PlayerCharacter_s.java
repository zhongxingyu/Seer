 package edu.chl.codenameg.model.entity;
 
 import java.awt.Point;
 
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Vector2D;
 
 public class PlayerCharacter implements Entity{
 	private final Hitbox hitbox = new Hitbox(5,10);
 	private boolean gameWon;
 	private Point pt;
 	
 	public PlayerCharacter() {
 		gameWon = false;
 	}
 	
 	public PlayerCharacter(Point position) {
 		this();
 		this.setPosition(position);
 	}
 	
 	public void jump() {
 		//TODO Complete this method
 	}
 	
 	public void move() {
 		//TODO Complete this method
 	}
 
 	@Override
 	public Hitbox getHitbox() {
 		return new Hitbox(hitbox);
 	}
 
 	@Override
 	public Vector2D getVector2D() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isColliding() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void collide(Entity e) {
		// TODO Auto-generated method stub
 		System.out.println("PlayerCharacter collided with a "+e.getClass().getCanonicalName());
 	}
 	
 	@Override
 	public void update(int elapsedTime) {
 		this.setPosition(new Point(this.getPosition().x,this.getPosition().y+1));
 	}
 	public boolean winGame(){
 		this.gameWon=true;
 		return this.gameWon;
 	}
 
 	@Override
 	public void setPosition(Point p) {
 		this.pt=p;
 		
 	}
 
 	@Override
 	public Point getPosition() {
 		return new Point(this.pt);
 	}
 }

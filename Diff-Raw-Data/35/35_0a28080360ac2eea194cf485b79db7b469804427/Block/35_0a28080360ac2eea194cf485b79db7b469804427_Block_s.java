 package edu.chl.codenameg.model.entity;
 
 import java.awt.Point;
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Vector2D;
 
 
 public class Block implements Entity{
 	private Hitbox hb;
 	private final Vector2D v = new Vector2D(0,0);
 	private boolean colliding;
 	
 	public void setHitbox(Hitbox hb){
 		this.hb = hb;
 	}
 
 	@Override
 	public Hitbox getHitbox() {
 		if(hb == null)
 			return new Hitbox(0,0);
 		else {
 			return new Hitbox(this.hb);
 		}
 	}
 
 	@Override
 	public Vector2D getVector2D() {
 		return v;
 	}
 
 	@Override
 	public boolean isColliding() {
 		return colliding;
 	}
 
 	@Override
 	public void collide(Entity e) {
 		this.colliding = true;
 		
 	}
 	public Block(/*Graphic g,*/ Hitbox hb,Point position){
 		this.setPosition(position);
 		this.hb = hb;
 		this.colliding = false;
 		
 	}
 
 	@Override
 	public void setPosition(Point p) {
		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Point getPosition() {
		// TODO Auto-generated method stub
		return null;
 	}
 
 	@Override
 	public void update(int elapsedTime) {
 		// TODO Auto-generated method stub
 		
 	}
 }

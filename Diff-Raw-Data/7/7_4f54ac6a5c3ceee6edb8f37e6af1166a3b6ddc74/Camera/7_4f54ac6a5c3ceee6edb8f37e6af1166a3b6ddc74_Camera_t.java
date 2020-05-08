 package edu.chl.codenameg.model;
 
 import java.awt.Dimension;
 
 public class Camera {
 	
 	private Position position;
 	private Dimension size;
 	
 	public Camera() {
 		this.position = new Position(0,0);
 		this.size = new Dimension(1024, 768);
 	}
 	
 	public Camera(Dimension size) {
 		this();
 		this.size = size; 
 	}
 	
 	public Position getPosition() {
 		Position p = new Position(this.position);
 		return p;
 	}
 	
 	public Dimension getSize() {
 		return size;
 	}
 	
 	public void update() {
 		this.update(10);
 	}
 	
 	public void update(int elapsedTime) {
 		float x = this.position.getX();
 		float y = this.position.getY();
 		float newX = (float)(x+1)/*(x + (10 / elapsedTime))*/;
 		this.position.setX(newX);
 		this.position.setY(y);
 	}
 	
 }

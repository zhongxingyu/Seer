 package com.androidmontreal.rhok.pieces;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 /**
 * This piece represents a pipe between two other pieces (pipes, water source,
 * pump, no piece)
  */
 
 public class Pipe implements Piece {
 
 	List<Gate> gates;
 	Point position;
	
 
 	public Pipe() {
 	}
 
 	public Pipe(int x, int y) {
 		position.x = x;
 		position.y = y;
 	}
 
 	@Override
 	public List<Gate> getGates() {
 		if (gates == null) {
 			return new ArrayList<Gate>();
 		}
 		return gates;
 	}
 
 	@Override
 	public Point getPosition() {
 		return null;
 	}
 
 	@Override
 	public void tick(long timedelta) {
 
 	}
 
 	@Override
 	public boolean isTicked() {
 		return false;
 	}
 
 	@Override
 	public void resetTick() {
 
 	}
 
 	@Override
 	public Sprite getCurrentSprite() {
 		return null;
 	}
 
 }

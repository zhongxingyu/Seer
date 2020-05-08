 package com.gradiuss.game.models;
 
 import android.graphics.Bitmap;
 import android.graphics.Rect;
 import android.util.Log;
 
 public class Asteroid extends Enemy {
 	
 	private int previousLife;
 	
 	public Asteroid(Bitmap bitmap, int x, int y) {
 		super(bitmap, x, y);
 		super.setVisible(true);
 	}
 	
 	public Asteroid(Bitmap bitmap, int x, int y, Rect rectangle) {
 		super(bitmap, x, y, rectangle);
 	}
 
 	@Override
 	public void updateState() {
 		
 		// Movement
 		if (moveLeft) {	
 			setX((int) (getX() - (getVx() * getDirectionX()) ));
 		}
 		if (moveRight) {	
 			setX((int) (getX() + (getVx() * getDirectionX()) ));
 		}
 		if (moveUp) {	
 			setY((int) (getY() - (getVy() * getDirectionY()) ));
 		}
 		if (moveDown) {	
 			setY((int) (getY() + (getVy() * getDirectionY()) ));
 		}
 		
 		// Size of the asteroid when hit
 		if (isHit()) {
 			float damage = previousLife - getLife();
 			float shrinkPercentage = (100/damage-1)/(100/damage);
 			Log.d("ASTEROID TEST: shrinkPercentage = ", shrinkPercentage + "");
			//setBitmap(0, Bitmap.createBitmap(getBitmap(), 0, 0, Math.round(shrinkPercentage*getBitmap().getWidth()), 
				//	Math.round(shrinkPercentage*getBitmap().getHeight())));
			setBitmap(0, Bitmap.createScaledBitmap(getBitmap(), Math.round(shrinkPercentage*(getRect().width())), 
					Math.round(shrinkPercentage*(getRect().height())), true));

 			setHit(false);
 		}
 		
 		// Calls the superclass method that updates the rectangle automatically.
 		super.updateState(); 
 	}
 	
 	@Override
 	public void setLife(int life) {
 		previousLife = getLife();
 		super.setLife(life);
 	}
 	
 	@Override
 	public void setAlive(boolean isAlive) {
 		super.setAlive(isAlive);
 	}
 
 	
 	@Override
 	public boolean isAlive() {
 		return super.isAlive();
 	}
 
 	
 	@Override
 	public int getLife() {
 		return super.getLife();
 	}
 
 }

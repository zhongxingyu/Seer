 package org.MAG;
 
 import android.graphics.Bitmap;
 
 /**
  * Contains everything for drawing sprites to a MySurfaceView
  * @author undergear
  *
  */
 public class Sprite {
 
 	//Alignment Constants
 	public static final int ALIGNMENT_CENTER = 0;
 	public static final int ALIGNMENT_TOP = 1;
 	public static final int ALIGNMENT_BOTTOM = 2;
 	public static final int ALIGNMENT_LEFT = 3;
 	public static final int ALIGNMENT_RIGHT = 4;
 	public static final int ALIGNMENT_TOPLEFT = 5;
 	public static final int ALIGNMENT_TOPRIGHT = 6;
 	public static final int ALIGNMENT_BOTTOMLEFT = 7;
 	public static final int ALIGNMENT_BOTTOMRIGHT = 8;
 	
 	private Bitmap bmp; //the actual image!
 	private float x, y; //these are relative coordinates, not absolute. 0.0f-1.0f
 	private float rotation; //the rotation of the sprite in degrees
 	private float alignX, alignY; //x- and y-alignments of the sprite. These will be calculated by the constructor based on int alignment.
 	
 	/**
 	 * Constructor. x and y should be values 0.0f-1.0f, representing a relative point on the screen. alignX, alignY should be set with alignment as Sprite.ALIGNMENT_*
 	 * 
 	 * @param bmp image of the sprite
 	 * @param x relative coordinate
 	 * @param y relative coordinate
 	 * @param rotation of the sprite
 	 * @param alignment where the sprite will be pinned
 	 */
 	public Sprite(Bitmap bmp, float x, float y, float rotation, int alignment) {
 		this.bmp = bmp;
 		this.x = x;
 		this.y = y;
 		this.rotation = rotation;
 		switch (alignment) {
 		
 		case ALIGNMENT_CENTER:
 			alignX = alignY = 0.5f;
 			break;
 		case ALIGNMENT_TOP:
 			alignX = 0.5f;
 			alignY = 0.0f;
 			break;
 		case ALIGNMENT_BOTTOM:
 			alignX = 0.5f;
 			alignY = 1.0f;
 			break;
 		case ALIGNMENT_LEFT:
 			alignX = 0.0f;
 			alignY = 0.5f;
 			break;
 		case ALIGNMENT_RIGHT:	
 			alignX = 1.0f;
 			alignY = 0.5f;
 			break;
 		case ALIGNMENT_TOPLEFT:
 			alignX = alignY = 0.0f;
 			break;
 		case ALIGNMENT_TOPRIGHT:
 			alignX = 1.0f;
 			alignY = 0.0f;
 			break;
 		case ALIGNMENT_BOTTOMLEFT:
 			alignX = 0.0f;
 			alignY = 1.0f;
 			break;
 		case ALIGNMENT_BOTTOMRIGHT:
 			alignX = alignY = 1.0f;
 			alignY = 0.0f;
 			break;
 		default:
 			alignX = alignY = 0.0f; //default topleft
 			break;
 		}
 	}
 	
 	public float getX() {
 		return x;
 	}
 	
 	public float getY() {
 		return y;
 	}
 	
 	public Bitmap getImage() {
 		return bmp;
 	}
 	
 	public float getRotation() {
 		return rotation;
 	}
 	
 	public void setRotation(float r) {
 		rotation = r;
 	}
 	
 	public void setImage(Bitmap bmp) {
 		this.bmp = bmp;
 	}
 	
 	public float getAlignY() {
 		return alignY;
 	}
 	
 	public float getAlignX() {
 		return alignX;
 	}
 }

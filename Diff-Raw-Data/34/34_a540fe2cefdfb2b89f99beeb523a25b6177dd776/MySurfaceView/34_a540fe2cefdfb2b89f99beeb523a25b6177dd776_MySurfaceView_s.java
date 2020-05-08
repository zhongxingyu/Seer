 package org.MAG;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.util.AttributeSet;
 import android.view.SurfaceView;
 
 /**
  * MySurfaceView is used to draw sprites on top of a background. Attach this to a root node with whatever background.
  * Be sure to set this one's z order on top and set its holder pixel format to TRANSPARENT.
  * @author undergear
  *
  */
 public class MySurfaceView extends SurfaceView {
 	
 	private ArrayList<Sprite> sprites; //list of the sprites to be drawn on this surface
 	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); //this paint will be used to draw each sprite
 	
 	private Matrix matrix;
 	
 	/**
 	 * Constructors.
 	 * @param context
 	 */
 	public MySurfaceView(Context context) {
 		super(context);
 		sprites = new ArrayList<Sprite>();
 		matrix = new Matrix();
 	}
 	
 	public MySurfaceView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		sprites = new ArrayList<Sprite>();
 		matrix = new Matrix();
 	}
 	
 	public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		sprites = new ArrayList<Sprite>();
 		matrix = new Matrix();
 	}
 	
 	/**
 	 * Overridden for drawing sprites onto the SurfaceView
 	 * 
 	 * @param canvas to be drawn to
 	 */
 	@Override
 	public void draw(Canvas canvas) {
 		
 		if (getHolder().getSurface().isValid()) { //Make sure that the surface is actually valid before trying to draw on it!
 			
 			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //Clear out what was previously there with alpha.
 			
 			for (Sprite sprite : sprites) { //Iterate through our sprites list.
 				
 				float x, y; //Absolute coordinates at which to draw the sprite. Remember, this is the top left of the image.
 				x = this.getWidth() * sprite.getX() - sprite.getImage().getWidth() * sprite.getAlignX();
 				y = this.getHeight() * sprite.getY() - sprite.getImage().getHeight() * sprite.getAlignY();
 				if (sprite.getRotation() == 0.) { //If the sprite is not rotated
 					
 					//Calculate absolute coordinates to draw each sprite. Based on relative coordinates, its height/width, and its alignment settings.
 					
 					canvas.drawBitmap(sprite.getImage(), x, y, paint); //Draw the sprite at our coordinates!
 				} else { //The sprite is rotated. We will have to account for this.
 					
 					//Rotation matrix we'll use on the sprite's bitmap
 					
 					//image's rotation, x coordinate of rotation, y coordinate of rotation
 					matrix.setRotate(sprite.getRotation(), this.getWidth() * sprite.getX() + sprite.getImage().getWidth() * (sprite.getAlignX() - 0.5f), sprite.getY() * this.getHeight() + sprite.getImage().getHeight() * (sprite.getAlignY() - 0.5f));
 					
 					canvas.drawBitmap(sprite.getImage(), matrix, paint);
 					matrix.reset();
 				}
 			}
 		}
 		super.draw(canvas); //Pass it off to super to deal with.
 	}
 	
 	/**
 	 * Adding sprite to our list of sprites.
 	 * 
 	 * @param sprite to add
 	 */
 	public void addSprite(Sprite sprite) {
 		sprites.add(sprite);
 	}
 	
 	/**
 	 * Replacing the sprite at index
 	 * 
 	 * @param index
 	 * @param newSprite
 	 */
 	public void replaceSprite(int index, Sprite newSprite) {
 		sprites.set(index, newSprite);
 	}
 	
 	/**
 	 * Wipe out our sprites.
 	 */
 	public void clearSprites() {
 		sprites.clear();
 	}
 }

 package com.game.Units;
 
import com.game.mobileblocks.GamePanel;

 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 
 public class  Block {
 	
 	protected int x;
 	protected int y;
 	public static int width = 50;
 	public static int height = 50;
 	protected int deltaX;
 	protected int deltaY;
 	protected int color;
 	protected RectF r;
 	protected Bitmap bitmap;
 	protected Paint paint;
 
 	
 	private Block(int x, int y, int width, int height, int deltaX, int deltaY)
 	{
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		this.deltaX = deltaX;
 		this.deltaY = deltaY;
 	}
 	
 	public Block(int x, int y, int width, int height, int deltaX, int deltaY,
 			int color) {
 		
 		this(x, y, width,height,deltaX,deltaY);
 		this.color = color;
 		this.paint = new Paint();
 		paint.setColor(this.color);
 	}
 	
 	
 	public Block(int x, int y, int color) {
 		this(x, y, width, height, 2, 2, color);
 	}
 	
 	public Block(int x, int y, int width, int height, int deltaX, int deltaY,Bitmap b ) {
 		this(x, y, width,height,deltaX,deltaY);
 		this.bitmap = b;
 		
 	}
 	
 	public int getX() {
 		return x;
 	}
 	public void setX(int x) {
 		this.x = x;
 	}
 	public int getY() {
 		return y;
 	}
 	public void setY(int y) {
 		this.y = y;
 	}
 	public int getWidth() {
 		return width;
 	}
 	public void setWidth(int width) {
 		this.width = width;
 	}
 	public int getDeltaX() {
 		return deltaX;
 	}
 	public void setDeltaX(int deltaX) {
 		this.deltaX = deltaX;
 	}
 	public int getDeltaY() {
 		return deltaY;
 	}
 	public void setDeltaY(int deltaY) {
 		this.deltaY = deltaY;
 	}
 	public int getColor() {
 		return color;
 	}
 	public void setColor(int color) {
 		this.color = color;
 	}
 	
 	public void update(long delta) {
		//if(x+width < GamePanel.width-1)
			//this.x += (delta * deltaX) / 1000;
		if(y+height < GamePanel.height-1)
			this.y += (delta * deltaY) / 1000;
 	}
 	
 	public void draw(Canvas canvas) {
		
 		if(bitmap != null) {
 			canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), null);
 		}
 		else {
 			canvas.drawRect(x, y, x+width, y+height,paint);
 		}
 		
 	}
 	
 
 
 	public boolean isCollision(int x, int y) {
 
 		return ! ( (x> (this.x+this.width))
 				|| ((x+40) < this.x) 
 				|| (y > (this.y+ this.height))
 				|| ((y+40) < this.y));
 
 	}
 	
 	public boolean isCollisionBlock(int x, int y, int w, int h) {
 
 		return ! ( (x> (this.x+this.width))
 				|| ((x+w) < this.x) );
 				//|| (y > (this.y+ this.height))
 				//|| ((y+h) < this.y));
 
 	}
 
 	public int getHeight() {
 		return height;
 	}
 	
 	
 	
 	
 
 }

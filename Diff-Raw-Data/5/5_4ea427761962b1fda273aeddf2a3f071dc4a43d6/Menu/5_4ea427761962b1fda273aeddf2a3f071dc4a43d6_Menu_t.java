 package org.racenet.racesow;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.racenet.framework.GLGame;
 import org.racenet.framework.TexturedBlock;
 import org.racenet.framework.Vector2;
 
 import android.os.Looper;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 
 /**
  * Class which represends a horizontal slideable openGL menu
  * 
  * @author soh#zolex
  *
  */
 public class Menu implements GestureDetector.OnGestureListener {
 
 	List<TexturedBlock> items = new ArrayList<TexturedBlock>();
 	List<Callback> callbacks = new ArrayList<Callback>();
 	GLGame game;
 	float viewWidth;
 	float viewHeight;
 	float spaceWidth = 0.1f;
 	float velocity = 0;
 	boolean scrolling = true;
 	float scale;
 	
 	/**
 	 * Simple callback for menu item clicks
 	 * 
 	 * @author soh#zolex
 	 *
 	 */
 	public class Callback {
 		
 		public void handle() {}
 	}
 	
 	/**
 	 * Constructor. TODO: calls the looper.prepare method once
 	 * 
 	 * @param GLGame game
 	 * @param float viewWidth
 	 * @param float viewHeight
 	 */
 	public Menu(GLGame game, float viewWidth, float viewHeight, float scale) {
 		
 		this.game = game;
 		this.viewWidth = viewWidth;
 		this.viewHeight = viewHeight;
 		this.scale = scale;
 	}
 	
 	public void setScrolling(boolean scrolling) {
 		
 		this.scrolling = scrolling;
 	}
 	
 	/**
 	 * Add a menu item right of the last added item
 	 * 
 	 * @param String texture
 	 * @param Callback callback
 	 */
 	public void addItem(String texture, Callback callback) {
 		
 		TexturedBlock item = new TexturedBlock(texture, TexturedBlock.FUNC_NONE, -1, -1, 0, 0, new Vector2(0, 0), new Vector2(this.viewWidth / 4, 0));
 		
 		float posX = 0;
 		int length = this.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			posX += this.items.get(i).width + this.spaceWidth;
 		}
 		
 		item.vertices[0].x = posX;
 		item.vertices[0].y = this.viewHeight / 2 - item.height / 2 - this.viewHeight / 5;
 		
 		this.items.add(item);
 		this.callbacks.add(callback);
 	}
 	
 	/**
 	 * When the user touches the menu
 	 */
 	public boolean onDown(MotionEvent event) {
 		
 		this.velocity = 0;
 		return true;
 	}
 
 	/**
 	 * When the user flings the menu
 	 */
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 		
 		if (!this.scrolling) {
 			
 			return false;
 		}
 		
 		if (e1 == null || e2 == null) {
 			
 			return false;
 		}
 		
 		if (Math.abs(e1.getX() - e2.getX()) < 50) {
 			
 			return false;
 		}
 		
		velocityX /= this.scale;
		
 		if (!this.allowMoveMenu(velocityX)) {
 			
 			return false;
 		} 
 		
 		this.velocity = velocityX;
 		
 		return true;
 	}
 
 	/**
 	 * When the user presses the menu long
 	 */
 	public void onLongPress(MotionEvent arg0) {
 		
 	}
 
 	/**
 	 * When the user scrolls the menu
 	 */
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
 		
 		if (!this.scrolling) {
 			
 			return false;
 		}
 		
 		if (e1 == null || e2 == null || Math.abs(e1.getX() - e2.getX()) < 50) {
 			
 			return false;
 		}
 		
		distanceX /= this.scale;
		
 		return this.moveMenu(distanceX);
 	}
 
 	/**
 	 * XXX: Dunno what this is...
 	 */
 	public void onShowPress(MotionEvent arg0) {
 		
 	}
 
 	/**
 	 * When the user just touches and releases the menu
 	 */
 	public boolean onSingleTapUp(MotionEvent event) {
 
 		if (event == null) {
 			
 			return false;
 		}
 		
 		this.buttonPress(event.getX() / this.scale, this.viewHeight - (event.getY() / this.scale));
 		return true;
 	}
 	
 	/**
 	 * Check if a menu item was pressed. If so
 	 * call the handler form the callback
 	 * 
 	 * @param float x
 	 * @param float y
 	 */
 	public void buttonPress(float x, float y) {
 		
 		int length = this.items.size();
 		for (int i = 0; i < length; i++) {
 		
 			TexturedBlock item = this.items.get(i);
 			if (x > item.vertices[0].x && x < item.vertices[0].x + item.width &&
 				y > item.vertices[0].y && y < item.vertices[0].y + item.height) {
 				
 				this.callbacks.get(i).handle();
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Check if the menu is allowed to move.
 	 * Stops on left and right end of the menu.
 	 * 
 	 * @param float distance
 	 * @return boolean
 	 */
 	public boolean allowMoveMenu(float distance) {
 		
 		if (this.items != null && items.size() > 0) {
 			
 			TexturedBlock first = this.items.get(0);
 			TexturedBlock last = this.items.get(this.items.size() - 1);
 			
 			if ((first.vertices[0].x > this.viewWidth / 2 - first.width / 2 && distance < 0) ||
 				(last.vertices[0].x < this.viewWidth / 2 - last.width / 2 && distance > 0)) {
 				
 				return false;
 			
 			} else {
 			
 				return true;
 			}
 			
 		} else {
 			
 			return false;
 		}
 	}
 	
 	/**
 	 * Move the menu by the given distance
 	 * 
 	 * @param float distance
 	 * @return boolean
 	 */
 	public boolean moveMenu(float distance) {
 		
 		if (!this.allowMoveMenu(distance)) {
 			
 			return false;
 		}
 		
 		int length = this.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.items.get(i).vertices[0].x -= distance;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Update he menu position according to it's velocity
 	 * 
 	 * @param float deltaTime
 	 */
 	public void update(float deltaTime) {
 		
 		if (this.velocity != 0) {
 			
 			this.moveMenu(-this.velocity  * deltaTime);
 			this.velocity = this.velocity / 1.025f;
 		}
 	}
 	
 	/**
 	 * Draw the menu
 	 */
 	public void draw() {
 		
 		int length = this.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.items.get(i).draw();
 		}
 	}
 	
 	/**
 	 * Reload all menu item's textures
 	 */
 	public void reloadTextures() {
 		
 		int length = this.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.items.get(i).reloadTexture();
 		}
 	}
 	
 	/**
 	 * Get rid of all menu item's textures
 	 */
 	public void dispose() {
 		
 		int length = this.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.items.get(i).dispose();
 		}
 	}
 }

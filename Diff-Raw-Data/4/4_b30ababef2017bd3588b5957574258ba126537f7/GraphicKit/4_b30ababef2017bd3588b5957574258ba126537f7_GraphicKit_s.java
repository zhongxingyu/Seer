 package factory.graphics;
 
 import java.awt.*;
 import java.util.*;
 
 /**
  * @author Tobias Lee <p>
  * <b>{@code GraphicKit.java}</b> (40x80)<br>
  * A graphical representation of a Kit
  */
 
 public class GraphicKit {
 	
 	/**The x coordinate of the Kit*/
 	private int x;
 	/**The y coordinate of the Kit*/
 	private int y;
 	/**The direction the Kit is facing with 8, 6, 2, 4 being North, East, South, and West respectively*/
 	private int direction;
 	/**The Items the Kit is holding*/
 	private ArrayList<GraphicItem> items;
 	/**The width of the Kit*/
 	public static int width = 40;
 	/**The height of the Kit*/
 	public static int height = 80;
 	
 	/**
 	 * Creates a Kit at the given x and y coordinates
 	 * @param x The initial x coordinate of the Kit
 	 * @param y The initial y coordinate of the Kit
 	 */
 	public GraphicKit(int x, int y) {
 		//Constructor
 		this.x = x;
 		this.y = y;
 		direction = 4;
 		items = new ArrayList<GraphicItem>();
 	}
 	
 	/**
 	 * Paints the Kit
 	 * @param g The specified graphics window
 	 */
 	public void paint(Graphics g) {
 		if (direction == 4 || direction == 6) {
 			g.setColor(new Color(241, 198, 67));
 			g.fillRect(x, y, width, height);
 			g.setColor(Color.black);
 			g.drawRect(x, y, width, height);
 			g.drawRect(x, y, width, height/4);
 			g.drawRect(x, y+height/4, width, height/4);
 			g.drawRect(x, y+height/2, width, height/4);
 			g.drawRect(x, y+3*height/4, width, height/4);
 			g.drawLine(x+width/2, y, x+width/2, y+height);
 		}
 		else if (direction == 2 || direction == 8) {
 			g.setColor(new Color(241, 198, 67));
 			g.fillRect(x, y, height, width);
 			g.setColor(Color.black);
 			g.drawRect(x, y, height, width);
 			g.drawRect(x, y, height/4, width);
 			g.drawRect(x+height/4, y, height/4, width);
 			g.drawRect(x+height/2, y, height/4, width);
 			g.drawRect(x+3*height/4, y, height/4, width);
 			g.drawLine(x, y+width/2, x+height, y+width/2);
 		}
 		paintItems(g);
 	}
 	
 	/**
 	 * Paints the Items in the Kit
 	 * @param g The specified graphics window
 	 */
 	public void paintItems(Graphics g) {
 		validateItems();
 		for (int i = 0; i < items.size(); i++) {
 			items.get(i).paint(g);
 		}
 	}
 	
 	/**
 	 * Repositions the Items in the Kit
 	 */
 	public void validateItems() {
 		for (int i = 0; i < items.size(); i++) {
 			switch (direction) {
 			case 4:	
 			case 6:	items.get(i).setX(x+(i%2)*21+1);
 					items.get(i).setY(y+(i/2)*21+1);
 					break;
 			case 2:	
			case 8:	items.get(i).setX(x+(i/4)*21+1);
					items.get(i).setY(y+(i%4)*21+1);
 			}
 		}
 	}
 	
 	/**
 	 * Moves the Kit horizontally
 	 * @param v The horizontal distance to move
 	 */
 	public void moveX(int v) {
 		x += v;
 	}
 	
 	/**
 	 * Moves the Kit vertically
 	 * @param v The vertical distance to move
 	 */
 	public void moveY(int v) {
 		y += v;
 	}
 	
 	/**
 	 * Moves the Kit to the given position
 	 * @param x The x coordinate to move to
 	 * @param y The y coordinate to move to
 	 */
 	public void move(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 	
 	/**
 	 * Sets the x coordinate of the Kit
 	 * @param x The new x coordinate of the Kit
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 	
 	/**
 	 * Sets the y coordinate of the Kit
 	 * @param y The new y coordinate of the Kit
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	/**
 	 * Gets the x coordinate of the Kit
 	 * @return The x coordinate of the Kit
 	 */
 	public int getX() {
 		return x;
 	}
 	
 	/**
 	 * Gets the y coordinate of the Kit
 	 * @return The y coordinate of the Kit
 	 */
 	public int getY() {
 		return y;
 	}
 	
 	/**
 	 * Sets the direction of the Kit
 	 * @param d The new direction of the Kit
 	 */
 	public void setDirection(int d) {
 		direction = d;
 	}
 	
 	/**
 	 * Gets the direction of the Kit
 	 * @return The direction of the Kit
 	 */
 	public int getDirection() {
 		return direction;
 	}
 	
 	/**
 	 * Adds an Item to the Kit
 	 * @param item The Item to be added to the Kit
 	 */
 	public void addItem(GraphicItem item) {
 		items.add(item);
 	}
 	
 	/**
 	 * Gets the Item at the given index of the Kit, or the Item at the nearest valid index
 	 * @param index The index of the item to retrieve
 	 * @return The Item at the given index
 	 * @exception ArrayIndexOutOfBoundsException If there is nothing in the Kit
 	 */
 	public GraphicItem getItem(int index) {
 		if (index >= items.size())
 			return items.get(items.size()-1);
 		if (index < 0)
 			return items.get(0);
 		return items.get(index);
 	}
 	
 }

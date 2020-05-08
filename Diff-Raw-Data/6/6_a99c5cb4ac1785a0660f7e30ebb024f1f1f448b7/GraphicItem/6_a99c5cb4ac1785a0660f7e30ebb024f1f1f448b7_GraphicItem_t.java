 package factory.graphics;
 
 import java.awt.*;
 import javax.swing.*;
 
 /**
  * @author Minh La, Tobias Lee, and George Li <p>
  * <b>{@code GraphicItem.java}</b> (20x20)<br>
  * This is the graphical representation of a Part
  */
 
 public class GraphicItem {
 	
 	/**The x coordinate of the Item*/
 	private int x;
 	/**The y coordinate of the Item*/
 	private int y;
 	/**The horizontal velocity of the Item*/
 	private int vx;
 	/**The vertical velocity of the Item*/
 	private int vy;
 	/**The Item's image path*/
 	private String imagePath;
 	/**The Item's image*/
 	private ImageIcon image;
 	private static final Image BAD_ITEM_IMAGE = new ImageIcon("Images/badItemX.png").getImage();
 	/**The number of horizontal steps down the Lane*/
 	private int stepX;
 	/**The number of vertical steps down the Lane*/
 	private int stepY;
 	/**Whether or not this Item goes to the top or bottom Lane of a Lane pair*/
 	private boolean divergeUp;
 	private boolean isBad;
 
 	private boolean inQueue;
 	
 	/**
 	 * Creates an Item with the given image at the given x and y coordinates
 	 * @param x The initial x coordinate of the Item
 	 * @param y The initial y coordinate of the Item
 	 * @param imagepath The String path of the Image
 	 */
 	public GraphicItem(int x, int y, String imagepath) {
 		this.x = x;
 		this.y = y;
 		imagePath = imagepath;
 		image = new ImageIcon(imagePath);
 		
 		//Related to Lane
 		vx = 0;
 		vy = 0;
 		stepX = 20;
 		stepY = 5;
 		divergeUp = false;
 		
 		isBad = false;
 	}
 	
 	/**
 	 * Gets the x coordinate of the Item
 	 * @return The x coordinate of the Item
 	 */
 	public int getX() {
 		return x;
 	}
 	
 	/**
 	 * Gets the y coordinate of the Item
 	 * @return The y coordinate of the Item
 	 */
 	public int getY() {
 		return y;
 	}
 	
 	/**
 	 * Sets the x coordinate of the Item
 	 * @param x The new x coordinate of the Item
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 	
 	/**
 	 * Sets the y coordinate of the Item
 	 * @param y The new y coordinate of the Item
 	 */
 	public void setY(int y) {
 		this.y = y;
 	}
 	
 	/**
 	 * Moves the Item horizontally
 	 * @param v The horizontal distance to move
 	 */
 	public void moveX(int v) {
 		x += v;
 	}
 	
 	/**
 	 * Moves the Item vertically
 	 * @param v The vertical distance to move
 	 */
 	public void moveY(int v) {
 		y += v;
 	}
 	
 	/**
 	 * Gets the Item's x velocity along the Lane
 	 * @return The Item's x velocity along the Lane
 	 */
 	public int getVX() {
 		return vx;
 	}
 	
 	/**
 	 * Gets the Item's y velocity along the Lane
 	 * @return The Item's y velocity along the Lane
 	 */
 	public int getVY() {
 		return vy;
 	}
 	
 	/**
 	 * Sets the Item's x velocity along the Lane
 	 * @param v The Item's x velocity along the Lane
 	 */
 	public void setVX(int v) {
 		vx = v;
 	}
 	
 	/**
 	 * Sets the Item's y velocity along the Lane
 	 * @param v The Item's y velocity along the Lane
 	 */
 	public void setVY(int v) {
 		vy = v;
 	}
 	
 	/**
 	 * Moves the Item horizontally along its predetermined {@code vx}
 	 */
 	public void moveVX() {
 		x += vx;
 	}
 	
 	/**
 	 * Moves the Item vertically along its predetermined {@code vy}
 	 */
 	public void moveVY() {
 		y += vy;
 	}
 	
 	/**
 	 * Paints the Item
 	 * @param g The specified graphics window
 	 */
 	public void paint(Graphics g) {
 		g.drawImage(image.getImage(), x, y, null);
 		if(isBad)
 			g.drawImage(BAD_ITEM_IMAGE, x, y, null);
 	}
 	
 	/**
 	 * Paints the Item at the given coordinates
 	 * @param g The specified graphics window
 	 * @param x The x coordinate to paint at
 	 * @param y The y coordinate to paint at
 	 */
 	public void paint(Graphics g, int x, int y) {
 		g.drawImage(image.getImage(), x, y, null);
 		if(isBad)
 			g.drawImage(BAD_ITEM_IMAGE, x, y, null);
 	}
 	
 	/**
 	 * Sets the number of steps to move horizontally down a Lane
 	 * @param sx The number of steps to move
 	 */
 	public void setStepX(int sx) {
 		stepX = sx;
 	}
 	
 	/**
 	 * Sets the number of steps to move vertically down a Lane
 	 * @param sy The number of steps to move
 	 */
 	public void setStepY(int sy) {
 		stepY = sy;
 	}
 	
 	/**
 	 * Gets the number of steps to move horizontally down a Lane
 	 * @return The number of steps to move
 	 */
 	public int getStepX() {
 		return stepX;
 	}
 	
 	/**
 	 * Gets the number of steps to move vertically down a Lane
 	 * @return The number of steps to move
 	 */
 	public int getStepY() {
 		return stepY;
 	}
 	
 	/**
 	 * Sets whether the Item will go down the top Lane or the bottom Lane
 	 * @param diverge Whether or not the item will go to the top Lane
 	 */
 	public void setDivergeUp(boolean diverge) {
 		divergeUp = diverge;
 	}
 	
 	/**
 	 * Checks if the Item will go down the top Lane or the bottom Lane
 	 * @return {@code true} if it will go down the top Lane; {@code false} if it will go down the bottom Lane
 	 */
 	public boolean getDivergeUp() {
 		return divergeUp;
 	}
 
 	public void setInQueue(boolean queue) {
 		inQueue = queue;
 	}
 	
 	public boolean getInQueue() {
 		return inQueue;
 	}
 	
 	/**
 	 * Gets the image of the Item
 	 * @return The ImageIcon of the Item
 	 */
 	public ImageIcon getImage() {
 		return image;
 	}
 	
 	/**
 	 * Gets the image path of the Item
 	 * @return The image path of the Item
 	 */
 	public String getImagePath() {
 		return imagePath;
 	}
 	
 	public void setIsBad() {
 		isBad = true;
	}
	
}

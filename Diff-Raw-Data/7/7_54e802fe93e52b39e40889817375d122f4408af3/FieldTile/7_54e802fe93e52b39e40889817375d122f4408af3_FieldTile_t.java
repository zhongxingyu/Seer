 package view;
 import java.awt.event.*;
 import java.awt.geom.Ellipse2D;
 import java.awt.*;
 import java.util.ArrayList;
 import javax.swing.*;
 
 /**
  * @author Stephen Bos
  * June, 2013
  * 
  * FieldTile objects model the circular tiles on the Ludo board where pawns can be placed.
  * FieldTile subclasses JComponent and thus can be added to any Swing container object. 
  * 
  * A tile can be in one of three states: active, inactive, or set as an active destination.
  * If a tile is active, it responds to mouse events and is user-interactable. If the tile is
  * inactive, it will not respond to any user input. If the tile is set as an active destination,
  * it will be highlighted in red, rather than the regular blue or red.
  * 
  * It should be noted that FieldTile objects do not store an occupied state. FieldTiles are simply
  * circles that can be filled with different colors. A separate data model should be responsible for
  * keeping track of the tile occupation.
  * 
  * Although it is not enforced, each tile should be set with an id string so that it can be uniquely identified.
  * 
  * FielTiles can respond to the following mouse events:
  *  - mouseClicked: used to register when a player clicks a pawn, ex. to signal a move
  *  - mouseEntered: can be used by listeners to highlight a pawn's move destination
  *  - mouseExited: can be used by listeners to un-highlight a pawn's move destination 
  */
 public class FieldTile extends JComponent implements MouseListener{
 	
 	public static final String EXIT_EVENT = "exit";
 	public static final String ENTER_EVENT = "enter";
 	public static final String CLICK_EVENT ="click";
 	
 	private static final long serialVersionUID = 1L;
 	private static final int Padding = 5;
 	private static final int Diameter = 30;
 	
 	public static final int HEIGHT = 2*Padding+Diameter;
 	public static final int WIDTH = 2*Padding+Diameter;
 	
 	private static final Dimension dimension = new Dimension(WIDTH,HEIGHT);
 	
 	/*===================================
 	 FIELDS
 	 ===================================*/
 	private boolean isActive;
 	private boolean mouseEntered;
 	private boolean mousePressed;
 	private boolean isActiveDestination;
 	private Shape circle;
 	private Color fieldColor;
 	private Color borderColor;
 	private ArrayList<ActionListener> actionListeners;
 	private String id;
 	
 	/*===================================
 	 CONSTRUCTORS
 	 ===================================*/
 	public FieldTile(){
 		super();
 		this.enableInputMethods(true);
 		this.addMouseListener(this);
 		this.setPreferredSize(FieldTile.dimension);
 		
 		this.actionListeners = new ArrayList<ActionListener>();
 		
 		this.fieldColor=Color.WHITE;
 		this.borderColor=Color.BLACK;
 		this.circle = new Ellipse2D.Double(Padding,Padding,Diameter,Diameter);
 		this.isActive = false;
 		this.id="";
 	}
 	
 	public FieldTile(Color c){
 		super();
 		this.enableInputMethods(true);
 		this.addMouseListener(this);
 		this.setPreferredSize(FieldTile.dimension);
 		
 		this.actionListeners = new ArrayList<ActionListener>();
 		
 		this.fieldColor = c;
 		this.borderColor = Color.BLACK;
 		this.circle = new Ellipse2D.Double(Padding,Padding,Diameter,Diameter);
 		this.isActive = false;
 		this.id="";
 	}
 	
 	/*===================================
 	 GETTERS & SETTERS
 	 ===================================*/
 	
 	public void setId(String id){
 		this.id=id;
 	}
 	
 	public String getId(){
 		return this.id;
 	}
 	
 	public void setBorderColor(Color c){
 		this.borderColor = c;
 	}
 	
 	public Color getBorderColor(){
 		return this.borderColor;
 	}
 	
 	public void toggleIsActive(){
 		this.isActive=this.isActive?false:true;
 	}
 	
 	public void setActive(){
 		this.isActive = true;
 	}
 	
 	public void setInactive(){
 		this.isActive = false;
 		this.mouseEntered = false;
		this.repaint();
 	}
 	
 	public boolean isActive(){
 		return this.isActive;
 	}
 	
 	public void setColor(Color c){
		this.isActiveDestination=false;
 		this.fieldColor=c;
 		this.repaint();
 	}
 	
 	public Color getColor(){
 		return this.fieldColor;
 	}
 	
 	public Dimension getPrefferedSize(){
 		return FieldTile.dimension;
 	}
 	
 	public Dimension getMinimumSize(){
 		return FieldTile.dimension;
 	}
 	
 	public Dimension getMaximumSize(){
 		return FieldTile.dimension;
 	}
 	
 	public void toggleIsActiveDestination(){
 		this.isActiveDestination = isActiveDestination?false:true;
 	}
 	
 	public void setIsActiveDestination(boolean isActiveDestination){
 		this.isActiveDestination=isActiveDestination;
 	}
 	
 	public boolean isActiveDestination(){
 		return this.isActiveDestination;
 	}
 	
 	public void addActionListener(ActionListener subscriber){
 		this.actionListeners.add(subscriber);
 	}
 	
 	public void removeActionListener(ActionListener subscriber){
 		this.actionListeners.remove(subscriber);
 	}
 	
 	public String toString(){
 		return this.id;
 	}
 	
 	/*===================================
 	 DRAWING METHODS
 	 ===================================*/
 	
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		
 		// Get 2D graphics object and turn anti aliasing on.
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		// Fill field
 		g2.setColor(mousePressed?Color.GRAY:this.fieldColor);
 		g2.fill(this.circle);
 		
 		// Draw border
 		if(mouseEntered){
 			g2.setColor(Color.BLUE);
 		}
 		else if(isActiveDestination){
 			g2.setColor(Color.RED);
 		}
 		else{
 			g2.setColor(borderColor);
 		}
 		g2.setStroke(new BasicStroke(3));
 		g2.draw(this.circle);
 	}
 	
 	/*===================================
 	 MOUSE EVENT HANDLERS
 	 ===================================*/
 	
 	/**
 	 * Fires an ActionEvent to all subscribers when the component is clicked.
 	 */
 	public void mouseClicked(MouseEvent e) {
 		if(this.isActive){
 			ActionEvent event = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,FieldTile.CLICK_EVENT);
 			for(ActionListener subscriber:this.actionListeners){
 				subscriber.actionPerformed(event);
 			}
 		}
 	}
 
 	/**
 	 * If the mouse enters the component, the field is redrawn with a highlighted border.<br>
 	 * An ActionEvent is also fired to all subscribers.
 	 */
 	public void mouseEntered(MouseEvent e) {
 		if(this.isActive){
 			this.mouseEntered = true;
 			this.repaint();	
 			
 			ActionEvent event = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,FieldTile.ENTER_EVENT);
 			for(ActionListener subscriber:this.actionListeners){
 				subscriber.actionPerformed(event);
 			}
 		}
 	}
 	
 	/**
 	 * If the mouse exits the component, the field is redrawn with a normal border.<br>
 	 * An ActionEvent is also fired to all subscribers
 	 */
 	public void mouseExited(MouseEvent e) {
 		
 		this.mouseEntered = false;
 		this.repaint();
 		
 		if(this.isActive){
 			ActionEvent event = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,FieldTile.EXIT_EVENT);
 			for(ActionListener subscriber:this.actionListeners){
 				subscriber.actionPerformed(event);
 			}
 		}
 	}
 	
 	/**
 	 * When the mouse is held down, paints the field background gray.
 	 */
 	public void mousePressed(MouseEvent e) {
 		if(this.isActive){
 			this.mousePressed = true;
 			this.repaint();	
 		}
 	}
 	
 	/**
 	 * When the mouse is released, paints the field background its regular color.
 	 */
 	public void mouseReleased(MouseEvent e) {
 		if(this.isActive){
 			this.mousePressed = false;
 			this.repaint();
 		}
 	}
 }

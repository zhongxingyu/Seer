 package game;
 import gui.CentipedeSprite;
 import gui.EntitySprite;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Point2D;
 
 /**
  * A class which represents a Centipede segment. Centipedes are essentially
  * a list of linked nodes.
  *
  */
 public class Centipede implements Entity {
 	
 	private boolean 			m_isHead;
 	private Board				m_board;
 	private Point				m_location;
 	private EntitySprite		m_sprite;
 	private Rectangle			m_boundingBox;
 	private Direction			m_direction;
 	private Centipede			m_nextSegment;
 	
 	private final int SQUARE_SIZE = 50;
 	
 	
 	/**
 	 * Construction of a Centipede requires a factory method in order to
 	 * create all segments and link them.
 	 * @param isHead	true if this segment is the head
 	 * @param board		the Board on which this centipede resides
 	 * @param loc		the location of this segment
 	 * @param dir		the direction in which this Centipede will travel
 	 * @param nextSeg	the next Centipede segment: null if this is the tail
 	 */
 	private Centipede(	boolean isHead,
 						Board board,
 						Point loc,
 						Direction dir,
 						Centipede nextSeg	) {
 		this.m_isHead = isHead;
 		this.m_board = board;
 		this.m_location = loc;
 		this.m_direction = dir;
 		this.m_nextSegment = nextSeg;
 		
 		// Set by the factory method
 		this.m_sprite = null;
 		this.m_boundingBox = null;
 	}
 	
 	public boolean isHead(){
 		return this.m_isHead;
 	}
 	
 	public Direction getDirection(){
		// THIS IS BAD
		// Need to return an unmodifiable version, but can't construct enums
		return this.m_direction;
		
 	}
 	
 	public void move() {
 		switch(this.m_direction) {
 			case LEFT:
 				
 				break;
 			case RIGHT:
 				
 				break;
 				
 			case DOWN:
 				
 				break;
 				
 			case UP:
 				
 				break;
 		}
 		
 	}
 
 	public void collidesWith(Entity entity) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Kill this segment. If there is a next segment, it becomes a new head.
 	 * If the segment killed is not a head, there will now be two Centipedes.
 	 */
 	public void die() {
 		if (this.m_nextSegment != null) {
 			
 		}
 		// Problem: how does the previous segment know it has lost its next?
 		// Will we use a doubly linked list?
 	}
 
 	/*
 	 * These will also let our private data escape!
 	 */
 	
 	public EntitySprite getSprite() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Point2D getLocation() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Rectangle getBoundingBox() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public int getRadius() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	public Centipede makeCentipede(	int length,
 									Board board,
 									Point headLocation,
 									Direction initialDirection	) {
 		Centipede head = null;
 		Centipede curr = null;
 		for (int i=length-1; i>1; --i) {	// Build from tail up
 			// assuming we are building head at left, tail to right
 			Point segmentLocation = new Point(headLocation.x + SQUARE_SIZE * i,
 												headLocation.y);
 			curr = new Centipede(false, board, segmentLocation,
 									initialDirection, curr);
 			curr.m_sprite = new CentipedeSprite(curr);
 			curr.recalcBoundingBox();
 		}
 		head = new Centipede(true, board, headLocation, initialDirection, curr);
 		head.m_sprite = new CentipedeSprite(head);
 		head.recalcBoundingBox();
 		return head;
 	}
 	
 	private void recalcBoundingBox() {
 		int x = m_location.x - SQUARE_SIZE / 2;
 		int y = m_location.y - SQUARE_SIZE / 2;
 		m_boundingBox = new Rectangle( x, y, SQUARE_SIZE, SQUARE_SIZE );
 	}
 
 }

 package by.epamlab.elevator.ui.widgets;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.Stroke;
 
 import org.jsfml.system.Vector2i;
 
 
 public class WBase implements DynamicWidget {
 	/* Inspired by SFML */
 	static final Vector2i DEFAULT_POSITION = new Vector2i(0, 0);
 	static final Vector2i DEFAULT_ORIGIN = new Vector2i(0 ,0);
 	static final Vector2i DEFAULT_SIZE = new Vector2i(20, 20);
 	
 	private Color color = Color.BLACK;
 	private float lineWidth = 1;
 	private boolean positionNeedUpdate = true;
 	
 	protected Vector2i position = DEFAULT_POSITION;
 	protected Vector2i origin = DEFAULT_ORIGIN;
 	protected Vector2i size = DEFAULT_SIZE;
 	private Vector2i screenPos = DEFAULT_POSITION;
 	
 	
 	public WBase() {
 
 	}
 	
 	public WBase(Vector2i position) {
 		this.position = position;
 	}
 	
 	public WBase(Vector2i position, Vector2i size) {
 		this.position = position;
 		this.size = size;
 	}
 	
 	public Vector2i getPosition() {
 		return position;
 	}
 
 	public void setPosition(Vector2i position) {
 		this.position = position;
		screenPos = Vector2i.add(position, origin);
 	}
 
 	public Vector2i getOrigin() {
 		return origin;
 	}
 	
 	public void setOrigin(Vector2i origin) {
 		this.origin = origin;
		screenPos = Vector2i.add(position, origin);
 	}
 	
 	public Vector2i getSize() {
 		return size;
 	}
 
 	public void setSize(Vector2i size) {
 		this.size = size;
 	}
 
 	public float getLineWidth() {
 		return lineWidth;
 	}
 
 	public void setLineWidth(float lineWidth) {
 		this.lineWidth = lineWidth;
 	}
 	
 	public Color getColor() {
 		return color;
 	}
 	
 	public void setColor(Color color) {
 		this.color = color;
 	}
 	
 	protected Vector2i getScreenPos() {
 		if (positionNeedUpdate) {
 			screenPos = Vector2i.sub(position, origin);
 			positionNeedUpdate = false;
 		}
 		
 		return screenPos;
 	}
 	
 	//FIXME COPY 'ORIGIN' IMPL FROM SFML!!!
 		//Partially done
 	//FIXME JUST FIXME!11
 	@Override
 	public void draw(Graphics target) {
 		target.setColor(color);
 		Vector2i pos = getScreenPos();
 		((Graphics2D) target).setStroke(new BasicStroke(lineWidth)); 
 		target.drawRect(pos.x, pos.y, size.x, size.y);
 	}
 	
 	@Override
 	public void update(float dt) {
 		//pass
 	}
 
 }

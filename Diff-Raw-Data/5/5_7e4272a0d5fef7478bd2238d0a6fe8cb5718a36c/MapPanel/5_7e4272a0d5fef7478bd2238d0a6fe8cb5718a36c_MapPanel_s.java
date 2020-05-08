 package vms.gui;
 
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseMotionListener;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import javax.swing.JPanel;
 import vms.*;
 import vms.Alert.AlertType;
 import vms.ConnectionServer.Observer;
 import common.*;
 import common.Vessel.VesselType;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 public class MapPanel extends JPanel implements MouseListener, MouseWheelListener, MouseMotionListener {
 	/**
 	 * 
 	 */
 	public interface Observer {
 		public void update(Coord center, Coord pointer, int range, int maxRange, double width, double height);
 	}
 	
 	private static final long serialVersionUID = -3982174526003182203L;
 	private int RANGE;
 	private final int MAX_RANGE = 5000;
 	private final int HIGH_RISK = 50;
 	private final int LOW_RISK = 200;
 	private Coord center;
 	private Coord currentMouseCoords;
 	private Coord previousMouseCoords;
 	private double scale;
 	
 	private List<Vessel> _Vessels;
 	private List<Alert> _Alerts;
 	private List<Observer> _Observers;
 	
 	public MapPanel() {
 		_Vessels = new ArrayList<Vessel>();
 		_Alerts = new ArrayList<Alert>();
 		_Observers = new ArrayList<Observer>();
 		RANGE = MAX_RANGE;
 		scale = (double)MAX_RANGE/RANGE;
 		center = new Coord(0,0);
 		this.addMouseListener(this);
 		this.addMouseWheelListener(this);
 		this.addMouseMotionListener(this);
 	}
 	
 	public void update(final List<Alert> alerts, final List<Vessel> vessels) {
 		_Alerts = alerts;
 		_Vessels = vessels;
 		this.repaint();
 	}
 	
 	public void registerObserver(Observer o) {
 		if (!_Observers.contains(o))
 			_Observers.add(o);
 	}
 	
 	public void unregisterObserver(Observer o) {
 		_Observers.remove(o);
 	}
 	
 	public void updateObservers(Coord center, Coord pointer, int range, int maxRange, double width, double height) {
 		for (int i=0; i < _Observers.size(); i++)
 			_Observers.get(i).update(center, pointer, range, maxRange, width, height);
 	}
 	
 	public void changeRange(double x) {
 		RANGE = (int) Math.ceil((double) MAX_RANGE / (double) x);
 		this.repaint();
 	}
 	
 	public int getRange() {
 		return RANGE;
 	}
 	
 	public void changeCenter(Coord newCenter) {
 		center = newCenter;
 		this.repaint();
 	}
 	
 	public Coord getCenter() {
 		return center;
 	}
 	
 	public Coord convertMouseToCoords(int x, int y) {
 		double width = (double) this.getWidth();
 		double height = (double) this.getHeight();
 		double xPos = center.x();
 		double yPos = center.y();
 		if (width > height) {
 			xPos = (x*2*RANGE*(1+(width-height)/height)/width - RANGE*(width-height)/height - RANGE + center.x());
 			yPos = (y*(-2)*RANGE/height + RANGE + center.y());
 		}
 		else {
 			xPos = (x*2*RANGE/width - RANGE + center.x());
 			yPos = (y*(-2)*RANGE*(1+(height-width)/width)/height + RANGE*(height-width)/width + RANGE + center.y());
 		}
 		
 		return new Coord(xPos, yPos);
 	}
 	
 	public Coord correctOutOfBounds(Coord coords) {
 		double xPos = coords.x();
 		double yPos = coords.y();
 		if (xPos > MAX_RANGE)
 			xPos = MAX_RANGE;
 		if (xPos < -MAX_RANGE)
 			xPos = -MAX_RANGE;
 		if (yPos > MAX_RANGE)
 			yPos = MAX_RANGE;
 		if (yPos < -MAX_RANGE)
 			yPos = -MAX_RANGE;
 		
 		return new Coord(xPos, yPos);
 	}
 	
 	public Rectangle getDrawableArea(int width, int height) {
 		Rectangle r = new Rectangle();
 		if (width > height) {
 			r.x = (width - height) / 2;
 			r.y = 0;
 			r.width = height;
 			r.height = height;
 		}
 		else {
 			r.x = 0;
 			r.y = (height - width) / 2;
 			r.width = width;
 			r.height = width;
 		}
 		return r;
 	}
 	
 	private void drawArrow(Course v, Point p, Graphics2D g) {
 		int cx = p.x;
 		int cy = p.y;
 		int vx = (int) v.xVel();
 		int vy = (int) v.yVel();
 		int dx = cx - vx;
 		int dy = cy - vy;
 		int lenBody = (int) Math.sqrt( Math.pow((double) dx, 2.0) + Math.pow((double) dy, 2.0)) /4;
 		final int RATIO = RANGE/500;
 		g.drawLine(cx+vx*-1/RATIO, cy+vy/RATIO, cx-vx*-1/RATIO, cy-vy/RATIO); // Body of arrow
 		g.drawLine(cx+vx*-1/RATIO, cy+vy*-1/RATIO, cx-vx*-1/RATIO, cy-vy/RATIO); // Arrow Head of Diagnol
 		g.drawLine(cx-vx*-1/RATIO, cy-vy*-1/RATIO, cx-vx*-1/RATIO, cy-vy/RATIO); 
 	}
 	
 	public Point place(Coord c, Rectangle b, int range) {
 		Point p = new Point();
 //		p.x = (int) Math.ceil(c.x()) * b.width / (range/2) + b.x + (b.width / 2);
 //		p.y = (int) Math.ceil(c.y()) * b.height / (range/2) + b.y + (b.height / 2);
 //		p.x = (int) Math.ceil((c.x()/2) * b.width / (range)) + b.x + (b.width/2);
 //		p.y = (int) Math.ceil((c.y()/-2) * b.height / (range)) + b.y + (b.height/2);
 		p.x = (int) (Math.ceil(c.x())*b.width/(2*range) + b.x + b.width/2 - center.x()*b.width/(2*range));
 		p.y = (int) (Math.ceil(c.y())*b.height/(-2*range) + b.y + b.height/2 + center.y()*b.height/(2*range));
 		//System.out.println("[" + c.x() + "," + c.y() + "] -> [" + p.x + "," + p.y + "]");
 		return p;
 	}
 	
 	public Color getTypeColor(VesselType t) {
 		switch (t) {
 		case SWIMMER:
 			return Color.PINK;
 		case FISHING_BOAT:
 			return Color.CYAN;
 		case SPEED_BOAT:
 			return Color.GREEN;
 		case CARGO_BOAT:
 			return Color.ORANGE;
 		case PASSENGER_VESSEL:
 			return Color.MAGENTA;
 		case UNKNOWN:
 			return Color.WHITE;
 		default:
 			return Color.WHITE;
 		}
 	}
 	
 	public void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setColor(Color.black);
 		super.paintComponent(g);
 //		g2.fillRect(0, 0, getWidth(), getHeight()-50);
 		g2.fillRect(0, 0, getWidth(), getHeight());
 		
 		//draw bounds
 		g2.setColor(Color.getHSBColor(125, 100, 83));
 //		Rectangle b = getDrawableArea(getWidth(), getHeight()-50);
 		Rectangle b = getDrawableArea(getWidth(), getHeight());
 		g2.drawRect(b.x, b.y, b.width, b.height);
 		
 		scale = (double)MAX_RANGE/RANGE;
 		
 		//draw outer range
 		g2.setColor(Color.WHITE);
 		g2.drawOval((int)(b.x - center.x()*scale*b.width/(2*MAX_RANGE) - 0.5*b.width*(scale-1)),
 				(int)(b.y + center.y()*scale*b.height/(2*MAX_RANGE) - 0.5*b.height*(scale-1)),
 				(int)(b.width*scale), (int)(b.height*scale));
 		
 		//draw grid
 		g2.setColor(Color.GRAY);
 		for (int i=0; i<=MAX_RANGE/250; i++) {
 //			if (i == MAX_RANGE/200)
 //				g2.setColor(Color.WHITE);
 //			else
 				g2.setColor(Color.GRAY);
 			g.drawLine((int)(b.x - center.x()*scale*b.width/(2*MAX_RANGE) - 0.5*b.width*(scale-1)),
 					(int)(b.y + center.y()*scale*b.height/(2*MAX_RANGE) - 0.5*b.height*(scale-1) + i*b.height*scale/(MAX_RANGE/250)),
 					(int)(b.x - center.x()*scale*b.width/(2*MAX_RANGE) - 0.5*b.width*(scale-1) + b.width*scale),
 					(int)(b.y + center.y()*scale*b.height/(2*MAX_RANGE) - 0.5*b.height*(scale-1) + i*b.height*scale/(MAX_RANGE/250)));
 			g.drawLine((int)(b.x - center.x()*scale*b.width/(2*MAX_RANGE) - 0.5*b.width*(scale-1) + i*b.width*scale/(MAX_RANGE/250)),
 					(int)(b.y + center.y()*scale*b.height/(2*MAX_RANGE) - 0.5*b.height*(scale-1)),
 					(int)(b.x - center.x()*scale*b.width/(2*MAX_RANGE) - 0.5*b.width*(scale-1) + i*b.width*scale/(MAX_RANGE/250)),
 					(int)(b.y + center.y()*scale*b.height/(2*MAX_RANGE) - 0.5*b.height*(scale-1) + b.height*scale));
 		}
 		
 		//draw axis
 		g2.setColor(Color.WHITE);
 		g.drawLine(b.x + (b.width/2), b.y, b.x + (b.width/2), (b.y + b.height));
 		g.drawLine(b.x, b.y + (b.height/2), (b.x + b.width), b.y + (b.height/2));
 		
 		Calendar now = Calendar.getInstance();
 		for (Vessel v : _Vessels) {
 			if (v.getDistance(Calendar.getInstance()) <= MAX_RANGE) {
 				Color defaultColor = getTypeColor(v.getType());
 				g2.setColor(defaultColor);
 				Coord c = v.getCoord(now);
 				Course co = v.getCourse(now);
 				Point p = place(c, b, RANGE);
 				drawArrow(co, p, g2);
 				g.drawString(v.getId(), p.x+6, p.y+6);
 				
 				//Search for worst alert
 				Alert worstAlert = null;
 				for (Alert a : _Alerts) {
 					if (a.contains(v)) {
 						if (worstAlert == null || worstAlert.getType() == AlertType.NONE || a.getType() == AlertType.HIGHRISK) {
 							worstAlert = a;
 						}
 					}
 				}
 				
 				// Draws circles around the ships; adds color if there is a high-risk or low-risk alert
 				if (worstAlert != null && worstAlert.getType() == AlertType.HIGHRISK)
 					g2.setColor(Color.red);
 				
 				Point ul, lr;
 				ul = place(new Coord(c.x() - HIGH_RISK, c.y() + HIGH_RISK), b, RANGE);
 				lr = place(new Coord(c.x() + HIGH_RISK, c.y() - HIGH_RISK), b, RANGE);
 				g.drawOval(ul.x, ul.y, lr.x - ul.x, lr.y - ul.y);
 				g2.setColor(defaultColor);
 				
 				if (worstAlert != null && worstAlert.getType() == AlertType.LOWRISK)
 					g2.setColor(Color.yellow);
 	
 				ul = place(new Coord(c.x() - LOW_RISK, c.y() + LOW_RISK), b, RANGE);
 				lr = place(new Coord(c.x() + LOW_RISK, c.y() - LOW_RISK), b, RANGE);
 				g.drawOval(ul.x, ul.y, lr.x - ul.x, lr.y - ul.y);
 			}
 		}
 	}	
 
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		center = correctOutOfBounds(convertMouseToCoords(e.getX(), e.getY()));
 		updateObservers(center, currentMouseCoords, RANGE, MAX_RANGE, this.getWidth(), this.getHeight());
 		this.repaint();
 	}
 	
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		RANGE += e.getWheelRotation()*(500);
 		if (RANGE < 500)
 			RANGE = 500;
 		if (RANGE > 2*MAX_RANGE)
 			RANGE = 10000;
		previousMouseCoords = currentMouseCoords;
 		currentMouseCoords = convertMouseToCoords(e.getX(), e.getY());
 		updateObservers(center, currentMouseCoords, RANGE, MAX_RANGE, this.getWidth(), this.getHeight());
 		this.repaint();
 	}
 	
 	public void mouseDragged(MouseEvent e) {
 		if (e.getX() < 0 || e.getY() < 0 || e.getX() > this.getWidth() || e.getY() > this.getHeight())
 			return;
 		
 		previousMouseCoords = currentMouseCoords;
 		currentMouseCoords = convertMouseToCoords(e.getX(), e.getY());
 		
 		double xPos, yPos;
 		xPos = center.x() - (currentMouseCoords.x() - previousMouseCoords.x());
 		yPos = center.y() - (currentMouseCoords.y() - previousMouseCoords.y());
 		center = correctOutOfBounds(new Coord(xPos, yPos));
 			
 		currentMouseCoords = convertMouseToCoords(e.getX(), e.getY());
 		updateObservers(center, currentMouseCoords, RANGE, MAX_RANGE, this.getWidth(), this.getHeight());
 		this.repaint();
 	}
 	
 	public void mouseMoved(MouseEvent e) {
		previousMouseCoords = currentMouseCoords;
 		currentMouseCoords = convertMouseToCoords(e.getX(), e.getY());
 		updateObservers(center, currentMouseCoords, RANGE, MAX_RANGE, this.getWidth(), this.getHeight());
 	}
 	
 }

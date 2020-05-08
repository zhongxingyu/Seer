 package vms.gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import javax.swing.JPanel;
 import vms.*;
 import vms.Alert.AlertType;
 import common.*;
 import common.Vessel.VesselType;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 public class MapPanel extends JPanel {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3982174526003182203L;
 	private final int RANGE = 5000;
 	private final int HIGH_RISK = 50;
 	private final int LOW_RISK = 200;
 	
 	private List<Vessel> _Vessels;
 	private List<Alert> _Alerts;
 	
 	public MapPanel() {
 		_Vessels = new ArrayList<Vessel>();
 		_Alerts = new ArrayList<Alert>();
 	}
 	
 	public void update(final List<Alert> alerts, final List<Vessel> vessels) {
 		_Alerts = alerts;
 		_Vessels = vessels;
 		this.repaint();
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
 	
 	public Point place(Coord c, Rectangle b, int range) {
 		Point p = new Point();
 //		p.x = (int) Math.ceil(c.x()) * b.width / (range/2) + b.x + (b.width / 2);
 //		p.y = (int) Math.ceil(c.y()) * b.height / (range/2) + b.y + (b.height / 2);
		p.x = (int) Math.ceil(c.x()/2) * b.width / (range/2) + b.x + (b.width/2);
		p.y = (int) Math.ceil(c.y()/2) * b.height / (range/2) + b.y + (b.height/2);
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
 		g2.fillRect(0, 0, getWidth(), getHeight());
 		g2.setColor(Color.getHSBColor(125, 100, 83));
 		
 		Rectangle b = getDrawableArea(getWidth(), getHeight());
 		g2.drawRect(b.x, b.y, b.width, b.height);
 		g2.setColor(Color.WHITE);
 		
 		g.drawLine(b.x + (b.width/2), b.y, b.x + (b.width/2), (b.y + b.height));
 		g.drawLine(b.x, b.y + (b.height/2), (b.x + b.width), b.y + (b.height/2));
 		Calendar now = Calendar.getInstance();
 		for (Vessel v : _Vessels) {
 			Color defaultColor = getTypeColor(v.getType());
 			g2.setColor(defaultColor);
 			Coord c = v.getCoord(now);
 			Point p = place(c, b, RANGE);
 			g.fillOval(p.x-3, p.y-3, 6, 6);
 			//g.drawLine(p.x, p.y, (b.x + b.width)/2, (b.y + b.height)/2);
 			g.drawString(v.getId(), p.x+6, p.y+6);
 			
 			//Search for worst alert
 			Alert worstAlert = null;
 			for (Alert a : _Alerts) {
 				if (a.contains(v)) {
 					if (worstAlert == null || a.getType() == AlertType.HIGHRISK) {
 						worstAlert = a;
 					}
 				}
 			}
 			
 			// Draws circles around the ships; adds color if there is a high-risk or low-risk alert
 			if (worstAlert != null && worstAlert.getType() == AlertType.HIGHRISK)
 				g2.setColor(Color.red);
 			
 			Point ul, lr;
 			ul = place(new Coord(c.x() - (HIGH_RISK/2), c.y() - (HIGH_RISK/2)), b, RANGE);
 			lr = place(new Coord(c.x() + (HIGH_RISK/2), c.y() + (HIGH_RISK/2)), b, RANGE);
 			g.drawOval(ul.x, ul.y, lr.x - ul.x, lr.y - ul.y);
 			g2.setColor(defaultColor);
 			
 			if (worstAlert != null && worstAlert.getType() == AlertType.LOWRISK)
 				g2.setColor(Color.yellow);
 
 			ul = place(new Coord(c.x() - (LOW_RISK/2), c.y() - (LOW_RISK/2)), b, RANGE);
 			lr = place(new Coord(c.x() + (LOW_RISK/2), c.y() + (LOW_RISK/2)), b, RANGE);
 			g.drawOval(ul.x, ul.y, lr.x - ul.x, lr.y - ul.y);
 		}
 	}
 
 }

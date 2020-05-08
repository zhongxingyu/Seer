 package edu.berkeley.gcweb.gui.gamescubeman.ThreeD;
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 
 import javax.swing.JComponent;
 import javax.swing.Timer;
 
 @SuppressWarnings("serial")
 public class Canvas3D extends JComponent implements KeyListener, ActionListener, MouseListener, MouseMotionListener, FocusListener {
 	private static final double VIEWPORT = 1;
 	private int SCALE = 400;
 	private Timer t;
 	public Canvas3D() {
 		setFocusable(true);
 		setOpaque(true);
 		addKeyListener(this);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addFocusListener(this);
 		t = new Timer(10, this);
 		t.start();
 	}
 	
 	public int getScale() {
 		return SCALE;
 	}
 	public void setScale(int scale) {
 		SCALE = scale;
 		dirty = true;
 	}
 	
 	public void focusGained(FocusEvent e) {
 		dirty = true;
 	}
 	public void focusLost(FocusEvent e) {
 		dirty = true;
 	}
 	public void actionPerformed(ActionEvent e) {
 		int x = 0, y = 0;
 		if(keys.contains(KeyEvent.VK_LEFT))
 			y--;
 		if(keys.contains(KeyEvent.VK_RIGHT))
 			y++;
 		if(keys.contains(KeyEvent.VK_UP))
 			x++;
 		if(keys.contains(KeyEvent.VK_DOWN))
 			x--;
 		if(dirty || !rotationRate.isIdentity() || x != 0 || y != 0) {
 			if(!dragging) {
 				RotationMatrix temp = rotationRate.multiply(new RotationMatrix(0, x).multiply(new RotationMatrix(1, y)));
 				for(Shape3D s : shapes)
 					s.rotate(temp);
 			}
 			for(Shape3D s : shapes) {
 				polys = s.getPolygons();
 				Collections.sort(polys);
 				polyProjection = new ArrayList<Shape>();
 				for(Polygon3D poly : polys) {
					if(!poly.isVisible()) continue;
					Shape proj = poly.projectXYPlane(VIEWPORT, SCALE);
					polyProjection.add(proj);
 				}
 			}
 			if(!dragging || colorEditing)
 				refreshSelectedPolygon();
 			repaint();
 			dirty = false;
 		}
 	}
 	
 	private boolean dirty = false;
 	public void fireCanvasChange() {
 		dirty = true;
 	}
 
 	private RotationMatrix rotationRate = new RotationMatrix();
 	private Point old;
 	private long lastDrag;
 	private HashSet<Integer> keys = new HashSet<Integer>();
 	public void keyPressed(KeyEvent e) {
 		keys.add(e.getKeyCode());
 	}
 	public void keyReleased(KeyEvent e) {
 		keys.remove(e.getKeyCode());
 	}
 	public void keyTyped(KeyEvent e) {}
 	public void mouseClicked(MouseEvent e) {
 		requestFocusInWindow();
 		if(colorEditing) {
 			Polygon3D selected = getSelectedPolygon();
 			if(selected != null) {
 				firePolyClicked(selected.getOGPoly());
 				dirty = true;
 			}
 		}
 	}
 	
 	public interface PolyClickListener {
 		public void polyClicked(Polygon3D clicked);
 	}
 	private ArrayList<PolyClickListener> polyListeners = new ArrayList<PolyClickListener>();
 	public void addPolyClickListener(PolyClickListener l) {
 		polyListeners.add(l);
 	}
 	private void firePolyClicked(Polygon3D selected) {
 		for(PolyClickListener l : polyListeners)
 			l.polyClicked(selected);
 	}
 	
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	public void mousePressed(MouseEvent e) {
 		rotationRate = new RotationMatrix();
 		dirty = true;
 	}
 	public void mouseReleased(MouseEvent e) {
 		if(System.currentTimeMillis() - lastDrag > 100)
 			mousePressed(null);
 		dragging = false;
 	}
 	private boolean dragging = false;
 	public void mouseDragged(MouseEvent e) {
 		dragging = true;
 		lastDrag = System.currentTimeMillis();
 		double deltaX = e.getX() - old.x;
 		double deltaY = e.getY() - old.y;
 		rotationRate = new RotationMatrix(1, deltaX).multiply(new RotationMatrix(0, -deltaY));
 		old = e.getPoint();
 
 		RotationMatrix m = new RotationMatrix();
 		m = m.multiply(new RotationMatrix(1, deltaX).multiply(new RotationMatrix(0, -deltaY)));
 		for(Shape3D s : shapes)
 			s.rotate(m);
 		dirty = true;
 	}
 	public void mouseMoved(MouseEvent e) {
 		old = e.getPoint();
 		if(colorEditing)
 			refreshSelectedPolygon();
 	}
 	private boolean colorEditing = false;
 	public void setColorEditing(boolean editing) {
 		this.colorEditing = editing;
 		dirty = true;
 	}
 	private void refreshSelectedPolygon() {
 		if(!colorEditing) {
 			for(Polygon3D rendered : polys) {
 				rendered.setOpacity(1f);
 				rendered.setBorderColor(Color.BLACK);
 				rendered.getOGPoly().setOpacity(1f);
 				rendered.getOGPoly().setBorderColor(Color.BLACK);
 			}
 		} else {
 			for(Polygon3D rendered : polys) {
 				rendered.setOpacity(.8f);
 				rendered.setBorderColor(null);
 				rendered.getOGPoly().setOpacity(.8f);
 				rendered.getOGPoly().setBorderColor(null);
 			}
 			Polygon3D poly = getSelectedPolygon();
 			if(poly != null) {
 				poly.setOpacity(1f);
 				poly.setBorderColor(Color.BLACK);
 				poly.getOGPoly().setOpacity(1f);
 				poly.getOGPoly().setBorderColor(Color.BLACK);
 			}
 		}
 		dirty = true;
 	}
 	private Polygon3D getSelectedPolygon() {
 		Point p = getMousePosition();
 		if(p == null)	return null;
 		double x = -(p.x - getWidth() / 2.);
 		double y = -(p.y - getHeight() / 2.);
 		int match = -1;
 		for(int i = 0; i < polyProjection.size(); i++)
 			if(polyProjection.get(i).contains(x, y))
 				match = i;
 		if(match == -1)
 			return null;
 		return polys.get(match);
 	}
 
 	//TODO - who are we kidding? this was coded w/ exactly one shape in mind
 	//it'll take a bit of work to get this to work for n shape3ds
 	private ArrayList<Shape3D> shapes = new ArrayList<Shape3D>();
 	public void addShape3D(Shape3D s) {
 		s.setCanvas(this);
 		shapes.add(s);
 	}
 
 	private boolean antialiasing = true;
 	public void setAntialiasing(boolean aa) {
 		 antialiasing = aa;
 		 dirty = true;
 	}
 	public boolean isAntialiasing() {
 		return antialiasing;
 	}
 	
 	private ArrayList<Polygon3D> polys;
 	private ArrayList<Shape> polyProjection;
 	protected void paintComponent(Graphics g) {
 		if(isOpaque()) {
 			g.setColor(getBackground());
 			g.fillRect(0, 0, getWidth(), getHeight());
 		}
 		Graphics2D g2d = (Graphics2D) g;
 		if(!isFocusOwner()) {
 			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
 			g2d.setComposite(ac);
 		}
 		
 		Stroke oldStroke = g2d.getStroke();
 		RenderingHints oldHints = g2d.getRenderingHints();
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
 		g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 		
 		AffineTransform toCartesian = new AffineTransform();
 		toCartesian.translate(getWidth() / 2, getHeight() / 2);
 		toCartesian.rotate(Math.toRadians(180));
 		g2d.transform(toCartesian);
 
 		g2d.setColor(Color.BLUE); //draw the axis
 		g2d.drawLine(0, -getHeight() / 2, 0, getHeight() / 2);
 		g2d.drawLine(-getWidth() / 2, 0, getWidth() / 2, 0);
 		
 		g2d.setColor(Color.BLACK);
 		
 		//TODO - deal with z ordering! break everything into triangles?
 		for(int i = 0; i < polys.size(); i++) {
 			Polygon3D poly = polys.get(i);
 			Shape proj = polyProjection.get(i);
 			if(poly.getFillColor() != null) {
 				g2d.setColor(poly.getFillColor());
 				Composite oldComposite = g2d.getComposite();
 				g2d.setComposite(poly.getOpacity());
 				g2d.fill(proj);
 				g2d.setComposite(oldComposite);
 			}
 			if(poly.getBorderColor() != null) {
 				g2d.setColor(poly.getBorderColor());
 				g2d.draw(proj);
 			}
 		}
 
 		g2d.setRenderingHints(oldHints);
 		g2d.setStroke(oldStroke);
 	}
 }

 package uk.ac.york.jbb.jbreadboard;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.event.MouseInputAdapter;
 import javax.swing.event.MouseInputListener;
 
 public class LED extends JComponent implements Device, CircuitSelection {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public static final int red = 0;
 	public static final int yellow = 1;
 	public static final int green = 2;
 	private static final String[] gfxfiles = { "rled.gif", "yled.gif", "gled.gif" };
 
 	private static int defaultcolor = 0;
 	private Circuit circuit;
 	private int color;
 	private ImageIcon image;
 	private boolean lit = false;
 	private Node[] pins = new Node[2];
 
 	private MouseInputListener mia = new MouseInputAdapter() {
 		int XFudge = 0;
 		int YFudge = 0;
 		boolean moved = false;
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			LED.this.circuit.select((LED) e.getComponent());
 			this.XFudge = e.getX();
 			this.YFudge = e.getY();
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			LED led = (LED) e.getComponent();
 			BreadBoard origb = (BreadBoard) led.getParent();
 
 			Circuit pane = (Circuit) origb.getParent();
 
 			if (pane.getJBreadBoard().getMode().equals("move")) {
 				origb.remove(led);
 				if (LED.this.pins[0] != null) {
 					LED.this.pins[0].setType("NC");
 					LED.this.pins[0].setDevice(null);
 				}
 				if (LED.this.pins[1] != null) {
 					LED.this.pins[1].setType("NC");
 					LED.this.pins[1].setType(null);
 				}
 
 				int X = e.getX() + led.getX() + origb.getX() - this.XFudge;
 				int Y = e.getY() + led.getY() + origb.getY() - this.YFudge;
 
 				Board board = pane.getBoardAt(X, Y);
 				BreadBoard newb;
 				if (board != null && board instanceof BreadBoard) {
 					newb = (BreadBoard) board;
 				} else {
 					newb = origb;
 				}
 
 				int X2 = X - newb.getX();
 				int Y2 = Y - newb.getY();
 
 				X2 -= (X2 - 4) % 8;
 				if (X2 < 76) {
 					X2 = 76;
 				}
 
 				if (Y2 < 46) {
 					Y2 = 16;
 				} else if (Y2 < 102) {
 					Y2 = 72;
 				} else {
 					Y2 = 128;
 				}
 				if (newb.isHole(X2, Y2) && newb.isHole(X2, Y2 + 24)) {
 					Rectangle dest = new Rectangle(X2, Y2, LED.this.getWidth(), LED.this.getHeight());
 					Component[] components = newb.getComponents();
 					boolean clear = true;
 
 					for (int i = 0; clear && i < components.length; i++) {
 						clear = !dest.intersects(components[i].getBounds());
 					}
 					if (clear) {
 						led.setLocation(X2, Y2);
 					} else {
 						newb = origb;
 					}
 				} else {
 					newb = origb;
 				}
 
				newb.add((Component) led);
 				led.updateConnections();
 
 				origb.repaint();
 				newb.repaint();
 			}
 		}
 	};
 
 	public static void setDefaultColor(int d) {
 		if (d == 0 || d == 1 || d == 2) {
 			LED.defaultcolor = d;
 		}
 	}
 
 	public boolean getState() {
 		return this.lit;
 	}
 
 	@Override
 	public void select() {
 		this.repaint();
 	}
 
 	@Override
 	public void deselect() {
 		this.repaint();
 	}
 
 	@Override
 	public void delete() {
 		Container cont = this.getParent();
 		cont.remove(this);
 		cont.repaint();
 		for (int i = 0; i < 2; i++) {
 			if (this.pins[i] != null) {
 				this.pins[i].setDevice(null);
 				this.pins[i].setType("NC");
 			}
 		}
 	}
 
 	@Override
 	public String getInfo() {
 		String lit;
 		if (this.getState()) {
 			lit = "ON";
 		} else {
 			lit = "OFF";
 		}
 		return "Component Name: \nLED\n\nStatus:\n" + lit + "\n\n" + "JBreadBoard library name: \n" + this.getClass().getName().substring(12) + ".class";
 	}
 
 	@Override
 	public String getDiagram() {
 		return "uk/ac/york/jbb/images/leddia.gif";
 	}
 
 	@Override
 	public void reset() {
 		this.lit = false;
 		this.repaint();
 	}
 
 	@Override
 	public void simulate() {
 		if (!this.pins[0].getType().equals("NC") && !this.pins[0].getType().equals("IN") && !this.pins[1].getType().equals("NC") && !this.pins[1].getType().equals("IN")) {
 			if (this.pins[0].getPotential().toTTL().equals("HIGH") && this.pins[1].getPotential().toTTL().equals("LOW")) {
 				this.lit = true;
 			} else {
 				this.lit = false;
 			}
 		} else {
 			this.lit = false;
 			this.repaint();
 		}
 		this.repaint();
 		if (this.circuit.selected == this) {
 			this.circuit.select(this);
 		}
 	}
 
 	@Override
 	public void updateConnections() {
 		int x = this.getX();
 		int y = this.getY();
 		for (int i = 0; i < 2; i++) {
 			if (this.pins[i] != null) {
 				this.pins[i].setDevice(null);
 				this.pins[i].setType(null);
 			}
 			Board b = (Board) this.getParent();
 			this.pins[i] = b.getNodeAt(x, y);
 			this.pins[i].setDevice(this);
 			this.pins[i].setType("IN");
 			y += 24;
 		}
 	}
 
 	public LED(Circuit c) {
 		this(c, LED.defaultcolor);
 	}
 
 	public LED(Circuit c, int colint) {
 		this.circuit = c;
 		if (colint == 0 || colint == 1 || colint == 2) {
 			this.color = colint;
 		}
 		this.image = c.getJBreadBoard().getImageIcon("uk/ac/york/jbb/images/" + LED.gfxfiles[this.color]);
 		this.setBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());
 		this.addMouseListener(this.mia);
 		this.addMouseMotionListener(this.mia);
 	}
 
 	@Override
 	public String toString() {
 		return new String("Device " + this.getClass().getName() + " " + this.getX() + " " + this.getY() + " " + this.color);
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		int x = 0;
 		if (this.lit) {
 			x += 8;
 		}
 		if (this.circuit.selected == this) {
 			x += 16;
 		}
 
 		this.image.paintIcon(this, g, -x, 0);
 	}
 
 	@Override
 	public boolean isOpaque() {
 		return false;
 	}
 
 	@Override
 	public int getWidth() {
 		return 8;
 	}
 
 	@Override
 	public int getHeight() {
 		return 32;
 	}
 
 	@Override
 	public Rectangle getBounds() {
 		return new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
 	}
 }

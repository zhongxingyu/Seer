 package uk.ac.york.jbb.jbreadboard;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JComponent;
 import javax.swing.event.MouseInputAdapter;
 import javax.swing.event.MouseInputListener;
 
 public class Dipswitch extends JComponent implements Device, CircuitSelection {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private int pins = 3;
 	private boolean[] states;
 	private Node[] topnode;
 	private Node[] bottomnode;
 	private Potential[] connections;
 	private Circuit circuit;
 	MouseInputListener mia = new MouseInputAdapter() {
 		int XFudge = 0;
 		int YFudge = 0;
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			Dipswitch dip = (Dipswitch) e.getComponent();
 			Dipswitch.this.circuit.select(dip);
 			this.XFudge = e.getX();
 			this.YFudge = e.getY();
 
 			if (e.getY() > 7 && e.getY() < 23 && e.getX() % 8 > 1 && e.getX() % 8 < 6) {
 				if (e.getY() < 16) {
 					dip.setState(e.getX() / 8, true);
 				} else {
 					dip.setState(e.getX() / 8, false);
 				}
 				Dipswitch.this.repaint();
 			}
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			Dipswitch dip = (Dipswitch) e.getComponent();
 			BreadBoard origb = (BreadBoard) dip.getParent();
 
 			Circuit pane = (Circuit) origb.getParent();
 
 			if (pane.getJBreadBoard().getMode().equals("move")) {
 				int X = e.getX() + dip.getX() + origb.getX() - this.XFudge;
 				int Y = e.getY() + dip.getY() + origb.getY() - this.YFudge;
 
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
 				Component[] components = newb.getComponents();
 				boolean isspace = false;
 
 				while (!isspace && X2 < 452 - dip.getWidth()) {
 					Rectangle dest = new Rectangle(X2, Y2, dip.getWidth(), dip.getHeight());
 					isspace = true;
 
 					for (int i = 0; isspace && i < components.length; i++) {
 						if (components[i] != dip) {
 							isspace = !dest.intersects(components[i].getBounds());
 						}
 					}
 
 					for (int i = 0; isspace && i < dip.getPins(); i++) {
 						isspace = isspace && newb.isHole(X2 + i * 8, Y2);
 						isspace = isspace && newb.isHole(X2 + i * 8, Y2 + dip.getHeight() - 8);
 					}
 					X2 += 8;
 				}
 
 				X2 -= 8;
 
 				for (int dx = e.getX(); !isspace && dx > 0; dx -= 8) {
 					X2 -= 8;
 					Rectangle dest = new Rectangle(X2, Y2, dip.getWidth(), dip.getHeight());
 					isspace = true;
 
 					for (int i = 0; isspace && i < components.length; i++) {
 						if (components[i] != dip) {
 							isspace = !dest.intersects(components[i].getBounds());
 						}
 					}
 
 					for (int i = 0; isspace && i < dip.getPins(); i++) {
 						isspace = isspace && newb.isHole(X2 + i * 8, Y2);
 						isspace = isspace && newb.isHole(X2 + i * 8, Y2 + dip.getHeight() - 8);
 					}
 
 				}
 
 				if (isspace) {
 					int oldx = dip.getX();
 					int oldy = dip.getY();
 
 					dip.setLocation(X2, Y2);
 
 					if (origb != newb || oldx != X2 || oldy != Y2) {
 						Dipswitch.this.updateConnections();
 					}
 				} else {
 					newb = origb;
 				}
 
				newb.add(dip);
 				newb.repaint();
 				origb.repaint();
 			}
 		}
 	};
 
 	public int getPins() {
 		return this.pins;
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
 
 		for (Potential connection : this.connections) {
 			if (connection != null) {
 				connection.delete();
 			}
 		}
 	}
 
 	@Override
 	public String getInfo() {
 		return "Component Name: \nDip Switches\n\nJBreadBoard library name: \n" + this.getClass().getName().substring(12) + ".class";
 	}
 
 	@Override
 	public String getDiagram() {
 		return "uk/ac/york/jbb/images/dipdia.gif";
 	}
 
 	@Override
 	public void simulate() {}
 
 	@Override
 	public void reset() {}
 
 	@Override
 	public void updateConnections() {
 		Board board = (Board) this.getParent();
 		for (int i = 0; i < this.pins; i++) {
 			this.setState(i, false);
 			this.topnode[i] = board.getNodeAt(this.getX() + i * 8, this.getY());
 			this.bottomnode[i] = board.getNodeAt(this.getX() + i * 8, this.getY() + this.getHeight() - 8);
 		}
 	}
 
 	public void setState(int pin, boolean state) {
 		if (pin >= 0 && pin <= this.pins) {
 			this.states[pin] = state;
 			if (!state) {
 				if (this.connections[pin] != null) {
 					this.connections[pin].delete();
 					this.connections[pin] = null;
 					this.circuit.getSimulation().addEvent(this.topnode[pin], 0);
 					this.circuit.getSimulation().addEvent(this.bottomnode[pin], 0);
 				}
 			} else if (this.connections[pin] == null) {
 				if (!NodeType.conflicts(this.topnode[pin].getType(), this.bottomnode[pin].getType())) {
 					this.connections[pin] = new Potential(this.topnode[pin], this.bottomnode[pin]);
 					this.circuit.getSimulation().addEvent(this.topnode[pin], 0);
 				} else {
 					this.setState(pin, false);
 				}
 			}
 		}
 	}
 
 	public Dipswitch(Circuit c, int p) {
 		this.circuit = c;
 		this.pins = p;
 		if (this.pins < 1) {
 			this.pins = 1;
 		}
 		this.states = new boolean[this.pins];
 		this.topnode = new Node[this.pins];
 		this.bottomnode = new Node[this.pins];
 		this.connections = new Potential[this.pins];
 
 		for (int i = 0; i < this.pins; i++) {
 			this.states[i] = false;
 		}
 
 		this.addMouseListener(this.mia);
 		this.addMouseMotionListener(this.mia);
 		this.setBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());
 	}
 
 	@Override
 	public String toString() {
 		return new String("Device " + this.getClass().getName() + " " + this.getX() + " " + this.getY() + " " + this.getPins());
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		Color dipcolor = new Color(33, 49, 255);
 		Color pincolor = Color.lightGray;
 		Color slidecolor = new Color(0, 0, 120);
 		Color switchcolor = Color.lightGray;
 
 		if (this.circuit.selected == this) {
 			dipcolor = Color.yellow;
 			pincolor = Color.orange;
 			slidecolor = Color.orange;
 			switchcolor = Color.white;
 		}
 
 		g.setColor(dipcolor);
 		g.fillRect(0, 4, this.getWidth() - 1, this.getHeight() - 9);
 
 		g.setColor(pincolor);
 		for (int i = 0; i < this.pins; i++) {
 			g.fillRect(i * 8 + 1, 2, 5, 2);
 			g.fillRect(i * 8 + 1, 27, 5, 2);
 		}
 
 		g.setColor(slidecolor);
 		for (int i = 0; i < this.pins; i++) {
 			g.fillRect(i * 8 + 1, 8, 5, 14);
 		}
 
 		g.setColor(switchcolor);
 		for (int i = 0; i < this.pins; i++) {
 			if (this.states[i]) {
 				g.fillRect(i * 8 + 3, 9, 2, 6);
 			} else {
 				g.fillRect(i * 8 + 3, 15, 2, 6);
 			}
 		}
 	}
 
 	@Override
 	public boolean isOpaque() {
 		return false;
 	}
 
 	@Override
 	public int getWidth() {
 		return this.pins * 8;
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

 package org.computer.knauss.reqtDiscussion.ui.visualization.sna;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.geom.Point2D;
 
 import javax.swing.JPanel;
 
 import de.luh.pi.geometry.Bounds;
 import de.luh.pi.geometry.IBounds;
 import de.luh.se.layout.forcebased.ForceBasedLayouter;
 import de.luh.se.layout.forcebased.forces.BorderRepulsionForce;
 import de.luh.se.layout.forcebased.forces.CoulumbNodeRepulsionForce;
 import de.luh.se.layout.forcebased.forces.HookeConnectionForce;
 
 public class NetworkPanel extends JPanel {
 
 	private static final Bounds BOUNDS = new Bounds(0, 0, 2000, 2000);
 	private static final long serialVersionUID = 1L;
 	private SocialNetwork network;
 	private ForceBasedLayouter<SocialNetwork, Connection<Double, Node>, Node> layouter;
 	private SNAGraphProvider graphProvider;
 	private double zoomFactor = 1.0d;
 
 	public NetworkPanel() {
 		this.graphProvider = new SNAGraphProvider();
 		setPreferredSize(new Dimension((int) BOUNDS.getWidth(),
 				(int) BOUNDS.getHeight()));
 	}
 
 	public SocialNetwork getNetwork() {
 		return network;
 	}
 
 	public void setNetwork(SocialNetwork network) {
 		this.network = network;
		this.graphProvider.printMatrix(network);
 
 		getLayouter().initialize(this.network, this.graphProvider, BOUNDS, 200);
 		getLayouter().layout(1);
 		repaint();
 	}
 
 	public ForceBasedLayouter<SocialNetwork, Connection<Double, Node>, Node> getLayouter() {
 		if (this.layouter == null) {
 			this.layouter = new ForceBasedLayouter<SocialNetwork, Connection<Double, Node>, Node>();
 			this.layouter.setDamping(0.6);
 
 			this.layouter.addForce(new HookeConnectionForce());
 			this.layouter.addForce(new CoulumbNodeRepulsionForce());
 			IBounds b = new Bounds();
 			b.setX(BOUNDS.getX() - 10);
 			b.setY(BOUNDS.getY() - 10);
 			b.setWidth(BOUNDS.getWidth() + 10);
 			b.setHeight(BOUNDS.getHeight() + 10);
 			// System.out.println(b.getX() + " " + b.getY() + " " + b.getWidth()
 			// + " " + b.getHeight());
 			this.layouter.addForce(new BorderRepulsionForce(b));
 
 		}
 		return this.layouter;
 	}
 
 	public void setMinWeight(double minWeight) {
 		this.graphProvider.setMinWeight(minWeight);
 	}
 
 	public void setZoomFactor(double d) {
 		this.zoomFactor = d;
 		repaint();
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		((Graphics2D) g).scale(zoomFactor, zoomFactor);
 		g.setColor(Color.white);
 		g.fillRect(0, 0, (int) BOUNDS.getWidth() * 2,
 				(int) BOUNDS.getHeight() * 2);
 		g.setColor(Color.BLACK);
 		for (Node a : this.network.getActors()) {
 			g.setColor(Color.GRAY);
 
 			Point2D.Double pos = this.graphProvider.getPosition(a);
 			for (Connection<Double, Node> c : this.graphProvider.getSuccessors(
 					this.network, a)) {
 				Point2D.Double pos2 = this.graphProvider.getPosition(c
 						.getTarget());
 
 				Stroke otherStroke = ((Graphics2D) g).getStroke();
 				((Graphics2D) g).setStroke(new BasicStroke(c.getWeight()
 						.floatValue()));
 				g.drawLine((int) pos.x, (int) pos.y, (int) pos2.x, (int) pos2.y);
 				((Graphics2D) g).setStroke(otherStroke);
 			}
 		}
 		for (Node a : this.network.getActors())
 			paintNode(a, this.graphProvider.getPosition(a), g);
 	}
 
 	private void paintNode(Node a, Point2D.Double pos, Graphics g) {
 		// int width = 8;
 		// int height = a.getClarification() + a.getCoordination();
 
 		Slice[] slices = new Slice[] {
 				new Slice(a.getClarification(), Color.RED),
 				new Slice(a.getCoordination(), Color.BLUE) };
 
 		int d = 8 + a.getClarification() + a.getCoordination();
 
 		paintPie((Graphics2D) g, new Rectangle((int) pos.x - d / 2, (int) pos.y
 				- d / 2, d, d), slices);
 		// g.setColor(Color.BLUE);
 		// g.fillRect((int) (pos.x - width / 2), (int) (pos.y - height / 2),
 		// width, a.getCoordination());
 		// g.setColor(Color.RED);
 		// g.fillRect((int) (pos.x - width / 2),
 		// (int) (pos.y - height / 2 + a.getCoordination()), width,
 		// a.getClarification());
 
 		g.setColor(Color.BLACK);
		g.drawString(a.getLabel(), (int) pos.x + d/2, (int) pos.y);
 	}
 
 	/**
 	 * In parts from http://www.tutorialspoint.com/javaexamples/gui_piechart.htm
 	 * @param g
 	 * @param area
 	 * @param slices
 	 */
 	void paintPie(Graphics2D g, Rectangle area, Slice[] slices) {
 		double total = 0.0D;
 		for (int i = 0; i < slices.length; i++) {
 			total += slices[i].value;
 		}
 		double curValue = 0.0D;
 		int startAngle = 0;
 		for (int i = 0; i < slices.length; i++) {
 			startAngle = (int) (curValue * 360 / total);
 			int arcAngle = (int) (slices[i].value * 360 / total);
 			g.setColor(slices[i].color);
 			g.fillArc(area.x, area.y, area.width, area.height, startAngle,
 					arcAngle);
 			curValue += slices[i].value;
 		}
 	}
 
 	private class Slice {
 		double value;
 		Color color;
 
 		public Slice(double value, Color color) {
 			this.value = value;
 			this.color = color;
 		}
 	}
 }

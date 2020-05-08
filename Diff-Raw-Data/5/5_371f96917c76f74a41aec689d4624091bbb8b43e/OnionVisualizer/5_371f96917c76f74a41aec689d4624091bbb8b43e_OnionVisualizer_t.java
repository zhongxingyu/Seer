 package nature.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import nature.BitString;
 
 public class OnionVisualizer extends JPanel {
 	private static final int MARKER_SIZE = 6;
 	private static final Color MARKER_COLOR = new Color(255, 0, 0);
 
 	private List<Point2D.Double> points = new ArrayList<Point2D.Double>();
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		Graphics2D g2 = (Graphics2D) g;
 		Dimension size = getSize();
 
 		g2.drawOval(0, 0, size.width - 1, size.height - 1);
 
 		for (int i = 1; i < points.size(); i++) {
 			Point2D.Double p1 = points.get(i - 1);
 			Point2D.Double p2 = points.get(i);
 
 			int x1 = (int) (p1.x * size.width);
 			int y1 = (int) (p1.y * size.height);
 			int x2 = (int) (p2.x * size.width);
 			int y2 = (int) (p2.y * size.height);
 
 			g2.drawLine(x1, y1, x2, y2);
 		}
 
 		if (points.size() > 0) {
 			Point2D.Double p = points.get(points.size() - 1);
 
 			int x = (int) (p.x * size.width);
 			int y = (int) (p.y * size.height);
 
 			g2.setColor(MARKER_COLOR);
 			g2.fillOval(x - MARKER_SIZE/2, y - MARKER_SIZE/2, MARKER_SIZE, MARKER_SIZE);
 		}
 	}
 
 	public void addPoint(BitString bitString) {
 		double x = (double) bitString.numberOfOnes() / bitString.length();
		double h = Math.cos(Math.PI / 2.0 * Math.abs(x * 2 - 1));
		double y = (1 - h) / 2.0 + h * bitString.positionOfOnes();
 
 		Point2D.Double point = new Point2D.Double(x, y);
 		points.add(point);
 		repaint();
 	}
 
 	public void clearPoints() {
 		points.clear();
 		repaint();
 	}
 }

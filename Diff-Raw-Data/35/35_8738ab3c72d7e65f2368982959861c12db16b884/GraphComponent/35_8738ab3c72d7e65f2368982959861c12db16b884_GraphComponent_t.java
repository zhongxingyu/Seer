 package org.haldean.chopper.server;
 
 import java.util.LinkedList;
 import java.util.Iterator;
 import javax.swing.*;
 import java.awt.*;
 
 /**
  *  A class which draws a graph based on a series of given result sets
  */
 public class GraphComponent extends JComponent {
     private LinkedList<Double> series;
     private double max = 1;
     private double min = -1;
     private final int sampleCount = 300;
 
     private final Color background = new Color(28, 25, 20);
     private final Color line = Color.white;
     private final Color axes = Color.darkGray;
 
     public GraphComponent() {
 	super();
 	series = new LinkedList<Double>();
     }
 
     public void setMax(double _max) {
 	max = _max;
 	repaint();
     }
 
     public void setMin(double _min) {
 	min = _min;
 	repaint();
     }
 
     /**
      *  Adds a result set to the graph as its own series. Note that results added
      *  MUST BE SORTED in ascending order.
      *  @param r The result set to add 
      */
     public void addPoint(double p) {
 	synchronized (series) {
 	    series.add(new Double(p));
 	    if (p > max)
 		max = p;
 	    if (p < min)
 		min = p;
 	    while (series.size() > sampleCount)
 		series.removeFirst();
 	}
 	repaint();
     }
 
     public int pointToY(double p) {
 	return (int) (((max - p) / (max - min)) * getSize().getHeight());
     }
 
     public int pointToX(int x) {
 	return (int) (((float) x / (float) sampleCount) * getSize().getWidth());
     }
 
     /**
      *  Paints the graph onto the provided graphics object
      *  @param g The graphics object to paint onto
      */
     public void paintComponent(Graphics g) {
 	Graphics2D g2 = (Graphics2D)g;
 
 	/* Background */
 	g2.setColor(background);
 	g2.fillRect(0, 0, (int) getSize().getWidth(), (int) getSize().getHeight());
 
 	/* Axes */
 	g2.setColor(axes);
 	/* Vertical axis */
 	g2.drawLine(0, 0, 0, (int) getSize().getHeight());
 	/* Horizontal axis */
 	g2.drawLine(0, pointToY(0), (int) getSize().getWidth(), pointToY(0));
 
 	g2.setColor(line);
 	synchronized (series) {
	    if (series.size() > 0) {
		double lastY = series.get(0);
		for (int i=1; i<series.size(); i++) {
		    double y = series.get(i);
		    g2.drawLine(pointToX(i-1), pointToY(lastY), pointToX(i), pointToY(y));
		    lastY = y;
		}
 	    }
 	}
     }
 
     public static void main(String args[]) {
 	JFrame f = new JFrame();
 	final GraphComponent gc = new GraphComponent();
 	gc.setPreferredSize(new Dimension(700, 400));
 	f.add(gc);
 	f.pack();
 	f.setVisible(true);
 	
 	try {
 	    double j=0;
 	    for (int i=0; i<=360; i++) {
 		j += 0.1;
 		gc.addPoint(j * Math.sin(Math.toRadians(i)));
 		Thread.sleep(20);
 		if (i==360)
 		    i = 0;
 	    }
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 }

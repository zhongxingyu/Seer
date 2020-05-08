 package org.haldean.chopper.server;
 
 import java.awt.*;
 import javax.swing.*;
 
 public class AccelerationComponent extends JPanel {
     private GraphComponent xAccel;
     private GraphComponent yAccel;
     private GraphComponent zAccel;
 
     private JPanel statsPanel;
     private final JLabel xLabel;
     private final JLabel yLabel;
     private final JLabel zLabel;
     private final JLabel avgLabel;
 
     private LinkedList<Double> points;
     private final int averagePointCount = 20;
 
     private final Color background = new Color(28, 25, 20);
 
     public AccelerationComponent() {
 	super(new GridLayout(2,2));
 
 	points = new LinkedList<Double>();
 
 	xAccel = new GraphComponent();
 	yAccel = new GraphComponent();
 	zAccel = new GraphComponent();
 
 	xLabel = new JLabel();
 	yLabel = new JLabel();
 	zLabel = new JLabel();
 	avgLabel = new JLabel();
 
 	statsPanel = new JPanel(new GridLayout(4,1));
 	statsPanel.add(xLabel);
 	statsPanel.add(yLabel);
 	statsPanel.add(zLabel);
 
 	add(xAccel);
 	add(yAccel);
 	add(zAccel);
 	add(statsPanel);
     }
 
     private double average() {
	double sum;
 	int i;
 	for (i=0; i<points.size(); i++)
 	    sum += points.get(i);
 
 	return sum / i;
     }
 
     public void setAcceleration(double x, double y, double z) {
	double mag = Math.sqrt(Math.pow(x, 2),
			       Math.pow(y, 2),
 			       Math.pow(z, 2));
 	points.add(mag);
 	while (points.size() > averagePointCount)
 	    points.removeFirst();
 
 	xAccel.addPoint(x);
 	yAccel.addPoint(y);
 	zAccel.addPoint(z);
 
 	xLabel.setText("a_X: " + x);
 	yLabel.setText("a_Y: " + y);
 	zLabel.setText("a_Z: " + z);
 	avgLabel.setText("a_avg " + average());
 
 	repaint();
     }
 }

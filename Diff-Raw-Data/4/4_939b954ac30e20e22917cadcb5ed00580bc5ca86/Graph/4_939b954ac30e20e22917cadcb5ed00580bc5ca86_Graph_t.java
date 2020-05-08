 package jevolution.ui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import javax.swing.*;
 import jevolution.stats.Snapshot;
 import jevolution.stats.Stat;
 import net.miginfocom.swing.MigLayout;
 
 /**
  *
  * @author kuhlmancer
  */
 public class Graph extends JPanel {
 	private final static int NUM_INTERVALS = 20;
 
 	JComboBox comboBox;
 	GraphInnerPanel graph;
 
 	Graph(Iterable<Stat> stats) {
 		super(new MigLayout("wrap, insets 0", "[grow]", "[][grow]"));
 
 		this.comboBox = new JComboBox();
 		for (Stat st: stats) {
 			comboBox.addItem(st);
 		}
 
 		Stat selectedStat = (Stat)comboBox.getSelectedItem();
 
 		graph = new GraphInnerPanel(selectedStat);
 
 		comboBox.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Stat selectedStat = (Stat)e.getItem();
 
 					graph.setStat(selectedStat);
 				}
 			}
 		});
 
 		this.add(comboBox);
 		this.add(graph, "grow");
 	}
 
 	private class GraphInnerPanel extends JPanel {
 		private Stat stat;
 
 		GraphInnerPanel(Stat stat) {
 			this.stat = stat;
 		}
 
 		void setStat(Stat stat) {
 			this.stat = stat;
 		}
 
 		@Override
 		protected void paintComponent(Graphics gr) {
 			super.paintComponent(gr);
 			Graphics2D g = (Graphics2D)gr;
 
 			double largestYValue = stat.getLargestValue();
 			double smallestYValue = stat.getSmallestValue();
 			double yDifference = largestYValue - smallestYValue;
 			double yInterval = yDifference / NUM_INTERVALS;
 
 			long largestXValue = stat.getLatestTime();
 			long smallestXValue = stat.getEarliestTime();
 			double xDifference = largestXValue - smallestXValue;
 			double xInterval = xDifference / NUM_INTERVALS;
 
 			for (Snapshot snapshot: stat.getSnapshots()) {
 				long time = snapshot.getTime();
 
 				// standard deviation
 				g.setColor(Color.green);
 				drawStandardDeviation(g, smallestXValue, largestXValue, smallestYValue, largestYValue, time, snapshot.getAverage(), snapshot.getStandardDeviation());
 				
 				// minimum
 				g.setColor(Color.red);
 				drawPoint(g, smallestXValue, largestXValue, smallestYValue, largestYValue, time, snapshot.getMinimum());
 
 				// average
 				g.setColor(Color.black);
 				drawPoint(g, smallestXValue, largestXValue, smallestYValue, largestYValue, time, snapshot.getAverage());
 
 				// maximum
 				g.setColor(Color.blue);
 				drawPoint(g, smallestXValue, largestXValue, smallestYValue, largestYValue, time, snapshot.getMaximum());
 			}
 		}
 
 		private void drawPoint(Graphics2D g, double minX, double maxX, double minY, double maxY, double xValue, double yValue) {
 			int width = getWidth();
 			int height = getHeight();
 
 			double scaledX = (xValue - minX) / (maxX - minX) * width;
 			double scaledY = (yValue - minY) / (maxY - minY) * height;
 
 			int x = (int)Math.round(scaledX);
 			// remember (0,0) is in the upper left corner
 			// have to flip y here so our graph has (0,0) at the bottom left
 			int y = height - (int)Math.round(scaledY);
 
 			g.drawRect(x, y, 1, 1);
 		}
 
 		private void drawStandardDeviation(Graphics2D g, double minX, double maxX, double minY, double maxY, double xValue, double midLineYValue, double lineHeight) {
 			int width = getWidth();
 			int height = getHeight();
 
 			double scaledX = (xValue - minX) / (maxX - minX) * width;
 			double scaledMinY = (midLineYValue - lineHeight / 2 - minY) / (maxY - minY) * height;
 			double scaledMaxY = (midLineYValue + lineHeight / 2 - minY) / (maxY - minY) * height;
 
 			int x = (int)Math.round(scaledX);
 
 			// remember (0,0) is in the upper left corner
 			// have to flip y here so our graph has (0,0) at the bottom left
 			int yMin = height - (int)Math.round(scaledMinY);
 			int yMax = height - (int)Math.round(scaledMaxY);
 
 			g.drawLine(x, yMin, x, yMax);
 		}
 	}
 }

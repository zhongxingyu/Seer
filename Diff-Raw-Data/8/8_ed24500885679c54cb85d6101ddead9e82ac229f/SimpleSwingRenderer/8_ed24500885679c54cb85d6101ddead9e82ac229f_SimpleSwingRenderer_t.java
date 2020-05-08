 package br.ufrj.jfirn.simulator.renderer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.beans.Transient;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTable;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.AbstractTableModel;
 
 import org.apache.commons.math3.util.FastMath;
 
 import br.ufrj.jfirn.common.Point;
 import br.ufrj.jfirn.common.Robot;
 import br.ufrj.jfirn.intelligent.Collision;
 import br.ufrj.jfirn.intelligent.IntelligentRobot;
 import br.ufrj.jfirn.intelligent.MobileObstacleStatistics;
 import br.ufrj.jfirn.intelligent.Thoughts;
 import br.ufrj.jfirn.intelligent.evaluation.CollisionEvaluation;
 import br.ufrj.jfirn.intelligent.evaluation.Reason;
 
 /**
  * Very simple implementation of a {@link TimedSimulationRenderer}.
  * @author <a href="mailto:ramiro.p.magalhaes@gmail.com">Ramiro Pereira de Magalhães</a>
  *
  */
 public class SimpleSwingRenderer implements SimulationRenderer, ChangeListener {
 
 	private static final int AREA_WIDTH = 1024;
 	private static final int AREA_HEIGHT = 768;
 
 	private final JFrame simulationFrame;
 	private final JFrame thoughtsFrame;
 	private final JSlider tickSelector;
 	private final JTable thoughtsTable;
 
 	/**
 	 * Stores all robots positions through time.
 	 */
 	private List<List<RobotData>> robotData = new ArrayList<>(0);
 	
 	/**
 	 * Represents the IR knowledge base positions through time.
 	 */
 	private List<List<Obstacle>> thoughtsData = new ArrayList<>(0);
 
 	private int currentTick;
 	private int tickToDisplay = 0;
 
 	public SimpleSwingRenderer() {
 		simulationFrame = new JFrame("Simulator");
         simulationFrame.setLayout(new BorderLayout());
 
 		tickSelector = new TimeTickSelector();
 		tickSelector.addChangeListener(this);
 
         simulationFrame.add(new MainPane(), BorderLayout.CENTER);
         simulationFrame.add(tickSelector, BorderLayout.SOUTH);
         simulationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         simulationFrame.pack();
 
         thoughtsFrame = new JFrame("Thoughts");
         thoughtsFrame.setLayout(new BorderLayout());
         thoughtsFrame.setLocation(simulationFrame.getLocation().x + AREA_WIDTH, simulationFrame.getLocation().y);
         thoughtsFrame.setPreferredSize(new Dimension(550, AREA_HEIGHT/3));
 
         thoughtsTable = new ThoughtsTable();
         thoughtsFrame.add(thoughtsTable.getTableHeader(), BorderLayout.PAGE_START);
         thoughtsFrame.add(thoughtsTable, BorderLayout.CENTER);
         thoughtsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         thoughtsFrame.pack();
 	}
 
 	@Override
 	public void draw(Robot robot) {
 		if (currentTick >= robotData.size()) {
 			robotData.add(new ArrayList<RobotData>());
 			thoughtsData.add(new ArrayList<Obstacle>());
 			tickSelector.setMaximum(currentTick);
 		}
 
 		robotData.get(currentTick).add(
 			new RobotData(robot.position(), robot.direction(), robot.hashCode())
 		);
 
 		if (robot instanceof IntelligentRobot) {
 			final Thoughts t = ((IntelligentRobot)robot).getThoughts();
 
 			for (Integer i : t.knownObstacles()) {
 				MobileObstacleStatistics stats = t.obstacleStatistics(i);
 				CollisionEvaluation eval = t.collisionEvaluation(i);
 
 				thoughtsData.get(currentTick).add(new Obstacle(stats, eval));
 			}
 		}
 	}
 
 	@Override
 	public void nextTick() {
 		this.currentTick++;
 	}
 
 	@Override
 	public void done() {
 		simulationFrame.setVisible(true);
         thoughtsFrame.setVisible(true);
 	}
 
 	/**
 	 * Updates what is shown based on the current tick.
 	 */
 	@Override
 	public void stateChanged(ChangeEvent evt) {
 		this.tickToDisplay = ((JSlider)evt.getSource()).getValue();
 		simulationFrame.repaint();
 		thoughtsFrame.repaint();
 	}
 
 
 
 	public static class Obstacle {
 		final public int id;
 		final public Point position;
 		final public double meanSpeed, meanDirection;
 		final public Collision collision;
 		final public Reason reason;
 
 		public Obstacle(MobileObstacleStatistics statistic, CollisionEvaluation evaluation) {
 			this.id = statistic.getObservedObjectId();
 			this.position = new Point(statistic.lastKnownPosition().x(), AREA_HEIGHT - statistic.lastKnownPosition().y());
 			this.meanSpeed = statistic.speedMean();
 			this.meanDirection = statistic.directionMean();
 			this.collision = evaluation.collision();
 			this.reason = evaluation.reason();
 		}
 	}
 
 
 
 	/**
 	 * Stores relevant data of robots.
 	 * 
 	 * @author <a href="mailto:ramiro.p.magalhaes@gmail.com">Ramiro Pereira de Magalhães</a>
 	 *
 	 */
 	private static class RobotData {
 		public final int hashCode;
 		public final double direction;
 		public final Point position;
 
 		public RobotData(final Point position, final double direction, final int hashCode) {
 			this.position = new Point(position.x(), AREA_HEIGHT - position.y());
 			this.direction = direction;
 			this.hashCode = hashCode;
 		}
 	}
 
 
 
 	private static class TimeTickSelector extends JSlider {
 		private static final long serialVersionUID = 1L;
 
 		public TimeTickSelector() {
 			super(JSlider.HORIZONTAL);
 			this.setMinimum(0);
 			this.setPaintTicks(false);
 			this.setPaintLabels(false);
 		}
 	}
 
 
 
 	private class ThoughtsTable extends JTable {
 		private static final long serialVersionUID = 1L;
 
 		private final AbstractTableModel model = new AbstractTableModel() {
 			private static final long serialVersionUID = 1L;
 
 			private final String[] columnNames = {
 				"Obstacle", "Probability", "X", "Y", "Reason"
 			};
 
 			@Override
 			public Object getValueAt(int row, int col) {
 				Obstacle o = thoughtsData.get(tickToDisplay).get(row);
 
 				switch (col) {
 					case 0: return o.id;
 					case 1: return o.collision != null ? o.collision.probability : 0;
 					case 2: return o.position.x();
 					case 3: return o.position.y();
					case 4: return o.reason != null ? o.reason : "null";
 					default: throw new IllegalArgumentException();
 				}
 			}
 
 			@Override
 			public int getRowCount() {
 				if (thoughtsData.isEmpty()) {
 					return 0;
 				} else {
 					return thoughtsData.get(tickToDisplay).size();
 				}
 			}
 
 			@Override
 			public int getColumnCount() {
 				return columnNames.length;
 			}
 
 			public boolean isCellEditable(int row, int col) {
 				return false;
 			}
 
 			@Override
 			public String getColumnName(int column) {
 				return columnNames[column];
 			}
 
 			@Override
 			public Class<?> getColumnClass(int col) {
 				switch (col) {
 					case 0: return Integer.class;
 					case 1: return Double.class;
 					case 2: return Double.class;
 					case 3: return Double.class;
 					case 4: return String.class;
 					default: throw new IllegalArgumentException();
 				}
 			}
 
 		};
 
 		public ThoughtsTable() {
 			super();
 			setModel(model);
 		}
 
 	}
 
 
 
 	/**
 	 * The {@link JPanel} that draws the robots moving area.
 	 * 
 	 * @author <a href="mailto:ramiro.p.magalhaes@gmail.com">Ramiro Pereira de Magalhães</a>
 	 *
 	 */
 	private class MainPane extends JPanel {
 		private static final long serialVersionUID = 1L;
 		private final Dimension preferredSize = new Dimension(AREA_WIDTH, AREA_HEIGHT);
 
 		public MainPane() {
 		}
 
 		@Override @Transient
 		public Dimension getPreferredSize() {
 			return preferredSize;
 		}
 
 		public void paintComponent(final Graphics componentGraphics) {
 			super.paintComponent(componentGraphics);
 
 			//Draws the selected tickSelector situation
 			final Image image = new BufferedImage(AREA_WIDTH, AREA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
 			final Graphics imageGraphics = image.getGraphics();
 
 			//paints the whole background as white
 			imageGraphics.setColor(Color.white);
 			imageGraphics.fillRect(0, 0, AREA_WIDTH, AREA_HEIGHT);
 
 			//paint the thoughts
 			for (Obstacle obs : thoughtsData.get(tickToDisplay)) {
 				paintCollisionArea(obs, imageGraphics);
 				paintObstacle(obs, imageGraphics);
 			}
 			
 			//paint the robots
 			for (RobotData data : robotData.get(tickToDisplay)) {
 				paintRobotData(data, imageGraphics);
 			}
 
 			//draw the image in the panel.
 			componentGraphics.drawImage(image, 0, 0, AREA_WIDTH, AREA_HEIGHT, Color.WHITE, null);
 		}
 
 
 		private void paintRobotData(RobotData data, Graphics imageGraphics) {
 			final Triangle t = new Triangle(data.position, data.direction);
 			imageGraphics.setColor(ColorPaleteForRobots.getColor(data.hashCode));
 			imageGraphics.fillPolygon(t.x, t.y, t.n);
 			imageGraphics.drawOval((int)data.position.x() - 5, (int)data.position.y() - 5, 10, 10);
 
 			//TODO use that to print the sensor around the intelligent agent when you decide how you're gonna do this 
 //			imageGraphics.setColor(Color.gray);
 //			imageGraphics.drawOval((int)irData.position.x() - 200, (int)irData.position.y() - 200, 400, 400);
 		}
 
 		private void paintObstacle(Obstacle obstacle, Graphics imageGraphics) {
 			imageGraphics.setColor(ColorPaleteForRobots.getLighter(obstacle.id));
 
 			imageGraphics.drawLine((int)obstacle.position.x(), (int)obstacle.position.y(),
 				(int)(FastMath.cos(obstacle.meanDirection) * 10 * obstacle.meanSpeed + obstacle.position.x()),
 				(int)(-FastMath.sin(obstacle.meanDirection) * 10 * obstacle.meanSpeed + obstacle.position.y())
 			);

			imageGraphics.drawString(Integer.toString(obstacle.id),
				(int)(FastMath.cos(obstacle.meanDirection + FastMath.PI) * 10 + obstacle.position.x()),
				(int)(-FastMath.sin(obstacle.meanDirection + FastMath.PI) * 10 + obstacle.position.y())
			);
 		}
 
 		private void paintCollisionArea(Obstacle obstacle, Graphics imageGraphics) {
 			if (obstacle.collision != null) {
 				final Collision collision = obstacle.collision;
 				collision.area = arrangePointsAsConvexQuadrilateral(collision.area);
 				final int[] x = new int[collision.area.length];
 				final int[] y = new int[collision.area.length];
 				for (int i = 0; i < collision.area.length; i++) {
 					x[i] = (int)collision.area[i].x();
 					y[i] = AREA_HEIGHT - (int)collision.area[i].y();
 				}
 				imageGraphics.setColor(ColorPaleteForRobots.getLighter(collision.withObjectId));
 				imageGraphics.fillPolygon(x, y, collision.area.length);
 			}
 		}
 
 		private Point[] arrangePointsAsConvexQuadrilateral(Point[] points) {
 			//TODO there probably is a better algorithm for this...
 			final Point center = new Point(
 				(points[0].x() + points[1].x() + points[2].x() + points[3].x()) / 4d,
 				(points[0].y() + points[1].y() + points[2].y() + points[3].y()) / 4d
 			);
 
 			final Map<Double, Point> map = new HashMap<>(4);
 			map.put(center.directionTo(points[0]), points[0]);
 			map.put(center.directionTo(points[1]), points[1]);
 			map.put(center.directionTo(points[2]), points[2]);
 			map.put(center.directionTo(points[3]), points[3]);
 
 			Double[] angles = new Double[4];
 			angles = map.keySet().toArray(angles);
 			Arrays.sort(angles);
 
 			final LinkedList<Point> toReturn = new LinkedList<>();
 			for (Double angle : angles) {
 				toReturn.addLast(map.get(angle));
 			}
 
 			return toReturn.toArray(new Point[4]);
 		}
 
 		/**
 		 * Helper class to draw the robots as triangles that points towards their movement direction.
 		 * 
 		 * @author <a href="mailto:ramiro.p.magalhaes@gmail.com">Ramiro Pereira de Magalhães</a>
 		 */
 		private class Triangle {
 			public final int x[], y[], n = 3;
 
 			private final double alpha = FastMath.PI / 6d + FastMath.PI / 2d;
 			private final double beta = 10d;
 
 			public Triangle(final Point center, final double direction) {
 				x = new int[] {
 					(int)(center.x() + FastMath.cos(direction) * beta),
 					(int)(center.x() + FastMath.cos(direction + alpha) * beta / 2d),
 					(int)(center.x() + FastMath.cos(direction - alpha) * beta / 2d)
 				};
 
 				//Here we use minus because the the image Y dimension orientation
 				//grows towards the bottom of the screen.
 				y = new int[] {
 					(int)(center.y() - FastMath.sin(direction) * beta),
 					(int)(center.y() - FastMath.sin(direction + alpha) * beta / 2d),
 					(int)(center.y() - FastMath.sin(direction - alpha) * beta / 2d)
 				};
 			}
 		}
 
 	}
 
 }
 
 final class ColorPaleteForRobots {
 
 	private static final Color[] colorArray = {
 		Color.orange, Color.blue,
 		Color.red, Color.green,
 		Color.darkGray, Color.magenta,
 		new Color(0x9c00ff), new Color(0x23707e)
 	};
 
 	private static final Color[] lighterColorArray = {
 		new Color(0xffc880), new Color(0xa9b3ff),
 		new Color(0xff8989), new Color(0x80ff8c),
 		Color.lightGray, new Color(0xf998ff),
 		new Color(0xd085ff), new Color(0x23707e)
 	};
 
 	public static final Color getColor(int id) {
 		return colorArray[id % colorArray.length];
 	}
 
 	public static final Color getLighter(int id) {
 		return lighterColorArray[id % lighterColorArray.length];
 	}
 
 }

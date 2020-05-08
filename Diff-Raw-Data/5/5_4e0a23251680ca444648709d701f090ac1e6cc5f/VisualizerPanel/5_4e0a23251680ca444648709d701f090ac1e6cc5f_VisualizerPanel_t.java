 package raisa.ui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.event.HierarchyBoundsListener;
 import java.awt.event.HierarchyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Path2D;
 import java.awt.geom.Point2D.Float;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JPanel;
 
 import raisa.domain.Grid;
 import raisa.domain.Particle;
 import raisa.domain.Robot;
 import raisa.domain.Sample;
 import raisa.domain.WorldModel;
 import raisa.util.CollectionUtil;
 import raisa.util.GeometryUtil;
 import raisa.util.Vector2D;
 
 public class VisualizerPanel extends JPanel implements Observer {
 	private static final long serialVersionUID = 1L;
 	private Color measurementColor = new Color(0.4f, 0.4f, 0.4f);
 	private Float camera = new Float();
 	private Float mouse = new Float();
 	private Float mouseDownPosition = new Float();
 	private Float mouseDragStart = new Float();
 	private boolean mouseDragging = false;
 	private float scale = 1.0f;
 	private List<Sample> latestIR = new ArrayList<Sample>();
 	private List<Sample> latestSR = new ArrayList<Sample>();
 	private Stroke dashed;
 	private Stroke arrow;
 	private VisualizerFrame visualizerFrame;
 	private WorldModel worldModel;
 
 	public void reset() {
 		worldModel.reset();
 		camera = new Float();
 		mouse = new Float();
 		mouseDragStart = new Float();
 		scale = 1.0f;
 		latestIR = new ArrayList<Sample>();
 		latestSR = new ArrayList<Sample>();
 	}
 
 	public VisualizerPanel(VisualizerFrame frame, WorldModel worldModel) {
 		this.visualizerFrame = frame;
 		this.worldModel = worldModel;
 		worldModel.addObserver(this);
 		setBackground(Color.gray);
 		setFocusable(true);
 		addHierarchyBoundsListener(new PanelSizeHandler());
 		addMouseMotionListener(new MouseMotionHandler());
 		addMouseListener(new MouseHandler());
 		addMouseWheelListener(new MouseWheelHandler());
 		reset();
 		dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 15.0f }, 0.0f);
 		arrow = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
 	}
 
 	public void update(Observable model, Object s) {
 		Sample sample = (Sample)s;
 		if (sample.isInfrared1MeasurementValid()) {
 			Vector2D spotPosition = GeometryUtil.calculatePosition(worldModel.getRobot().getPosition(), worldModel.getRobot().getHeading() + sample.getInfrared1Angle(), sample.getInfrared1Distance());
 			worldModel.setGridPosition(spotPosition, true);
 			latestIR.add(sample);
 			latestIR = CollectionUtil.takeLast(latestIR, 10);
 		}
 		if (sample.isUltrasound1MeasurementValid()) {
 			//grid.addSpot(sample.getSrSpot());
 			latestSR.add(sample);
 			latestSR = CollectionUtil.takeLast(latestSR, 10);
 		}
 		repaint();
 	}
 
 	public void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		clearScreen(g);
 		drawGrid(g2);
 		drawRobotTrail(g2, worldModel.getStates());
 		drawRobot(g2);
 		drawArrow(g2);
 		drawUltrasoundResults(g);
 		drawIrResults(g2);
 		drawParticles(g2);
 		if (mouseDragging) {
 			drawMeasurementLine(g2, toWorld(new Vector2D(mouseDownPosition)), toWorld(new Vector2D(mouse)));
 		}
 	}
 
 	private void drawParticles(Graphics2D g2) {
 		g2.setColor(Color.magenta);
 		for (Particle particle : visualizerFrame.getParticleFilter().getParticles()) {			
 			drawParticle(g2, particle);
 		}
 	}
 
 	private void drawParticle(Graphics2D g2, Particle particle) {
 		Robot robot = particle.getLastSample();
 		if (robot != null) {
 			Vector2D to = GeometryUtil.calculatePosition(robot.getPosition(), robot.getHeading(), toWorld(10.0f));
 			drawPoint(g2, robot.getPosition());
 			drawLine(g2, robot.getPosition(), to);
 		}
 	}
 
 	private void drawRobotTrail(Graphics2D g2, List<Robot> states) {
 		g2.setColor(Color.gray);
 		Robot lastState = null;
 		float distanceSoFar = 0.0f;
 		float lastDistanceString = -100.0f;
 		boolean drawDistanceString = false;
 		float distanceMarkerSize = 4.0f;
 		for (Robot state : states) {
 			if (lastState != null) {
 				drawLine(g2, lastState.getPosition(), state.getPosition());
 				distanceSoFar += lastState.getPosition().distance(state.getPosition());
 			}
 			if (distanceSoFar - lastDistanceString >= 100.0f) {
 				drawDistanceString = true;
 			}
 			if (drawDistanceString) {
 				String distanceString = String.format("%3.1f m", distanceSoFar / 100);
 				Float screenPosition = toScreen(state.getPosition());
 				g2.fillRect((int)(screenPosition.x - 0.5f * distanceMarkerSize), (int)(screenPosition.y - 0.5f * distanceMarkerSize), (int)distanceMarkerSize, (int)distanceMarkerSize);
 				g2.drawString(distanceString, (int)screenPosition.x, (int)screenPosition.y);
 				drawDistanceString = false;
 				lastDistanceString = distanceSoFar;
 			}
 			lastState = state;
 		}
 	}
 
 	private void drawLine(Graphics2D g2, Float from, Float to) {
 		Float screenFrom = toScreen(from);
 		Float screenTo = toScreen(to);
 		g2.drawLine((int)screenFrom.x, (int)screenFrom.y, (int)screenTo.x, (int)screenTo.y);
 	}
 
 	private void clearScreen(Graphics g) {
 		int screenWidth = getBounds().width;
 		int screenHeight = getBounds().height;
 		g.clearRect(0, 0, screenWidth, screenHeight);
 	}
 
 	private void drawGrid(Graphics2D g2) {
 		float size = Grid.GRID_SIZE * Grid.CELL_SIZE;
 		Float screen = toScreen(new Float(- size * 0.5f, - size * 0.5f));
 		int screenSize = (int)toScreen(size);
 		g2.drawImage(worldModel.getUserImage(), (int)screen.x, (int)screen.y, screenSize, screenSize, null);
 		g2.drawImage(worldModel.getBlockedImage(), (int)screen.x, (int)screen.y, screenSize, screenSize, null);
 	}
 	
 	private void drawIrResults(Graphics2D g2) {
 		if (!latestIR.isEmpty()) {
 			List<Sample> irs = new ArrayList<Sample>(latestIR);
 			Collections.reverse(irs);
 			float ir = 1.0f;
 			for (Sample sample : irs) {
 				Robot robot = worldModel.getRobot();
 				if (sample.isInfrared1MeasurementValid()) {
 					g2.setColor(new Color(1.0f, 0.0f, 0.0f, ir));
 					Float spot = GeometryUtil.calculatePosition(robot.getPosition(), robot.getHeading() + sample.getInfrared1Angle(), sample.getInfrared1Distance());
 					if (ir >= 1.0f) {
 						drawMeasurementLine(g2, robot.getPosition(), spot);
 					} else {
 						drawMeasurementLine(g2, robot.getPosition(), spot, false);
 					}
 					drawPoint(g2, spot);
 					ir *= 0.8f;
 				} else {
 					g2.setColor(new Color(1.0f, 0.0f, 0.0f, ir));
 					Stroke stroke = g2.getStroke();
 					g2.setStroke(dashed);
 					float angle = robot.getHeading() + sample.getInfrared1Angle() - (float) Math.PI * 0.5f;
 					float dx = (float) Math.cos(angle) * 250.0f;
 					float dy = (float) Math.sin(angle) * 250.0f;
 					Float position = robot.getPosition();
 					Float away = new Float(position.x + dx, position.y + dy);
 					drawMeasurementLine(g2, position, away, false);
 					g2.setStroke(stroke);
 				}
 			}
 		}
 	}
 
 	private void drawUltrasoundResults(Graphics g) {
 		Robot robot = worldModel.getRobot();
 		if (!latestSR.isEmpty()) {
 			float sonarWidth = 42.0f;
 			List<Sample> srs = new ArrayList<Sample>(latestSR);
 			Collections.reverse(srs);
 			float sr = 1.0f;
 			Float position = robot.getPosition();
 			for (Sample sample : srs) {
 				Vector2D spot = GeometryUtil.calculatePosition(position, robot.getHeading() + sample.getUltrasound1Angle(), sample.getUltrasound1Distance());
 				g.setColor(new Color(0.0f, 0.6f, 0.6f, sr));
 				if (sr >= 1.0f) {
 					drawMeasurementLine(g, position, spot);
 					// g.setColor(new Color(0.0f, 0.6f, 0.6f, 0.05f));
 					drawSector(g, position, spot, sonarWidth);
 				} else {
 					// g.setColor(new Color(0.0f, 0.6f, 0.6f, 0.05f));
 					drawSector(g, position, spot, sonarWidth);
 				}
 				g.setColor(new Color(0.0f, 0.6f, 0.6f, sr));
 				drawPoint(g, spot);
 				sr *= 0.8f;
 			}
 		}
 	}
 
 	private void drawRobot(Graphics2D g2) {
 		Robot robot = worldModel.getRobot();	
 		
 		g2.setColor(Color.gray);
 		Float robotScreen = toScreen(robot.getPosition());
 		float widthScreen = toScreen(11.0f);
 		float heightScreen = toScreen(20.0f);
 		float turretScreen = toScreen(5.4f);
 		Path2D.Float p = new Path2D.Float();
 		double x1 = -widthScreen * 0.5f;
 		double x2 = +widthScreen * 0.5f;
 		double y1 = -(heightScreen - turretScreen);
 		double y2 = +turretScreen;
 		p.moveTo(x1, y1);
 		p.lineTo(x2, y1);
 		p.lineTo(x2, y2);
 		p.lineTo(x1, y2);
 		p.closePath();
 		p.transform(AffineTransform.getRotateInstance(robot.getHeading()));
 		p.transform(AffineTransform.getTranslateInstance(robotScreen.x, robotScreen.y));
 		g2.fill(p);
 
 		Float wheelLeftScreen = toScreen(robot.getPositionLeftTrack());
 		Path2D.Float wheelLeft = new Path2D.Float();
 		wheelLeft.moveTo(-2f, +turretScreen) ;
 		wheelLeft.lineTo(-2f, -(heightScreen - turretScreen));
 		wheelLeft.lineTo(2f, -(heightScreen - turretScreen));
 		wheelLeft.lineTo(2f, +turretScreen);
 		wheelLeft.closePath();
 		wheelLeft.transform(AffineTransform.getRotateInstance(robot.getHeading()));
 		wheelLeft.transform(AffineTransform.getTranslateInstance(wheelLeftScreen.x, wheelLeftScreen.y));
 		g2.setColor(Color.orange);
 		g2.fill(wheelLeft);			
 
 		Float wheelRightScreen = toScreen(robot.getPositionRightTrack());
 		Path2D.Float wheelRight = new Path2D.Float();
 		wheelRight.moveTo(-2f, +turretScreen) ;
 		wheelRight.lineTo(-2f, -(heightScreen - turretScreen));
 		wheelRight.lineTo(2f, -(heightScreen - turretScreen));
 		wheelRight.lineTo(2f, +turretScreen);
 		wheelRight.closePath();
 		wheelRight.transform(AffineTransform.getRotateInstance(robot.getHeading()));
 		wheelRight.transform(AffineTransform.getTranslateInstance(wheelRightScreen.x, wheelRightScreen.y));
 		g2.setColor(Color.RED);
 		g2.fill(wheelRight);			
 	}
 
 	private void drawArrow(Graphics2D g2) {
 		Robot robot = worldModel.getRobot();
 		g2.setColor(Color.black);
 		Float robotScreen = toScreen(robot.getPosition());
 		Path2D.Float p = new Path2D.Float();
 		p.moveTo(0, 0);
 		p.lineTo(0, toScreen(-30.0f));
 		p.lineTo(-toScreen(4.0f), -toScreen(25.0f));
 		p.moveTo(0, toScreen(-30.0f));
 		p.lineTo(+toScreen(4.0f), -toScreen(25.0f));
 		p.transform(AffineTransform.getRotateInstance(robot.getHeading()));
 		p.transform(AffineTransform.getTranslateInstance(robotScreen.x, robotScreen.y));
 		Stroke old = g2.getStroke();
 		g2.setStroke(arrow);
 		g2.draw(p);
 		g2.setStroke(old);
 	}
 
 	private void drawSector(Graphics g, Float from, Float to, float sector) {
 		Float p1 = toScreen(from);
 		Float p2 = toScreen(to);
 		g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
 		float dx = (p2.x - p1.x);
 		float dy = (p2.y - p1.y);
 		float l = (float) Math.sqrt(dx * dx + dy * dy);
 		float a = (float) (Math.atan2(-dy, dx) / Math.PI * 180.0) - sector * 0.5f;
 		g.drawArc((int) (p1.x - l), (int) (p1.y - l), (int) (2.0f * l), (int) (2.0f * l), (int) a, (int) sector);
 	}
 
 	private void drawPoint(Graphics g, Float point) {
 		float w = 3.0f;
 		float h = 3.0f;
 		Float p = toScreen(point);
 		g.fillRect((int) (p.x - 0.5f * w), (int) (p.y - 0.5f * h), (int) w, (int) h);
 	}
 
 	private void drawMeasurementLine(Graphics g, Float from, Float to) {
 		drawMeasurementLine(g, from, to, true);
 	}
 
 	private void drawMeasurementLine(Graphics g, Float from, Float to, boolean drawDistanceString) {
 		g.setColor(measurementColor);
 		Float p1 = toScreen(from);
 		Float p2 = toScreen(to);
 		g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
 		if (drawDistanceString) {
 			float dx = (from.x - to.x);
 			float dy = (from.y - to.y);
 			float l = (float) Math.sqrt(dx * dx + dy * dy);
 			String distanceString = String.format("%3.1f cm", l);
 			g.drawString(distanceString, (int) (p2.x + 10), (int) p2.y);
 		}
 	}
 
 	public float toWorld(float screenDistance) {
 		return screenDistance / scale;
 	}
 	
 	public Vector2D toWorld(Vector2D screen) {
 		int screenWidth = getBounds().width;
 		int screenHeight = getBounds().height;
 		return new Vector2D(camera.x + (screen.x - screenWidth * 0.5f) / scale, camera.y
 				+ (screen.y - screenHeight * 0.5f) / scale);
 	}
 
 
 	public float toScreen(float size) {
 		return size * scale;
 	}
 
 	public Float toScreen(Float from) {
 		int screenWidth = getBounds().width;
 		int screenHeight = getBounds().height;
 		float x1 = (from.x - camera.x) * scale + 0.5f * screenWidth;
 		float y1 = (from.y - camera.y) * scale + 0.5f * screenHeight;
 		Float f = new Float(x1, y1);
 		return f;
 	}
 
 	private final class PanelSizeHandler implements HierarchyBoundsListener {
 		@Override
 		public void ancestorResized(HierarchyEvent arg0) {
 			resetMouseLocationLine();
 			repaint();
 		}
 
 		private void resetMouseLocationLine() {
 			mouse.x = camera.x;
 			mouse.y = camera.y;
 		}
 
 		@Override
 		public void ancestorMoved(HierarchyEvent arg0) {
 		}
 	}
 
 	private final class MouseMotionHandler extends MouseAdapter {
 		@Override
 		public void mouseMoved(MouseEvent mouseEvent) {
 			mouse.x = mouseEvent.getX();
 			mouse.y = mouseEvent.getY();
 			visualizerFrame.getCurrentTool().mouseMoved(mouseEvent, mouse);
 			repaint();
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent mouseEvent) {
 			mouse.x = mouseEvent.getX();
 			mouse.y = mouseEvent.getY();
 			visualizerFrame.getCurrentTool().mouseDragged(mouseEvent, mouseDragStart, mouse);
 			mouseDragStart.x = mouse.x;
 			mouseDragStart.y = mouse.y;
 			repaint();
 		}
 	}
 
 	private final class MouseWheelHandler implements MouseWheelListener {
 		@Override
 		public void mouseWheelMoved(MouseWheelEvent event) {
 			if (event.isShiftDown() || event.isMetaDown() || event.isControlDown() || event.isAltGraphDown() || event.isPopupTrigger()) {
 				camera.x += event.getWheelRotation() * 10.0f / scale;				
				VisualizerPanel.this.repaint();
 			} else {
 				camera.y += event.getWheelRotation() * 10.0f / scale;
				VisualizerPanel.this.repaint();
 			}
 		}
 	}
 	
 	private final class MouseHandler extends MouseAdapter {
 		@Override
 		public void mousePressed(MouseEvent mouseEvent) {
 			mouseDragStart.x = mouseEvent.getX();
 			mouseDragStart.y = mouseEvent.getY();			
 			mouseDownPosition.x = mouseEvent.getX();
 			mouseDownPosition.y = mouseEvent.getY();
 			mouseDragging = true;
 			visualizerFrame.getCurrentTool().mousePressed(mouseEvent, mouseDragStart);
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent mouseEvent) {
 			mouse.x = mouseEvent.getX();
 			mouse.y = mouseEvent.getY();
 			mouseDragging = false;
 			visualizerFrame.getCurrentTool().mouseReleased(mouseEvent, mouseDownPosition, mouse);
 		}
 	}
 
 	public void zoomIn() {
 		scale *= 1.25f;
 		repaint();
 	}
 
 	public void zoomOut() {
 		scale *= 0.8f;
 		repaint();
 	}
 	
 	public void clear() {
 		worldModel.clearSamples();
 		repaint();
 	}
 
 	public void removeOldSamples() {
 		worldModel.removeOldSamples(1000);
 		repaint();
 	}
 
 	public float getScale() {
 		return scale;
 	}
 
 	public void panCameraBy(float dx, float dy) {
 		camera.x += dx;
 		camera.y += dy;
 	}
 }

package com.github.joukojo.testgame.world.graphics;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.github.joukojo.testgame.Constants;
import com.github.joukojo.testgame.Player;
import com.github.joukojo.testgame.PlayerMoveListener;
 import com.github.joukojo.testgame.world.core.Drawable;
 import com.github.joukojo.testgame.world.core.Moveable;
 import com.github.joukojo.testgame.world.core.WorldCore;
 import com.github.joukojo.testgame.world.core.WorldCoreFactory;
 
 public class GraphicEngine extends JFrame {
 
 	private final static Logger LOG = LoggerFactory
 			.getLogger(GraphicEngine.class);
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Canvas canvas;
 	private BufferedImage bi;
 	private BufferStrategy buffer;
 
 	public GraphicEngine() {
 		super();
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
 		canvas = new Canvas();
 		canvas.setIgnoreRepaint(true);
 		canvas.setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
 		PlayerMoveListener mouseListener = new PlayerMoveListener();
 		canvas.addMouseMotionListener(mouseListener);
 		canvas.addMouseListener(mouseListener);
 		
 		add(canvas);
 		
 		setLocationRelativeTo(null);
 		
 		pack();
 		setVisible(true);
 		canvas.createBufferStrategy(2);
 		buffer = canvas.getBufferStrategy();
 
 		// Get graphics configuration...
 		GraphicsEnvironment ge = GraphicsEnvironment
 				.getLocalGraphicsEnvironment();
 		GraphicsDevice gd = ge.getDefaultScreenDevice();
 		GraphicsConfiguration gc = gd.getDefaultConfiguration();
 
 		bi = gc.createCompatibleImage(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
 
 	}
 
 	public void init() {
 	}
 
 	public void drawObjects() {
 		LOG.debug("drawing objects");
 		// Objects needed for rendering...
 	    Graphics graphics = null;
 	    Graphics2D g2d = null;
 	    Color background = Color.BLACK;
 		try {
 		
 			LOG.debug("clearing buffer"); 
 			// clear back buffer...
 			g2d = bi.createGraphics();
 			g2d.setColor(background);
 			g2d.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
 
 			LOG.debug("drawing objects"); 
 			drawObjects(g2d);
 		
 			
 			g2d.setFont(new Font("Courier New", Font.PLAIN, 12));
 			g2d.setColor(Color.GREEN);
 			WorldCore worldCore = WorldCoreFactory.getWorld();
 			Player player  = (Player) worldCore.getMoveable("player");
 			int level = player.level;  
 			g2d.drawString("Level:" + level, 20, 20);
 			g2d.drawString("Score:" + player.score, 20, 40);
 			g2d.drawString("Health:" +player.health, 20, 60);
 			
 			// display frames per second...
 //			g2d.setFont(new Font("Courier New", Font.PLAIN, 12));
 //			g2d.setColor(Color.GREEN);
 //			g2d.drawString(String.format("FPS: %s", fps), 20, 20);
 
 			// Blit image and flip...
 			LOG.debug("blit image and flip");
 			graphics = buffer.getDrawGraphics();
 			graphics.drawImage(bi, 0, 0, null);
 			if (!buffer.contentsLost())
 				buffer.show();
 
 		} finally {
 			// release resources
 			if (graphics != null)
 				graphics.dispose();
 			if (g2d != null)
 				g2d.dispose();
 		}
 	}
 
 
 	public void drawObjects(Graphics g) {
 		WorldCore worldCore = WorldCoreFactory.getWorld();
 
 		List<Drawable> allDrawables = worldCore.getAllDrawables();
 
 		drawDrawableObjects(g, allDrawables);
 
 		List<Moveable> allMoveables = worldCore.getAllMoveables();
 		drawMoveableObjects(g, allMoveables);
 
 	}
 
 	protected void drawDrawableObjects(Graphics g, List<Drawable> allDrawables) {
 		for (Drawable drawable : allDrawables) {
 			LOG.trace("drawing object: {}", drawable);
 			drawable.draw(g);
 		}
 		
 		LOG.trace("all drawable objects drawn");
 	}
 
 	protected void drawMoveableObjects(Graphics g, List<Moveable> allMoveables) {
 		for (Drawable drawable : allMoveables) {
 			drawable.draw(g);
 		}
 		LOG.trace("all moveable objects drawn");
 	}
 
 }

 package com.github.joukojo.testgame.world.graphics;
 
 import java.awt.Canvas;
 import java.awt.Color;
import java.awt.DisplayMode;
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
 
 import com.github.joukojo.testgame.DisplayConfiguration;
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
 	private final Canvas canvas;
 	private final BufferedImage bi;
 	private final BufferStrategy buffer;
 
 	public GraphicEngine(GraphicsConfiguration graphicsConfiguration) {
 		
 		super(graphicsConfiguration);
 		setTitle("testgame - alpha");
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		
 		
 		setUndecorated(true);
 		setSize(DisplayConfiguration.getInstance().getWidth(), DisplayConfiguration.getInstance().getHeight());
 		canvas = new Canvas();
 		canvas.setIgnoreRepaint(true);
 		canvas.setSize(DisplayConfiguration.getInstance().getWidth(), DisplayConfiguration.getInstance().getHeight());
 		final PlayerMoveListener mouseListener = new PlayerMoveListener();
 		canvas.addMouseMotionListener(mouseListener);
 		canvas.addMouseListener(mouseListener);
 
 		add(canvas);
 
 		setLocationRelativeTo(null);
 
 		pack();
 		setVisible(true);
 		canvas.createBufferStrategy(2);
 		buffer = canvas.getBufferStrategy();
 
 		// Get graphics configuration...
 		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		final GraphicsDevice gd = ge.getDefaultScreenDevice();
 		final GraphicsConfiguration gc = gd.getDefaultConfiguration();
 
 		bi = gc.createCompatibleImage(DisplayConfiguration.getInstance().getWidth(), DisplayConfiguration.getInstance().getHeight());
 
 	}
 
 	public void init() {
 	}
 
 	public void drawObjects() {
 		LOG.trace("drawing objects");
 		// Objects needed for rendering...
 		Graphics graphics = null;
 
 		try {
 
 			LOG.debug("clearing buffer");
 			// clear back buffer...
 			drawBufferImage();
 
 			// Blit image and flip...
 			LOG.debug("blit image and flip");
 			graphics = buffer.getDrawGraphics();
 			graphics.drawImage(bi, 0, 0, null);
 			if (!buffer.contentsLost()) {
 				buffer.show();
 			}
 
 		} finally {
 			// release resources
 			if (graphics != null) {
 				graphics.dispose();
 			}
 
 		}
 	}
 
 	private void drawBufferImage() {
 		Graphics2D g2d = null;
 		try {
 			g2d = bi.createGraphics();
 			drawBackground(g2d);
 
 			LOG.trace("drawing objects");
 			drawObjects(g2d);
 			LOG.trace("drawing status texts");
 			drawStatusTexts(g2d);
 
 			LOG.trace("buffer image is complete");
 		} finally {
 			if (g2d != null) {
 				g2d.dispose();
 			}
 		}
 	}
 
 	private void drawBackground(final Graphics2D g2d) {
 		final Color background = Color.BLACK;
 		g2d.setColor(background);
 		g2d.fillRect(0, 0, DisplayConfiguration.getInstance().getWidth(), DisplayConfiguration.getInstance().getHeight());
 	}
 
 	private void drawStatusTexts(final Graphics2D g2d) {
 		g2d.setFont(new Font("Courier New", Font.PLAIN, 12));
 		g2d.setColor(Color.GREEN);
 		final WorldCore worldCore = WorldCoreFactory.getWorld();
 		final Player player = (Player) worldCore.getMoveable("player");
 		if (player != null) {
 			final int level = player.getLevel();
 			g2d.drawString("Level:" + level, 20, 20);
 			g2d.drawString("Score:" + player.getScore(), 20, 40);
 			g2d.drawString("Health:" + player.getHealth(), 20, 60);
 		}
 	}
 
 	public void drawObjects(final Graphics g) {
 		final WorldCore worldCore = WorldCoreFactory.getWorld();
 		LOG.trace("Starting to draw drawables");
 		final List<Drawable> allDrawables = worldCore.getAllDrawables();
 
 		drawDrawableObjects(g, allDrawables);
 		LOG.trace("Starting to draw moveables");
 		final List<Moveable> allMoveables = worldCore.getAllMoveables();
 		drawMoveableObjects(g, allMoveables);
 		LOG.trace("all objects are drawn");
 
 	}
 
 	protected void drawDrawableObjects(final Graphics g,
 			final List<Drawable> allDrawables) {
 		for (final Drawable drawable : allDrawables) {
 			LOG.trace("drawing object: {}", drawable);
 			drawable.draw(g);
 		}
 
 		LOG.trace("all drawable objects drawn");
 	}
 
 	protected void drawMoveableObjects(final Graphics g,
 			final List<Moveable> allMoveables) {
 		for (final Drawable drawable : allMoveables) {
 			drawable.draw(g);
 		}
 		LOG.trace("all moveable objects drawn");
 	}
 
 }

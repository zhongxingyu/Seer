 package com.rosenthal.retrogame;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 
 import javax.swing.JFrame;
import javax.swing.JOptionPane;
 
 import com.rosenthal.retrogame.entities.Player;
 import com.rosenthal.retrogame.graphics.Screen;
 import com.rosenthal.retrogame.graphics.SpriteSheet;
 import com.rosenthal.retrogame.level.Level;
 
 public class Game extends Canvas implements Runnable {
 
 	private static final long serialVersionUID = 1L;
 
 	public static final int WIDTH = 160;
 	public static final int HEIGHT = WIDTH / 12 * 9;
 	public static final int SCALE = 3;
 	public static final String TITLE = "Captain Andrew Summer Edit";
 
 	private JFrame frame;
 	public boolean running = false;
 	public int tickCount = 0;
 
 	private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
 	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
 	private int[] colours = new int[6 * 6 * 6];
 
 	private Screen screen;
 	public InputHandler input;
 	public Level level;
 	public Player player;
 
 	public Game() {
 		setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 		setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 		frame = new JFrame(TITLE);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLayout(new BorderLayout());
 		frame.add(this, BorderLayout.CENTER);
 		frame.pack();
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setAlwaysOnTop(true);
 		frame.setVisible(true);
 	}
 
 	public void init() {
 		int index = 0;
 		for (int r = 0; r < 6; r++) {
 			for (int g = 0; g < 6; g++) {
 				for (int b = 0; b < 6; b++) {
 					int rr = (r * 255 / 5);
 					int gg = (g * 255 / 5);
 					int bb = (b * 255 / 5);
 
 					colours[index++] = rr << 16 | gg << 8 | bb;
 				}
 			}
 		}
 		screen = new Screen(WIDTH, HEIGHT, new SpriteSheet("/sprite_sheet.png"));
 		input = new InputHandler(this);
 		level = new Level("/levels/watter_world.png");
		player = new Player(level, 0, 0, input, JOptionPane.showInputDialog(this, "Please Enter A Username!"));
 		level.addEntity(player);
 	}
 
 	public synchronized void start() {
 		new Thread(this).start();
 		running = true;
 	}
 
 	public synchronized void stop() {
 		running = false;
 	}
 
 	public void run() {
 		long lastTime = System.nanoTime();
 		long timer = System.currentTimeMillis();
 		final double ns = 1000000000.0 / 60.0;
 		double delta = 0;
 		init();
 		int frames = 0;
 		int updates = 0;
 		requestFocus();
 		while (running) {
 			long now = System.nanoTime();
 			delta += (now - lastTime) / ns;
 			lastTime = now;
 			boolean shouldRender = true;
 			while (delta >= 1) {
 				updates++;
 				tick();
 				delta--;
 				shouldRender = true;
 			}
 
 			if (shouldRender) {
 				frames++;
 				render();
 			}
 
 			if (System.currentTimeMillis() - timer >= 1000) {
 				timer += 1000;
 				frame.setTitle(TITLE + "  |  " + updates + " ups, " + frames + " fps");
 				updates = 0;
 				frames = 0;
 			}
 		}
 	}
 
 	public void tick() {
 		tickCount++;
 		level.tick();
 	}
 
 	public void render() {
 		BufferStrategy bs = getBufferStrategy();
 		if (bs == null) {
 			createBufferStrategy(3);
 			return;
 		}
 
 		int xOffset = player.x - (screen.width / 2);
 		int yOffset = player.y - (screen.height / 2);
 
 		level.renderTile(screen, xOffset, yOffset);
 		
 		level.renderEntities(screen);
 
 		for (int y = 0; y < screen.height; y++) {
 			for (int x = 0; x < screen.width; x++) {
 				int colourCode = screen.pixels[x + y * screen.width];
 				if (colourCode < 255) pixels[x + y * WIDTH] = colours[colourCode];
 			}
 		}
 
 		Graphics g = bs.getDrawGraphics();
 		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
 		g.dispose();
 		bs.show();
 	}
 
 	public static void main(String[] args) {
 		new Game().start();
 	}
 
 }

 
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 
 import javax.swing.JFrame;
 
public class Render extends Canvas implements Runnable {
 	private static final long serialVersionUID = 1L;
 
	public static int width;
	public static int height;
 	public static int scale = 2;
 	
 	public String title = "Title";
 
 	private Thread thread;
 	private JFrame frame;
 	private boolean running = false;
 
 	private Screen screen;
 
	private BufferedImage image;
	private int[] pixels;
 
	public Render(int width, int height) {
		this.width = width;
		this.height = height;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		Dimension size = new Dimension(width*scale, height*scale);
		setPreferredSize(size);
		screen = new Screen(width, height);
		frame = new JFrame();
 	}
 	
	public Render() {
		width = 320;
		height = 240;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
 		Dimension size = new Dimension(width*scale, height*scale);
 		setPreferredSize(size);
 		screen = new Screen(width, height);
 
 		frame = new JFrame();
 	}
 
 	public synchronized void start() {
 		running = true;
 		thread = new Thread(this, "Display");
 		thread.start();
 	}
 
 	public synchronized void stop() {
 		running = false;
 		try {
 			thread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void run() {
 		long lastTime = System.nanoTime();
 		long timer = System.currentTimeMillis();
 		double delta = 0;
 		int frames = 0;
 		int updates = 0;
 		long now;
 		while (running) {
 			now = System.nanoTime();
 			delta += (now - lastTime) * 6.0 / 100000000.0;
 			lastTime = now;
 			while (delta >= 1) {
 				update();
 				updates++;
 				delta--;
 				render();
 				frames++;
 			}
 			if (System.currentTimeMillis() - timer > 1000) {
 				timer += 1000;
 				frame.setTitle(title + " | updates: " + updates + " | frames: "+ frames);
 				frames = 0;
 				updates = 0;
 			}
 		}
 		stop();
 	}
 
 	public void update() {
 	}
 
 	public void render() {
 		BufferStrategy bs = getBufferStrategy();
 		if (bs == null) {
 			createBufferStrategy(3);
 			return;
 		}
 
 		screen.clear();
 		screen.render();
 
 		for (int i = 0; i < pixels.length; i++) {
 			pixels[i] = screen.pixels[i];
 		}
 
 		Graphics g = bs.getDrawGraphics();
 		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
 		g.dispose();
 		bs.show();
 	}
 
 	public static void main(String[] args) {
		Render render = new Render(160,120);
 		render.frame.setResizable(false);
 		render.frame.setTitle(render.title);
 		render.frame.add(render);
 		render.frame.pack();
 		render.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		render.frame.setLocationRelativeTo(null);
 		render.frame.setVisible(true);
 
 		render.start();
 	}
 }

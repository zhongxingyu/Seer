 package flakes.engine.org;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import javax.swing.JFrame;
 
 //import FlakesEngineGraphics
 import flakes.engine.org.gfx.SpriteSheet;
 
 public class Game extends Canvas implements Runnable{
 
 private static final long serialVersionUID = 1L;
 	public static final int WIDTH = 160;
 	public static final int HEIGHT = WIDTH/12*9;
 	public static final int SCALE = 3;
	public static final String NAME = "Flakes Engine 0.0.2a";
 	public boolean running = false;
 	public int tickCount = 0;
 	
 	private BufferedImage image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
 	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
 	
	private SpriteSheet spriteSheet = new SpriteSheet("/SpriteSheet.png");
 	private JFrame frame;
 	
 	public Game(){
 		setMinimumSize(new Dimension(WIDTH*SCALE, HEIGHT*SCALE));
 		setMaximumSize(new Dimension(WIDTH*SCALE, HEIGHT*SCALE));
 		setPreferredSize(new Dimension(WIDTH*SCALE, HEIGHT*SCALE));
 		frame = new JFrame(NAME);
 		
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLayout(new BorderLayout());
 		
 		frame.add(this, BorderLayout.CENTER);
 		frame.pack();
 		
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 		
 		
 	}
 	
 	public static void main (String[] args) {
 		new Game().start();
 	}
 
 
 	public synchronized void start() {
 			running = true;
 			new Thread(this).start();	
 	}
 	public synchronized void stop() {
 			running = false;
 	}
 
 	public void run() {
 		long lastTime = System.nanoTime();
 		double nsPerTick = 1000000000D/60D;
 		int frames = 0;
 		int ticks = 0;
 		
 		
 		long lasTimer = System.currentTimeMillis();
 		double delta = 0;
 		while(running){
 			long now = System.nanoTime();
 			delta+=(now - lastTime) /nsPerTick;
 			lastTime = now;
 			boolean shouldRender = true;
 			//making trying to downscale frames a sec to nsTime
 			while(delta >= 1){
 				ticks++;
 				tick();
 				delta -=1;
 				shouldRender = true;
 			}
 			//pausing frames
 			try{
 				Thread.sleep(2);
 				}catch(InterruptedException e){
 					e.printStackTrace();
 				}
 			
 			if(shouldRender){
 			frames++;
 			render();
 			}
 			
 			if(System.currentTimeMillis() - lasTimer > 1000){
 				lasTimer += 1000;
 				System.out.println(ticks+" Ticks, "+frames+" Frames");
 				frames = 0;
 				ticks = 0;
 			}
 			
 		}
 	}
 	public void tick(){
 		tickCount++;
 		
 		for(int i=0; i<pixels.length; i++){
 			pixels[i] = i * tickCount;
 		}
 	}
 	public void render(){
 		BufferStrategy bs = getBufferStrategy();
 		if(bs == null){
 			createBufferStrategy(3);
 			return;
 		}
 		
 		Graphics g = bs.getDrawGraphics();
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 0, getWidth(), getHeight());
 		
 		g.drawImage(image, 0 ,0, getWidth(), getHeight(), null);
 		
 		g.dispose();
 		bs.show();
 	}
 }
 

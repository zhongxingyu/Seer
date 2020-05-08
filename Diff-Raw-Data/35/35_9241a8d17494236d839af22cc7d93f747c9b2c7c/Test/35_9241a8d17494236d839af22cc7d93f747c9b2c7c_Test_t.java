 package base;
 
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.JFrame;
 
 import screen.*;
 import display.*;
 
 public class Test extends BatGame{
 	public static void main(String[] argv){
 		Test test = new Test();
 		JFrame frame = new JFrame("BatTest");
//		frame.setUndecorated(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.add(test);
 		frame.pack();
 		frame.setVisible(true);
 		test.init();
 		System.out.println("Loaded");
 	}
 	
 	public static int WIDTH = 800;
 	public static int HEIGHT = 600;
 	
 	BatDisplay display;
 	BatScreenManager scrnMan;
 	
 	public Test() {
 		super(1000);
 		this.setSize(new Dimension(WIDTH,HEIGHT));
  		
 		display = new BatDisplay(Test.WIDTH, Test.HEIGHT);
 		this.setCursor(Cursor.getDefaultCursor());
 		scrnMan = new BatScreenManager();
 		scrnMan.pushScreen(new TestScreen());
 	}
 
 	@Override
 	public void onTick(float dt) {
 		update(dt);
 		render(dt);
 	}
 	
 	public void update(float dt) {
 		scrnMan.update(dt);
 		//scrnMan.handleMousePress((int)this.getMousePosition().getX(), (int)this.getMousePosition().getY(), 0);
 	}
 	
 	public synchronized void render(float dt) {
 		BufferStrategy bs = getBufferStrategy();
 		if(bs == null){
 			createBufferStrategy(3);
 			requestFocus();
 			return;
 		}
 		display.clear(0);
 		scrnMan.render(display);
 		
 		Graphics g = bs.getDrawGraphics();
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRect(0, 0, WIDTH, HEIGHT);
 		g.drawImage(display.getGraphics(), 0, 0, WIDTH, HEIGHT, null);
 		g.setColor(Color.BLACK);
		g.drawString("FPS: " + getFPS() , 10, 10);
 		bs.show();	
 	}
 }

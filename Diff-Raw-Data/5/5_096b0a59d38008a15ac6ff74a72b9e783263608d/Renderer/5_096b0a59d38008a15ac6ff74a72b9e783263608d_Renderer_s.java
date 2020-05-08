 package graphics;
 
 /*
  * GameEntity IDs
  * 0-9 tanks
  * 10-19 projectiles
  */
 import javax.swing.*;
 import java.awt.image.BufferStrategy;
 import java.awt.*;
 import java.util.ArrayList;
 
 import core.engine.Land;
 import core.engine.Input;
 import core.entities.GameEntity;
 import core.entities.top.HumanTank;
 
 @SuppressWarnings("serial")
 public class Renderer extends Canvas{
 	private ArrayList<Sprite> tanks;
 	private ArrayList<Sprite> destTanks;
 	private Sprite screens[];
 	private Sprite props[];
 	ArrayList<HumanTank> humanTanks;
     private JFrame frame;
     private BufferStrategy buffer;
     private Graphics g;
     private Graphics2D g2D;
     
     private GUI ui;
 
     public final int width;
     public final int height;
     //constructor
     public Renderer(Input inp){
 		width = 1248;
 		height = 702;
 		tanks = new ArrayList<Sprite>();
 		destTanks = new ArrayList<Sprite>();
 		props = new Sprite[20];
 		screens = new Sprite[8];
 		frame = new JFrame("OOTanks");
 		ui = new GUI();
 		//get content of the frame, determine size
 		JPanel panel = (JPanel) frame.getContentPane();
 		panel.setPreferredSize(new Dimension(width,height));
 		panel.setLayout(null);
 	
 		//set up canvas and boundaries, add panel to 'this' canvas
 		setBounds(0,0,width,height);
 		panel.add(this);
 	
 		//we will paint manually, so
 		setIgnoreRepaint(true);
 		frame.setIgnoreRepaint(true);
 	
 		frame.pack();
 		frame.setResizable(false);
 		frame.setVisible(true);
 	
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	
 		//creating buffering strategy
 		createBufferStrategy(2);
 		buffer = getBufferStrategy();
 	
 		//handle input init. Sadly, we need to you this out of our canvas
 		setKeyListener(inp);
     }
 
     //function to draw tank at (x,y) and rotate it by an angle in degrees
     /**
      * Draws a tank
      * @param x X centre coordinate of the tank
      * @param y Y centre coordinate of the tank
      * @param angle
      */
     public void drawTank(int x,int y,double angle, int width, int height, int id, boolean destroyed){
     	if (!destroyed){
     		tanks.get(id-1).draw(g2D, x, y, width, height, angle);
     	} else {
     		destTanks.get(id-1).draw(g2D, x, y, width, height, angle);
     	}
     }
     /**
      * Draws a projectile
      * @param x X centre coordinate of the Projectile
      * @param y Y centre coordinate of the Projectile
      * @param angle Angle of the Projectile
      */
     public void drawShell(int x, int y, double angle, int width, int height, int id){
 		int d = 8;
 		g2D.translate(x,y);
 		g2D.rotate(angle);
 		if (id == 11){
 			g2D.setColor(Color.BLACK);
 			g2D.fillArc(-width/2, -height/2, d, d, 0, 360);	
 		} else if (id == 12){
 			g2D.setColor(Color.RED);
 			g2D.fillRect(-width/2, -height/2, width, height );
 		}
 		g2D.rotate(-angle);
 		g2D.translate(-x,-y);
     }
     /**
      * Initialises the renderer, loads resources.
      */
     public void init(ArrayList<HumanTank> tanks){
 	//load resources here
     	ui.init(tanks);
     	humanTanks = tanks;
     	props[0] = new Sprite("/resources/build2.png");
     	props[1] = new Sprite("/resources/build1-1.png");
     	props[2] = new Sprite("/resources/build1-2.png");
     	props[3] = new Sprite("/resources/build1-3.png");
    	props[4] = new Sprite("/resources/build3.png");
     	this.tanks.add(new Sprite("/resources/tank1.png"));
     	this.tanks.add(new Sprite("/resources/tank2.png"));
     	this.destTanks.add(new Sprite("/resources/ExplodedTank1.png"));
     	this.destTanks.add(new Sprite("/resources/ExplodedTank2.png"));
     	screens[0] = new Sprite("/resources/terrainFINAL.png");
     	screens[1] = new Sprite("/resources/GameStartFINAL.png");
     	screens[2] = new Sprite("/resources/SPACEtoSTART.png");
     	screens[3] = new Sprite("/resources/GameOverFINAL.png");
     	screens[4] = new Sprite("/resources/SPACEtoReplay.png");
     	
     }
     public void initScreen(Input input){
     	boolean showScreen = true;
     	final long TARGET_TIME=1000000000/60;
     	int frame = 0;
     	boolean blinker = false;
 
     	while(showScreen){
     		if(frame == 32){
     			frame = 0;
     			if (blinker){
     				blinker = false;
     			} else {
     				blinker = true;
     			}
     		}
     		g2D = null;
     		
     		// get ready to draw
     		g = buffer.getDrawGraphics();
     		if(input.buttons[0][4]) showScreen = false;
     		//creating a java 2D graphic object
     		g2D = (Graphics2D) g;
     		long frameTime = System.nanoTime();    		
     		screens[1].draw(g2D, 0, 0, 0, 0, 0);
     		if (blinker){
     			screens[2].draw(g2D, 325, 410, 0, 0, 0);
     		}
     		try{Thread.sleep((frameTime-System.nanoTime()+TARGET_TIME) / 1000000 );} catch (Exception e){}
     		Toolkit.getDefaultToolkit().sync();
     		if(!buffer.contentsLost()){
     		    buffer.show();
     		} else {
     		    System.out.println("Data Lost in buffer");
     		}
     		frame++;
     	}
     }
     public boolean endScreen(int winner, Input input){
     	boolean showScreen = true;
     	final long TARGET_TIME=1000000000/60;
     	int frame = 0;
     	boolean blinker = false;
 
     	while(showScreen){
     		if(frame == 32){
     			frame = 0;
     			if (blinker){
     				blinker = false;
     			} else {
     				blinker = true;
     			}
     		}
     		g2D = null;
     		
     		// get ready to draw
     		g = buffer.getDrawGraphics();
     		if(input.buttons[0][4]) return true;
     		//creating a java 2D graphic object
     		g2D = (Graphics2D) g;
     		long frameTime = System.nanoTime();    		
     		screens[3].draw(g2D, 0, 0, 0, 0, 0);
     		if (blinker){
     			screens[4].draw(g2D, 425, 510, 0, 0, 0);
     		}
     		try{Thread.sleep((frameTime-System.nanoTime()+TARGET_TIME) / 1000000 );} catch (Exception e){}
     		Toolkit.getDefaultToolkit().sync();
     		if(!buffer.contentsLost()){
     		    buffer.show();
     		} else {
     		    System.out.println("Data Lost in buffer");
     		}
     		frame++;
     	}
     	return false;
     }
     
     /**
      * Renders the game each frame
      * @param map map that the objects will be taken from
      */
     public void draw(double x, double y, double width, double height, double angle, int id){
     	if (id  < 10)
     		drawTank((int)x,(int)y,angle,(int)width,(int)height,id,humanTanks.get(id-1).isDestroyed());
     	else if (id < 20)
     		drawShell((int)x,(int)y,angle,(int)width,(int)height,id);
     	else if (id < 40)
     		drawProp((int)x,(int)y,angle,(int)width,(int)height,id);
     }
     private void drawProp(int x, int y, double angle, int width, int height, int id) {
 		switch(id){
 		case 20:
 			props[0].draw(g2D, x-18, y-49, width, height, angle);
 			break;
 		case 24:
			props[4].draw(g2D, x, y-30, width, height, angle);
 			break;
 		}
 	}
 
 	public void update(Land map){
 		//reset the graphics
 		g = null;
 		g2D = null;
 	
 		// get ready to draw
 		g = buffer.getDrawGraphics();
 	
 		//creating a java 2D graphic object
 		g2D = (Graphics2D) g;
 	
 		//Sets background to terrain
 		screens[0].draw(g2D, 0, 0,0,0, 0);
 		
 		//drawing will be done here
 		
 		for (GameEntity e : map.gameEntities){
 			draw(e.getX(),e.getY(),e.getWidth(),e.getHeight(),e.getAngle(), e.getId());
 		}
 		
 		//end of drawing
 		
 		ui.update(g2D);
 		
 
 		g2D.setColor(Color.BLUE);
 		g2D.drawRect(700, 500, 1, 1);
 		//syncs everything to smooth java frames
 		Toolkit.getDefaultToolkit().sync();
 		if(!buffer.contentsLost()){
 		    buffer.show();
 		} else {
 		    System.out.println("Data Lost in buffer");
 		}
     }
     /**
      * Releases used resources
      */
     public void release(){
 
     }
     /**
      * Function for input handler and to sync Canvas 
      * @param inp input handler
      */
     public void setKeyListener(Input inp) {
 	addKeyListener(inp);
        	requestFocus();
     }
 };

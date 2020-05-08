 package demo;
 
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import javax.swing.*;
 
 
 import deform.Point;
 import deform.TexturedShape;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 
 
 
 import java.util.List;
 import java.util.Random;
 
 public abstract class DemoBase extends JFrame implements KeyListener,MouseWheelListener,MouseListener, MouseMotionListener, WindowListener //, ComponentListener
 {
 	public Point size;
     public Insets insets;
     protected BufferStrategy bufferStrategy;
     private boolean isRunning;
     private boolean isFpsLimited;
     private BufferedImage drawing;
     private double fps;
     private Graphics2D lg;
 	public Point mouse;
 	public String textInput;
 	public String lastLine;
 	public double wheel;
 	protected Graphics2D g;
 	public double rscale;
     
     public DemoBase()
     {
         super();
         mouse = new Point(0,0);
 
         textInput = "";
         lastLine ="";
        size = new Point(1600,1000);
         setTitle("Superawesome demo");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setVisible(true);
         setIgnoreRepaint(true); // don't need Java painting for us
         setResizable(false); // don't want someone resizing our "game" for us
         ((JComponent)getContentPane()).addMouseMotionListener(this);
        ((JComponent)getContentPane()).addMouseListener(this);
        ((JComponent)getContentPane()). addMouseWheelListener(this);
         addKeyListener(this);
         // set up our UnRepaintManager
         RepaintManager repaintManager = new UnRepaintManager();
         repaintManager.setDoubleBufferingEnabled(false);
         RepaintManager.setCurrentManager(repaintManager);
         
                 
         // Correct change width and height of window so that the available
         // screen space actually cooresponds to what is passed, another
         // method is the Canvas object + pack()
         setSize((int)size.x(), (int)size.y());
         rscale = Math.min(size.x(),size.y());
         insets = this.getInsets();
         int insetWide = insets.left + insets.right;
         int insetTall = insets.top + insets.bottom;
         setSize(getWidth() + insetWide, getHeight() + insetTall);
     
 
         
         isFpsLimited = false;
         
         // The JFrame's content pane's background will paint over any other graphics
         // we painted ourselves, so let's turn it transparent
         ((JComponent)getContentPane()).setOpaque(false);
        
         
         // create a buffer strategy using two buffers
         createBufferStrategy(2);
         // set this JFrame's BufferStrategy to our instance variable
         bufferStrategy = getBufferStrategy();
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
//        defaultScreen.setFullScreenWindow(this);
 //        GraphicsConfiguration[] configurations = defaultScreen.getConfigurations();
         isRunning = true;
         init();
         gameLoop(); // enter the game loop
     }
     
     public abstract void init();
     
     /**
      * Method containing the game's loop.
      * Each iteration of the loop updates all animations and sprite locations
      * and draws the graphics to the screen
      */
     public void gameLoop()
     {
         long oldTime = System.currentTimeMillis();
         long nanoseconds = 0;
         int frames = 0;
         long times[] = new long[10];
         int timesIndex = 0;
         fps = 0;
         
         // create a image to draw to to match 0,0 up correctly.
         
         while(isRunning)
         {
             // relating to updating animations and calculating FPS
 //  	
         	long time = System.currentTimeMillis();
         	times[timesIndex] = time - oldTime;
         	timesIndex = (timesIndex + 1) % times.length;
         	long total =0;
         	for(long l : times){
         		total+=l;
         	}
         	fps = (total / times.length);
             Graphics2D g = null;
             oldTime = time;
             try
             {
                 g = (Graphics2D)bufferStrategy.getDrawGraphics();
                 
                 this.g = g;
                 g.setBackground(Color.white);
                 g.translate(insets.left, insets.top);
                 g.clearRect(0, 0, (int)size.x(),(int)size.y());
 
                 draw(); // enter the method to draw everything
                
 
         		g.setColor(Color.black);
         		int fontHeight = g.getFontMetrics(this.getFont()).getHeight();
         		g.drawString("FPS/UPS: " + String.format("%3.2f",fps), insets.left, insets.top + fontHeight);
             }
             finally
             {
                 g.dispose();
             }
             if (!bufferStrategy.contentsLost())
             {
                 bufferStrategy.show();
             }
 //            Toolkit.getDefaultToolkit().sync(); // prevents possible event queue problems in Linux
             
 //            if (isFpsLimited)
 //            {
 //                // sleep to let the processor handle other programs running
 //                try
 //                {
 //                    // comment this out to not limit the FPS
 //                    Thread.sleep(10);
 //                }
 //            catch (Exception e){};
 //            }
         }
     }
     
     /**
      * Updates any objects that need to know how much time has elapsed
      * to update animations and locations
      * @param elapsedTime How much time has elapsed since the last update
      */
     public void update(long elapsedTime)
     {
     }
     
 
     public abstract void draw();
    
     
 
 	public void handleMouseClick(int button) {
 	}
 	
 	public void handleKeyStroke(char key){
 	}
 
 	
 	@Override
 	public void windowOpened(WindowEvent e) {
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 		this.setEnabled(false);
 		System.exit(0);
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {
 		
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 	}
 
 	void computeMouse(Point screen){
 		mouse = screen.$minus(size.$div(2)).$div(rscale/2);
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		computeMouse(new Point(e.getX(),e.getY()));
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {	
 		computeMouse(new Point(e.getX(),e.getY()));
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		handleMouseClick(e.getButton());
 	}
 
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		handleMouseRelease(e.getButton());
 		
 	}
 
 	public void handleMouseRelease(int button) {
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		handleKeyStroke(e.getKeyChar());
 		if(e.getKeyChar() == '\b'){
 			if(!textInput.isEmpty()){
 				textInput = textInput.substring(0, textInput.length()-1);
 			}
 			if(!lastLine.isEmpty()){
 				lastLine = lastLine.substring(0, lastLine.length()-1);
 			}
 		}
 		else if(e.getKeyChar() == '\n'){
 			lastLine = "";
 			textInput+=e.getKeyChar();
 		} else {
 			lastLine+=e.getKeyChar();
 			textInput+=e.getKeyChar();
 		}
 	}
 
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		
 	}
 
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		wheel += e.getUnitsToScroll();
 		handleMouseWheel(e.getUnitsToScroll());
 	}
  
     public void handleMouseWheel(int unitsToScroll) {
 		
 	}
 
 	/**
      * UnRepaintManager is a RepaintManager that removes the functionality
      * of the original RepaintManager for us so we don't have to worry about
      * Java repainting on it's own.
      */
     class UnRepaintManager extends RepaintManager
     {
         public void addDirtyRegion(JComponent c, int x, int y, int w, int h){}
         public void addInvalidComponent(JComponent invalidComponent){}
         public void markCompletelyDirty(JComponent aComponent){}
         public void paintDirtyRegions(){}    
     }
 }

 package graphics;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 
 import environment.Engine;
 import environment.FishState;
 import environment.Food;
 import environment.Rules;
 import environment.Vector;
 import environment.WorldState;
 
 public class Visualizer extends JFrame implements Runnable, MouseMotionListener {
 	private static final long serialVersionUID = -6856794708211750050L;
 	private BufferedImage buffer;
     private Graphics2D bufferGraphics;
     private static Engine fishtank;
     private Thread engineThread;
     private WorldState state;
     private Vector mousePosition;
     private Color background = new Color(134, 177, 225);
 
     public Visualizer() {
         //Set up the fishtank
     	fishtank = new Engine();
         buffer = new BufferedImage(Rules.tankWidth, Rules.tankHeight, BufferedImage.TYPE_INT_RGB);
         bufferGraphics = (Graphics2D)buffer.getGraphics();
         bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         
         engineThread = new Thread(fishtank);
         
         mousePosition = new Vector();
         this.addMouseMotionListener(this);
     }
 
     /* Helper function to print the usage statement */
     private static void usage() {
         System.err.println("Usage: java Visualizer");
     }
 
     public void run() {
         //Set up visualization window
         this.setVisible(true);
         this.setSize(buffer.getWidth(), buffer.getHeight());
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         //Start the engine
         engineThread.start();
 
         //Repeatedly display the simulation
         long nextState = 0;
         while(engineThread.isAlive()) {
             state = fishtank.getState(nextState);
             repaint();
             nextState = state.seqID;
         }
     }
     
     private FishState drawFish() {
     	FishState fishUnderMouse = null;
     	for (FishState fs : state.getFish()) {
             if(fs.isAlive()){
                 Vector pos = fs.getPosition();
                 // Draw tail and outline according to speed
                 int tailBrightness = (int)((double)fs.getSpeed() / Rules.MAX_SPEED * 255);
                 bufferGraphics.setColor(new Color(tailBrightness, tailBrightness, tailBrightness));
                 bufferGraphics.drawLine((int)pos.x, (int)pos.y,
                 		(int)(pos.x - fs.getRudderVector().x * fs.getRadius() * 2),
                 		(int)(pos.y - fs.getRudderVector().y * fs.getRadius() * 2));
                 bufferGraphics.fillOval((int)pos.x - fs.getRadius(),
                                         (int)pos.y - fs.getRadius(),
                                         2 * fs.getRadius(), 2 * fs.getRadius());
                 bufferGraphics.setColor(fishtank.getColor(fs.fish_id));
                 bufferGraphics.fillOval((int)pos.x - fs.getRadius() + 1,
                                         (int)pos.y - fs.getRadius() + 1,
                                         2 * fs.getRadius() - 2,
                                         2 * fs.getRadius() - 2);
             }
             if (fs.getPosition().minus(mousePosition).length() < fs.getRadius()) {
             	fishUnderMouse = fs;
             }
         }
     	return fishUnderMouse;
     }
     
     private void drawTooltip(FishState fishUnderMouse) {
     	int tooltipHeight = 20, tooltipBottomMargin = 10;
         bufferGraphics.setColor(Color.WHITE);
         bufferGraphics.fillRect(0, buffer.getHeight() - tooltipHeight, buffer.getWidth(), tooltipHeight);
         bufferGraphics.setColor(Color.BLACK);
         if (fishUnderMouse != null) {
         	bufferGraphics.drawString(String.format("Frame %d ID %d, Nut %.2f, Change %.2f, Speed %.2f, Dir %s",
 	        			state.seqID, fishUnderMouse.fish_id,
 	        			fishUnderMouse.getNutrients(), Rules.decay(fishUnderMouse),
 	        			fishUnderMouse.getSpeed(), fishUnderMouse.getRudderVector()),
         			5, buffer.getHeight() - tooltipBottomMargin);
         } else {
         	bufferGraphics.drawString(String.format("Frame %d, Fish %d, Controllers %d, Max fish %d, Max nut %.2f",
         				state.seqID, state.getNumFish(),
         				state.getNumControllers(),
         				state.getMaxFish(), state.getMaxNutrients()),
         			5, buffer.getHeight() - tooltipBottomMargin);
         }
     }
     
     private void drawFood() {
     	for (Food food : state.getFood()) {
         	bufferGraphics.setColor(Color.GREEN);
         	bufferGraphics.fillOval((int)(food.position.x - food.getRadius()), (int)(food.position.y - food.getRadius()), (int)(2 * food.getRadius()), (int)(2 * food.getRadius()));
         }
     }
 
     //Push the visualization of the state to the window
     public void paint(Graphics g) {
     	//Draw the tank
         bufferGraphics.setColor(background);
         bufferGraphics.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
    
         //Draw the fish
 
        // TODO draw fish features like rudder
         FishState fishUnderMouse = null;
         if (state == null) {
                 return;
         }
         
         fishUnderMouse = drawFish();
         
        drawTooltip(fishUnderMouse);
        
         drawFood();
 
         g.drawImage(buffer, 0, 0, this);
 
     }
 
     public static void main(String[] argv) {
         //Parse command-line args
         if(argv.length != 0){
             usage();
             //System.exit(-1);
         }
 
         Thread visThread = new Thread(new Visualizer());
         visThread.start();
 
         //Poll for the user to end the simulation
         Scanner in = new Scanner(System.in);
         while(visThread.isAlive()) {
             String input = in.nextLine();
             if(input.equals("exit")) {
                 System.exit(0);
             } else if (input.equals("spawn")) {
                 input = in.nextLine();
                 for(int i = 0; i < Integer.parseInt(input); i++)
                     fishtank.add();
             } else if (input.equals("print")) {
             	fishtank.printState();
             }
         }
     }
 
 	public void mouseMoved(MouseEvent arg0) {
 		mousePosition = new Vector(arg0.getX(), arg0.getY());
 	}
 
 	public void mouseDragged(MouseEvent arg0) {
 	}
 }
 

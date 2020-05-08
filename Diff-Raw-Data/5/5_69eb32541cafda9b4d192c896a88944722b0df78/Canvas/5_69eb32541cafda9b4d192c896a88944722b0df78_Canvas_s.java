 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.Timer;
 import javax.swing.event.MouseInputAdapter;
 
import simulation.SpriteFactory;
import simulation.PhysicsFactory;
 import simulation.Model;
 
 
 /**
  * Creates an area of the screen in which the game will be drawn that supports:
  * <UL>
  *   <LI>animation via the Timer
  *   <LI>mouse input via the MouseListener and MouseMotionListener
  *   <LI>keyboard input via the KeyListener
  * </UL>
  * 
  * @author Robert C Duvall
  */
 public class Canvas extends JComponent {
     // default serialization ID
     private static final long serialVersionUID = 1L;
     // animate 25 times per second if possible
     public static final int FRAMES_PER_SECOND = 25;
     // better way to think about timed events (in milliseconds)
     public static final int ONE_SECOND = 1000;
     public static final int DEFAULT_DELAY = ONE_SECOND / FRAMES_PER_SECOND;
     // only one so that it maintains user's preferences
     private static final JFileChooser INPUT_CHOOSER = 
             new JFileChooser(System.getProperties().getProperty("user.dir"));
     // input state
     public static final int NO_KEY_PRESSED = -1;
     public static final Point NO_MOUSE_PRESSED = null;
 
     // drives the animation
     private Timer myTimer;
     // game to be animated
     private Model mySimulation;
     // input state
     private int myLastKeyPressed;
     private Point myLastMousePosition;
     private Set<Integer> myKeys;
     
     private int mouseClick = MouseEvent.BUTTON1;
 
 
     /**
      * Create a panel so that it knows its size
      */
     public Canvas (Dimension size) {
         // set size (a bit of a pain)
         setPreferredSize(size);
         setSize(size);
         // prepare to receive input
         setFocusable(true);
         requestFocus();
         myKeys = new HashSet<Integer>();
         setInputListeners();
         
     }
 
     /**
      * Paint the contents of the canvas.
      * 
      * Never called by you directly, instead called by Java runtime
      * when area of screen covered by this container needs to be
      * displayed (i.e., creation, uncovering, change in status)
      * 
      * @param pen used to paint shape on the screen
      */
     @Override
     public void paintComponent (Graphics pen) {
         pen.setColor(Color.WHITE);
         pen.fillRect(0, 0, getSize().width, getSize().height);
         // first time needs to be special cased :(
         if (mySimulation != null) {
             mySimulation.paint((Graphics2D) pen);
         }
     }
 
     /**
      * Returns last key pressed by the user or -1 if nothing is pressed.
      */
     public int getLastKeyPressed () {
         return myLastKeyPressed;
     }
     
     public boolean isKeyContained(int key)
     {
     	return myKeys.contains(key);
     }
     
 
     /**
      * Returns all keys currently pressed by the user.
      */
     public Set<Integer> getKeysPressed () {
         return myKeys;
     }
 
     /**
      * Returns last position of the mouse in the canvas.
      */
     public Point getLastMousePosition () {
         return myLastMousePosition;
     }
     
     public int getMouseButton()
     {
     	return mouseClick;
     }
 
     /**
      * Start the animation.
      */
     public void start () {
         // create a timer to animate the canvas
         myTimer = new Timer(DEFAULT_DELAY, 
             new ActionListener() {
                 public void actionPerformed (ActionEvent e) {
                     step();
                 }
             });
         // start animation
         mySimulation = new Model(this);
         loadModel();
         myTimer.start();
     }
 
     /**
      * Stop the animation.
      */
     public void stop () {
         myTimer.stop();
     }
 
     /**
      * Take one step in the animation.
      */
     public void step () {
         mySimulation.update((double)FRAMES_PER_SECOND / ONE_SECOND);
         // indirectly causes paint to be called
         repaint();
     }
 
     /**
      * Create listeners that will update state based on user input.
      */
     private void setInputListeners () {
         // initialize input state
         myLastKeyPressed = NO_KEY_PRESSED;
         addKeyListener(new KeyAdapter() {
             @Override
             public void keyPressed (KeyEvent e) {
                 myKeys.add(e.getKeyCode());
             }
             @Override
             public void keyReleased (KeyEvent e) {
                 myKeys.remove(e.getKeyCode());
             }
         });
         
         myLastMousePosition = new Point();
         addMouseMotionListener(new MouseMotionAdapter() {
             @Override
             public void mouseMoved (MouseEvent e) {
                 myLastMousePosition = e.getPoint();
             }
         });
         mouseClick = MouseEvent.NOBUTTON;
         addMouseListener(
         		new MouseInputAdapter() {
         			@Override
         			public void mousePressed(MouseEvent e)
         			{
         				mouseClick = e.getButton();
         			}
         			
         			@Override
         			public void mouseReleased(MouseEvent e)
         			{
         				mouseClick = e.NOBUTTON;
         			}	
 				}
         		);
     }
     
     // load model from file chosen by user
     private void loadModel () {
         SpriteFactory factory = new SpriteFactory();
         PhysicsFactory loadPhys = new PhysicsFactory();
         int response = INPUT_CHOOSER.showOpenDialog(null);
         if (response == JFileChooser.APPROVE_OPTION) {
         	
         	loadPhys.loadModel(mySimulation,INPUT_CHOOSER.getSelectedFile());
             response = INPUT_CHOOSER.showOpenDialog(null);
             factory.loadModel(mySimulation, INPUT_CHOOSER.getSelectedFile());
             
         }
        
     }
 	public void loadAssembly() {
         SpriteFactory factory = new SpriteFactory();
         int response = INPUT_CHOOSER.showOpenDialog(null);
         if (response == JFileChooser.APPROVE_OPTION) {
         	response = INPUT_CHOOSER.showOpenDialog(null);
             factory.loadModel(mySimulation, INPUT_CHOOSER.getSelectedFile()); //same simulation or should be passed in?
         }
     }
 }

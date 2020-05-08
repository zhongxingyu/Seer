 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hopkins.rocknrollracing;
 
 import com.hopkins.rocknrollracing.controllers.AppController;
 import com.hopkins.rocknrollracing.state.CarModel;
 import com.hopkins.rocknrollracing.state.ControllerState;
 import com.hopkins.rocknrollracing.state.GameState;
 import com.hopkins.rocknrollracing.utils.ImageUtils;
 import com.hopkins.rocknrollracing.views.Screen;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  *
  * @author ian
  */
 public class Application implements Runnable {
     public static final long NS_PER_MS = 1000000;
     public static final long MS_PER_SECOND = 1000;
     public static final long DESIRED_FPS = 60;
     public static final long DESIRED_PERIOD_MS = MS_PER_SECOND / DESIRED_FPS;
    public static final String INITIAL_CONTROLLER = "Intro";
     public static final String TITLE = "Rock 'n Roll Racing - Javagator Remake";
     public static final String ICON_SPRITE_PATH = "images/cars/marauder-blue.png";
     
     protected long startTimeNano;
     protected GameState gameState;
     protected boolean running;
     protected AppController controller;
     protected BufferedImage buffer;
     protected Graphics bufferGraphics;
     protected JFrame frame;
     protected JPanel panel;
     protected ControllerState controllerState;
     protected long ticker;
     
     /**
      * Constructor to setup the application
      */
     public Application() {
         gameState = new GameState();
         gameState.Player1.Model = CarModel.AirBlade;
         initBuffer();
         loadController(INITIAL_CONTROLLER);
     }
     
     /**
      * Method to initialize the window for the application and its components
      */
     protected void initWindow() {
         
         // Setup a JPanel as a drawing surface
         panel = new JPanel();
         panel.setPreferredSize(new Dimension(Screen.WIDTH * 2, Screen.HEIGHT * 2));
         panel.setIgnoreRepaint(true);
         
         // Load the icon image
         Image icon = null;
         try {
             icon = ImageUtils.loadSprite(ICON_SPRITE_PATH);
         } catch (ImageUtils.ImageLoadException ex) {
             System.err.println("Error loading icon sprite: " + ex.toString());
         }
         
         // Setup a basic window container
         frame = new JFrame();
         frame.getContentPane().add(panel);
         frame.pack();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setVisible(true);
         frame.setTitle(TITLE);
         frame.setIconImage(icon);
         
         // Setup an input receiver
         controllerState = new ControllerState();
         controllerState.loadConfig("config/keys.properties");
         frame.addKeyListener(controllerState);
     }
     
     /**
      * Initialize an empty back buffer the size of the screen for double buffering
      */
     protected final void initBuffer() {
         buffer = new BufferedImage(Screen.WIDTH, Screen.HEIGHT, BufferedImage.TYPE_INT_ARGB);
     }
     
     /**
      * Internal method used to 
      * @param controllerName name of the controller to dispatch to
      */
     protected final void loadController(String controllerName) {
         ticker = 0;
         controller = instantiateController(controllerName);
         controller.load(gameState);
     }
     
     /**
      * This method is called to instantiate a controller by name (via reflection)
      * 
      * @param controllerName the name of the controller
      * @return the instantiated controller
      */
     protected AppController instantiateController(String controllerName) {
         String className = String.format("com.hopkins.rocknrollracing.controllers.%sController", controllerName);
         try {
             Class<?> clazz = Class.forName(className);
             return (AppController) clazz.getConstructor().newInstance();
         } catch (Exception ex) {
             throw new RuntimeException(String.format(
                     "Unknown controller type: %s %s",
                     controllerName, className),
                     ex);
         }
     }
     
     /**
      * Main game loop
      */
     public void run() {
         running = true;
         initWindow();
         
         startTimeNano = System.nanoTime();
         while (running) {
             update();
             render();
             paint();
             sleep();
         }
     }
     
     /**
      * Sleep method to achieve desired FPS
      */
     protected void sleep() {
         // the timer
         long timeNS = System.nanoTime();
         long deltaNS = timeNS - startTimeNano;
         startTimeNano = timeNS;
         
         // sleeping
         try {
             Thread.sleep(Math.max(5, DESIRED_PERIOD_MS - deltaNS/NS_PER_MS));
         } catch (InterruptedException ex) {
             // noop
         }
     }
     
     /**
      * Run the controller's update method
      */
     protected void update() {
         
         // Handle any requested dispatch
         String dispatchTo = controller.getDispatchTo();
         if (dispatchTo != null) {
             loadController(dispatchTo);
         }
         
         // Run the controller update, update the input state
         controller.update(controllerState, ticker);
         controllerState.afterUpdate();
         ticker++;
     }
     
     /**
      * Render the view to the back buffer
      */
     protected void render() {
         Graphics g = buffer.getGraphics();
         controller.getView().render(g, ticker);
         g.dispose();
     }
     
     /**
      * Write the back buffer to the display
      */
     protected void paint() {
         // Draw the back buffer to the panel
         Graphics g = panel.getGraphics();
         g.drawImage(buffer, 0, 0, panel.getWidth(), panel.getHeight(), frame);
         g.dispose();
         
         // Synchronize the display (mostly for X Windows systems)
         Toolkit.getDefaultToolkit().sync();
     }
     
     /**
      * Application entry point
      * @param args unused
      */
     public static void main(String[] args) {
         Application app = new Application();
         app.run();
     }
 }

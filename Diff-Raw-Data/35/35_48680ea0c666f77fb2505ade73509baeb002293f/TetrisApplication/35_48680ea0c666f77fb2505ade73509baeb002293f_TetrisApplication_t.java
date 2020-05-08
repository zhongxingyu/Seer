 import javax.swing.JApplet;
 import javax.swing.JFrame;
 
 import java.awt.Toolkit;
 import java.awt.Dimension;
 import java.awt.Color;
 import java.awt.Graphics;
import java.awt.Image;
 
 public class TetrisApplication extends JApplet {
     // the controller for the game
     private GameController controller;
     // used to determine if the application is running in an applet
     // or from a call from main()
     private boolean isApplet;
     // use frame if the game isn't running in an applet
     private JFrame frame;
    // //  the dimension of the applet
    // private Dimension dimension;
    // // image used for double buffering
    // Image offscreen;
    // // the second buffer
    // Graphics bufferGraphics;
     
     // the init function of the applet
     public void init() {
         super.init();
        // dimension = getSize();
        // offscreen = createImage(dimension.width, dimension.height);
        // bufferGraphics = offscreen.getGraphics();
         setBackground(Color.BLACK);
         getRootPane().setBackground(Color.BLACK);
         initInApplet();
     }
     
     private void initInApplet() {
         isApplet = true;
         // setFocusable(true);
         // requestFocusInWindow();
         controller = new GameController(this);
     }
     
     // the start function of the applet
     public void start() {
         super.start();
         getRootPane().setContentPane(controller.getContentPane());
         getRootPane().setVisible(true);
         setVisible(true);
     }
     
    // @Override
    // public void paint(Graphics g){
    //     bufferGraphics.clearRect(0, 0, dimension.width, dimension.height);
    //     super.paint(bufferGraphics);
    //     g.drawImage(offscreen, 0, 0, this);
    // }
     
     // the function called to init the game in a frame instead of an applet
     public void initInFrame() {
         isApplet = false;
         controller = new GameController(this);
         
         frame = new JFrame("Tetris");
 		frame.setContentPane(controller.getContentPane());
 		frame.pack();
 		frame.setResizable(false);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Toolkit toolkit =  Toolkit.getDefaultToolkit ();
 		Dimension screenSize = toolkit.getScreenSize();
 		frame.setLocation(
 		    (screenSize.width - controller.RESOLUTION.width)  / 2,
 			(screenSize.height - controller.RESOLUTION.height) / 2
 		);
 		frame.setVisible(true);
     }
     
     // used to exit the application
     public void exit() {
         if (isApplet) {
             stop();
             destroy();
         } else {
             frame.dispose();
         }
     }
 }
 

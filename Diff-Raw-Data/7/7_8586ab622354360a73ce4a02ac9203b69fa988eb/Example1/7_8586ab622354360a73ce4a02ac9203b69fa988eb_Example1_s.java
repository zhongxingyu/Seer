 package example1;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 class Example1Gui {
 protected  void example() {
         final JFrame examp = new JFrame("A very simple gui");
 // A label with "0" in the centre of the label 
 // is created when the applet is created.
         final JLabel onlyComponent = new JLabel("0",SwingConstants.CENTER);
 
 // The add method puts the label inside the container 
 // and the label is displayed when the container is drawn.
         examp.add(onlyComponent);
         examp.setSize(100,100);
 // The program exits when the window is closed.
         examp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 // The screen is not visible until it is complete.
         examp.setVisible(true);
     }
 }
 
 public class Example1  {
 
     public static void main(final String [] args) {
 // Starts the GUI on a separate thread.
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 final Example1Gui gui = new Example1Gui();
                 gui.example();
             }
         });
     }
 }
 

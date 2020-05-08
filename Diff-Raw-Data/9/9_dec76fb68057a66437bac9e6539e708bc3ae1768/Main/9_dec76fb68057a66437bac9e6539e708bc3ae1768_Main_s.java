 package simple;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 
 public class Main {
 	public static void main(String args[]) {
         System.out.println("Start");
 
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
            	JFrame frame = new JFrame("ListDemo");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
                 //Create and set up the content pane.
                 JComponent newContentPane;
 				try {
 					newContentPane = new SimpleGUI1();
 	                newContentPane.setOpaque(true); //content panes must be opaque
 	                frame.setContentPane(newContentPane);
 
 	                //Display the window.
 	                frame.pack();
 	                frame.setVisible(true);
 	                
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
             }
         });
     }
 }

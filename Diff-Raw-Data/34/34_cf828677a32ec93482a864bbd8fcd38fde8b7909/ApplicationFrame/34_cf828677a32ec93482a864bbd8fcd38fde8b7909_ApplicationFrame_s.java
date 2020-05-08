 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jevolution;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 /**
  *
  * @author kuhlmancer
  */
public class ApplicationFrame extends JFrame implements Runnable {
	public void run() {
 		ApplicationPanel panel = new ApplicationPanel();
 		JFrame window = new JFrame("jevolution 2D");
 		window.add(panel);
 
 		window.pack();
 		window.setVisible(true);
 		window.setLocationRelativeTo(null);
 		window.setResizable(false);
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	public static void main(String[] args) {
		SwingUtilities.invokeLater(new ApplicationFrame());
 	}
 }

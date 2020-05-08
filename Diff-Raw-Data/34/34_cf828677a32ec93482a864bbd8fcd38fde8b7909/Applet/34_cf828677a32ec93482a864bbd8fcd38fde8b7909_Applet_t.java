 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jevolution;
 
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javax.swing.JApplet;
import javax.swing.SwingUtilities;
 
 /**
  *
  * @author kuhlmancer
  */
 public class Applet extends JApplet {
 
 	@Override
 	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					guiInit();
				}
			});
		} catch (Exception ex) {
			Logger.getLogger(Applet.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void guiInit() {
 		ApplicationPanel panel = new ApplicationPanel();
 		getContentPane().add(panel);
 	}
 }

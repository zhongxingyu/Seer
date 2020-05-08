 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jevolution;
 
 import javax.swing.JApplet;
 
 /**
  *
  * @author kuhlmancer
  */
 public class Applet extends JApplet {
 
 	@Override
 	public void init() {
 		ApplicationPanel panel = new ApplicationPanel();
 		getContentPane().add(panel);
 	}
 }

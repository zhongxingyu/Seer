 package wombat;
 
 import gui.MainFrame;
 
 /**
  * Main entry point of the program.
  */
 public class Wombat {
	public static final String VERSION = "{REVISION}";
 	
     public static void main(String[] argv) {
     	System.setSecurityManager(null);
     	new Wombat();
     }
 
     public Wombat() {
     	MainFrame main = new MainFrame();
         main.setVisible(true);
     }
 }

 /**
  * 
  */
 package cz.muni.fi.spc.SchedVis;
 
 import javax.swing.JFrame;
 
 import cz.muni.fi.spc.SchedVis.ui.MainFrame;
 
 /**
  * The main class for the SchedVis project.
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  */
 public final class Main {
 
 	private static MainFrame frame;
 
 	/**
 	 * MainFrame method for the whole project.
 	 * 
 	 * @param args
 	 */
 	public static void main(final String[] args) {
 		JFrame.setDefaultLookAndFeelDecorated(true);
 		// Schedule a job for the event-dispatching thread:
 		// creating and showing this application's GUI.
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				Main.frame = new MainFrame();
 				Main.frame.setVisible(true);
 			}
 		});
 	}
 
 	public static void update() {
		Main.frame.update();
 	}
 
 }

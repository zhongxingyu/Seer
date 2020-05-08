 package codebots;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 
 public class Viewer {
 	
 	private Communicator communicator;
 
 	public static void main(String[] args) {
 		Viewer viewer = new Viewer();
 		viewer.main2(args);
 	}
 	
 	public void main2(String[] args) {
 		JFrame frame = new JFrame("Codebots viewer");
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent we) {
				Viewer.this.communicator.terminate();
 				System.exit(0);
 			}
 		});
 		frame.setSize(500, 400);
 		ViewerPanel panel = new ViewerPanel();
 		frame.setContentPane(panel);
 		this.communicator = new Communicator(-1, "def", "localhost", 12345, panel);
 		this.communicator.start();
 		frame.setVisible(true);
 	}
 
 }

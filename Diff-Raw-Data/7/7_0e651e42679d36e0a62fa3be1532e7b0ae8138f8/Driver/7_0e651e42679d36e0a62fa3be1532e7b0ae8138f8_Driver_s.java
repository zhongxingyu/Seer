 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 
 public class Driver {
 
 	public static void main(String[] args) { //Responsible for creating the MainControl object as well as creating and
 		//starting the threads for the BluetoothListener and the GUI
 		final MainControl mainControl = new MainControl();
 		
		/*BluetoothListener mainListener = new BluetoothListener(mainControl);
		mainListener.start();*/
		
		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
				
				
 				JFrame frame = new JFrame("Control Station");
 				GUI controlPanel = new GUI(mainControl);
 				mainControl.getController().setControlPanel(controlPanel);
 				frame.setContentPane(controlPanel.createContentPane());
 				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				frame.setLocation(20,20);
 				frame.setSize(1000,600);
 				frame.setVisible(true);
 				BluetoothListener mainListener = new BluetoothListener(mainControl, controlPanel);
 				controlPanel.setBluetoothListener(mainListener);
 				mainListener.start();
 			}
 		});
 	}
 
 }

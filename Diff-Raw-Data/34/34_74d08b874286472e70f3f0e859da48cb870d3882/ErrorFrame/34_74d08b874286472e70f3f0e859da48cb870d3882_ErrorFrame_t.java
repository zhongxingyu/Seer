 package spaceshooters.util;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 public class ErrorFrame extends RuntimeException {
 	private static final long serialVersionUID = -892885264903212691L;
 	
 	public ErrorFrame(String error, boolean crashes) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			System.out.println("Cannot get system's look and feel! :(");
 		}
 		
 		JOptionPane.showMessageDialog(null, error + (crashes ? " \nThe game will be terminated!" : ""), "Error!", JOptionPane.ERROR_MESSAGE);
 		
 		if (crashes) {
 			System.exit(1);
 		}
 	}
 	
 	public ErrorFrame(String error) {
 		this(error, false);
 	}
 	
	public ErrorFrame(Exception e, boolean crashes) throws Exception {
 		this(e.getMessage(), crashes);
 	}
 	
 	protected ErrorFrame(int error) {
 		this(String.valueOf(error));
 	}
 	
 	protected ErrorFrame(boolean error) {
 		this(String.valueOf(error));
 	}
 }

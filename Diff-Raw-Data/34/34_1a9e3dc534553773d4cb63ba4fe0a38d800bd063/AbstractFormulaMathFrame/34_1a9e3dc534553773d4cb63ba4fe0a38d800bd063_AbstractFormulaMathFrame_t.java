 package fr.ickik.formulamath.view;
 
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 /**
  * Abstract which define the default behavior of frames. This abstract class
  * contains common function for views.
  * @author Ickik
 * @version 0.1.006, 5 June 2012
  */
 public abstract class AbstractFormulaMathFrame {
 
 	public static final String NAME = "FormulaMath";
	public static final String VERSION = "0.3.2";
 	private final JFrame frame;
 	
 	/**
 	 * Default constructor of the frame.
 	 */
 	public AbstractFormulaMathFrame() {
 		this.frame = new JFrame(getTitle());
 	}
 	
 	JFrame getFrame() {
 		return frame;
 	}
 
 	public static String getTitle() {
 		return NAME + " " + VERSION;
 	}
 	
 	public void close() {
 		frame.dispose();
 	}
 	
 	void displayFrame() {
 		frame.pack();
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.setIconImages(getIconList());
 		centeredFrame();
 		frame.setVisible(true);
 	}
 	
 	private List<Image> getIconList() {
 		ImageIcon icon = new ImageIcon(AbstractFormulaMathFrame.class.getResource("img/FormulaMath_icon.png"));
 		ImageIcon bigIcon = new ImageIcon(AbstractFormulaMathFrame.class.getResource("img/FormulaMath_big_icon.png"));
 		return Arrays.asList(icon.getImage(), bigIcon.getImage());
 	}
 	
 	/**
 	 * Display a Dialog message box with an error logo.
 	 * @param msg the message to display.
 	 */
 	void displayErrorMessage(String msg) {
 		JOptionPane.showMessageDialog(frame, msg, getTitle() + " - ERROR!", JOptionPane.ERROR_MESSAGE);
 	}
 	
 	/**
 	 * Display a Dialog message box with an information logo.
 	 * @param msg the message to display.
 	 */
 	void displayMessage(String msg) {
 		JOptionPane.showMessageDialog(frame, msg, getTitle(), JOptionPane.INFORMATION_MESSAGE);
 	}
 	
 	/**
 	 * Centered the JFrame in the screen.
 	 */
 	private void centeredFrame() {
 		double w = frame.getWidth();
 		double h = frame.getHeight();
 		double l = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
 		double l2 = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
 		frame.setLocation((int) (l / 2 - w / 2), (int)(l2 / 2 - h / 2));
 	}
 	
 	public void disable() {
 		frame.setEnabled(false);
 	}
 	
 	public void enable() {
 		frame.setEnabled(true);
 		frame.toFront();
 	}
 	
 	public void updateLnF(String className) {
 		SwingUtilities.updateComponentTreeUI(frame);
 	}
 }

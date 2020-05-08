 package emcshop.gui;
 
 import java.awt.Font;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.commons.lang3.exception.ExceptionUtils;
 
 import emcshop.gui.images.ImageManager;
 
 /**
  * Generic dialog for displaying uncaught exceptions.
  * @author Michael Angstadt
  */
 @SuppressWarnings("serial")
 public class ErrorDialog extends JDialog {
 	private static final Logger logger = Logger.getLogger(ErrorDialog.class.getName());
 
 	private ErrorDialog(Window owner, String displayMessage, Throwable thrown, String buttonText) {
 		super(owner, "Error");
 		setModalityType(ModalityType.DOCUMENT_MODAL); //go on top of all windows
 
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 		//close when escape is pressed
 		getRootPane().registerKeyboardAction(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();
 			}
 		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 		JTextArea displayText = new JTextArea(displayMessage);
 		displayText.setEditable(false);
 		displayText.setBackground(getBackground());
 		displayText.setLineWrap(true);
 		displayText.setWrapStyleWord(true);
 
 		//http://stackoverflow.com/questions/1196797/where-are-these-error-and-warning-icons-as-a-java-resource
 		JLabel errorIcon = new JLabel(ImageManager.getErrorIcon());
 
 		JTextArea stackTrace = new JTextArea(ExceptionUtils.getStackTrace(thrown));
 		stackTrace.setEditable(false);
 		stackTrace.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
 
 		JButton close = new JButton(buttonText);
 		close.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();
 			}
 		});
 
 		setLayout(new MigLayout());
 		add(errorIcon, "span 1 3");
		add(displayText, "w 200, align left, wrap"); //TODO when this uses "growx", it won't shrink when the window width decreases
 		JScrollPane scroll = new JScrollPane(stackTrace);
 		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scroll, "grow, align center, wrap");
 		add(close, "align center");
 
 		setSize(500, 300);
 	}
 
 	public static void show(Window owner, String displayMessage, Throwable thrown) {
 		show(owner, displayMessage, thrown, "Close");
 	}
 
 	public static void show(Window owner, String displayMessage, Throwable thrown, String buttonText) {
 		logger.log(Level.SEVERE, displayMessage, thrown);
 		new ErrorDialog(owner, displayMessage, thrown, buttonText).setVisible(true);
 	}
 }

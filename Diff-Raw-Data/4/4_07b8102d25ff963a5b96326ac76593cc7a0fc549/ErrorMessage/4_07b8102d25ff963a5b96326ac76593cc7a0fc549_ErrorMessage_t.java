 package nl.nikhef.jgridstart.gui.util;
 
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.Window;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.logging.Logger;
 
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import nl.nikhef.jgridstart.logging.LogWindowHandler;
 
 /** Uniform way to handle error messages.
  * <p>
  * This shows an error dialog to the user, and logs the exception, if any.
  */
 public class ErrorMessage {
     
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui.util");
     
     /** show an error to the user. This method is for errors that are
      * meaningful to the user, such as an IOException.
      * 
      * @param parent Parent window
      * @param title Title of the dialog
      * @param e Exception to get information from
      * @param extramsg Additional error message details to display
      */
     public static void error(Component parent, String title, Throwable e, String extramsg) {
 	logException(e);
 	String msg = e.getLocalizedMessage();
 	if (msg==null || msg=="") msg = "Unknown error";
 	if (extramsg!=null) msg += "\n" + extramsg;
 	showErrorDialog(parent, title, msg);
     }
     /** @see #error(Component, String, Throwable, String) */
     public static void error(Component parent, String title, Throwable e) {
 	error(parent, title, e, null);
     }
     /** @see #error(Component, String, Throwable, String) */
     public static void error(Component parent, String title, Exception e, String extramsg) {
 	error(parent, title, (Throwable)e, extramsg);
     }
     /** @see #error(Component, String, Throwable, String) */
     public static void error(Component parent, String title, Exception e) {
 	error(parent, title, (Throwable)e, null);
     }
     
     /** show an error to the user. This method is for errors that should
      * not occur; the dialog indicates that the user can contact
      * the developers.
      * 
      * @param parent Parent window
      * @param e Exception to get information from
      */
     public static void internal(Component parent, Throwable e) {
 	logException(e);
 	internal(parent, e.getLocalizedMessage());
     }
     /** @see #internal(Component, Throwable) */
     public static void internal(Component parent, Exception e) {
 	internal(parent, (Throwable)e);
     }
 
     /** show an error to the user. This method is for errors that should
      * not occur; the dialog indicates that the user can contact
      * the developers.
      * 
      * @param parent Parent window
      * @param msg Message to describe to error
      */
     public static void internal(Component parent, String msg) {
 	String s = "I'm sorry to report that an unexpected internal error occured.\n"
 	          +"Please contact technical support for help.\n";
 	// TODO include contact details for technical support
 	showErrorDialog(parent, "Internal problem", s+msg);
     }
     
     /** logs an exception */
     public static void logException(Throwable e) {
 	logger.warning("[Exception] "+e.getMessage());
 	StackTraceElement[] trace = e.getStackTrace();
 	for (int i=0; i<trace.length; i++)
 	    logger.fine("  "+trace[i].toString());
	if (e.getCause()!=null) {
	    logger.fine("[Caused by exception:]");
	    logException(e.getCause());
	}
     }
     
     private static final String btntxtCopy = "Copy detailed log";
     private static final String btntxtOk = "Close";
     
     /** show dialog with error message to user */
     private static void showErrorDialog(Component parent, String title, String msg) {
 	// message
 	JTextArea area = new JTextArea(msg);
 	area.setEditable(false);
 	JLabel dummylbl = new JLabel();
 	area.setBackground(dummylbl.getBackground()); // use JLabel layout 
 	area.setForeground(dummylbl.getForeground());
 	area.setFont(dummylbl.getFont());
 	JScrollPane pane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 	pane.setBorder(null);
 	// dialog
         final JOptionPane optionPane = new JOptionPane(
         	new Object[] { pane },
                 JOptionPane.ERROR_MESSAGE,
                 JOptionPane.YES_NO_OPTION,
                 null,
                 new Object[] {btntxtCopy, btntxtOk}, btntxtOk);
 	final JDialog dlg = createJDialog(parent);
 	dlg.setTitle(title);
         dlg.setContentPane(optionPane);
 	dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 	optionPane.addPropertyChangeListener(
 		new PropertyChangeListener() {
 		    public void propertyChange(PropertyChangeEvent e) {
 			if (dlg.isVisible() && (e.getSource() == optionPane)) {
 			    if (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
 				    && (e.getNewValue().equals(btntxtCopy)) ) {
 				// copy debug log to clipboard
 				LogWindowHandler.getInstance().getWindow().copyToClipboard();
 			    } else if (e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
 				    && (e.getNewValue().equals(btntxtOk)) ) {
 				// close dialog
 				dlg.dispose();
 			    }
 			}
 		    }
 		});
 	dlg.pack();
 	dlg.setModal(true);
 	dlg.setVisible(true);
     }
     
     /** Create new {@linkplain JDialog} with a parent {@linkplain Component}.
      * <p>
      * If the component is no {@linkplain Frame} or {@linkplain JDialog},
      * a new unparented dialog is returned.
      * <p>
      * {@linkplain JDialog} has no constructor with {@linkplain Window} on
      * Java5 and below, so that is not used. */
     protected static JDialog createJDialog(Component parent) {
 	if (parent instanceof Frame) return new JDialog((Frame)parent);
 	else if (parent instanceof JDialog) return new JDialog((JDialog)parent);
 	else return new JDialog();
     }
 }

 package org.vpac.grisu.client.view.swing.utils;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import org.apache.log4j.Logger;
 import org.jdesktop.swingx.JXErrorPane;
 import org.vpac.grisu.client.control.EnvironmentManager;
 import org.vpac.grisu.client.view.swing.mainPanel.Grisu;
 import org.vpac.helpDesk.model.HelpDesk;
 import org.vpac.helpDesk.model.HelpDeskNotAvailableException;
 import org.vpac.helpDesk.model.Person;
 import org.vpac.helpDesk.model.PersonException;
 import org.vpac.helpDesk.view.DispalyErrorMessageUsingJXErrorPane;
 import org.vpac.helpDesk.view.HelpDeskErrorDialog;
 
 public class Utils {
 	
 	static final Logger myLogger = Logger.getLogger(Utils.class
 			.getName());
 
 	private static ResourceBundle errorMessages = ResourceBundle.getBundle("ErrorMessagesBundle", Locale.getDefault());
 
 	public static JScrollPane getMessagePane(String message) {
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setPreferredSize(new Dimension(400, 200));
 
 		JTextArea pane = new JTextArea(message, 0, 40);
 		pane.setLineWrap(true);
 		// pane.setText(message);
 
 		scrollPane.setViewportView(pane);
 
 		return scrollPane;
 	}
 	
 	/**
 	 * This shows an error dialog with details about the error and the possibility for the user to 
 	 * connect to a {@link HelpDesk}. Be sure that the {@link EnvironmentManager#getDefaultManager()} is already initialized if
 	 * you use this one.
 	 * @param parent the parent window
 	 * @param message the error message
 	 * @param e the exception (or null)
 	 */
 	public static void showErrorMessage(EnvironmentManager em, Component parent, String message, Exception e) {
 		
 		Person user = null;
 		
 		if ( em == null ) {
 			try {
 				user = new Person("Anonymous");
 			} catch (PersonException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		} else { 
 			user = em.getUser();
 		}
 		
 		showErrorMessage(user, parent, message, e);
 	}
 
 	public static void showErrorMessage(Person user, Component parent, String message,
 			Throwable e) {
 
 		String message_new = null;
 		if (e == null) {
 			message_new = errorMessages.getString(message + ".error") + ".";
 		} else {
 			String em = errorMessages.getString(message + ".error");
 			if ( em == null || "".equals(em) ) {
 				message_new = e.getLocalizedMessage();
 			} else {
				message_new = em + "\n"
				+ e.getLocalizedMessage()+"\n\n";
 			}
 		}
 		
 //		HelpDeskErrorDialog errdialog = new HelpDeskErrorDialog();
 		
 		File grisuDebugFile = new File(System.getProperty("user.home")+File.separator+".grisu", "grisu.debug");
 		
 		try {
 //			errdialog.initialize(new String[]{"org.vpac.helpDesk.model.anonymousRT.AnonymousRTHelpDesk", "org.vpac.helpDesk.model.irc.IrcHelpDesk", "org.vpac.helpDesk.model.trac.TracHelpDesk"}, 
 //			errdialog.initialize(new String[]{"org.vpac.helpDesk.model.anonymousRT.AnonymousRTHelpDesk", "org.vpac.helpDesk.model.trac.TracHelpDesk"}, 
 //				"support.properties", user, errorMessages.getString(message+".title"), message_new, new Object[]{e, grisuDebugFile});
 			DispalyErrorMessageUsingJXErrorPane.display(parent, user, errorMessages.getString(message+".title"), message_new, new Object[]{e, grisuDebugFile}, Grisu.DEFAULT_HELPDESK_CLASSES, Grisu.HELPDESK_CONFIG);
 		} catch (HelpDeskNotAvailableException e1) {
 			JOptionPane.showMessageDialog(null,
 				    "Could not connect to help desk:\n"+e.getLocalizedMessage(),
 				    "Connection error",
 				    JOptionPane.ERROR_MESSAGE);
 		}
 		
 //		errdialog.setVisible(true);
 //		JOptionPane.showMessageDialog(parent,
 //				Utils.getMessagePane(message_new), errorMessages
 //						.getString(message + ".title"),
 //				JOptionPane.ERROR_MESSAGE);
 
 	}
 
 	public static void showErrorMessage(Component parent, Person user, String message,
 			String message2, Exception e) {
 
 		String message_new = null;
 		if (e == null) {
 			message_new = errorMessages.getString(message + ".error") + " "
 					+ message2 + ".";
 		} else {
 			message_new = errorMessages.getString(message + ".error") + " "
 					+ message2 + ".";// + e.getMessage();
 		}
 		
 //		JXErrorPane.showDialog(e);
 
 		
 //		HelpDeskErrorDialog errdialog = new HelpDeskErrorDialog();
 		
 
 		File grisuDebugFile = new File(System.getProperty("user.home")+File.separator+".grisu", "grisu.debug");
 
 		try {
 //			errdialog.initialize(new String[]{"org.vpac.helpDesk.model.irc.IrcHelpDesk", "org.vpac.helpDesk.model.trac.TracHelpDesk"}, 
 //				"support.properties", user, errorMessages.getString(message+".title"), message_new, new Object[]{e, grisuDebugFile});
 			DispalyErrorMessageUsingJXErrorPane.display(parent, user, errorMessages.getString(message+".title"), message_new, new Object[]{e, grisuDebugFile}, Grisu.DEFAULT_HELPDESK_CLASSES, Grisu.HELPDESK_CONFIG);
 		} catch (HelpDeskNotAvailableException e1) {
 			JOptionPane.showMessageDialog(null,
 				    "Could not connect to help desk: "+e.getLocalizedMessage(),
 				    "Connection error",
 				    JOptionPane.ERROR_MESSAGE);
 		}
 		
 //		JOptionPane.showMessageDialog(parent,
 //				Utils.getMessagePane(message_new), errorMessages
 //						.getString(message + ".title"),
 //				JOptionPane.ERROR_MESSAGE);
 
 	}
 
 	public static String getStackTrace(Throwable t) {
 		StringWriter sw = new StringWriter();
 		PrintWriter pw = new PrintWriter(sw, true);
 		t.printStackTrace(pw);
 		pw.flush();
 		sw.flush();
 		return sw.toString();
 	}
 	
 
 
 }

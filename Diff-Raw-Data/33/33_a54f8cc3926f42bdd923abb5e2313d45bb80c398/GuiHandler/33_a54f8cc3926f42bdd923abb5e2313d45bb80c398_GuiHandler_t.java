 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.sourceforge.frcsimulator.gui;
 
import java.io.OutputStream;
import java.io.PrintStream;
 import net.sourceforge.frcsimulator.Client;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import javax.swing.JOptionPane;
 
 /**
  * Display an error dialog for errors, then die.
  * @author wolf
  */
 class GuiHandler extends Handler {
	PrintStream systemErr;
	public GuiHandler(PrintStream iSystemErr) {
		systemErr=iSystemErr;
	}
 	@Override
 	public void publish(LogRecord lr) {
 		if (lr.getLevel().equals(Level.SEVERE) || lr.getLevel().equals(Level.WARNING)) {
 			boolean severe = lr.getLevel().equals(Level.SEVERE);
 			Object[] message = {severe?"Fatal error":"Warning",lr.getMessage(),null,null,null};
 			if (lr.getThrown() != null) {
				lr.getThrown().printStackTrace(systemErr);
 				message[2] = lr.getThrown().getClass().toString();
 				message[3] = lr.getThrown().getMessage();
 			}
 			if (lr.getParameters() != null) {
 				message[4] = lr.getParameters();
 			}
 			JOptionPane.showMessageDialog(null,message,
 					"Fatal Error"+(lr.getMessage()==null?"":" - "+lr.getMessage()),
 					severe?JOptionPane.ERROR_MESSAGE:JOptionPane.WARNING_MESSAGE);
 			if (severe) System.exit(Client.E_SIMFAIL);
 		}
 	}
 	@Override
 	public void flush() {
 		// Nothing needs to be flushed.
 	}
 	@Override
 	public void close() throws SecurityException {
 		// Nothing needs to be closed.
 	}
 }

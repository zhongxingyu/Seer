 package main;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 
 import javax.swing.JOptionPane;
 
 import org.picocontainer.Startable;
 
 import ui.swing.utils.PatchacaUncaughtExceptionHandler;
 import wheel.io.files.Directory;
 
 public class PathcacaDefaultExceptionHandler implements Startable,
 		PatchacaUncaughtExceptionHandler {
 
 	private final Directory directory;
 
 	public PathcacaDefaultExceptionHandler(final Directory directory) {
 		this.directory = directory;
 	}
 
 	@Override
 	public void start() {
 		Thread.setDefaultUncaughtExceptionHandler(this);
 	}
 
 	@Override
 	public void stop() {
 		// Nothing special.
 	}
 
 	@Override
 	public synchronized void uncaughtException(final Thread t, final Throwable e) {
 		logExceptionToErrorLog(e);
 		e.printStackTrace();
 		JOptionPane
 				.showMessageDialog(
 						null,
 						"An error has occured, see ~/.jpatchaca/error.log for further information. It's recomended to restart the application.");
 	}
 
 	private void logExceptionToErrorLog(final Throwable e) {
 		OutputStream log = null;
 		try {
 			log = directory.openFileForAppend("error.log");
 		} catch (final IOException e1) {
 			e.printStackTrace();
 			e1.printStackTrace();
 		}
 
 		try {
 			final PrintWriter s = new PrintWriter(log);
 			e.printStackTrace(s);
 			s.flush();
 		} finally {
			close(log);
 		}
 	}
 
 	private void close(final OutputStream log) {
 		try {
 			log.close();
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }

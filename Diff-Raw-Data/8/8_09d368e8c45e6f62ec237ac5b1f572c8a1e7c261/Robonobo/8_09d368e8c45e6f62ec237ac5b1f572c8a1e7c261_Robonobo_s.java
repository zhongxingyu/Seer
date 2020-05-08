 package com.robonobo;
 
 import static com.robonobo.common.util.TextUtil.*;
 
 import java.awt.GraphicsEnvironment;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 
 import javax.swing.SwingUtilities;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.Errot;
 import com.robonobo.common.util.TextUtil;
 import com.robonobo.console.RobonoboConsole;
 import com.robonobo.core.*;
 import com.robonobo.core.api.RobonoboStatus;
 import com.robonobo.eon.EONException;
 import com.robonobo.gui.frames.EULAFrame;
 import com.robonobo.gui.frames.RobonoboFrame;
 
 /**
  * Just a mainline - starts a RobonoboFrame or RobonoboConsole as appropriate
  * 
  * @author macavity
  */
 public class Robonobo {
 	private static final String HTML_EULA_PATH = "/eula.html";
 	private static final String TEXT_EULA_PATH = "/eula.txt";
 
 	public static void main(String[] args) throws Exception {
 		// 1st-stage arg checker
 		boolean consoleOnly = false;
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].equalsIgnoreCase("-console"))
 				consoleOnly = true;
 		}
 		if (GraphicsEnvironment.isHeadless())
 			consoleOnly = true;
 		// Is there an instance already running?
 		RobonoboRuntime rt = new RobonoboRuntime(homeDir());
 		boolean handedOver;
 		try {
 			handedOver = rt.handoverIfRunning(argForRunningInstance(args));
 		} catch(EONException e) {
 			// TODO: there is a running instance, but it's wedged - ask the user to kill it...
 			throw new Errot("Wedged rbnb instance");
 		}
 		if(handedOver) {
 			System.exit(0);
 		}
 		Platform.getPlatform().init();
 		if (!consoleOnly)
 			Platform.getPlatform().setLookAndFeel();
 		checkEulaAndStartup(args, consoleOnly);
 	}
 
 	public static File homeDir() {
 		if (System.getenv().containsKey("ROBOHOME"))
 			return new File(System.getenv("ROBOHOME"));
 		else
 			return Platform.getPlatform().getDefaultHomeDirectory();
 	}
 	
 	public static String argForRunningInstance(String[] args) {
 		for (String arg : args) {
 			if(!"-console".equals(arg))
 				return arg;
 		}
 		return null;
 	}
 	
 	public static void checkEulaAndStartup(final String[] args, boolean consoleOnly) throws Exception {
 		// Make sure they've agreed to the eula
 		final RobonoboController control = new RobonoboController(args);
 		if (control.getConfig().getAgreedToEula())
 			startup(control, args, consoleOnly);
 		else {
 			if (consoleOnly) {
 				boolean acceptedEula = showConsoleEula();
 				if (acceptedEula) {
 					control.getConfig().setAgreedToEula(true);
 					control.saveConfig();
 					startup(control, args, true);
 				} else {
 					System.exit(0);
 				}
 			} else {
 				CatchingRunnable onAccept = new CatchingRunnable() {
 					public void doRun() throws Exception {
 						control.getConfig().setAgreedToEula(true);
 						control.saveConfig();
 						startup(control, args, false);
 					}
 				};
 				CatchingRunnable onCancel = new CatchingRunnable() {
 					public void doRun() throws Exception {
 						System.exit(0);
 					}
 				};
 				EULAFrame eulaFrame = new EULAFrame(HTML_EULA_PATH, control.getExecutor(), onAccept, onCancel);
 				eulaFrame.setVisible(true);
 			}
 		}
 
 	}
 
 	private static boolean showConsoleEula() throws IOException {
 		// Copy the eula to a temporary file and ask them to read it
 		InputStream is = Robonobo.class.getResourceAsStream(TEXT_EULA_PATH);
 		File eulaFile = File.createTempFile("robonobo-eula-", ".txt");
 		OutputStream os = new FileOutputStream(eulaFile);
 		byte[] buf = new byte[1024];
 		int numRead;
 		while ((numRead = is.read(buf)) > 0) {
 			os.write(buf, 0, numRead);
 		}
 		is.close();
 		os.close();
 		String promptText = "The robonobo End-User License Agreement has been copied to the file "
 				+ eulaFile.getAbsolutePath()
 				+ " - please read this file carefully and then type 'accept' or 'cancel' below.  By typing 'accept', you are agreeing to the terms of the agreement.\n";
 		PrintStream out = System.out;
 		out.println(promptText);
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		try {
 			while (true) {
 				out.print("> ");
 				String response = in.readLine();
 				if (response.equalsIgnoreCase("accept"))
 					return true;
 				if (response.equalsIgnoreCase("cancel"))
 					return false;
 				out.println("Please type either 'accept' or 'cancel'.");
 			}
 		} finally {
 			eulaFile.delete();
 		}
 	}
 
 	/**
 	 * If this is a cold startup, argControl will be non-null as we need to create a controller to see if they've agreed
 	 * to the eula. If it's a restart, argControl will be null, so we make a new controller
 	 */
	public static void startup(RobonoboController argControl, String[] args, boolean consoleOnly) throws Exception,
 			InterruptedException {
 		final RobonoboController control = (argControl == null) ? new RobonoboController(args) : argControl;
 
 		// Start the controller and the gui in parallel
 		Thread cThread = new Thread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				control.start();
 			}
 		});
 		cThread.start();
 
 		if (consoleOnly) {
 			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 			PrintWriter out = new PrintWriter(System.out);
 			RobonoboConsole console = new RobonoboConsole(control, in, out);
 			Thread consoleThread = new Thread(console);
 			consoleThread.start();
 			// If the user has login details entered, perform the login here (gui handles this in RobonoboFrame.setVisible() to show the login sheet)
 			if(isNonEmpty(control.getConfig().getMetadataUsername()))
 				control.login(control.getConfig().getMetadataUsername(), control.getConfig().getMetadataPassword());
 		} else {
			final RobonoboFrame frame = new RobonoboFrame(control, args);
			Platform.getPlatform().initMainWindow(frame);
 			SwingUtilities.invokeLater(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					frame.setVisible(true);
 				}
 			});
 		}
 	}
 }

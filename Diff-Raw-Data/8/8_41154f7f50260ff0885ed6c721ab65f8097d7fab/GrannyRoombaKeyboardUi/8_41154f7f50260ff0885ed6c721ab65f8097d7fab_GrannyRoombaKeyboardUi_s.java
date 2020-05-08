 package org.flupes.ljf.grannyroomba.pctests;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.TTCCLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.flupes.ljf.grannyroomba.net.RoombaLocomotorClient;
 import org.flupes.ljf.grannyroomba.net.ServoClient;
 import org.flupes.ljf.grannyroomba.pctests.SwtKeyboardController;
 
 public class GrannyRoombaKeyboardUi {
 
 	static Logger s_logger = Logger.getLogger("grannyroomba");
 
 	static final String IMAGE_FILE = "KeyboardController.png";
 	
 	protected static final boolean m_debug = true;
 
 	enum Mode {
 		LOCAL("local", "localhost", 6666, 7777),
 		PRIVATE("private", "172.16.0.39", 3333, 4444),
 		PUBLIC("public", "67.188.2.6", 3140, 3141);
 
 		final String mode;
 		final String host;
 		final Integer servoPort;
 		final Integer locoPort;
 
 		Mode(String mode, String host, Integer servoPort, Integer locoPort) {
 			this.mode = mode;
 			this.host = host;
 			this.servoPort = servoPort;
 			this.locoPort = locoPort;
 		}
 
 	};
 
 	public static void main(String[] args) {
 
 		Mode connectMode = Mode.PRIVATE; 
 		String modeStr = System.getProperties().getProperty("connection");
 		if ( modeStr != null ) { // override default connection mode
 			if ( 0 == modeStr.compareToIgnoreCase(Mode.LOCAL.mode) ) {
 				connectMode = Mode.LOCAL;
 			}
 			else if ( 0 == modeStr.compareToIgnoreCase(Mode.PRIVATE.mode) ) {
 				connectMode = Mode.PRIVATE;
 			}
 			else if ( 0 == modeStr.compareToIgnoreCase(Mode.PUBLIC.mode) ) {
 				connectMode = Mode.PUBLIC;
 			}
 		}
 
 		// initialize logger
 		s_logger.setLevel(Level.TRACE);
 		System.out.println("Connect Mode = " + connectMode);
 		Appender appender;
 		if ( connectMode == Mode.PUBLIC) {
 			String homeDir = System.getProperty("user.home");
 			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
 			String logFile = homeDir+"/granny_roomba_"+timeStamp+".txt";
 			try {
 				appender = new FileAppender(new TTCCLayout(), logFile);
 			} catch (IOException e) {
 				System.err.println("Could not create log file: "+logFile);
 				e.printStackTrace();
 				return;
 			}
 		}
 		else {
 			appender = new ConsoleAppender(new TTCCLayout(), ConsoleAppender.SYSTEM_OUT);
 		}
 		s_logger.addAppender(appender);
 
 		Display display = new Display( );
 
 		Shell shell = new Shell (display);
 		shell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
 
 		GridLayout gl = new GridLayout();
 		gl.marginBottom = 4;
 		gl.marginTop = 4;
 		gl.marginLeft = 4;
 		gl.marginRight = 4;
 		shell.setLayout (gl);
 		Image image = new Image (display, SwtKeyboardController.class.getResourceAsStream (IMAGE_FILE));
 
 		Group group = new Group(shell, SWT.NONE);
 		GridData gd = new GridData();
 		//	gd.horizontalAlignment = GridData.FILL_HORIZONTAL;
 		//	gd.verticalAlignment = GridData.FILL_VERTICAL;
 		//	gd.grabExcessHorizontalSpace = true;
 		//	gd.grabExcessVerticalSpace = true;
 		//	gd.horizontalAlignment = GridData.CENTER;
 		
 		// WTF: why these dimension have to be smaller
 		// than the real image!!!
 		gd.heightHint = 294;
 		gd.widthHint = 464;
 
 		group.setLayoutData(gd);
 		//	group.setSize(240, 320);
 		group.setBackgroundImage(image);
 
 		// Create client and keyboard dispatcher
 		String host = connectMode.host;
 		int servoPort = connectMode.servoPort;
 		int locoPort = connectMode.locoPort;
 
 		ServoClient servoClient = new ServoClient(host, servoPort);
 		RoombaLocomotorClient locoClient = new RoombaLocomotorClient(host, locoPort);
 
 		servoClient.connect();
 		locoClient.connect();
 
 		boolean connected = true;
 		// check if connection is up
 		try {
 			locoClient.getStatus();
 		} catch (Exception e) {
 			s_logger.error("locoClient.getStatus() failed -> exit");
 			MessageBox msg = new MessageBox(shell, SWT.OK);
			msg.setMessage("Connection to GrannyRoomba failed!\nTry again later");
 			msg.open();	
 			connected = false;
 		}
 
 		if ( connected ) {
 
 			SwtKeyboardController kc = new SwtKeyboardController(servoClient, locoClient);
 			shell.addKeyListener(kc.controller());
 			shell.addFocusListener(kc.stopper());
 
 			shell.pack ();
 			shell.open ();
 
 			while ( !shell.isDisposed() && kc.connected() ) {
 				if (!display.readAndDispatch ()) display.sleep ();
 			}
 			if ( kc.connected() ) {
 				kc.cancel();
 			}
 		}
 
 		locoClient.disconnect();
 		servoClient.disconnect();
 
 		image.dispose ();
 		display.dispose ();
 	}
 
 }

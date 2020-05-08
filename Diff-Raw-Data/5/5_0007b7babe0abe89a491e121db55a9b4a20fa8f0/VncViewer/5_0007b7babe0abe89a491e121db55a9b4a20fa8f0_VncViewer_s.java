 //
 //  Copyright (C) 2001-2004 HorizonLive.com, Inc.  All Rights Reserved.
 //  Copyright (C) 2002 Constantin Kaplinsky.  All Rights Reserved.
 //  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
 //
 //  This is free software; you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation; either version 2 of the License, or
 //  (at your option) any later version.
 //
 //  This software is distributed in the hope that it will be useful,
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 //
 //  You should have received a copy of the GNU General Public License
 //  along with this software; if not, write to the Free Software
 //  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 //  USA.
 //
 
 //
 // VncViewer.java - the VNC viewer applet.  This class mainly just sets up the
 // user interface, leaving it to the VncCanvas to do the actual rendering of
 // a VNC desktop.
 //
 
 package com.tigervnc;
 
 import java.applet.Applet;
import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.Panel;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.EOFException;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.NoRouteToHostException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.tigervnc.rfb.Encodings;
 import com.tigervnc.rfb.RfbProto;
 import com.tigervnc.ui.AuthPanel;
 import com.tigervnc.ui.OptionsFrame;
 import com.tigervnc.ui.ReloginPanel;
 import com.tigervnc.ui.VncCanvas;
 
 public class VncViewer implements java.lang.Runnable,
 		WindowListener, ComponentListener {
 
 	public static Logger logger = Logger.getLogger(VncViewer.class);
 	static{
 		logger.setLevel(Level.INFO);
 	}
 	public static boolean inAnApplet = true;
 	public static boolean inSeparateFrame = false;
 	public static Applet applet;
 
 	//
 	// main() is called when run as a java program from the command line.
 	// It simply runs the applet inside a newly-created frame.
 	//
 
 	public static void main(String[] argv) {
		VncViewer v = new VncViewer();
		v.mainArgs = argv;
 	}
 
 	public String[] mainArgs;
 	public RfbProto rfb;
 	
 	Thread rfbThread;
 
 	public JFrame vncFrame;
 	public Container vncContainer;
 	public JLabel connStatusLabel;
 	public VncCanvas vncCanvas;
 	public Panel canvasPanel = new Panel();
 	public OptionsFrame options;
 
 	String cursorUpdatesDef;
 	String eightBitColorsDef;
 
 	// Variables read from parameter values.
 	public String socketFactory;
 	public String host;
 	public String windowTitle;
 	public int port;
 	public String log_level;
 	public String passwordParam;
 	public boolean showControls;
 	public boolean offerRelogin;
 	public boolean showOfflineDesktop;
 	public int deferScreenUpdates;
 	public int deferCursorUpdates;
 	public int deferUpdateRequests;
 	public int debugStatsExcludeUpdates;
 	public int debugStatsMeasureUpdates;
 
 
 	public VncViewer(String[] argv) {
 		BasicConfigurator.configure();
 		mainArgs = argv;
 		
 		readParameters();
 		setLogLevel(log_level);
 
 		vncFrame = new JFrame("TigerVNC");
 		vncFrame.setResizable(false);
 		vncContainer = vncFrame;
 
 		options = new OptionsFrame(this);
 		
 		cursorUpdatesDef = null;
 		eightBitColorsDef = null;
 
 
 		if (inSeparateFrame) {
 			vncFrame.addWindowListener(this);
 			vncFrame.addComponentListener(this);
 		}
 
 		rfbThread = new Thread(this);
 		rfbThread.start();
 	}
 
 	public void update(Graphics g) {}
 
 	//
 	// run() - executed by the rfbThread to deal with the RFB socket.
 	//
 	public void run() {
 
 		try {
 			connectAndAuthenticate();
 			doProtocolInitialisation();
 			createCanvas(0, 0);
 
 			// Create a panel which itself is resizeable and can hold
 			// non-resizeable VncCanvas component at the top left corner.
 			canvasPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
 			canvasPanel.add(vncCanvas);
 
 			// If auto scale is not enabled we don't need to set first frame
 			// size to fullscreen
 			if (!options.autoScale) {
 				vncCanvas.isFirstSizeAutoUpdate = false;
 			}
 
 			// Finally, add our panel to the Frame window.
 			vncFrame.add(canvasPanel);
 			vncFrame.setTitle(windowTitle);
 			vncFrame.pack();
 
 			moveFocusToDesktop();
 			processNormalProtocol();
 
 		} catch (NoRouteToHostException e) {
 			fatalError("Network error: no route to server: " + host, e);
 		} catch (UnknownHostException e) {
 			fatalError("Network error: server name unknown: " + host, e);
 		} catch (ConnectException e) {
 			String msg = "Network error: could not connect to server: " + host+ ":" + port;
 			VncEventPublisher.publish(VncEvent.CONNECTION_ERROR, msg, e);
 
 		} catch (EOFException e) {
 			if (showOfflineDesktop) {
 				e.printStackTrace();
 				System.out
 						.println("Network error: remote side closed connection");
 				if (vncCanvas != null) {
 					vncCanvas.enableInput(false);
 				}
 				vncFrame.setTitle(windowTitle + " [disconnected]");
 				
 				if (rfb != null && !rfb.closed())
 					rfb.close();
 			} else {
 				fatalError("Network error: remote side closed connection", e);
 			}
 		} catch (IOException e) {
 			String str = e.getMessage();
 			if (str != null && str.length() != 0) {
 				fatalError("Network Error: " + str, e);
 			} else {
 				fatalError(e.toString(), e);
 			}
 		} catch (Exception e) {
 			String str = e.getMessage();
 			if (str != null && str.length() != 0) {
 				fatalError("Error: " + str, e);
 			} else {
 				fatalError(e.toString(), e);
 			}
 		}
 
 	}
 
 	//
 	// Create a VncCanvas instance.
 	//
 	void createCanvas(int maxWidth, int maxHeight) throws IOException {
 		// Determine if Java 2D API is available and use a special
 		// version of VncCanvas if it is present.
 		vncCanvas = null;
 		try {
 			// This throws ClassNotFoundException if there is no Java 2D API.
 			Class cl = Class.forName("java.awt.Graphics2D");
 			// If we could load Graphics2D class, then we can use VncCanvas2D.
 			cl = Class.forName("com.tigervnc.vncviewer.VncCanvas2");
 			Class[] argClasses = { this.getClass(), Integer.TYPE, Integer.TYPE };
 			java.lang.reflect.Constructor cstr = cl.getConstructor(argClasses);
 			Object[] argObjects = { this, new Integer(maxWidth),
 					new Integer(maxHeight) };
 			vncCanvas = (VncCanvas) cstr.newInstance(argObjects);
 		} catch (Exception e) {
 			logger.warn("Java 2D API is not available");
 		}
 
 		// If we failed to create VncCanvas2D, use old VncCanvas.
 		if (vncCanvas == null)
 			vncCanvas = new VncCanvas(this, maxWidth, maxHeight);
 	}
 
 	//
 	// Process RFB socket messages.
 	// If the rfbThread is being stopped, ignore any exceptions,
 	// otherwise rethrow the exception so it can be handled.
 	//
 
 	void processNormalProtocol() throws Exception {
 		try {
 			vncCanvas.processNormalProtocol();
 		} catch (Exception e) {
 			if (rfbThread == null) {
 				logger.info("Ignoring RFB socket exceptions"
 						+ " because applet is stopping");
 			} else {
 				throw e;
 			}
 		}
 	}
 
 	//
 	// Connect to the RFB server and authenticate the user.
 	//
 
 	void connectAndAuthenticate() throws Exception {
 		showConnectionStatus("Initializing...");
 		
         vncFrame.pack();
         vncFrame.show();
 
 		showConnectionStatus("Connecting to " + host + ", port " + port + "...");
 
 		rfb = new RfbProto(host, port, this);
 		showConnectionStatus("Connected to server");
 
 		rfb.readVersionMsg();
 		showConnectionStatus("RFB server supports protocol version "
 				+ rfb.serverVersion);
 
 		rfb.writeVersionMsg();
 		showConnectionStatus("Using RFB protocol version " + rfb.clientVersion);
 
 		int secType = rfb.negotiateSecurity();
 		int authType;
 		if (secType == Encodings.SecTypeTight) {
 			showConnectionStatus("Enabling TightVNC protocol extensions");
 			rfb.setupTunneling();
 			authType = rfb.negotiateAuthenticationTight();
 		} else {
 			authType = secType;
 		}
 
 		switch (authType) {
 		case Encodings.AuthNone:
 			showConnectionStatus("No authentication needed");
 			rfb.authenticateNone();
 			break;
 		case Encodings.AuthVNC:
 			showConnectionStatus("Performing standard VNC authentication");
 			if (passwordParam != null) {
 				rfb.authenticateVNC(passwordParam);
 			} else {
 				String pw = askPassword();
 				rfb.authenticateVNC(pw);
 			}
 			break;
 		default:
 			throw new Exception("Unknown authentication scheme " + authType);
 		}
 	}
 
 	//
 	// Show a message describing the connection status.
 	// To hide the connection status label, use (msg == null).
 	//
 
 	void showConnectionStatus(String msg) {
 		return;
 	}
 
 	//
 	// Show an authentication panel.
 	//
 	void doAuthentification(int secType) throws Exception {
 		switch (secType) {
 		case Encodings.SecTypeNone:
 			showConnectionStatus("No authentication needed");
 			rfb.authenticateNone();
 			break;
 		case Encodings.SecTypeVncAuth:
 			showConnectionStatus("Performing standard VNC authentication");
 			if (passwordParam != null) {
 				rfb.authenticateVNC(passwordParam);
 			} else {
 				String pw = askPassword();
 				rfb.authenticateVNC(pw);
 			}
 			break;
 		case Encodings.SecTypeVeNCrypt:
 			showConnectionStatus("VeNCrypt chooser");
 			secType = rfb.authenticateVeNCrypt();
 			doAuthentification(secType);
 			break;
 		case Encodings.SecTypePlain:
 			showConnectionStatus("Plain authentication");
 			{
 				String user = askUser();
 				String pw = askPassword();
 				rfb.authenticatePlain(user, pw);
 			}
 			break;
 		default:
 			throw new Exception("Unknown authentication scheme " + secType);
 		}
 	}
 
 	String askPassword() throws Exception {
 		showConnectionStatus(null);
 
 		AuthPanel authPanel = new AuthPanel(this, true);
 
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		gbc.anchor = GridBagConstraints.NORTHWEST;
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
 		gbc.ipadx = 100;
 		gbc.ipady = 50;
 
 		vncContainer.add(authPanel);
 		vncFrame.pack();
 		
 		authPanel.moveFocusToDefaultField();
 		String pw = authPanel.getPassword();
 		vncContainer.remove(authPanel);
 
 		return pw;
 	}
 
 	//
 	// Do the rest of the protocol initialisation.
 	//
 
 	void doProtocolInitialisation() throws IOException {
 		rfb.writeClientInit();
 		rfb.readServerInit();
 
 		logger.info("Desktop size is " + rfb.server.fb_width + " x "
 				+ rfb.server.fb_height);
 		
 	}
 
 	String askUser() throws Exception {
 
 		showConnectionStatus(null);
 
 		AuthPanel authPanel = new AuthPanel(this, false);
 		vncContainer.add(authPanel);
 		
         vncFrame.pack();
 
 		authPanel.moveFocusToDefaultField();
 		String pw = authPanel.getPassword();
 		vncContainer.remove(authPanel);
 
 		return pw;
 	}
 
 	//
 	// Send current encoding list to the RFB server.
 	//
 
 	List<Integer> encodingsSaved = new ArrayList<Integer>();
 	int nEncodingsSaved;
 
 	public void setEncodings() {
 		setEncodings(false);
 	}
 
 	public void autoSelectEncodings() {
 		setEncodings(true);
 	}
 
 	void setEncodings(boolean autoSelectOnly) {
 		if (options == null || rfb == null || !rfb.inNormalProtocol)
 			return;
 
 		int preferredEncoding = options.preferredEncoding;
 		if (preferredEncoding == -1) {
 			long kbitsPerSecond = rfb.kbitsPerSecond();
 			if (nEncodingsSaved < 1) {
 				// Choose Tight or ZRLE encoding for the very first update.
 				logger.info("Using Tight/ZRLE encodings");
 				preferredEncoding = Encodings.EncodingTight;
 			} else if (kbitsPerSecond > 2000
 					&& encodingsSaved.get(0) != Encodings.EncodingHextile) {
 				// Switch to Hextile if the connection speed is above 2Mbps.
 				logger.info("Throughput " + kbitsPerSecond
 						+ " kbit/s - changing to Hextile encoding");
 				preferredEncoding = Encodings.EncodingHextile;
 			} else if (kbitsPerSecond < 1000
 					&& encodingsSaved.get(0) != Encodings.EncodingTight) {
 				// Switch to Tight/ZRLE if the connection speed is below 1Mbps.
 				logger.info("Throughput " + kbitsPerSecond
 						+ " kbit/s - changing to Tight/ZRLE encodings");
 				preferredEncoding = Encodings.EncodingTight;
 			} else {
 				// Don't change the encoder.
 				if (autoSelectOnly)
 					return;
 				preferredEncoding = encodingsSaved.get(0);
 			}
 		} else {
 			// Auto encoder selection is not enabled.
 			if (autoSelectOnly)
 				return;
 		}
 
 		ArrayList<Integer> encodings = new ArrayList<Integer>();
 
 		encodings.add(preferredEncoding);
 		if (options.useCopyRect) {
 			encodings.add(Encodings.EncodingCopyRect);
 		}
 
 		if (preferredEncoding != Encodings.EncodingTight) {
 			encodings.add(Encodings.EncodingTight);
 		}
 		if (preferredEncoding != Encodings.EncodingZRLE) {
 			encodings.add(Encodings.EncodingZRLE);
 		}
 		if (preferredEncoding != Encodings.EncodingHextile) {
 			encodings.add(Encodings.EncodingHextile);
 		}
 		if (preferredEncoding != Encodings.EncodingZlib) {
 			encodings.add(Encodings.EncodingZlib);
 		}
 		if (preferredEncoding != Encodings.EncodingCoRRE) {
 			encodings.add(Encodings.EncodingCoRRE);
 		}
 		if (preferredEncoding != Encodings.EncodingRRE) {
 			encodings.add(Encodings.EncodingRRE);
 		}
 
 		if (options.compressLevel >= 0 && options.compressLevel <= 9) {
 			encodings.add(Encodings.EncodingCompressLevel0
 					+ options.compressLevel);
 		}
 		if (options.jpegQuality >= 0 && options.jpegQuality <= 9) {
 			encodings
 					.add(Encodings.EncodingQualityLevel0 + options.jpegQuality);
 		}
 
 		if (options.requestCursorUpdates) {
 			encodings.add(Encodings.EncodingXCursor);
 			encodings.add(Encodings.EncodingRichCursor);
 			if (!options.ignoreCursorUpdates)
 				encodings.add(Encodings.EncodingPointerPos);
 		}
 
 		encodings.add(Encodings.ENCODING_EXTENDED_KEY_EVENT);
 		encodings.add(Encodings.EncodingLastRect);
 		encodings.add(Encodings.EncodingNewFBSize);
 
 		boolean encodingsWereChanged = false;
 		if (encodings.size() != nEncodingsSaved) {
 			encodingsWereChanged = true;
 		} else {
 			for (int i = 0; i < encodings.size(); i++) {
 				if (encodings.get(i) != encodingsSaved.get(i)) {
 					encodingsWereChanged = true;
 					break;
 				}
 			}
 		}
 
 		if (encodingsWereChanged) {
 			try {
 				rfb.writeSetEncodings(encodings);
 				if (vncCanvas != null) {
 					vncCanvas.softCursorFree();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			encodingsSaved = (ArrayList<Integer>) encodings.clone();
 			nEncodingsSaved = encodings.size();
 		}
 	}
 
 	//
 	// setCutText() - send the given cut text to the RFB server.
 	//
 
 	public void setCutText(String text) {
 		try {
 			if (rfb != null && rfb.inNormalProtocol) {
 				rfb.writeClientCutText(text);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	//
 	// readParameters() - read parameters from the html source or from the
 	// command line. On the command line, the arguments are just a sequence of
 	// param_name/param_value pairs where the names and values correspond to
 	// those expected in the html applet tag source.
 	//
 	
 	private String getParameter(String name, String default_value){
 		String value = readParameter(name, false);
 		return value != null ? value : default_value;
 	}
 	
 	private void setLogLevel(String log_level){
 		switch (log_level.charAt(0)) {
 		case 'd':
 		case 'D':
 			logger.setLevel(Level.DEBUG);
 			break;
 		case 'i':
 		case 'I':
 			logger.setLevel(Level.INFO);
 			break;
 		case 'w':
 		case 'W':
 			logger.setLevel(Level.WARN);
 			break;
 		case 'e':
 		case 'E':
 			logger.setLevel(Level.ERROR);
 			break;
 		case 'f':
 		case 'F':
 			logger.setLevel(Level.FATAL);
 			break;
 		default:
 			System.err.println(": Invalid debug level: "
 					+ log_level.charAt(0));
 		}
 	}
 
 	void readParameters() {
 		host = readParameter("host", true);
 		port = readIntParameter("port", 5900);
 		log_level = getParameter("log_level", "info");
 
 		// Read "ENCPASSWORD" or "PASSWORD" parameter if specified.
 		readPasswordParameters();
 
 		windowTitle = readParameter("window_title", false);
 		if (windowTitle == null) {
 			windowTitle = "VncViewer";
 		}
 
 		String str;
 
 		// "Show Controls" set to "No" disables button panel.
 		showControls = true;
 		str = readParameter("show_controls", false);
 		System.out.println("str:" + str);
 		if (str != null && str.equalsIgnoreCase("No"))
 			showControls = false;
 
 		// "Offer Relogin" set to "No" disables "Login again" and "Close
 		// window" buttons under error messages in applet mode.
 		offerRelogin = true;
 		str = readParameter("Offer Relogin", false);
 		if (str != null && str.equalsIgnoreCase("No"))
 			offerRelogin = false;
 
 		// Do we continue showing desktop on remote disconnect?
 		showOfflineDesktop = false;
 		str = readParameter("Show Offline Desktop", false);
 		if (str != null && str.equalsIgnoreCase("Yes"))
 			showOfflineDesktop = true;
 
 		// Fine tuning options.
 		deferScreenUpdates = readIntParameter("Defer screen updates", 20);
 		deferCursorUpdates = readIntParameter("Defer cursor updates", 10);
 		deferUpdateRequests = readIntParameter("Defer update requests", 0);
 
 		// Debugging options.
 		debugStatsExcludeUpdates = readIntParameter("DEBUG_XU", 0);
 		debugStatsMeasureUpdates = readIntParameter("DEBUG_CU", 0);
 
 		// SocketFactory.
 		socketFactory = readParameter("SocketFactory", false);
 	}
 
 	//
 	// Read password parameters. If an "ENCPASSWORD" parameter is set,
 	// then decrypt the password into the passwordParam string. Otherwise,
 	// try to read the "PASSWORD" parameter directly to passwordParam.
 	//
 
 	private void readPasswordParameters() {
 		String encPasswordParam = readParameter("ENCPASSWORD", false);
 		if (encPasswordParam == null) {
 			passwordParam = readParameter("PASSWORD", false);
 		} else {
 			// ENCPASSWORD is hexascii-encoded. Decode.
 			byte[] pw = { 0, 0, 0, 0, 0, 0, 0, 0 };
 			int len = encPasswordParam.length() / 2;
 			if (len > 8)
 				len = 8;
 			for (int i = 0; i < len; i++) {
 				String hex = encPasswordParam.substring(i * 2, i * 2 + 2);
 				Integer x = new Integer(Integer.parseInt(hex, 16));
 				pw[i] = x.byteValue();
 			}
 			// Decrypt the password.
 			byte[] key = { 23, 82, 107, 6, 35, 78, 88, 7 };
 			DesCipher des = new DesCipher(key);
 			des.decrypt(pw, 0, pw, 0);
 			passwordParam = new String(pw);
 		}
 	}
 
 	public String readParameter(String name, boolean required) {
 		logger.info("read parameter " + name);
 
 		for (int i = 0; i < mainArgs.length; i += 2) {
 			if (mainArgs[i].equalsIgnoreCase(name)) {
 				try {
 					return mainArgs[i + 1];
 				} catch (Exception e) {
 					if (required) {
 						fatalError(name + " parameter not specified");
 					}
 					return null;
 				}
 			}
 		}
 		if (required) {
 			fatalError(name + " parameter not specified");
 		}
 		return null;
 	}
 
 	public int readIntParameter(String name, int defaultValue) {
 		String str = readParameter(name, false);
 		int result = defaultValue;
 		if (str != null) {
 			try {
 				result = Integer.parseInt(str);
 			} catch (NumberFormatException e) {
 			}
 		}
 		return result;
 	}
 
 	//
 	// moveFocusToDesktop() - move keyboard focus either to VncCanvas.
 	//
 
 	public void moveFocusToDesktop() {
 		if (vncContainer != null) {
 			if (vncCanvas != null && vncContainer.isAncestorOf(vncCanvas))
 				vncCanvas.requestFocus();
 		}
 	}
 
 	//
 	// disconnect() - close connection to server.
 	//
 
 	synchronized public void disconnect() {
 
 		if (vncCanvas != null) {
 			double sec = (System.currentTimeMillis() - vncCanvas.statStartTime) / 1000.0;
 			double rate = Math.round(vncCanvas.statNumUpdates / sec * 100) / 100.0;
 			long nRealRects = vncCanvas.statNumPixelRects;
 			long nPseudoRects = vncCanvas.statNumTotalRects
 					- vncCanvas.statNumPixelRects;
 			logger.info("Updates received: " + vncCanvas.statNumUpdates
 					+ " (" + nRealRects + " rectangles + " + nPseudoRects
 					+ " pseudo), " + rate + " updates/sec");
 			long numRectsOther = nRealRects - vncCanvas.statNumRectsTight
 					- vncCanvas.statNumRectsZRLE
 					- vncCanvas.statNumRectsHextile - vncCanvas.statNumRectsRaw
 					- vncCanvas.statNumRectsCopy;
 			logger.info("Rectangles:" + " Tight="
 					+ vncCanvas.statNumRectsTight + "(JPEG="
 					+ vncCanvas.statNumRectsTightJPEG + ") ZRLE="
 					+ vncCanvas.statNumRectsZRLE + " Hextile="
 					+ vncCanvas.statNumRectsHextile + " Raw="
 					+ vncCanvas.statNumRectsRaw + " CopyRect="
 					+ vncCanvas.statNumRectsCopy + " other=" + numRectsOther);
 
 			long raw = vncCanvas.statNumBytesDecoded;
 			long compressed = vncCanvas.statNumBytesEncoded;
 			if (compressed > 0) {
 				double ratio = Math.round((double) raw / compressed * 1000) / 1000.0;
 				logger.info("Pixel data: "
 						+ vncCanvas.statNumBytesDecoded + " bytes, "
 						+ vncCanvas.statNumBytesEncoded + " compressed, ratio "
 						+ ratio);
 			}
 		}
 
 		if (rfb != null && !rfb.closed())
 			rfb.close();
 		options.dispose();
 
 		if (inAnApplet) {
 			VncEventPublisher.publish(VncEvent.DESTROY, "destroyed");
 			showMessage("Disconnected");
 		} else {
 			System.exit(0);
 		}
 	}
 
 	//
 	// fatalError() - print out a fatal error message.
 	// FIXME: Do we really need two versions of the fatalError() method?
 	//
 
 	synchronized public void fatalError(String str) {
 		logger.error(str);
 
 		if (inAnApplet) {
 			// vncContainer null, applet not inited,
 			// can not present the error to the user.
 			Thread.currentThread().stop();
 		} else {
 			System.exit(1);
 		}
 	}
 
 	synchronized public void fatalError(String str, Exception e) {
 		logger.error(str);
 		
 		if (rfb != null && rfb.closed()) {
 			// Not necessary to show error message if the error was caused
 			// by I/O problems after the rfb.close() method call.
 			logger.info("RFB thread finished");
 			return;
 		}
 
 		e.printStackTrace();
 
 		if (rfb != null)
 			rfb.close();
 
 		if (inAnApplet) {
 			showMessage(str);
 		} else {
 			System.exit(1);
 		}
 	}
 
 	//
 	// Show message text and optionally "Relogin" and "Close" buttons.
 	//
 
 	void showMessage(String msg) {
 		vncContainer.removeAll();
 
 		Label errLabel = new Label(msg, Label.CENTER);
 		errLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));
 
 		if (offerRelogin) {
 
 			Panel gridPanel = new Panel(new GridLayout(0, 1));
 			Panel outerPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
 			outerPanel.add(gridPanel);
 			vncContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 16));
 			vncContainer.add(outerPanel);
 			Panel textPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
 			textPanel.add(errLabel);
 			gridPanel.add(textPanel);
 			gridPanel.add(new ReloginPanel(this));
 
 		} else {
 			vncContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 30));
 			vncContainer.add(errLabel);
 		}
         vncFrame.pack();
 		
 	}
 
 	//
 	// Stop the applet.
 	// Main applet thread will terminate on first exception
 	// after seeing that rfbThread has been set to null.
 	//
 
 	public void stop() {
 		logger.info("Stopping applet");
 		rfbThread = null;
 	}
 
 	//
 	// This method is called before the applet is destroyed.
 	//
 
 	public void destroy() {
 		logger.info("Destroying applet");
 
 		vncContainer.removeAll();
 		options.dispose();
 
 		if (rfb != null && !rfb.closed())
 			rfb.close();
 		vncFrame.dispose();
 	}
 
 	//
 	// Start/stop receiving mouse events.
 	//
 
 	public void enableInput(boolean enable) {
 		vncCanvas.enableInput(enable);
 	}
 
 	//
 	// Resize framebuffer if autoScale is enabled.
 	//
 
 	public void componentResized(ComponentEvent e) {
 		if (e.getComponent() == vncFrame) {
 			if (options.autoScale) {
 				if (vncCanvas != null) {
 					if (!vncCanvas.isFirstSizeAutoUpdate) {
 						vncCanvas.updateFramebufferSize();
 					}
 				}
 			}
 		}
 	}
 
 	//
 	// Ignore component events we're not interested in.
 	//
 
 	public void componentShown(ComponentEvent e) {
 	}
 
 	public void componentMoved(ComponentEvent e) {
 	}
 
 	public void componentHidden(ComponentEvent e) {
 	}
 
 	//
 	// Close application properly on window close event.
 	//
 
 	public void windowClosing(WindowEvent evt) {
 		logger.info("windowClosing");
 		if (rfb != null)
 			disconnect();
 
 		vncContainer.hide();
 
 		if (!inAnApplet) {
 			System.exit(0);
 		}
 		else{
 			if(VncViewer.applet != null){VncViewer.applet.destroy();}			
 		}
 	}
 
 	//
 	// Ignore window events we're not interested in.
 	//
 
 	public void windowActivated(WindowEvent evt) {
 		logger.info("windowActivated");
 	}
 
 	public void windowDeactivated(WindowEvent evt) {
 		logger.info("windowDeactived");
 		try {
 			rfb.releaseAllKeys();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void windowOpened(WindowEvent evt) {
 		logger.info("windowOpened");
 	}
 
 	@Override
 	public void windowClosed(WindowEvent evt) {
 		logger.info("windowClosed");
 	}
 
 	public void windowIconified(WindowEvent evt) {
 		logger.info("windowIconified");
 	}
 
 	public void windowDeiconified(WindowEvent evt) {
 		logger.info("windowDeiconified");
 	}
 }

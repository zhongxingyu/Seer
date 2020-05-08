 package com.fapiko.faceworm.server;
 
 import com.fapiko.faceworm.server.win32.User32;
 import com.sun.jna.platform.win32.WinDef;
 import com.sun.jna.platform.win32.WinDef.HWND;
 import com.sun.jna.platform.win32.WinUser;
 import com.sun.jna.platform.unix.X11;
 import org.apache.log4j.Logger;
 import org.zeromq.ZMQ;
 
 import java.awt.event.KeyEvent;
 
 public class FacewormServer {
 
 	private static final int VK_WIN_PLUS = 0x6B;
 	private static final int VK_WIN_MINUS = 0xBD;
 	private static final int APPLICATION_LOOP_DELAY = 50;
 	private static final int HEALTHCHECK_DELAY = 30000;
 
 	private HWND pandoraHandle;
 	private boolean isWindows = false;
 
 	private static Logger logger = Logger.getLogger(FacewormServer.class);
 
 	public void applicationLoop() {
 
 		isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
 
 		ZMQ.Context context = ZMQ.context(1);
 		ZMQ.Socket socket = context.socket(ZMQ.SUB);
 		ZMQ.Socket socketHealthcheck = context.socket(ZMQ.PUB);
 		socket.subscribe("ACTION".getBytes());
 
 		socket.bind("tcp://*:5555");
 		socketHealthcheck.bind("tcp://*:5556");
 
 		if (isWindows) {
 
 			pandoraHandle = User32.INSTANCE.FindWindow(null, "Pandora");
 			logger.info(pandoraHandle);
 
 		}
 
 		while(true) {
 
 			int healthcheckTime = 0;
 			byte[] reply = socket.recv(ZMQ.NOBLOCK);
 
 			if (reply != null) {
 
 				String message = new String(reply);
 				String[] pieces = message.split("\\|");
 
 				logger.debug(message);
 
 				if (pieces.length > 1 && pieces[0].equals("ACTION")) {
 
 					if (pieces[1].equals("NEXT_TRACK")) {
 
 						skipSong();
 
 					} else if (pieces[1].equals("PLAY_PAUSE")) {
 
 						togglePlayPause();
 
 					} else if (pieces[1].equals("THUMBS_UP")) {
 						thumbsUpSong();
 
 
 					} else if (pieces[1].equals("THUMBS_DOWN")) {
 
 						thumbsDownSong();
 
 					}
 
 				} else {
 
 					logger.warn("Message failed to split properly");
 					logger.warn("Message received: " + message);
 
 				}
 
 			}
 
 			healthcheckTime += APPLICATION_LOOP_DELAY;
 			if (healthcheckTime >= HEALTHCHECK_DELAY) {
				socketHealthcheck.send("APPLICATION|HEALTHCHECK".getBytes(), 0);
 			}
 
 			try {
 				Thread.sleep(APPLICATION_LOOP_DELAY);
 			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 			}
 
 		}
 
 	}
 
 	public void togglePlayPause() {
 		sendKeystroke(KeyEvent.VK_SPACE);
 	}
 
 	public void skipSong() {
 		sendKeystroke(KeyEvent.VK_RIGHT);
 	}
 
 	public void thumbsUpSong() {
 		sendKeystroke(VK_WIN_PLUS);
 	}
 
 	public void thumbsDownSong() {
 		sendKeystroke(VK_WIN_MINUS);
 	}
 
 	public void increaseVolume() {
 		sendKeystroke(KeyEvent.VK_UP);
 	}
 
 	public void decreaseVolume() {
 		sendKeystroke(KeyEvent.VK_DOWN);
 	}
 
 	public void maxVolume() {
 		sendKeyCombination(KeyEvent.VK_SHIFT, KeyEvent.VK_UP);
 	}
 
 	public void muteVolume() {
 		sendKeyCombination(0x10, 0x28);
 	}
 
 	public void sendKeystroke(int keystroke) {
 
 		logger.debug("Sending keystroke " + keystroke);
 
 		if (isWindows) {
 
 			User32.INSTANCE.PostMessage(pandoraHandle, WinUser.WM_KEYDOWN, new WinDef.WPARAM(keystroke), new WinDef.LPARAM(0));
 			User32.INSTANCE.PostMessage(pandoraHandle, WinUser.WM_KEYUP, new WinDef.WPARAM(keystroke), new WinDef.LPARAM(0));
 
 		} else {
 			logger.warn("Operating system unsupported");
 		}
 	}
 
 	public void sendKeyCombination(int ... keystrokes) {
 
 		/*
 		Watching the messages fly across the wire with Spy++ indicates the following combination should work
 		but isn't. I'm guessing the keyboard state may be a little whacky and we might have to make a call to
 		GetKeyState to check it.
 
 		For reference, this is the Spy++ output of a human hitting Shift+Down on the application window:
 		<03284> 001F0170 P WM_KEYDOWN nVirtKey:VK_SHIFT cRepeat:1 ScanCode:2A fExtended:0 fAltDown:0 fRepeat:0 fUp:0
 		<03285> 001F0170 P WM_KEYDOWN nVirtKey:VK_DOWN cRepeat:1 ScanCode:50 fExtended:1 fAltDown:0 fRepeat:0 fUp:0
 		<03286> 001F0170 P WM_KEYUP nVirtKey:VK_DOWN cRepeat:1 ScanCode:50 fExtended:1 fAltDown:0 fRepeat:1 fUp:1
 		<03287> 001F0170 P WM_KEYUP nVirtKey:VK_SHIFT cRepeat:1 ScanCode:2A fExtended:0 fAltDown:0 fRepeat:1 fUp:1
 
 		And when it is done programmatically:
         <03298> 001F0170 P WM_KEYDOWN nVirtKey:VK_SHIFT cRepeat:1 ScanCode:2A fExtended:0 fAltDown:0 fRepeat:0 fUp:0
 		<03299> 001F0170 P WM_KEYDOWN nVirtKey:VK_DOWN cRepeat:1 ScanCode:50 fExtended:1 fAltDown:0 fRepeat:0 fUp:0
 		<03300> 001F0170 P WM_KEYUP nVirtKey:VK_DOWN cRepeat:1 ScanCode:50 fExtended:1 fAltDown:0 fRepeat:1 fUp:1
 		<03301> 001F0170 P WM_KEYUP nVirtKey:VK_SHIFT cRepeat:0 ScanCode:2A fExtended:0 fAltDown:0 fRepeat:1 fUp:1
 		 */
 //		User32.INSTANCE.PostMessage(pandoraHandle, WinUser.WM_KEYDOWN, new WinDef.WPARAM(0x10), new WinDef.LPARAM(0x002A0001));
 //		User32.INSTANCE.PostMessage(pandoraHandle, WinUser.WM_KEYDOWN, new WinDef.WPARAM(0x28), new WinDef.LPARAM(0x01500001));
 //		User32.INSTANCE.PostMessage(pandoraHandle, WinUser.WM_KEYUP, new WinDef.WPARAM(0x28), new WinDef.LPARAM(0xC1500001));
 //		User32.INSTANCE.PostMessage(pandoraHandle, WinUser.WM_KEYUP, new WinDef.WPARAM(0x10), new WinDef.LPARAM(0xC02A0000));
 
 		for(int keystroke : keystrokes) {
 			User32.INSTANCE.SendMessage(pandoraHandle, WinUser.WM_KEYDOWN, new WinDef.WPARAM(keystroke), new WinDef.LPARAM(0x80000000));
 		}
 
 		for(int keystroke : keystrokes) {
 			User32.INSTANCE.SendMessage(pandoraHandle, WinUser.WM_KEYUP, new WinDef.WPARAM(keystroke), new WinDef.LPARAM());
 		}
 
 	}
 
 }

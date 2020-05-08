 package com.android.webscreen;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.android.chimpchat.ChimpManager;
 import com.android.chimpchat.adb.AdbBackend;
 import com.android.chimpchat.core.IChimpDevice;
 import com.android.chimpchat.core.TouchPressType;
 
 
 public class RuzzleCheat {
 	private static final int MARGIN_LEFT = 65;
 	private static final int MARGIN_TOP = 265;
 	private static final int SIZE = 116;
 	private static final int STD_WIDTH = 480;
 	private static final int PAUSE = 50;
 	
 	private IChimpDevice device;
 	private int width;
 	private int height;
 
 	public RuzzleCheat() {
 		Logger.getLogger(ChimpManager.class.getName()).setLevel(Level.OFF);
 		System.out.println("Waiting for device...");
 		device = new AdbBackend().waitForConnection();
 		System.out.println("...done");
 		width = Integer.parseInt(device.getProperty("display.width"));
 		height = Integer.parseInt(device.getProperty("display.height"));
 		System.out.println("Display info");
 		System.out.println("   width: " + width);
 		System.out.println("   height: " + height);
 	}
 	
 	private int getX(int panelID) {
		return MARGIN_LEFT + (panelID % 4) * SIZE * width / STD_WIDTH;
 	}
 	
 	private int getY(int panelID) {
		return MARGIN_TOP + (panelID / 4) * SIZE * width / STD_WIDTH;
 	}
 	
 	/**
 	 * Sends touch commands to the device for a word according to this numbering scheme:
 	 * 00 01 02 03
 	 * 04 05 06 07
 	 * 08 09 10 11
 	 * 12 13 14 15
 	 */
 	public void sendWord(int[] word) {
 		int prevX = getX(word[0]);
 		int prevY = getY(word[0]);
 		device.touch(prevX, prevY, TouchPressType.DOWN);
 		for (int letter : word) {
 			int x = getX(letter);
 			int y = getY(letter);
 			sleep();
 			device.touch((prevX + x) / 2, (prevY + y) / 2, TouchPressType.MOVE);
 			sleep();
 			device.touch(x, y, TouchPressType.MOVE);
 			prevX = x;
 			prevY = y;
 		}
 		device.touch(prevX, prevY, TouchPressType.UP);
 	}
 	
 	private static void sleep() {
 		try {
 			Thread.sleep(PAUSE);
 		} catch (InterruptedException e) {
 		}
 	}
 	
 	public static void main(String[] args) {
 		int[] word1 = {0, 1, 2, 3, 7, 11, 15, 14, 13, 12, 8, 4, 6, 10, 9};
 		int[] word2 = {0, 5, 10, 15, 11, 7, 3, 6, 9, 12};
 		RuzzleCheat ruzzleCheat = new RuzzleCheat();
 		for (int i = 0; i < 10; i++) {
 			ruzzleCheat.sendWord(word1);
 			ruzzleCheat.sendWord(word2);
 		}
 	}
 }

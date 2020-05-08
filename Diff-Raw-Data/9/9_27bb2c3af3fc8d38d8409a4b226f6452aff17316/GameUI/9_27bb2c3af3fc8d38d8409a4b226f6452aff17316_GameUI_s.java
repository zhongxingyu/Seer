 package ui;
 
 import jcurses.system.*;
 import map.Map;
 
 public class GameUI {
 	private static CharColor mapFontColor;
 	private static int windowWidth;
 	private static int windowHeight;
 	
 	public static void init() {
 		Toolkit.init();
 		mapFontColor = new CharColor(CharColor.BLACK, CharColor.WHITE);
 		windowWidth = Toolkit.getScreenWidth();
 		windowHeight = Toolkit.getScreenHeight();
		Toolkit.printString("Welcome in furry-robot!", windowWidth / 2, windowHeight / 2, mapFontColor);
		
 	}
 	
 	public static void drawMap() {
 		Toolkit.clearScreen(mapFontColor);
		int i = 0;
		while (i < Toolkit.getScreenHeight())
			Toolkit.printString(Map.toStringArray()[i], 0, i++, mapFontColor);
 	}
 	
 	public static char getInputChar() {
 		InputChar input = Toolkit.readCharacter();
 		while (input.isSpecialCode())
 			input = Toolkit.readCharacter();
 		return input.getCharacter();
 	}
 	
 	public static void showMessage(String msg) {
 		Toolkit.clearScreen(mapFontColor);
 		Toolkit.printString(msg, 0, 0, mapFontColor);
 		if (Toolkit.readCharacter() != null) return;
 	}
 	
 	public static void close() {
 		Toolkit.clearScreen(mapFontColor);
 		Toolkit.shutdown();
 	}
 	
 	public static int getMapWidth() {
 		return windowWidth;
 	}
 	
 	public static int getMapHeight() {
 		return windowHeight;
 	}
 
 }

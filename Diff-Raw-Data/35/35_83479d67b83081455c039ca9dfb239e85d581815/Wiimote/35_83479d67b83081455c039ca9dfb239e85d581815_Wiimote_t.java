 package org.cvpcs.android.cwiidconfig.config;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.regex.*;
 
 public class Wiimote {
 	private static Pattern wiimotePattern = Pattern.compile(
 			"wiimote.([a-z0-9_]+)=([a-z0-9_]+)",
 			Pattern.CASE_INSENSITIVE);
 	
 	private int buttonA = 0;
 	private int buttonB = 0;
 	private int button1 = 0;
 	private int button2 = 0;
 	private int buttonPlus = 0;
 	private int buttonMinus = 0;
 	private int buttonHome = 0;
 	private int buttonUp = 0;
 	private int buttonDown = 0;
 	private int buttonLeft = 0;
 	private int buttonRight = 0;
 	private Matcher wiimoteMatcher;
 	
 	public void setButtonA(int keysym) {
 		buttonA = keysym;
 	}
 	
 	public void setButtonB(int keysym) {
 		buttonB = keysym;
 	}
 	
 	public void setButton1(int keysym) {
 		button1 = keysym;
 	}
 	
 	public void setButton2(int keysym) {
 		button2 = keysym;
 	}
 	
 	public void setButtonPlus(int keysym) {
 		buttonPlus = keysym;
 	}
 	
 	public void setButtonMinus(int keysym) {
 		buttonMinus = keysym;
 	}
 	
 	public void setButtonHome(int keysym) {
 		buttonHome = keysym;
 	}
 	
 	public void setButtonUp(int keysym) {
 		buttonUp = keysym;
 	}
 	
 	public void setButtonDown(int keysym) {
 		buttonDown = keysym;
 	}
 	
 	public void setButtonLeft(int keysym) {
 		buttonLeft = keysym;
 	}
 	
 	public void setButtonRight(int keysym) {
 		buttonRight = keysym;
 	}
 	
 	public int getButtonA() {
 		return buttonA;
 	}
 	
 	public int getButtonB() {
 		return buttonB;
 	}
 	
 	public int getButton1() {
 		return button1;
 	}
 	
 	public int getButton2() {
 		return button2;
 	}
 	
 	public int getButtonPlus() {
 		return buttonPlus;
 	}
 	
 	public int getButtonMinus() {
 		return buttonMinus;
 	}
 	
 	public int getButtonHome() {
 		return buttonHome;
 	}
 	
 	public int getButtonUp() {
 		return buttonUp;
 	}
 	
 	public int getButtonDown() {
 		return buttonDown;
 	}
 	
 	public int getButtonLeft() {
 		return buttonLeft;
 	}
 	
 	public int getButtonRight() {
 		return buttonRight;
 	}
 	
 	public boolean readLine(String line) {
		if (line.length() <= 0 ||
		    line.charAt(0) == '#') {
 			return false;
 		}
 		wiimoteMatcher = wiimotePattern.matcher(line);
 		if (wiimoteMatcher.find()) {
 			String button = wiimoteMatcher.group(1);
 			String value = wiimoteMatcher.group(2);
 			setButtonValue(button, value);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public void save(BufferedWriter bw) throws IOException {
 		if (buttonA != 0) {
 			bw.write("Wiimote.A=" + ConfigManager.convertKeysym(buttonA) + "\n");
 		}
 		if (buttonB != 0) {
 			bw.write("Wiimote.B=" + ConfigManager.convertKeysym(buttonB) + "\n");
 		}
 		if (button1 != 0) {
 			bw.write("Wiimote.1=" + ConfigManager.convertKeysym(button1) + "\n");
 		}
 		if (button2 != 0) {
 			bw.write("Wiimote.2=" + ConfigManager.convertKeysym(button2) + "\n");
 		}
 		if (buttonPlus != 0) {
 			bw.write("Wiimote.Plus=" + ConfigManager.convertKeysym(buttonPlus) + "\n");
 		}
 		if (buttonMinus != 0) {
 			bw.write("Wiimote.Minus=" + ConfigManager.convertKeysym(buttonMinus) + "\n");
 		}
 		if (buttonHome != 0) {
 			bw.write("Wiimote.Home=" + ConfigManager.convertKeysym(buttonHome) + "\n");
 		}
 		if (buttonUp != 0) {
 			bw.write("Wiimote.Up=" + ConfigManager.convertKeysym(buttonUp) + "\n");
 		}
 		if (buttonDown != 0) {
 			bw.write("Wiimote.Down=" + ConfigManager.convertKeysym(buttonDown) + "\n");
 		}
 		if (buttonLeft != 0) {
 			bw.write("Wiimote.Left=" + ConfigManager.convertKeysym(buttonLeft) + "\n");
 		}
 		if (buttonRight != 0) {
 			bw.write("Wiimote.Right=" + ConfigManager.convertKeysym(buttonRight) + "\n");
 		}
 	}
 	
 	private void setButtonValue(String button, String value) {
 		if (button.equalsIgnoreCase("A")) {
 			setButtonA(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("B")) {
 			setButtonB(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("1")) {
 			setButton1(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("2")) {
 			setButton2(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("PLUS")) {
 			setButtonPlus(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("MINUS")) {
 			setButtonMinus(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("HOME")) {
 			setButtonHome(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("UP")) {
 			setButtonUp(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("DOWN")) {
 			setButtonDown(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("LEFT")) {
 			setButtonLeft(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("RIGHT")) {
 			setButtonRight(ConfigManager.convertString(value));
 		}
 	}
 }

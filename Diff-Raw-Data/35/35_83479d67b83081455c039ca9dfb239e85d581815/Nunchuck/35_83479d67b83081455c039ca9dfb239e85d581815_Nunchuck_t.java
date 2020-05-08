 package org.cvpcs.android.cwiidconfig.config;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Nunchuck {
 	private static Pattern nunchuckPattern = Pattern.compile(
 			"(nunchuck|plugin.nunchuck_stick2btn).([a-z0-9_]+)=([a-z0-9_]+)",
 			Pattern.CASE_INSENSITIVE);
 	
 	private int buttonC = 0;
 	private int buttonZ = 0;
 	private int buttonUp = 0;
 	private int buttonDown = 0;
 	private int buttonLeft = 0;
 	private int buttonRight = 0;
 	private Matcher nunchuckMatcher;
 
 	public void setButtonC(int keysym) {
 		buttonC = keysym;
 	}
 	
 	public void setButtonZ(int keysym) {
 		buttonZ = keysym;
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
 
 	public int getButtonC() {
 		return buttonC;
 	}
 	
 	public int getButtonZ() {
 		return buttonZ;
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
 		nunchuckMatcher = nunchuckPattern.matcher(line);
 		if (nunchuckMatcher.find()) {
 			String button = nunchuckMatcher.group(2);
 			String value = nunchuckMatcher.group(3);
 			setButtonValue(button, value);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public void save(BufferedWriter bw) throws IOException {
 		if (buttonC != 0) {
 			bw.write("Nunchuck.C=" + ConfigManager.convertKeysym(buttonC) + "\n");
 		}
 		if (buttonZ != 0) {
 			bw.write("Nunchuck.Z=" + ConfigManager.convertKeysym(buttonZ) + "\n");
 		}
 		if (buttonUp != 0) {
 			bw.write("Plugin.nunchuck_stick2btn.Up=" + ConfigManager.convertKeysym(buttonUp) + "\n");
 		}
 		if (buttonDown != 0) {
 			bw.write("Plugin.nunchuck_stick2btn.Down=" + ConfigManager.convertKeysym(buttonDown) + "\n");
 		}
 		if (buttonLeft != 0) {
 			bw.write("Plugin.nunchuck_stick2btn.Left=" + ConfigManager.convertKeysym(buttonLeft) + "\n");
 		}
 		if (buttonRight != 0) {
 			bw.write("Plugin.nunchuck_stick2btn.Right=" + ConfigManager.convertKeysym(buttonRight) + "\n");
 		}
 	}
 	
 	private void setButtonValue(String button, String value) {
 		if (button.equalsIgnoreCase("C")) {
 			setButtonC(ConfigManager.convertString(value));
 		} else if (button.equalsIgnoreCase("Z")) {
 			setButtonZ(ConfigManager.convertString(value));
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

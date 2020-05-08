 package org.cvpcs.android.cwiidconfig.config;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 
 public class Config {
 	private String name;
 	private String summary;
 	private Wiimote wiimote;
 	private Nunchuck nunchuck;
 	private ClassicController classicController;
 	
 	public Config() {
 		name = "";
 		summary = "";
 		wiimote = new Wiimote();
 		nunchuck = new Nunchuck();
 		classicController = new ClassicController();
 	}
 	
 	public Config(BufferedReader br) throws IOException {
 		name = "";
 		summary = "";
 		wiimote = new Wiimote();
 		nunchuck = new Nunchuck();
 		classicController = new ClassicController();
 		load(br);
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getSummary() {
 		return summary;
 	}
 	
 	public Wiimote getWiimote() {
 		return wiimote;
 	}
 	
 	public Nunchuck getNunchuck() {
 		return nunchuck;
 	}
 	
 	public ClassicController getClassicController() {
 		return classicController;
 	}
 	
 	public void setName(String n) {
 		name = n;
 	}
 	
 	public void setSummary(String s) {
 		summary = s;
 	}
 	
 	private boolean readLine(String line) {
		if (line.length() <= 0 ||
 		    line.charAt(0) != '#') {
 			return false;
 		}
 		if (line.indexOf("#name=") == 0 && line.length() > 6) {
 			name = line.substring(6);
 			return true;
 		} else if (line.indexOf("#summary=") == 0 && line.length() > 9) {
 	    	summary = line.substring(9);
 	    	return true;
 	    } else {
 	    	return false;
 	    }
 	}
 	
 	public void save(BufferedWriter bw) throws IOException {
 		if (!name.equals("")) {
 			bw.write("#name=" + name + "\n");
 		}
 		if (!summary.equals("")) {
 			bw.write("#summary=" + summary + "\n");
 		}
 		wiimote.save(bw);
 		nunchuck.save(bw);
 		classicController.save(bw);
 	}
 	
 	private void load(BufferedReader br) throws IOException {
 		String line;
 		while ((line = br.readLine()) != null) {
 			if (!wiimote.readLine(line)) {
 				if (!classicController.readLine(line)) {
 					if (!nunchuck.readLine(line)) {
 						if (!readLine(line)) {
 							// unhandled line
 						}
 					}
 				}
 			}
 		}
 	}
 }

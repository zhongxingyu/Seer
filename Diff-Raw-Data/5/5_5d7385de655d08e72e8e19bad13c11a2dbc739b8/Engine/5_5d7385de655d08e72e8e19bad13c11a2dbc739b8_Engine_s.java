 package com.ijg.ijgsec.engine;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.ijg.ijgsec.gui.GUI;
 
 public class Engine implements Runnable {
 	
 	boolean running, bNotFinished;
 	
 	public double total, found;
 	public double percent;
 	public ArrayList<Vulnerability> vulnerabilities;
 	
 	public AssessmentModule assessModule;
 	
 	GUI gui;
 	
 	public static void main(String[] args) {
 		new Engine();
 	}
 
 	public Engine() {
 		bNotFinished = true;
 		vulnerabilities = new ArrayList<Vulnerability>(); // list of found vulnerabilities used to write the progress file
 		assessModule = new AssessmentModule(this);
 		start();
 	}
 	
 	public void start() {
 		/*
 		 * Init the gui and the thread, start
 		 * the gears turning, do initial 
 		 * scoring and display...
 		 */
 		running = true;
 		gui = new GUI(this);
 		Thread engine = new Thread(this, "engine");
 		engine.start();
 		assessModule.report();
 		gui.update();
 	}
 
 	public void run() {
 		long timer = System.currentTimeMillis();
 		while (running) {
 			if (bNotFinished) { // assessment is active
 				if (System.currentTimeMillis() - timer >= 60000L) { // auto check every 60 seconds
 					assessModule.report();
 					gui.update();
 					timer = System.currentTimeMillis();
 				}
 			} else {
 				System.exit(0);
 			}
 		}
 	}
 	
 	public void finishSession() {
 		bNotFinished = false;
 	}
 	
 	public void writeFoundList() {
 		/*
 		 * Write all found vulnerabilities
 		 * to the progress file in the format
 		 * of "name: description"
 		 */
 		BufferedWriter out = null;
 		try {
 			out = new BufferedWriter(new FileWriter(new File("progress")));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			out.write("#Any found (fixed) vulnerabilities are shown here in the format of:\n#Vulnerability name: Vulnerability description\n");
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		for (Vulnerability vuln : vulnerabilities) {
 			try {
 				out.write(vuln.name + ": " + vuln.description + "\n");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * =============================
 	 * Getters and setters
 	 * =============================
 	 */
 	
 	public String getTotal() {
 		return "" + total;
 	}
 
 	public void setTotal(double total) {
 		this.total = total;
 	}
 
 	public String getFound() {
 		return "" + found;
 	}
 
 	public void setFound(double found) {
 		this.found = found;
 	}
 
 	public String getPercent() {
 		return "" + (int) (percent*100) + "%";
 	}
 
 	public void setPercent(double percent) {
 		this.percent = percent;
 	}
 	
 	public void addVulnerability(Vulnerability vulnerability) {
 		vulnerabilities.add(vulnerability);
 	}
 	
 	/*
 	 * Should be used if a GUI is made to view the found vulnerabilities
 	 */
 	public ArrayList<Vulnerability> getVulnerabilities() {
 		return vulnerabilities;
 	}
 }

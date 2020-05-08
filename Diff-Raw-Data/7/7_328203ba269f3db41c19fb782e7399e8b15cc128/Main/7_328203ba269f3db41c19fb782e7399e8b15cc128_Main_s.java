 /**
  * Main class
  * 
  * Class containing the main method to run the simulation
  * Phase 2 Deliverable
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import javax.swing.SwingUtilities;
 
 public class Main {
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Thread(new Runnable() {
 			public void run() {
 				new SimulatorMenu();
 			}
 		}));
 
 	}
 
 }

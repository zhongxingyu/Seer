 /*
  * Project:     MyRobots.com integration for ARISE Human Hamster Wheel 
  * Authors:     Jeffrey Arcand <jeffrey.arcand@ariselab.ca>
  * File:        ca/ariselab/myhhw/LogTailer.java
  * Date:        Sat 2013-04-13
  * Copyright:   Copyright (c) 2013 by Jeffrey Arcand.  All rights reserved.
  * License:     GNU GPL v3
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package ca.ariselab.myhhw;
 
 import java.io.IOException;
 
 public abstract class LogTailer extends Tailer {
 	
 	private String rideStarted = null;
 	
 	/**
 	 * Create a new tailer for the specified HHW log file.
 	 * @param filename The file name of the log file to tail.
 	 */
 	public LogTailer(String filename) throws IOException {
 		super(filename);
 		startTailing();
 	}
 	
 	/**
 	 * Process the provided text line.
 	 * @param no The line number.
 	 * @param line The line of text to process.
 	 * @return Whether or not to continue tailing the file.
 	 */
 	public boolean processLine(int no, String line) {
 		
 		// Check if a ride is starting
		if (line.contains("M_RIDE - New ride started")) {
 			
 			// Save the line for when the ride ends
 			rideStarted = line;
 			
 		// Check if a ride is ending
		} else if (line.contains("M_RIDE - Ride ended: ")
		  || line.contains(
		    "M_FSM - Changing from state RUNNING to SAFETY_FAIL")) {
 			
 			try {
 				// Create the ride struct
 				Ride r = new Ride(rideStarted, line);
 				
 				// Process the ride
 				processRide(r);
 				
 			} catch (IllegalArgumentException e) {
 				
 				// If there is a parsing error, print a warning
 				System.out.println("WARN: Skipping a ride " 
 				  + "because of invalid data (" + rideStarted
 				  + " // " + line + "): " + e.getMessage());
 			}
 			rideStarted = null;
 		}
 		return true;
 	}
 	
 	/**
 	 * Process the provided ride.
 	 * @param r The ride to process.
 	 */
 	public abstract void processRide(Ride r);
 	
 	/**
 	 * Stand-alone test of the class.
 	 */
 	public static void main(String[] args) throws IOException {
 		
 		new LogTailer(args[0]) {
 			public void processRide(Ride r) {
 				System.out.println(r);
 			}
 		};
 		
 		System.out.println("After start tailing.");
 	}
 }
 

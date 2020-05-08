 /*
  * Project:     MyRobots.com integration for ARISE Human Hamster Wheel 
  * Authors:     Jeffrey Arcand <jeffrey.arcand@ariselab.ca>
  * File:        ca/ariselab/myhhw/Tailer.java
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
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 public abstract class Tailer {
 	
 	private static int SLEEP_DELAY = 500;
 	private boolean tailing = true;
 	private FileReader fr = null;
 	private BufferedReader br = null;
 	
 	/**
 	 * Create a new Tailer object for the specified file.
 	 * @param filename The file to tail.
 	 */
 	public Tailer(final String filename) throws IOException {
 		fr = new FileReader(filename);
 		br = new BufferedReader(fr);
 	}
 	
 	/**
 	 * The internal main file processing loop.
 	 */
 	private void mainLoop() throws IOException {
 		int no = 0;
 		String line;
 		while (tailing) {
 			line = br.readLine();
 			no++;
 
 			if (line != null) {
 				if (!processLine(no, line)) {
 					stopTailing();
 				}
 			} else {
 				try {
 					Thread.sleep(SLEEP_DELAY);
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Start tailing the file.
 	 */
 	public void startTailing() {
 		(new Thread() {
 			public void run() {
 
 				try {
 					mainLoop();
 				} catch (IOException e) {
					System.err.println(
					  "ERROR reading file: " + e);
 				} finally {
 					try {
 						br.close();
 					} catch (IOException e2) {
 					}
 					try {
 						fr.close();
 					} catch (IOException e2) {
 					}
 				}
 			}
 		}).start();
 	}
 	
 	/**
 	 * Tell the tailer to stop tailing and close the file.
 	 */
 	public void stopTailing() {
 		tailing = false;
 	}
 	
 	/**
 	 * Process the provided text line.
 	 * @param no The line number.
 	 * @param line The line of text to process.
 	 * @return Whether or not to continue tailing the file.
 	 */
 	public abstract boolean processLine(int no, String line);
 	
 	/**
 	 * Stand-alone test of the class.
 	 */
 	public static void main(String[] args) throws IOException {
 		
 		(new Tailer(args[0]) {
 			public boolean processLine(int no, String line) {
 				System.out.println("LINE " + no + ": " + line);
 				return true;
 			}
 		}).startTailing();
 		
 		System.out.println("After start tailing.");
 	}
 }
 

 package com.ijg.darklight.sdk.core;
 
 /*
  * Darklight Nova Core, a computer vulnerability simulation, designed to train and teach students about general cyber security
  * Copyright (C) 2013  Isaac Grant
  *  
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * Darklight Nova Core, a computer vulnerability simulation, designed
  * to train and teach students about general cyber security
  * 
  * @author Isaac Grant
  *
  */
 
 public class CoreEngine implements Runnable {
 	
 	private boolean running;
 	private boolean isFinished;
 	
 	public IssueHandler issueHandler;
 	public PluginHandler pluginHandler;
 	
 	private long lastUpdate = 0L;
 	private final long UPDATE_INTERVAL = 60000L; // 60 seconds
 	
 	/**
 	 * Invokes the constructor
 	 * @param args Command line arguments
 	 */
 	public static void main(String[] args) {
 		new CoreEngine();
 	}
 	
 	public CoreEngine() {
		Issue[] issues = new Issue[] {}; // place initialized issues here
 		issueHandler = new IssueHandler(issues);
 		pluginHandler = new PluginHandler(this);
		Plugin[] plugins = new Plugin[] {}; // place initialized plugins here
 		pluginHandler.setPlugins(plugins);
 		start();
 	}
 	
 	/**
 	 * Initiate isFinished and running booleans, start plugins
 	 * start the main thread, and do an initial issue check
 	 */
 	private void start() {
 		isFinished = false;
 		running = true;
 		pluginHandler.startAll();
 		Thread engine = new Thread(this, "engine");
 		engine.start();
 		issueHandler.checkAllIssues();
 	}
 	
 	public void run() {
 		while (running) {
 			if (System.currentTimeMillis() - lastUpdate > UPDATE_INTERVAL) {
 				update();
 			}
 			try {
 				Thread.sleep(20);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("Exiting...");
 		System.exit(0);
 	}
 	
 	/**
 	 * Check for any changes in found issues
 	 */
 	public void update() {
 		issueHandler.checkAllIssues();
 		lastUpdate = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Safely kill the engine
 	 */
 	public void finishSession() {
 		running = false;
 	}
 	
 	/**
 	 * Check if the all issues have been found
 	 * @return True if isFinished has been set true
 	 */
 	public boolean finished() {
 		return isFinished;
 	}
 }

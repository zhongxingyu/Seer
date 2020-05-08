 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.commands;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.rmi.RemoteException;
 
 import de.tuilmenau.ics.fog.topology.Simulation;
 
 
 /**
  * Class fetches commands from a stream and forwards them to a given
 * simulation. The process runs in a separate thread.
  */
 public class CommandExecutor
 {
 	private final static int CMD_THREAD_INACTIVE_SLEEP_MSEC = 500;
 	
 
 	/**
 	 * Using std input as command input.
 	 * 
 	 * @param pSimulation
 	 */
 	public CommandExecutor(Simulation pSimulation)
 	{
 		// use std in as default
 		mInStream = new BufferedReader(new InputStreamReader(System.in));
 		mSimulation = pSimulation;
 	}
 	
 	public CommandExecutor(BufferedReader pInStream, Simulation pSimulation)
 	{
 		mInStream = pInStream;
 		mSimulation = pSimulation;
 	}
 	
 	/**
 	 * Starts the command forwarding to the simulation.
	 * The forwarding is done in a separate thread and the method does not block.
 	 * 
 	 * Method does not start the simulation itself.
 	 */
 	public void start()
 	{
 		mExit = false;
 		
 		cmdThread = new Thread() {
 			public void run() {
				Thread.currentThread().setName("ComandExecutor@" + mSimulation.toString());

 				String tCmd = null;
 
 				mSimulation.getLogger().trace(this, "Cmd execution started");
 				
 				do {
 					try {
 						if(mInStream.ready()) {
 							tCmd = mInStream.readLine();
 						
 							if(mSimulation != null) {
 								if(!mSimulation.executeCommand(tCmd)) {
 									mSimulation.getLogger().err(this, "Command execution failed.");
 								}
 							} else {
 								mSimulation.getLogger().err(this, "No simulation available. Can not execute command.");
 							}
 						} else {
 							Thread.sleep(CMD_THREAD_INACTIVE_SLEEP_MSEC);
 						}
 					}
 					catch(RemoteException tExc) {
 						tExc.printStackTrace();
 						mSimulation.getLogger().err(this, "Remote exception: " +tExc.getMessage());
 					}
 					catch(IOException tExc) {
 						if(!mExit) {
 							tExc.printStackTrace();
 							mSimulation.getLogger().err(this, "IO exception: " +tExc.getMessage());
 						}
 						// else: stream was closed because of exit request
 					}
 					catch(RuntimeException tExc) {
 						tExc.printStackTrace();
 						mSimulation.getLogger().err(this, "Runtime exception: " +tExc.getMessage());
 					}
 					catch(Exception tExc) {
 						tExc.printStackTrace();
 						mSimulation.getLogger().err(this, "Exception: " +tExc.getMessage());
 					}
 				}
 				while(!mExit);
 				
 				mSimulation.getLogger().trace(this, "Cmd execution ended");
 			}
 		};
 		
 		cmdThread.start();
 	}
 	
 	/**
 	 * Terminates the command forwarding.
 	 * Method blocks until current command execution finished.
 	 * 
 	 * Method does not terminate the simulation itself.
 	 */
 	public void exit()
 	{
 		mExit = true;
 		
 		try {
 			cmdThread.join();
 		} catch(Exception tExc) {
 			// ignore it
 			mSimulation.getLogger().err(this, "Exception while closing command execution: " +tExc.getMessage());
 		}
 	}
 
 	
 	private Simulation mSimulation;
 	private boolean mExit;
 	private BufferedReader mInStream;
 	private Thread cmdThread;
 }

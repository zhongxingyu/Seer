 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.server;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Random;
 
 import com.gamezgalaxy.GGS.API.EventSystem;
 import com.gamezgalaxy.GGS.networking.PacketManager;
 import com.gamezgalaxy.GGS.system.LogInterface;
 import com.gamezgalaxy.GGS.system.Logger;
 import com.gamezgalaxy.GGS.system.heartbeat.Beat;
 import com.gamezgalaxy.GGS.system.heartbeat.MBeat;
 import com.gamezgalaxy.GGS.system.heartbeat.WBeat;
 import com.gamezgalaxy.GGS.world.Level;
 
 public class Server implements LogInterface {
 	private PacketManager pm;
 	private Logger logger;
 	private ArrayList<Tick> ticks = new ArrayList<Tick>();
 	private Thread tick;
 	private Beat heartbeater;
 	private EventSystem es;
 	public ArrayList<Player> players = new ArrayList<Player>();
 	public boolean Running;
 	public int Port;
 	public int MaxPlayers;
 	public String Name;
 	public String altName;
 	public String description;
 	public String flags;
 	public String MOTD;
 	public String Salt;
 	public Level MainLevel;
 	public boolean Public;
 	public final EventSystem getEventSystem() {
 		return es;
 	}
 	public PacketManager getPacketManager() {
 		return pm;
 	}
 	public Server(String Name, int Port, String MOTD) {
 		this.Port = Port;
 		this.Name = Name;
 		this.MOTD = MOTD;
 		pm = new PacketManager(this);
 		tick = new Ticker();
 	}
 
 	public void Start() {
 		Running = true;
 		es = new EventSystem(this);
 		Calendar cal = Calendar.getInstance();
 		cal.clear(Calendar.HOUR);
 		cal.clear(Calendar.MINUTE);
 		cal.clear(Calendar.SECOND);
 		cal.clear(Calendar.MILLISECOND);
 		logger = new Logger("logs/" + cal.getTime() + ".txt", this);
 		String filename = cal.getTime().toString().replace(" ", "-");
 		String finalname = filename.split("-")[0] + "-" + filename.split("-")[1] + "-" + filename.split("-")[2];
 		try {
 			logger.ChangeFilePath("logs/" + finalname + ".txt");
 		} catch (IOException e2) {
 			System.out.println("logs/" + finalname + ".txt");
 			e2.printStackTrace();
 		}
 		try {
 			logger.Start(false);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Log("Starting..");
 		pm.StartReading();
 		Log("Generating Level..");
 		MainLevel = new Level((short)64, (short)64, (short)64);
 		MainLevel.FlatGrass();
 		tick.start();
 		Log("Done!");
 		Log("Generating salt");
 		SecureRandom sr = null;
 		try {
 			sr = SecureRandom.getInstance("SHA1PRNG");
 		} catch (NoSuchAlgorithmException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		for (int i = 0; i < 100; i++) {
 			byte[] seedb = new byte[16];
 			sr.nextBytes(seedb);
 			Salt = new sun.misc.BASE64Encoder().encode(seedb);
 			if (new Random().nextDouble() < .3)
 				break;
 		}
 		Log("SALT: " + Salt);
 		Log("Create heartbeat..");
 		heartbeater = new Beat(this);
 		heartbeater.addHeart(new MBeat());
 		heartbeater.addHeart(new WBeat());
 		heartbeater.start();
 		Log("Done!");
 	}
 
 	public void Stop() throws InterruptedException {
 		Running = false;
		System.out.println("Stopping server..");
 		pm.StopReading();
 		tick.join();
 		logger.Stop();
		//heartbeater.stop(); // Don't stop because there is a sleep for 30 seconds. For some reason it has to sleep that long before continuing...
 		System.exit(0);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void Log(String log) {
 		logger.Log(log);
 	}
 	
 	public Logger getLogger() {
 		return logger;
 	}
 
 	public  void Add(Tick t) {
 		synchronized(ticks) {
 			if (!ticks.contains(t))
 				ticks.add(t);
 		}
 	}
 
 	public void Remove(Tick t) {
 		synchronized(ticks) {
 			if (ticks.contains(t))
 				ticks.remove(t);
 		}
 	}
 
 	public class Ticker extends Thread {
 
 		@Override
 		public void run() {
 			while (Running) {
 				synchronized(ticks) {
 					for (Tick t : ticks) {
 						t.Tick();
 					}
 					try {
 						Thread.sleep(10);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 	@Override
 	public void onLog(String message) {
 		//TODO ..colors?
 		System.out.println(message);
 	}
 	@Override
 	public void onError(String message) {
 		//TODO ..colors?
 		System.out.println("==!ERROR!==");
 		System.out.println(message);
 		System.out.println("==!ERROR!==");
 	}
 }

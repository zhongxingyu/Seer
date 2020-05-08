 package com.jpii.navalbattle.pavo;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.text.DecimalFormat;
 
 import com.jpii.navalbattle.renderer.Console;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.util.GameStatistics;
 
 public class GameBeta extends Renderable implements Runnable {
 	Thread updator;
 	Thread chunkrender;
 	Thread generator;
 	boolean gameRunning = true;
 	long timeLastUpdate = System.currentTimeMillis();
 	int state = 0;
 	World world;
 	WorldGen gen;
 	long numUpdates = 0;
 	boolean forceUpdate = false;
 	private int lastTime = -1;
 	public GameBeta() {
 		world = new World();
 		gen = new WorldGen();
 		threadInit();
 	}
 	public long getNumUpdates() {
 		return numUpdates;
 	}
 	private void threadInit() {
 		updator = new Thread(this);
 		state = 1;
 		updator.setPriority(Thread.MAX_PRIORITY);
 		updator.setName("updatorThread");
 		updator.start();
 		long lastStart = System.currentTimeMillis();
 		while (lastStart + 500 > System.currentTimeMillis()) {
 			
 		}
 		chunkrender = new Thread(this);
 		state = 2;
 		chunkrender.setPriority(Thread.MAX_PRIORITY);
 		chunkrender.setName("chunkGenThread");
 		chunkrender.start();
 		lastStart = System.currentTimeMillis();
 		while (lastStart + 500 > System.currentTimeMillis()) {
 			
 		}
 		state = 3;
 		generator = new Thread(this);
 		generator.setPriority(Thread.MAX_PRIORITY);
 		generator.setName("generatorThread");
 		generator.start();
 	}
 	private static GameStatistics stats = new GameStatistics();
 	public static GameStatistics getStats() {
 		return stats;
 	}
 	public void run() {
 		// Game updator
 		if (state == 1) {
 			while (gameRunning) {
 				//System.out.println("Game updator firing..." + Thread.currentThread().getName());
				while (timeLastUpdate + 100 > System.currentTimeMillis()) {
					if (forceUpdate)
						break;
 					;;;
 				}
 				numUpdates += 100;
 				forceUpdate = false;
 				long updateStart = System.currentTimeMillis();
 				while (getWorld().isLocked()) {}
 				getWorld().lock();
 				getWorld().update();
 				getWorld().unlock();
 				TimeManager tim = getWorld().getTimeManager();
 				if (tim.getState() != lastTime) {
 					lastTime = tim.getState();
 					if (lastTime == 3) {
 						becomingDay();
 					}
 					else if (lastTime == 2) {
 						becomingSunrise();
 					}
 					else if (lastTime == 1) {
 						becomingNight();
 					}
 					else if (lastTime == 0) {
 						becomingSunset();
 					}
 				}
 				update();
 				long updateFinish = System.currentTimeMillis() - updateStart;
 				getStats().SmSK280K99(updateFinish);
 				timeLastUpdate = System.currentTimeMillis();
 			}
 		}
 		// Chunk renderer
 		else if (state == 2) {
 			while (gameRunning) {
 				//System. out.println("Chunk gen firing..." + Thread.currentThread().getName());
 				if (getWorld().hasMoreChunks()) {
 					getWorld().genNextChunk();
 					// Make a small break between each generation.
 					long start = System.currentTimeMillis();
 					while (start + 650 > System.currentTimeMillis()) {
 						;;;
 					}
 				}
 				else {
 					break;
 				}
 			}
 		}
 		// World generator
 		else if (state == 3) {
 			//System.out.println("World gen firing..." + Thread.currentThread().getName());
 			gen.generateChunk();
 			getWorld().setWorldGen(gen);
 		}
 	}
 	public String getGenStatus() {
 		return "";
 	}
 	public int getGenAmount() {
 		return 1;
 	}
 	public void render() {
 		buffer = new BufferedImage(DynamicConstants.WND_WDTH,DynamicConstants.WND_HGHT,BufferedImage.TYPE_INT_RGB);
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		while (getWorld().isLocked()) {
 			
 		}
 		getWorld().lock();
 		getWorld().render();
 		g.drawImage(getWorld().getBuffer(),0,0,null);
 		g.drawImage(getWorld().getTimeManager().getBuffer(),0,0,null);
 		
 		GameStatistics gs = getStats();
 		g.setColor(Color.red);
 		g.setFont(Helper.GUI_GAME_FONT);
 		String frmtn = new DecimalFormat("00").format(getWorld().getTimeManager().getCurrentMinutes());
 		g.drawString((getWorld().getTimeManager().getTimeDescription() + " " + getWorld().getTimeManager().getCurrentHour() + ":"+frmtn),100,100);
 		g.drawString("Idling (should be low):" + gs.getDrawIdling() + ". Draw time:" + gs.getDrawTime() + " Live chunks:" + gs.getLiveChunks(),100,130);
 		getWorld().unlock();
 	}
 	public World getWorld() {
 		return world;
 	}
 	public void becomingSunset() {
 		
 	}
 	public void becomingSunrise() {
 		
 	}
 	public void becomingNight() {
 		
 	}
 	public void becomingDay() {
 		
 	}
 	/**
 	 * This method should be called sparsingly (which means DO NOT OVER USE). This method is multithreaded, so it puts no stress on the calling thread.
 	 * This method is not actually deprecated, but it is called so to ensure that the above message is read.
 	 * @deprecated
 	 */
 	public void forceUpdate() {
 		forceUpdate = true;
 	}
 	public void mouseMove(MouseEvent me) {
 		
 	}
 	public void mouseDown(MouseEvent me) {
 		
 	}
 	public void mouseUp(MouseEvent me) {
 		
 	}
 	public void mouseDragged(MouseEvent me) {
 		
 	}
 }

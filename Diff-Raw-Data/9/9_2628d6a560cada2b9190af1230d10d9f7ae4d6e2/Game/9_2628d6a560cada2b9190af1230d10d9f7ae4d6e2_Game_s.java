 /*
  * Copyright (C) 2012 JPII and contributors
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
  */
 
 package com.jpii.navalbattle.pavo;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.text.DecimalFormat;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.game.Location;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.Tile;
 import com.jpii.navalbattle.pavo.gui.GameWindow;
 import com.jpii.navalbattle.pavo.gui.GridWindow;
 import com.jpii.navalbattle.pavo.gui.WindowManager;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.renderer.weather.WeatherMode;
 import com.jpii.navalbattle.util.GameStatistics;
 
 public class Game extends Renderable implements Runnable {
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
 	boolean forceRender = false;
 	private int lastTime = -1;
 	int lastw = 0, lasth = 0;
 	private WindowManager windows;
 	BufferedImage shadow;
 	public static PavoSettings Settings = new PavoSettings();
 	/**
 	 * Creates a new instance of the game.
 	 */
 	public Game() {
 		windows = new WindowManager(this);
 		world = new World();
 		gen = new WorldGen();
 		threadInit();
 		buffer = new BufferedImage(Game.Settings.currentWidth,Game.Settings.currentHeight,BufferedImage.TYPE_INT_RGB);
 		world.getWeather().setWeather(WeatherMode.Sunny);
 		shadow = PavoHelper.createInnerShadow(Game.Settings.currentWidth,Game.Settings.currentHeight);
 	}
 	/**
 	 * Gets the window manager for the Game.
 	 * @return
 	 */
 	public WindowManager getWinMan() {
 		return windows;
 	}
 	/**
 	 * Sets the window manager for the Game.
 	 * @param wm
 	 */
 	public void setWinMan(WindowManager wm) {
 		windows = wm;
 	}
 	/**
 	 * Gets the total number of updates that the updator has performed.
 	 * @return
 	 */
 	public long getNumUpdates() {
 		return numUpdates;
 	}
 	/**
 	 * Don't play with.
 	 */
 	private void threadInit() {
 		int js = 0;
 		if (Settings.OverClock)
 			js = Thread.MAX_PRIORITY;
 		else
 			js = Thread.NORM_PRIORITY;
 		updator = new Thread(this);
 		state = 1;
 		updator.setPriority(js);
 		updator.setName("updatorThread");
 		updator.start();
 		long lastStart = System.currentTimeMillis();
 		while (lastStart + 500 > System.currentTimeMillis()) {
 			
 		}
 		chunkrender = new Thread(this);
 		state = 2;
 		chunkrender.setPriority(js);
 		chunkrender.setName("chunkGenThread");
 		chunkrender.start();
 		lastStart = System.currentTimeMillis();
 		//while (lastStart + 500 > System.currentTimeMillis()) {
 		//	
 		//}
 		//state = 3;
 		//generator = new Thread(this);
 		//generator.setPriority(Thread.MAX_PRIORITY);
 		//generator.setName("generatorThread");
 		//generator.start();
 	}
 	private static GameStatistics stats = new GameStatistics();
 	/**
 	 * The graphics statistics for the game.
 	 * @return
 	 */
 	public static GameStatistics getStats() {
 		return stats;
 	}
 	/**
 	 * Immortal caller.
 	 */
 	public void run() {
 		// Game updator
 		if (state == 1) {
 			while (gameRunning) {
 				//System.out.println("Game updator firing..." + Thread.currentThread().getName());
 				///while (timeLastUpdate + 100 > System.currentTimeMillis()) {
 					//if (forceUpdate)
 					//	break;
 				//}
 				while (!forceUpdate) {
 					;;;
 				}
 				if (lastw != Settings.currentWidth || lasth != Settings.currentHeight) {
 					lastw = Settings.currentWidth;
 					lasth = Settings.currentHeight;
 					shadow = PavoHelper.createInnerShadow(Game.Settings.currentWidth,Game.Settings.currentHeight);
 				}
 				//System.out.println("winupdate");
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
 					while (start + 550 > System.currentTimeMillis()) {
 						;;;
 					}
 				}
 				else {
 					Game.getStats().SmKdn02nOaP(1);
 					break;
 				}
 			}
 		}
 		// World generator
 		//else if (state == 3) {
 			//System.out.println("World gen firing..." + Thread.currentThread().getName());
 			//gen.generateChunk();
 			//getWorld().setWorldGen(gen);
 		//}
 	}
 	public String getGenStatus() {
 		return "";
 	}
 	public int getGenAmount() {
 		return 1;
 	}
 	/**
 	 * Renders the Game.
 	 */
 	public void render() {
 		long sjan = System.currentTimeMillis();
 		buffer = new BufferedImage(Game.Settings.currentWidth,Game.Settings.currentHeight,BufferedImage.TYPE_INT_RGB);
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		while (getWorld().isLocked()) {
 			
 		}
 		
 		getWorld().lock();
 		getWorld().render();
 		g.drawImage(getWorld().getBuffer(),0,0,null);
 		g.drawImage(getWorld().getTimeManager().getBuffer(),0,0,null);
 		g.drawImage(getWorld().getWeather().getBuffer(),0,0,null);
 		
 		GameStatistics gs = getStats();
 		g.setColor(Color.red);
 		//g.setFont(Helper.GUI_GAME_FONT);
 		String frmtn = new DecimalFormat("00").format(getWorld().getTimeManager().getCurrentMinutes());
 		int ma = 38;
 		g.drawString((getWorld().getTimeManager().getTimeDescription() + " " + getWorld().getTimeManager().getCurrentHour() + ":"+frmtn),12,ma+30);
 		g.drawString("Idling (should be low):" + gs.getDrawIdling() + ". Draw time:" + gs.getDrawTime() + " Live chunks:" + gs.getLiveChunks(),12,ma+60);
 		g.drawString("Is generating? " + gs.isGenerating() + ". Total update time:" + gs.getUpdateTime() + ". Last render length:" + gs.getTotalUpdate(), 12,ma+90);
 		getWorld().unlock();
 		
 		while (getWinMan().isLocked()) {
 			
 		}
 		getWinMan().lock();
 		for (int c = 0; c < getWinMan().size(); c++) {
 			GameWindow gw = getWinMan().get(c);
 			if (gw instanceof GridWindow && gw.isVisible()) {
 				GridWindow gr = (GridWindow)gw;
 				Location l = gr.getGridLocation();
 				if (l != null) {
 					Chunk chunk = PavoHelper.convertGridLocationToChunk(getWorld(), l);
 					if (chunk != null && PavoHelper.isChunkVisibleOnScreen(getWorld(), chunk)) {
 						g.setColor(Color.red);
 						int ssx = (getWorld().getScreenX())+(l.getCol()*50)+25;
 						int ssy = (getWorld().getScreenY())+(l.getRow()*50)+25;
 						int midx = gr.getX()+(gr.getWidth()/2);
 						int midy = gr.getY()+(gr.getHeight()/2);
 						if (Math.sqrt(Math.pow(ssx-midx,2)+Math.pow(ssy-midy,2)) <= gr.getDistanceConstraint()) {
 							Polygon p = new Polygon();
 							p.addPoint(ssx,ssy);
 							p.addPoint(midx-10,midy-10);
 							p.addPoint(midx+10,midy+10);
 							p.addPoint(ssx,ssy);
 							g.fillPolygon(p);
 							g.setColor(Color.black);
 							g.drawPolygon(p);
 						}
 					}
 				}
 			}
 		}
 		if (PavoHelper.getCalculatedSystemSpeed() != SystemSpeed.CREEPER && 
 				PavoHelper.getCalculatedSystemSpeed() != SystemSpeed.TURTLE) {
 			g.drawImage(shadow,0,0,null);
 		}
 		getWinMan().render();
 		g.drawImage(getWinMan().getBuffer(), 0, 0, null);
 		g.dispose();
 		getWinMan().unlock();
 		Game.getStats().sBm3ns02AKa99mqp392(System.currentTimeMillis() - sjan);
 	}
 	/**
 	 * Gets the active world for the Game.
 	 * @return
 	 */
 	public World getWorld() {
 		return world;
 	}
 	/**
 	 * Occurs when the game is going into sunset stage.
 	 */
 	public void becomingSunset() {
 		
 	}
 	/**
 	 * Occurs when the game is going into sunrise stage.
 	 */
 	public void becomingSunrise() {
 		
 	}
 	/**
 	 * Occurs when the game is going into night stage.
 	 */
 	public void becomingNight() {
 		
 	}
 	/**
 	 * Occurs when the game is going into day time stage.
 	 */
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
 		int chx = (-getWorld().getScreenX()) + me.getX();
 		int chy = (-getWorld().getScreenY()) + me.getY(); 
 		chx /= 50;
 		chy /= 50;
 		if (chx < PavoHelper.getGameWidth(getWorld().getWorldSize()) * 2 && chy < PavoHelper.getGameHeight(getWorld().getWorldSize()) * 2 &&
 		chx >= 0 && chy >= 0) {
 			Tile<Entity> e = getWorld().getEntityManager().getTile(chy,chx);
 			if (e != null)
 				e.getEntity().onMouseMove((-getWorld().getScreenX()) + me.getX() - (chx*50),
 					(-getWorld().getScreenY()) + me.getY() - (chy*50));
 		}
 	}
 	Timer mouseLogicTimer = new Timer();
 	TimerTask mouseLogicTask = new $$$MouseLogicTimer();
 	MouseEvent mouseEventSchedule;
 	private class $$$MouseLogicTimer extends TimerTask {
 	    public void run() {
 	        mouseHeldDown(mouseEventSchedule);
 	    }
 	}
 	public void mouseDown(MouseEvent me) {
 		mouseEventSchedule = me;
 		mouseLogicTask = new $$$MouseLogicTimer();
 		mouseLogicTimer.scheduleAtFixedRate(mouseLogicTask, 0, 10);
 	}
 	public void mouseUp(MouseEvent me) {
 		mouseLogicTask.cancel();
 	}
 	public void mouseDragged(MouseEvent me) {
 		
 	}
 	public void mouseHeldDown(MouseEvent me) {
 		
 	}
 }

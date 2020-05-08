 /** TheWalls2: The Walls 2 plugin.
   * Copyright (C) 2012  Andrew Stevanus (Hoot215) <hoot893@gmail.com>
   * 
   * This program is free software: you can redistribute it and/or modify
   * it under the terms of the GNU Affero General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   * 
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU Affero General Public License for more details.
   * 
   * You should have received a copy of the GNU Affero General Public License
   * along with this program.  If not, see <http://www.gnu.org/licenses/>.
   */
 
 package me.Hoot215.TheWalls2;
 
 public class TheWalls2AutoGameStartTimer implements Runnable {
 	private TheWalls2 plugin;
 	private long initialTime;
 	private long normalTime;
 	
 	public TheWalls2AutoGameStartTimer(TheWalls2 instance,
 			long initial, long normal) {
 		plugin = instance;
 		initialTime = initial;
 		normalTime = normal;
 	}
 	
 	public void run() {
 		try {
 			Thread.sleep(initialTime * 60000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
 				new Runnable() {
 			public void run() {
 				TheWalls2GameList gameList = plugin.getGameList();
				if (gameList != null && !TheWalls2World.isRestoring
 						&& plugin.startGame())
 					System.out.println("[TheWalls2] Game started automatically");
 			}
 		});
 		
 		while (true) {
 			try {
 				Thread.sleep(normalTime * 60000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
 					new Runnable() {
 				public void run() {
 					TheWalls2GameList gameList = plugin.getGameList();
					if (gameList != null && !TheWalls2World.isRestoring
 							&& plugin.startGame())
 						System.out.println("[TheWalls2] Game started automatically");
 				}
 			});
 		}
 	}
 }

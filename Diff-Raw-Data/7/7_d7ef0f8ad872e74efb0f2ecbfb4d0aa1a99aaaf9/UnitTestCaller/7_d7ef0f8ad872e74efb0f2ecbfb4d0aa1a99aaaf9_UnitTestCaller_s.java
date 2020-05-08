 /*
  * This file is part of GreatmancodeTools.
  *
  * Copyright (c) 2013-2013, Greatman <http://github.com/greatman/>
  *
  * GreatmancodeTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GreatmancodeTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GreatmancodeTools.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.greatmancode.tools.caller.unittest;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import com.greatmancode.tools.commands.interfaces.CommandReceiver;
 import com.greatmancode.tools.interfaces.Caller;
 import com.greatmancode.tools.interfaces.Loader;
 
 /**
  * Special caller for unit tests.
  * @author Greatman
  */
 public class UnitTestCaller extends Caller {
 	public static final String worldName = "UnitTestWorld";
 	public static final String worldName2 = "UnitTestWorld2";
 	public static final int dir = new Random().nextInt(9999999);
 
 	private Loader loader;
 	public UnitTestCaller(Loader loader) {
 		this.loader = loader;
 	}
 
 	@Override
 	public void disablePlugin() {
 	}
 
 	@Override
 	public boolean checkPermission(String playerName, String perm) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void sendMessage(String playerName, String message) {
 		System.out.println(playerName + ":" + message);
 	}
 
 	@Override
 	public String getPlayerWorld(String playerName) {
 		return "UnitTestWorld";
 	}
 
 	@Override
 	public boolean isOnline(String playerName) {
 		return playerName.equals("console");
 	}
 
 	@Override
 	public String addColor(String message) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean worldExist(String worldName) {
 		return worldName.equalsIgnoreCase(this.worldName) || worldName.equalsIgnoreCase(worldName2);
 	}
 
 	@Override
 	public String getDefaultWorld() {
 		return "UnitTestWorld";
 	}
 
 	@Override
 	public File getDataFolder() {
 		File file;
 		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
 			file = new File(System.getProperty("java.io.tmpdir"));
 		} else {
 			file = new File("/tmp");
 		}

		file = new File(file.getParentFile(), String.valueOf(dir));
 		file.mkdir();
		System.out.println(file.getAbsolutePath());
 		return file;
 	}
 
 	@Override
 	public int schedule(Runnable entry, long firstStart, long repeating) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int schedule(Runnable entry, long firstStart, long repeating, boolean async) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void cancelSchedule(int id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public int delay(Runnable entry, long start) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int delay(Runnable entry, long start, boolean async) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public List<String> getOnlinePlayers() {
 		List<String> list = new ArrayList<String>();
 		list.add("UnitTestPlayer");
 		return list;
 	}
 
 	@Override
 	public void addCommand(String name, String help, CommandReceiver manager) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public String getServerVersion() {
 		return "UnitTest 1.0";
 	}
 
 	@Override
 	public String getPluginVersion() {
 		return "1.0";
 	}
 
 	@Override
 	public boolean isOp(String playerName) {
 		return playerName.equals("UnitTestPlayer");
 	}
 
 	@Override
 	public void loadLibrary(String path) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void registerPermission(String permissionNode) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isOnlineMode() {
 		return true;
 	}
 
 	@Override
 	public Logger getLogger() {
 		return Logger.getLogger(this.getClass().getName());
 	}
 }

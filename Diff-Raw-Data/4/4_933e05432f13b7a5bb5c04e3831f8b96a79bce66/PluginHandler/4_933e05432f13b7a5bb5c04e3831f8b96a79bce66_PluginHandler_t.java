 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.API.plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import com.gamezgalaxy.GGS.server.Server;
 
 public class PluginHandler {
 	private ArrayList<Plugin> plugins = new ArrayList<Plugin>();
 	
 	private ArrayList<Game> games = new ArrayList<Game>();
 
 	public void loadplugins(Server server)
 	{
 		// TODO: NOT TESTED! Waiting for Eddie to make the server stable with his packet stuff.
 		// TODO: Clean up a bit. Use less memory.
 
 		// Basically instead of having for example "All-In-One.config" in plugins/, you put it inside the jar All-In-One.config.
 		// The first line should be "main-class = org.whatever.hello.Main" (your main class).
 
 		File pluginFolder = new File("plugins/");
 
 		if(!pluginFolder.exists())
 		{
 			System.out.println("Mods folder does not exist.");
 
 			return;
 		}
 
 		File[] pluginFiles = pluginFolder.listFiles();
 
 		for(int i = 0; i < pluginFiles.length; i++)
 		{
 			if(pluginFiles[i].isFile() && pluginFiles[i].getName().endsWith(".jar"))
 			{
 				JarFile file = null;
 
 				try {
 					file = new JarFile(pluginFiles[i]);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 				if(file != null)
 				{
 					Enumeration<JarEntry> entries = file.entries();
 
 					if(entries != null)
 					{
 						while(entries.hasMoreElements())
 						{
 							JarEntry fileName = entries.nextElement();
 
 							if(fileName.getName().endsWith(".config"))
 							{
 								Properties properties = new Properties();
 
 								try {
 									properties.load(file.getInputStream(fileName));
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 
 								File pluginFile = new File("plugins/" + pluginFiles[i].getName());
 								String[] args = new String[] { "-normal" };
 
 								loadplugin(pluginFile, properties.getProperty("main-class"), args, server, properties);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private String getClass(File file)
 	{
 		try {
 			Scanner scan = new Scanner(file);
 			return scan.nextLine();
 		} catch(Exception e) {
 			return "null";
 		}
 	}
 
 	public void loadplugin(File file, String classpath, String[] args, Server server, Properties properties)
 	{
 		// TODO: NOT TESTED! Waiting for Eddie to make the server stable with his packet stuff.
 
 		try {
 			URL[] urls = new URL[] { file.toURL() };
 			ClassLoader loader = URLClassLoader.newInstance(urls, getClass().getClassLoader());
 			Class<?> class_ = Class.forName(classpath, true, loader);
 			Class<? extends Plugin> runClass = class_.asSubclass(Plugin.class);
			Constructor<? extends Plugin> constructor = runClass.getConstructor(Server.class, Properties.class);
			Plugin plugin = constructor.newInstance(server, properties);
 			plugin.onLoad(args);
 			if(plugin instanceof Game)
 			{
 				games.add((Game)plugin);
 				server.Add((Game)plugin);
 			} else {
 				plugins.add(plugin);
 			}
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*public void loadplugins(Server server) {
 		if (!new File("plugins/").exists())
 			new File("plugins/").mkdir();
 		File folder = new File("plugins/");
 		File[] listOfFiles = folder.listFiles();
 		final String[] args = new String[] { "-normal" };
 		for (int i = 0; i < listOfFiles.length; i++) {
 		  if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".config")) {
 			  String classpath = getClass(listOfFiles[i]);
 			  if (classpath.equals("null"))
 				  continue;
 			  try {
 				loadPlugin(new File("plugins/" + classpath.split("=")[0].trim()), classpath.split("=")[1].trim(), args, server);
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		  } 
 		}
 	}
 	private String getClass(File file) {
 		try {
 			Scanner scan = new Scanner(file);
 			return scan.nextLine();
 		} catch(Exception e) {
 			return "null";
 		}
 	}
 	private void loadPlugin(File file, String classpath, String[] args, Server server) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
 		ClassLoader loader = URLClassLoader.newInstance(
 				new URL[] { file.toURL() },
 				getClass().getClassLoader()
 				);
 		Class<?> clazz = Class.forName(classpath, true, loader);
 		Class<? extends Plugin> runClass = clazz.asSubclass(Plugin.class);
 		// Avoid Class.newInstance, for it is evil.
 		Constructor<? extends Plugin> ctor = runClass.getConstructor(Server.class);
 		Plugin doRun = ctor.newInstance(server);
 		doRun.onLoad(args);
 		if (doRun instanceof Game)
 			games.add((Game)doRun);
 		else
 			plugins.add(doRun);
 	}*/
 
 }

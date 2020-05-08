 /*
  * This file is part of Spoutcraft Launcher (http://wiki.getspout.org/).
  * 
  * Spoutcraft Launcher is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Spoutcraft Launcher is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.spoutcraft.launcher;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 
 import org.spoutcraft.launcher.gui.LoadingScreen;
 
 import org.spoutcraft.launcher.gui.LoginForm;
 import org.spoutcraft.launcher.logs.SystemConsoleListener;
 import org.spoutcraft.launcher.modpacks.ModPackListYML;
 import org.spoutcraft.launcher.modpacks.ModPackUpdater;
 import org.spoutcraft.launcher.modpacks.ModPackYML;
 
 import com.beust.jcommander.JCommander;
 
 public class Main {
 	
 	static String[] args_temp;
 	public static String build = "0.5.0";
 	public static String currentPack;
 	static File recursion;
 	public static LoginForm loginForm;
 	
 	public Main() throws Exception {
 		main(new String[0]);
 	}
 
 	public static void reboot(String memory) {
 		try {
 			int mem = 1 << 9 + SettingsUtil.getMemorySelection();
 			String pathToJar = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
 			ArrayList<String> params = new ArrayList<String>();
 			if (PlatformUtils.getPlatform() == PlatformUtils.OS.windows) {
 				params.add("javaw"); // Windows-specific
 			} else {
 				params.add("java"); // Linux/Mac/whatever
 			}
 			if(memory.equals(("-Xmx" + mem + "m")))
 			{
 				params.add(memory);
 			}
 			else
 			{
 				params.add("-Xmx" + mem + "m");
 				params.add(memory);
 			}
 			params.add("-classpath");
 			params.add(pathToJar);
 			params.add("org.spoutcraft.launcher.Main");
 			for (String arg : args_temp) {
 				params.add(arg);
 			}
 			
 			
 			if (PlatformUtils.getPlatform() == PlatformUtils.OS.macos) {
 				params.add("-Xdock:name=\"Technic Launcher\"");
 				
 				try {
 						File icon = new File(PlatformUtils.getWorkingDirectory(), "launcher_icon.icns");
 						GameUpdater.copy(Main.class.getResourceAsStream("/org/spoutcraft/launcher/launcher_icon.icns"), new FileOutputStream(icon));
 						params.add("-Xdock:icon=" + icon.getCanonicalPath());
 				}
 				catch (Exception ignore) { }
 			}
 			ProcessBuilder pb = new ProcessBuilder(params);
 			Process process = pb.start();
 			if(process == null)
 				throw new Exception("!");
 			System.exit(0);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static boolean isDebug()
 	{
 		return 	java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");	
 	}
 	
 	public static void main(String[] args) throws Exception {
 		
 		LoadingScreen ls = new LoadingScreen();
 		if (!isDebug()) ls.setVisible(true);
 		Options options = new Options();
 		try {
 			new JCommander(options, args);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		MinecraftUtils.setOptions(options);
 		recursion = new File(PlatformUtils.getWorkingDirectory(), "rtemp");	
 
 		args_temp = args;
 		boolean relaunch = false;
 		try {
 			if (!recursion.exists()) {
 				relaunch = true;
 			} else {
 				recursion.delete();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		if (relaunch) {
 			ls.close();
 			if (SettingsUtil.getMemorySelection() < 6) {
 				int mem = 1 << (9 + SettingsUtil.getMemorySelection());
 				recursion.createNewFile();
 				if (isDebug())
 					System.exit(0);
 				else
 					reboot("-Xmx" + mem + "m");
 			}
 		}
 		
 		if (PlatformUtils.getPlatform() == PlatformUtils.OS.macos) {
 			try{
 				System.setProperty("apple.laf.useScreenMenuBar", "true");
 				System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Technic Launcher");
 				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			} catch (Exception ignore) { }
 		}
 		PlatformUtils.getWorkingDirectory().mkdirs();
 		new File(PlatformUtils.getWorkingDirectory(), "launcher").mkdir();
 
 		SystemConsoleListener listener = new SystemConsoleListener();
 
 		listener.initialize();
 		
 		System.out.println("------------------------------------------");
 		System.out.println("Launcher is starting....");
 		System.out.println("Launcher Build: " + getBuild());
 
 
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			System.out.println("Warning: Can't get system LnF: " + e);
 		}
 
 		loginForm = new LoginForm();
 
 		loginForm.loadLauncherData();
		ls.close();
 		loginForm.setVisible(true);
 	}
 
 	private static String getBuild() {
 		if (build == null) {
 			File buildInfo = new File(PlatformUtils.getWorkingDirectory(), "launcherVersion");
 			if (buildInfo.exists()) {
 				try {
 					BufferedReader bf = new BufferedReader(new FileReader(buildInfo));
 					build = bf.readLine();
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return build;
 	}
 }

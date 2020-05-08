 package fr.minepod.launcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.jar.Attributes;
 import java.util.jar.JarInputStream;
 import java.util.jar.Manifest;
 
 public class Start {
 	private static Profile Profile = new Profile();
 	private static Downloader Downloader = new Downloader();
 	
 	public static void main(String[] args) throws IOException {
 		if(args.length != 0)
 			new Config().SetBootstrapVersion(args[0]);
 		else
 			new Config().SetBootstrapVersion("unknown");
 		
 	    try {
 	        InputStream InputStream = Start.class.getProtectionDomain().getCodeSource().getLocation().openStream();
 	        JarInputStream JarInputStream = new JarInputStream(InputStream);
 	        Manifest Manifest = JarInputStream.getManifest();
 	        JarInputStream.close();
 	        InputStream.close();
	        System.out.println(Manifest);
 	        if(Manifest != null) {
 	            Attributes Attributes = Manifest.getMainAttributes();
 	            new Config().SetLauncherVersion(Attributes.getValue("Launcher-version"));
	            new Config().SetLauncherBuildTime("compil\u00E9 le " + new SimpleDateFormat("yyyy-MM-dd '\u00C0' HH:mm:ss").format(Attributes.getValue("Build-time")));
 	        } else {
 	        	new Config().SetLauncherVersion("version de d\u00E9veloppement");
 	        	new Config().SetLauncherBuildTime("");
 	        }
 		} catch(IOException e) {
 			CrashReport.SendReport(e.toString(), "doing main thread's tasks");
 		}
 		
 		new Config().SetConfig();
 		new fr.minepod.translate.Translate(Config.Language);
 		new Debug().SetDebug();
 		
 		DownloadRequiredFiles();
 	}
 	
 	 public static void DownloadRequiredFiles() {
 		 try {
 			 if(!new File(Config.LauncherLocation).exists())
 				 new File(Config.LauncherLocation).mkdir();			 
 			
 			 if(!new File(Config.MinecraftAppData + Config.Slash + "libraries").exists())
 				 new File(Config.MinecraftAppData + Config.Slash + "libraries").mkdir();
 			
 			 if(!new File(Config.MinecraftAppData + Config.Slash + "versions").exists())
 			 	 new File(Config.MinecraftAppData + Config.Slash + "versions").mkdir();
 			
 			 if(!new File(Config.MinecraftAppData + Config.Slash + "versions" + Config.Slash + Config.LauncherName).exists())
 			   	 new File(Config.MinecraftAppData + Config.Slash + "versions" + Config.Slash + Config.LauncherName).mkdir();
 			
 			 if(!new File(Config.LauncherLocation + Config.Slash + "mods").exists())
 				 new File(Config.LauncherLocation + Config.Slash + "mods").mkdir();
 
 			
 			 Downloader.DownloadFiles(new URL(Config.LauncherNewsHtmlUrl), Config.LauncherNewsHtml, false);
 			 Downloader.DownloadFiles(new URL(Config.LauncherNewsCssUrl), Config.LauncherNewsCss, false);
 			
 			 Config.Gui = new Gui(new URL("file:///" + Config.LauncherNewsCss), ClassFile.ReadFile(Config.LauncherNewsHtml), Config.LauncherVersion, Config.LauncherBuildTime);
 			 
 		     DownloaderThread DT1 = new DownloaderThread(Config.LibrariesLatestVersionUrl, Config.LauncherLocation + Config.Slash + "Libraries.md5", Config.MinecraftAppData + Config.Slash, "libraries", Config.LauncherZippedLibraries);
 		     DT1.start();
 		     
 		     DownloaderThread DT2 = new DownloaderThread(Config.VersionsLatestVersionUrl, Config.LauncherLocation + Config.Slash + "Versions.md5", Config.MinecraftAppData + Config.Slash + "versions" + Config.Slash, Config.LauncherName, Config.LauncherZippedVersions);
 		     DT2.start();
 		     
 		     DownloaderThread DT3 = new DownloaderThread(Config.ModsLatestVersionUrl, Config.LauncherLocation + Config.Slash + "Mods.md5", Config.LauncherLocation + Config.Slash, "mods", Config.LauncherZippedMods);
 		     DT3.start();
 		     
 		 	 /**
 		 	  * Temporary fix
 		 	  */
 		     //DownloaderThread DT4 = new DownloaderThread(Config.ResourcepacksLatestVersionUrl, Config.LauncherLocation + Config.Slash + "Resourcepacks.md5", Config.LauncherLocation + Config.Slash, "resourcepacks", Config.LauncherZippedResourcepacks);
 		     //DT4.start();
 		     /**
 		      * TODO: Check correctly md5 for resourcepacks
 		      */
 		     
 			 Downloader.DownloadFiles(new URL(Config.MinecraftLatestVersionUrl), Config.LauncherMinecraftJar, false);
 			
 			 if(new File(Config.ProfilesPath).exists()) {
 				 if(new File(Config.ProfilesVersionPath).exists()) {
 					 if(ClassFile.ReadFile(Config.ProfilesVersionPath).contains(Config.ProfilesVersion)) {
 						 Profile.Set(Config.LauncherName, Config.ProfilesPath, Config.LauncherLocation);
 					 } else {
 						 System.out.println("Current version: " + ClassFile.ReadFile(Config.ProfilesVersionPath));
 						 System.out.println("New profile version found: " + Config.ProfilesVersion);
 						 Profile.Update(Config.LauncherName, Config.ProfilesPath, Config.LauncherLocation);
 						 ClassFile.WriteFile(Config.ProfilesVersionPath, Config.ProfilesVersion);
 					 }
 				 } else {
 					 System.out.println("Profile version do not exists, creating new one");
 					 Profile.Set(Config.LauncherName, Config.ProfilesPath, Config.LauncherLocation);
 					 ClassFile.WriteFile(Config.ProfilesVersionPath, Config.ProfilesVersion);
 				 }
 				
 			 	 /**
 			 	  * Temporary fix
 			 	  */
 			     //while(DT1.isAlive() || DT2.isAlive() || DT3.isAlive() || DT4.isAlive()) {
 				 while(DT1.isAlive() || DT2.isAlive() || DT3.isAlive()) {
 			     /**
 			      * TODO: Check correctly md5 for resourcepacks
 			      */
 					 Thread.sleep(500);
 				 }
 				 
 				 System.out.println("Ready!");
 				 Config.Gui.EnableButton();
 
 			 } else {
 				 System.out.println("Profile do not exists");
 			     javax.swing.JOptionPane.showMessageDialog(null, "Lancez le jeu via le launcher Mojang, fermez-le et relancez le launcher " + Config.LauncherName, "Attention", javax.swing.JOptionPane.WARNING_MESSAGE);
 			     System.exit(0);
 			 }
 
 		 } catch (IOException e) {	 
 			 e.printStackTrace();
 			 CrashReport.SendReport(e.toString(), "doing main thread's tasks");
 		 } catch (Exception e) {
 			 e.printStackTrace();
 			 CrashReport.SendReport(e.toString(), "doing main thread's tasks");
 		 }
 		 
 	 }
 	 
 	 public static void LaunchGame(String ParLauncherMinecraftJar, String ParLauncherLocation) {
 		 System.out.println(ParLauncherMinecraftJar);
 		 try {
 			 new JarLoader(ParLauncherMinecraftJar);
 			 System.exit(0);
 		 } catch (Exception e) {
 			 CrashReport.SendReport(e.toString(), "launching game");
 		 }
 	 }
 }

 package fr.minepod.launcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 public class Start {
 	private static Profile Profile = new Profile();
 	private static Downloader Downloader = new Downloader();
 	
	public static void main(String[] args) throws IOException {
 		if(args.length != 0)
 			new Config().SetBootstrapVersion(args[0]);
 		else
 			new Config().SetBootstrapVersion("unknown");
 		
 		new Config().SetConfig();
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
 			
 			 Config.Gui = new Gui(new URL("file:///" + Config.LauncherNewsCss), fr.minepod.Utils.Files.ReadFile(Config.LauncherNewsHtml), Config.LauncherVersion, Config.LauncherBuildTime);
 			 
 		     DownloaderThread DT1 = new DownloaderThread(Config.LibrariesLatestVersionUrl, Config.LauncherLocation + Config.Slash + "Libraries.md5", Config.MinecraftAppData + Config.Slash, "libraries", Config.LauncherZippedLibraries, fr.minepod.Utils.Files.md5(Config.LauncherZippedLibraries));
 		     DT1.start();
 		     
 		     DownloaderThread DT2 = new DownloaderThread(Config.VersionsLatestVersionUrl, Config.LauncherLocation + Config.Slash + "Versions.md5", Config.MinecraftAppData + Config.Slash + "versions" + Config.Slash, Config.LauncherName, Config.LauncherZippedVersions, fr.minepod.Utils.Files.md5(Config.LauncherZippedVersions));
 		     DT2.start();
 		     
 		     DownloaderThread DT3 = new DownloaderThread(Config.ModsLatestVersionUrl, Config.LauncherLocation + Config.Slash + "Mods.md5", Config.LauncherLocation + Config.Slash, "mods", Config.LauncherZippedMods, fr.minepod.Utils.Files.md5(Config.LauncherZippedMods));
 		     DT3.start();
 		     
 		     DownloaderThread DT4 = new DownloaderThread(Config.ResourcepacksLatestVersionUrl, Config.LauncherLocation + Config.Slash + "Resourcepacks.md5", Config.LauncherLocation + Config.Slash, "resourcepacks", Config.LauncherZippedResourcepacks, fr.minepod.Utils.Files.md5(Config.LauncherZippedResourcepacks));
 		     DT4.start();
 		     
 			 Downloader.DownloadFiles(new URL(Config.MinecraftLatestVersionUrl), Config.LauncherMinecraftJar, false);
 			
 			 if(new File(Config.ProfilesPath).exists()) {
 				 if(new File(Config.ProfilesVersionPath).exists()) {
 					 if(fr.minepod.Utils.Files.ReadFile(Config.ProfilesVersionPath).contains(Config.ProfilesVersion)) {
 						 Profile.Set(Config.LauncherName, Config.ProfilesPath, Config.LauncherLocation);
 					 } else {
 						 Config.Logger.info("Current version: " + fr.minepod.Utils.Files.ReadFile(Config.ProfilesVersionPath));
 						 Config.Logger.info("New profile version found: " + Config.ProfilesVersion);
 						 Profile.Update(Config.LauncherName, Config.ProfilesPath, Config.LauncherLocation);
 						 fr.minepod.Utils.Files.WriteFile(Config.ProfilesVersionPath, Config.ProfilesVersion);
 					 }
 				 } else {
 					 Config.Logger.warning("Profile version does not exist, creating new one");
 					 Profile.Set(Config.LauncherName, Config.ProfilesPath, Config.LauncherLocation);
 					 fr.minepod.Utils.Files.WriteFile(Config.ProfilesVersionPath, Config.ProfilesVersion);
 				 }
 				
 			     while(DT1.isAlive() || DT2.isAlive() || DT3.isAlive() || DT4.isAlive()) {
 					 Thread.sleep(500);
 				 }
 				 
 			     Config.Gui.Finish();
 				 Config.Logger.info("Ready!");
 				 Config.Gui.EnableButton();
 
 			 } else {
 				 Config.Logger.severe("Profile do not exists");
 			     javax.swing.JOptionPane.showMessageDialog(null, "Lancez le jeu via le launcher Mojang, fermez-le et relancez le launcher " + Config.LauncherName, "Attention", javax.swing.JOptionPane.WARNING_MESSAGE);
 			     System.exit(0);
 			 }
 			 
 		 } catch (IOException e) {	 
 			CrashReport.SendReport(e.toString(), Langage.DOINGMAINTHREADTASKS.toString());
 		 } catch (Exception e) {
 			CrashReport.SendReport(e.toString(), Langage.DOINGMAINTHREADTASKS.toString());
 		 }
 		 
 	 }
 	 
 	 public static void LaunchGame() {
 		 try {
 			 new LauchMinecraft(Config.LauncherMinecraftJar);
 			 System.exit(0);
 		 } catch (Exception e) {
 			 CrashReport.SendReport(e.toString(), "launching game");
 		 }
 	 }
 }

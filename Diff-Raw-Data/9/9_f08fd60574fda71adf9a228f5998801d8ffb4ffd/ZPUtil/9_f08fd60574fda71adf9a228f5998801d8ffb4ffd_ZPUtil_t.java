 package net.zetaeta.libraries;
 
 import static org.bukkit.Bukkit.getPluginManager;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ListIterator;
import java.util.logging.Logger;
 
import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Provides utility methods for my plugins.
  * 
  * @author Zetaeta
  *
  */
public class ZPUtil {
     
     protected ZPUtil() { }
     
     /**
      * Checks whether a CommandSender has a permission, telling them they do not have acces to the command if they don't have it.
      * 
      * @param sender CommandSender to test
      * @param permission String permission to test
      * @return Whether sender has permission
      */
     public static boolean checkPermission(CommandSender sender, String permission) {
         return checkPermission(sender, permission, true, true);
     }
     
 
     public static boolean checkPermission(CommandSender sender, String permission, boolean warn, boolean recursive) {
         boolean hasPerm;
         if (recursive) {
             hasPerm = checkPermissionRecursive(sender, permission);
         }
         else {
             hasPerm = sender.hasPermission(permission);
         }
         if (!hasPerm && warn) {
             sender.sendMessage("cYou do not have access to that command!");
         }
         return hasPerm;
     }
     
     
     public static boolean checkPermissionRecursive(CommandSender sender, String permission) {
         String[] nodes = permission.split("\\.");
         if (nodes.length == 0) {
             return sender.hasPermission(permission);
         }
         if (getPluginManager().getPermission(permission) != null) {
             return sender.hasPermission(permission);
         }
         StringBuilder nodesIt = new StringBuilder(permission);
         String permIt = permission;
         List<String> toRegister = new ArrayList<String>(nodes.length);
         toRegister.add(permission);
        for (int i=nodes.length - 1, length = permission.length(); i > 0 && getPluginManager().getPermission(permIt) == null; --i) {
             nodesIt.delete(length - nodes[i].length(), length);
             length -= nodes[i].length();
             if (nodesIt.charAt(nodesIt.length() - 1) == '.') {
                 nodesIt.deleteCharAt(nodesIt.length() - 1);
                 --length;
             }
             permIt = nodesIt.toString().concat(".*");
             toRegister.add(permIt);
         }
         if (getPluginManager().getPermission(permIt) == null) {
             getPluginManager().addPermission(new Permission(permIt));
         }
         Permission parent = getPluginManager().getPermission(permIt), current = parent, previous = current;
         {
             for (ListIterator<String> registerIt = toRegister.listIterator(toRegister.size() - 1); registerIt.hasPrevious();) {
                 String permStr = registerIt.previous();
                 current = new Permission(permStr);
                 current.addParent(previous, true);
                 previous = current;
                 if (!getPluginManager().getPermissions().contains(current)) {
                     getPluginManager().addPermission(current);
                 }
             }
         }
         return sender.hasPermission(permission);
     }
     
     /**
      * Adds the basic colours (0 - f) to a String by replacing "&<colour digit>" with <colour digit>
      * 
      * @param string String to colourise
      * @return Colourised string.
      */
     public static String addBasicColour(String string) {
         string = string.replaceAll("(([a-fA-F0-9]))", "$2");
 
         string = string.replaceAll("(&([a-fA-F0-9]))", "$2");
 
         return string;
       }
     
     @SuppressWarnings("javadoc")
     public static FileConfiguration getFileConfiguration(JavaPlugin plugin, String filename, boolean load) throws IOException {
         File file = new File(plugin.getDataFolder(), filename);
         file.createNewFile();
         FileConfiguration newcfg = YamlConfiguration.loadConfiguration(file);
         if(load) {
             InputStream confDefStream = plugin.getResource(filename);
             YamlConfiguration confDef = YamlConfiguration.loadConfiguration(confDefStream);
             newcfg.setDefaults(confDef);
         }
         return newcfg;
     }
     
     
     /**
      * Concatenates an array of Strings into a single String by putting spaces between them
      * 
      * @param array Array of Strings to be concatenated
      * @return Single String representing the array
      */
     public static String arrayAsString(String[] array) {
         if (array.length == 0) {
             return "";
         }
         System.out.println("arrayAsString");
         StringBuilder sb = new StringBuilder();
         for (int i=0; i<array.length - 1; i++) {
             sb.append(array[i]).append(" ");
         }
         sb.append(array[array.length - 1]);
         return sb.toString();
     }
     
     /**
      * Concatenates an array of Strings into a single String by putting commas and spaces between them
      * 
      * @param array Array of Strings to be concatenated
      * @return Single String representing the array
      */
     public static String arrayAsCommaString(String[] array) {
         if (array.length == 0) {
             return "";
         }
         StringBuilder sb = new StringBuilder();
         for (int i=0; i<array.length - 1; i++) {
             sb.append(array[i]).append(", ");
         }
         sb.append(array[array.length - 1]);
         return sb.toString();
     }
     
     
     /**
      * Removes the first index of an array of <T>. Convenience method for Arrays.copyOfRange(array, 1, array.length)
      * 
      * @param array Array of <T> to be modified
      * @return array without the first index.
      */
     public static <T> T[] removeFirstIndex(T[] array) {
         if (array.length == 0) {
             return array;
         }
         return Arrays.copyOfRange(array, 1, array.length);
     }
     
     /**
      * Concatenates multiple objects into a String using StringBuilder and String.valueOf(Object)
      * 
      * @param args Objects to concat
      * @return String value of Objects concatenated.
      */
     public static String concatString(Object... args) {
         if (args.length == 0)
             return "";
         StringBuilder sb = new StringBuilder(String.valueOf(args[0]));
         for (Object o : args) {
             sb.append(String.valueOf(o));
         }
         return sb.toString();
     }
 
     /**
      * Concatenates multiple objects into a String using StringBuilder and String.valueOf(Object)
      * 
      * @param args Objects to concat
      * @param bufferLength Amount of prereserved space in the StringBuilder.
      * @return String value of Objects concatenated.
      */
     public static String concatString(int bufferLength, Object... args) {
         if (args.length == 0)
             return "";
         StringBuilder sb = new StringBuilder(bufferLength);
         for (Object o : args) {
             sb.append(String.valueOf(o));
         }
         return sb.toString();
     }
     
     /**
      * Concatenates multiple Strings into a single String using StringBuilder
      * 
      * @param args Strings to concat
      * @return String value of args concatenated.
      */
     public static String concatString(String... args) {
         if (args.length == 0)
             return "";
         StringBuilder sb = new StringBuilder(args.length * 8);
         for (String s : args) {
             sb.append(s);
         }
         return sb.toString();
     }
 
     /**
      * Concatenates multiple Strings into a single String using StringBuilder
      * 
      * @param args Strings to concat
      * @param bufferLength Amount of prereserved space in the StringBuilder.
      * @return String value of args concatenated.
      */
     public static String concatString(int bufferLength, String... args) {
         StringBuilder sb = new StringBuilder(bufferLength);
         for (String s : args) {
             sb.append(s);
         }
         return sb.toString();
     }
 }

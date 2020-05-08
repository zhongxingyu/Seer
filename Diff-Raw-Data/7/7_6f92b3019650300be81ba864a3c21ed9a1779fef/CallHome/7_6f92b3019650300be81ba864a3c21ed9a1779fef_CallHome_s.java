 /*
  * Copyright (c) 2011. SwearWord
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this
  * software and associated documentation files (the "Software"), to deal in the Software
  * without restriction, including without limitation the rights to use the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to the following
  * conditions:
  *
  * No modifications may be made to the Software. User must use the code as is.
  *
  * Users of any software implementing this Software must be made aware of this Software's
  * implementation. This Software may not be executed on uninformed clients.
  *
  * The above copyright notice and this permission notice shall be included in all copies
  * or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
  * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
  * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package blockface.stats;
 
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.config.Configuration;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 /*
 @
  */
 
 public class CallHome{
 
     private static Plugin plugin;
     private static int task=-1;
 
     public static void load(Plugin p) {
        if(!verifyConfig()) return;
         plugin = p;
         task = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin,new CallTask(plugin),0L,20L*60L*10);
         System.out.println(plugin.getDescription().getName() + " is keeping usage stats. To opt-out for whatever bizarre reason, check plugins/stats.");
 
     }
 
     private static Boolean verifyConfig() {
         File config = new File("plugins/usage/config.yml");
         if(!config.getParentFile().exists()) config.getParentFile().mkdir();
         if(!config.exists()) try {
             config.createNewFile();
         } catch (IOException e) {
             return false;
         }
         Configuration cfg = new Configuration(config);
         cfg.load();
        return cfg.getBoolean("opt-out",false);
     }
 
     public static void unload() {
         if(task>-1) {
             System.out.println(plugin.getDescription().getName() + " ");
 
         }
     }
 
 
 }
 
 class CallTask implements Runnable {
     private Plugin plugin;
 
     public CallTask(Plugin plugin) {
         this.plugin = plugin;
     }
 
 
     public void run() {
         try {
             if(postUrl().contains("Success")) return;
         } catch (Exception ignored) {
         }
         System.out.println("Could not call home.");
     }
 
     private String postUrl() throws Exception {
         String url = String.format("http://plugins.blockface.org/usage/update.php?name=%s&build=%s&plugin=%s&port=%s",
                 plugin.getServer().getName(),
                 plugin.getDescription().getVersion(),
                 plugin.getDescription().getName(),
                 plugin.getServer().getPort());
         URL oracle = new URL(url);
         URLConnection yc = oracle.openConnection();
         BufferedReader in = new BufferedReader(
                                 new InputStreamReader(
                                 yc.getInputStream()));
         String inputLine;
         while ((inputLine = in.readLine()) != null)
             inputLine += "";
         return inputLine;
     }
 }

 package de.damarus.mcdesktopinfo;
 
 import java.util.HashMap;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 public class RequestHandler {
 
     public Server server;
     private HashMap<String, String> values;
 
     public RequestHandler(Server server) {
         this.server = server;
 
         values = new HashMap<String, String>();
 
         updateValues();
     }
 
     public void updateValues() {
         values.put("playerCount", server.getOnlinePlayers().length + " / " + server.getMaxPlayers());
         values.put("serverName", server.getName()); // AFAIK it always returns "craftbukkit"
         values.put("serverVersion", server.getVersion());
         values.put("pluginVersion", Config.PLUGIN_VERSION);
     }
 
     public String get(String request, HashMap<String, String> params) {
         // Handle more complex requests
         if(request.equals("kick")) doKick(params);
         if(request.equals("playerList")) return getPlayerList(params);
 
         // Handle simpler requests
         if(values.containsKey(request)) return values.get(request);
 
         // ...else return an empty string
         return "";
     }
 
     public boolean doKick(HashMap<String, String> params) {
         // Report to serverlog that a kick was requested
         McDesktopInfo.log("The IP " + params.get("gadgetIp") + " requested to kick the player " + params.get("player") +
             " using the password " + params.get("adminPw"));
 
         if(PasswordSystem.checkAdminPW(params.get("adminPw"))) {
             Player player = server.getPlayer(params.get("player"));
             if(player != null) {
                 player.kickPlayer("Kicked with McDesktopInfo");
                 return true;
             }
         }
         return false;
     }
 
     public String getPlayerList(HashMap<String, String> params) {
         Player[] players = server.getOnlinePlayers();
 
         String output = "";
         for(Player p : players) {
             output += "+" + p.getName();
         }
 
         output = output.replaceFirst("[+]", "");
         return output;
     }
 }

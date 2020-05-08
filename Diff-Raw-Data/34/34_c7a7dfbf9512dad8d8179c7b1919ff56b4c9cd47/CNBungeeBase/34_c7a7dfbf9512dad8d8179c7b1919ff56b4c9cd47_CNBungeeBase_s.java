 package de.cubenation.cnbungeebase;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.md_5.bungee.api.Callback;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.ServerPing;
 import net.md_5.bungee.api.plugin.Command;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.api.plugin.Plugin;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.PluginMessageEvent;
 import net.md_5.bungee.api.event.ProxyPingEvent;
 import net.md_5.bungee.api.event.ServerKickEvent;
 import net.md_5.bungee.event.EventHandler;
 
 public class CNBungeeBase extends Plugin implements Listener {
     
     HashMap<String, Integer> serverFails = new HashMap<String, Integer>();
     HashMap<String, Integer> serverCounts = new HashMap<String, Integer>();
     HashMap<String, Integer> serverMaxs = new HashMap<String, Integer>();
     HashMap<String, String> serverMotds = new HashMap<String, String>();
     Timer updateTimer;
 
     public void onEnable() {
         ProxyServer.getInstance().getPluginManager().registerListener(this, this);
         ProxyServer.getInstance().registerChannel("cnt_summon");
 
         updateTimer = new Timer();
         updateTimer.schedule(new TimerTask() {
             @Override
             public void run() {
                 Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();
                 
                 for (final String server : servers.keySet()) {
                 	updateServer(servers.get(server));
                 }
             }
        }, 5000, 5000);
         
         
     	ProxyServer.getInstance().getPluginManager().registerCommand(this, new Command("sendall", "cnabase.sendall"){
 
 			@Override
 			public void execute(CommandSender sender, String[] args) {
 				if (args.length != 2) {
 					sender.sendMessage("Check documentation!");
 					return;
 				}
 				
 				sendPlayersFromTo(args[0], args[1]);
 			}
     	});
 
     }
     
     protected void updateServer(ServerInfo serverInfo) {
     	final String server = serverInfo.getName();
     	serverInfo.ping(new Callback<ServerPing>() {
 
             public void done(ServerPing ping, Throwable arg1) {
                 if (ping != null) {
                     serverCounts.put(server, ping.getCurrentPlayers());
                     serverMaxs.put(server, ping.getMaxPlayers());
                     serverMotds.put(server, ping.getMotd());
                     serverFails.remove(server);
                     
                 } else {
                     if (serverFails.containsKey(server)) {
                         int last = serverFails.get(server);
                         if (last >= 3) {
                             serverCounts.put(server, -1);
                             
                         } else {
                             serverFails.put(server, last+1);
                             
                         }
                         
                     } else {
                         serverFails.put(server, 1);
                         
                     }
                     
                 }
             }
             
         });
     }
 
 	public void onDisable() {
         updateTimer.cancel();
     }
     
 	/*
     @Subscribe
     public void onServerConnect (ServerConnectEvent ev) {
         final ServerInfo server = ev.getTarget();
         
         ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() {
             public void run() {
                 updateServer(server);
             }}, 1, TimeUnit.SECONDS);
     }
     */
     
 	@EventHandler
     public void onServerKick(ServerKickEvent event) {
         if (event.getKickReason().equalsIgnoreCase("fallback")) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onPluginMessageEvent (PluginMessageEvent event) {
     	
         if (event.getTag().equals("cnt_summon")) {
         	
             String pluginMessage = new String(event.getData());
             System.out.println(pluginMessage);
             
             // Split the message to get server/gate
             String[] data = pluginMessage.replaceAll("\\[","").replaceAll("\\]","").split("@");
             
         	if (data.length < 2) {
                 System.out.println("Faulty plugin message");
                 event.setCancelled(true);
             	return;
             }
             String fromServerName = data[0].trim();
             String toServerName = data[1].trim();
             
             sendPlayersFromTo(fromServerName, toServerName);
             
         }
 
 
     }
     
     private void sendPlayersFromTo(String fromServerName, String toServerName) {
         final ServerInfo toServer = ProxyServer.getInstance().getServerInfo(toServerName);
         ServerInfo fromServer = ProxyServer.getInstance().getServerInfo(fromServerName);
 
         if (fromServer == null) {
             System.out.println("Requested to send players from '" + fromServerName + "' to '" + toServerName + "', but source server is offline?!");
             return;
         } else if (toServer == null) {
             System.out.println("Requested to send players from '" + fromServerName + "' to '" + toServerName + "', but target server is offline?!");
             return;
         } else if (fromServer == toServer) {
             System.out.println("Requested to send players from and to the same server (" + toServerName + "). Cancelling...");
             return;
         }
 
         System.out.println("Requested to send players from '" + fromServerName + "' to '" + toServerName + "'");
         for (final ProxiedPlayer pPlayer : fromServer.getPlayers().toArray(new ProxiedPlayer[0])) {
 
         	// just to be sure
         	if (pPlayer.getServer().getInfo() != fromServer) continue;
         	
             System.out.println("Sending player '" + pPlayer.getDisplayName() + "' from '" + fromServerName + "' to '" + toServerName + "'");
             pPlayer.connect(toServer);
         }		
 	}
 
 	@EventHandler
     public void onProxyPing (ProxyPingEvent ev) {
         String defaultServer = ev.getConnection().getListener().getDefaultServer();
         
         if (serverCounts.containsKey(defaultServer)) {
             int playerCount = serverCounts.get(defaultServer);
             if (playerCount != -1) {
                 int playerMax = serverMaxs.get(defaultServer);
                 String serverMotd = serverMotds.get(defaultServer);
                 
                 ev.setResponse(new ServerPing(ProxyServer.getInstance().getProtocolVersion(),
                         ProxyServer.getInstance().getGameVersion(), 
                         serverMotd,
                         playerCount, 
                         playerMax)
                         );
             	
             } else {
                 ev.setResponse(new ServerPing(ProxyServer.getInstance().getProtocolVersion(),
                         ProxyServer.getInstance().getGameVersion(), 
                         "§4Wartungsarbeiten / Offline",
                         0, 
                         0)
                         );
             }
             
         } else {
             ev.setResponse(new ServerPing(ProxyServer.getInstance().getProtocolVersion(),
                     ProxyServer.getInstance().getGameVersion(), 
                     "§6Serverinfos werden gerade geladen",
                     0, 
                     0)
                     );
 
         }
 
     }
 }

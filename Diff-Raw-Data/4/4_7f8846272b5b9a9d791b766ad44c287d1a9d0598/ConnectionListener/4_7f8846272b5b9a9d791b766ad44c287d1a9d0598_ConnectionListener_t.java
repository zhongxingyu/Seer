 package com.minebans.minebansbungeecord;
 
 import java.net.InetAddress;
 
 import net.md_5.bungee.api.ServerPing;
 import net.md_5.bungee.api.event.ProxyPingEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 
 public class ConnectionListener implements Listener {
 	
 	private MineBansBungeeCord plugin;
 	
 	public ConnectionListener(MineBansBungeeCord plugin){
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onProxyPing(ProxyPingEvent event){
 		try{
 			InetAddress address = event.getConnection().getAddress().getAddress();
 			ServerPing ping = event.getResponse();
 			
 			if ((!address.isAnyLocalAddress() && address.getHostAddress().equals(InetAddress.getByName("minebans.com").getHostAddress())) || MineBansBungeeCord.DEBUG_MODE){
				if (plugin.requestProxy.getCurrentRequest() != null){
					event.setResponse(new ServerPing(ping.getProtocolVersion(), ping.getGameVersion(), plugin.requestProxy.getCurrentRequest().getMOTD(), ping.getCurrentPlayers(), ping.getMaxPlayers()));
				}
 			}
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 }

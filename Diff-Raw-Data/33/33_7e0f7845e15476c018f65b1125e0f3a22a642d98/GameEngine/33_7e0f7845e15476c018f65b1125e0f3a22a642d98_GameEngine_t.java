 package com.prsc.gs.core;
 
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
 
 import com.prsc.gs.Server;
 import com.prsc.gs.connection.Client;
 import com.prsc.gs.event.impl.ReloadFilterEvent;
 import com.prsc.gs.event.impl.SaveProfileEvent;
 import com.prsc.gs.event.impl.ShopRestockEvent;
 import com.prsc.gs.model.Player;
 import com.prsc.gs.model.Shop;
 import com.prsc.gs.model.World;
 import com.prsc.gs.plugins.PluginHandler;
 import com.prsc.gs.util.Logger;
 
 public final class GameEngine extends Thread {
 
     private static final World world = World.getWorld();
     
    private final Set<Client> clients = new CopyOnWriteArraySet<Client>();
 
     private final DelayedEventHandler eventHandler = new DelayedEventHandler();
     
     private final ClientUpdater clientUpdater = new ClientUpdater();
     
     private long lastSentClientUpdate = getAccurateTimestamp();
     
     private long lastSentClientUpdateFast = getAccurateTimestamp();
     
     private boolean running = true; 
     
    public Set<Client> getClients() {
     	return clients;
     }
     
     public void addClient(Client client) {
     	if(!clients.contains(client)) {
     		clients.add(client);
     	}
     }
     
     public void removeClient(Client client) {
     	if(clients.contains(client)) {
     		clients.remove(client);
     	}
     }
     
     public void emptyWorld() {
         for (Player p : world.getPlayers()) {
             p.save();
             p.getActionSender().sendLogout();
         }
         World.getWorld().getServer().getLoginConnector().getActionSender().saveProfiles(false);
     }
 
     public void kill() {
         Logger.println("Terminating GameEngine");
         Server.getInstance().getTaskManager().getAvailableTaskWorkers().shutdown();
         running = false;
     }
 
     public void processEvents() {
     	eventHandler.doEvents();
     } 
 
     private void processLoginServer() {
         LoginConnector connector = World.getWorld().getServer().getLoginConnector();
         if (connector != null) {
             connector.processIncomingPackets();
         }
     }
     
     private void processIncomingPackets() {
     	for(Client client : clients) {
     		client.process();
     	}
     }
     
     private void processUpdate() {		
     	long now = getAccurateTimestamp();
 		long timeSinceMajor = now - lastSentClientUpdate;
 		long timeSinceMinor = now - lastSentClientUpdateFast;
 		
 		if (timeSinceMajor >= 600) {
             lastSentClientUpdate = now;
             clientUpdater.doMajor();
 		}
 		
 		if (timeSinceMinor >= 104) { // send queued packets?
             lastSentClientUpdateFast = now;
             clientUpdater.sendQueuedPackets();
             clientUpdater.doMinor();
         } 
     }
     
     public static long getAccurateTimestamp() {
    	return System.currentTimeMillis();
     }
 
     @Override
     public void run() {
         PluginHandler.getPluginHandler().handleAction("Startup", new Object[]{});
 
         //eventHandler.add(new DatabaseReconnectionEvent());
         eventHandler.add(new SaveProfileEvent());
         eventHandler.add(new ReloadFilterEvent());
         
         for (Shop shop : world.getShops()) {
 			eventHandler.add(new ShopRestockEvent(shop));
 		}
         
         while (running) {
             try {
                 Thread.sleep(10);
             } catch (InterruptedException ie) {
             }
 	        processIncomingPackets(); // threaded
 	        processLoginServer(); // threaded
 	        processEvents(); // TODO thread out
 	        processUpdate(); // threaded
         } 
     }
 }

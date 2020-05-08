 package org.urish.banjii.connector;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.urish.banjii.model.Player;
 import org.urish.banjii.model.PlayerListener;
 import org.urish.banjii.model.PlayerManager;
 
 public class CintieConnector implements PlayerListener, Runnable {
 	private static final Logger logger = Logger.getLogger(CintieConnector.class.getName());
 
 	private final String cintieServer = "http://localhost:8080";
 	private final Set<Player> pendingUpdates = new HashSet<Player>(16);
 
 	public CintieConnector(PlayerManager playerManager) {
 		playerManager.addListener(this);
 
 		Thread thread = new Thread(this, "Cintie Connector Thread");
 		thread.setDaemon(true);
 		thread.start();
 	}
 
 	public void onPlayerUpdate(Player player) {
 		synchronized (pendingUpdates) {
 			pendingUpdates.add(player);
 			pendingUpdates.notify();
 		}
 	}
 
 	public void run() {
 		try {
 			new URL(cintieServer + "/app/start").openStream().close();
 		} catch (IOException e) {
 			logger.log(Level.WARNING, "Connection with cintie failed; No sound output will be available", e);
 			return;
 		}
 		while (true) {
 			String updateUrl = null;
 			String updateContent = null;
 			synchronized (pendingUpdates) {
 				try {
 					while (pendingUpdates.isEmpty()) {
 						pendingUpdates.wait();
 					}
 				} catch (InterruptedException e) {
 					logger.log(Level.SEVERE, "Cintie Connector Interrupted", e);
 					return;
 				}
				Player playerToUpdate = pendingUpdates.iterator().next();
 				updateUrl = "/app/pawns/" + (playerToUpdate.getId() + 1);
 				updateContent = "x=" + (playerToUpdate.getX() / 5) + "&y=" + (playerToUpdate.getY() / 5) + "&on="
 						+ playerToUpdate.isVisible();
				pendingUpdates.remove(playerToUpdate);
			}
			try {
				URLConnection connection = new URL(cintieServer + updateUrl).openConnection();
				connection.setDoOutput(true);
				connection.getOutputStream().write(updateContent.getBytes("utf-8"));
				connection.connect();
				connection.getInputStream();
			} catch (IOException e) {
				if (!e.getMessage().contains("500 for URL")) {
					logger.log(Level.WARNING, "Cintie Connection Failed", e);
 				}
 			}
 		}
 	}
 }

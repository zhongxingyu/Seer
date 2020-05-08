 package ru.dark32.chat;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class QuitListener implements Listener {

 	public void onLeft(final PlayerQuitEvent event ) {
 		String quitMessage = Main.localeConfig.getString(
 				"String.left." + Main.getPermissionsHandler().getGroup(event.getPlayer()), "&e$name left the game");
 		if (quitMessage.contains("$suffix")) {
 			quitMessage = quitMessage.replace("$suffix", Main.getPermissionsHandler().getSuffix(event.getPlayer()));
 		}
 		if (quitMessage.contains("$prefix")) {
 			quitMessage = quitMessage.replace("$prefix", Main.getPermissionsHandler().getPrefix(event.getPlayer()));
 		}
 		if (quitMessage.contains("$p")) {
 			quitMessage = quitMessage.replace("$p", "%1$s");
 		}
 		if (quitMessage.contains("$msg")) {
 			quitMessage = quitMessage.replace("$msg", "%2$s");
 		}
 		if (quitMessage.contains("$id")) {
 			String iden = Integer.toHexString(event.getPlayer().getTicksLived() + event.getPlayer().getEntityId());
 			quitMessage = quitMessage.replace("$id", iden);
 		}
 		event.setQuitMessage(ChanelRegister.colorUTF8(quitMessage, 3));
 
 	}
 }

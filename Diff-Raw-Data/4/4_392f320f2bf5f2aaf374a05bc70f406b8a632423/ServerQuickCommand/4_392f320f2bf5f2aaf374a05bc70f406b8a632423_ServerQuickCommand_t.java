 package lilypad.bukkit.portal.command;
 
 import java.util.List;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class ServerQuickCommand implements Listener {
 
 	private List<String> allowedServers;
 	private IRedirector redirector;
 	
 	public ServerQuickCommand(IConfig config, IRedirector redirector) {
 		this.allowedServers = config.getAllowedServers();
 		this.redirector = redirector;
 	}
 	
 	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		String command = event.getMessage().substring(1);
 		String server = null;
 		for(String allowedServer : this.allowedServers) {
 			if(!allowedServer.equalsIgnoreCase(command)) {
 				continue;
 			}
 			server = allowedServer;
 		}
 		if(server == null) {
 			return;
 		}
 		this.redirector.requestRedirect(event.getPlayer(), server);
 		event.setCancelled(true);
 	}
 
 }

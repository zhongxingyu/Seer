 package no.runsafe.bridge;
 
 import no.runsafe.framework.messaging.IMessageBusService;
 import no.runsafe.framework.messaging.Message;
 import no.runsafe.framework.messaging.MessageBusStatus;
 import no.runsafe.framework.messaging.Response;
 import no.runsafe.framework.plugin.PluginResolver;
 import org.bukkit.entity.Player;
 import org.kitteh.vanish.VanishManager;
 import org.kitteh.vanish.VanishPlugin;
 
 public class PlayerVanishStatus implements IMessageBusService
 {
 	public PlayerVanishStatus(PluginResolver resolver)
 	{
 		vanishNoPacket = resolver.<VanishPlugin>getPlugin("VanishNoPacket").getManager();
 	}
 
 	@Override
 	public String getServiceName()
 	{
 		return "PlayerStatus";
 	}
 
 	@Override
 	public Response processMessage(Message message)
 	{
 		Player player = message.getPlayer().getRaw();
 		if(message.getQuestion().equals("get.player.invisibility.on"))
 		{
 			Response response = new Response();
			if(!vanishNoPacket.isVanished(player))
 			{
 				response.setStatus(MessageBusStatus.OK);
				response.setResponse("Player is not vanished");
 			}
 			else
 			{
 				response.setStatus(MessageBusStatus.NOT_OK);
				response.setResponse("Player is vanished");
 			}
 			return  response;
 		}
 		if(message.getQuestion().equals("set.player.invisibility.on"))
 		{
 			if(!vanishNoPacket.isVanished(player))
 			{
 				vanishNoPacket.toggleVanish(player);
 				Response response = new Response();
 				response.setStatus(MessageBusStatus.OK);
 				response.setResponse("Player has vanished");
 				return response;
 			}
 		}
 		if(message.getQuestion().equals("set.player.invisibility.off"))
 		{
 			if(vanishNoPacket.isVanished(player))
 			{
 				vanishNoPacket.toggleVanish(player);
 				Response response = new Response();
 				response.setStatus(MessageBusStatus.OK);
 				response.setResponse("Player has appeared");
 				return  response;
 			}
 		}
 		return null;
 	}
 
 	private VanishManager vanishNoPacket;
 }

 package no.runsafe.nchat.events;
 
 import no.runsafe.framework.api.event.player.IPlayerChatEvent;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerChatEvent;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.framework.text.ChatColour;
 import no.runsafe.nchat.Constants;
 import no.runsafe.nchat.antispam.SpamHandler;
 import no.runsafe.nchat.handlers.ChatHandler;
 import no.runsafe.nchat.handlers.MuteHandler;
 
 public class ChatEvent implements IPlayerChatEvent
 {
 	public ChatEvent(ChatHandler chatHandler, MuteHandler muteHandler, SpamHandler spamHandler)
 	{
 		this.chatHandler = chatHandler;
 		this.muteHandler = muteHandler;
 		this.spamHandler = spamHandler;
 	}
 
 	@Override
 	public void OnPlayerChatEvent(RunsafePlayerChatEvent event)
 	{
 		RunsafePlayer player = event.getPlayer();
 
 		if (this.muteHandler.isPlayerMuted(player))
 		{
 			player.sendColouredMessage(Constants.CHAT_MUTED);
			event.cancel();
 			return;
 		}
 
 		String message = this.spamHandler.getFilteredMessage(player, event.getMessage());
 
 		if (message != null)
 		{
 			String chatMessage = this.chatHandler.formatChatMessage(message, player);
 			if (chatMessage != null)
 			{
 				event.setFormat(ChatColour.ToMinecraft(chatMessage.replaceAll(Constants.FORMAT_CHANNEL, "").trim()));
 				return;
 			}
 		}
		event.cancel();
 	}
 
 	private final ChatHandler chatHandler;
 	private final MuteHandler muteHandler;
 	private final SpamHandler spamHandler;
 }

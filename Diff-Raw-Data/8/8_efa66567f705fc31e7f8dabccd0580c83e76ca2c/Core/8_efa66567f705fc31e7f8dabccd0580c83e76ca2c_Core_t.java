 package no.runsafe.nchat;
 
 import no.runsafe.framework.RunsafeConfigurablePlugin;
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.configuration.IConfigurationFile;
 import no.runsafe.nchat.command.*;
 import no.runsafe.nchat.database.MuteDatabase;
 import no.runsafe.nchat.events.*;
 import no.runsafe.nchat.handlers.*;
 
 import java.io.InputStream;
 
 public class Core extends RunsafeConfigurablePlugin
 {
 	@Override
 	protected void PluginSetup()
 	{
 		// TODO: Implement an AFK system
 		// TODO: Re-implement channel functionality
 		// TODO: In-game whisper monitoring
 		// TODO: Fix odd death messages not being picked up
 		// TODO: Correct null pointers
 		// TODO: Fix all the shit that is wrong you fucking idiot programmer.
 		// TODO: Fix colour codes being usable in whisper.
		// TODO: Implement whispering from the console.
 
 		// Core
 		this.addComponent(Globals.class);
 		this.addComponent(DeathParser.class);
 
 		// Database
 		this.addComponent(MuteDatabase.class);
 
 		// Handlers
 		this.addComponent(ChatChannelHandler.class);
 		this.addComponent(ChatHandler.class);
 		this.addComponent(MuteHandler.class);
 		this.addComponent(WhisperHandler.class);
 		this.addComponent(SpamHandler.class);
 
 		// Commands
 		//this.addComponent(ChannelCommand.class);
 		this.addComponent(MuteCommand.class);
 		this.addComponent(UnMuteCommand.class);
 		this.addComponent(PlayerDeath.class);
 		this.addComponent(PuppetCommand.class);
 		this.addComponent(EmoteCommand.class);
 		this.addComponent(WhisperCommand.class);
 		this.addComponent(ReplyCommand.class);
 		this.addComponent(PlayerListRefreshCommand.class);
 
 		// Events
 		this.addComponent(JoinEvent.class);
 		this.addComponent(LeaveEvent.class);
 		this.addComponent(KickEvent.class);
 		this.addComponent(ChatEvent.class);
 		this.addComponent(VanishEvent.class);
 	}
 }

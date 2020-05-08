 package net.daiznaew.dbot3.Listeners.Commands;
 
 import net.daiznaew.dbot3.Listeners.core.BotCommand;
 import net.daiznaew.dbot3.util.Config;
 import net.daiznaew.dbot3.util.enums.AccessLevel;
 import net.daiznaew.dbot3.util.enums.ColorFormat;
 import net.daiznaew.dbot3.util.messages.Messages;
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.events.MessageEvent;
 
 /**
  *
  * @author Daiz
  */
 
 public class MapService extends BotCommand
 {
     private final String MAPSITE;
     
     public MapService()
     {
        getAliases().add("!site");
        getAliases().add("!website");
         setMinAccessLevel(AccessLevel.NORMAL);
         setArgumentsString("");
        setDescription("This goes to our website.");
         
         MAPSITE = Config.getProperty("dynmapservice");
     }
     
     @Override
     @SuppressWarnings("empty-statement")
     public void onMessage(MessageEvent<PircBotX> event) throws Exception
     {
         if (performGenericChecks(event.getChannel(), event.getUser(), event.getMessage().split(" ")))
         {
             if (getArgs().length == 1)
             {
                 Messages.respond(getChannel(), ColorFormat.NORMAL, getUser(), "The map of our server is dynmap powered and can be found at:");
                 Messages.respond(getChannel(), ColorFormat.NORMAL, getUser(), MAPSITE);
             } else {
                 showUsage();
             }
         }
     }
 }

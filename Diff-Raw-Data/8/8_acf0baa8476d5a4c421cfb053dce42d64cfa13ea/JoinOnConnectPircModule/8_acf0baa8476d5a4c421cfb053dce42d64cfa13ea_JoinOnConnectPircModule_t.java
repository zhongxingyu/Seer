 package org.jibble.pircbot.modules;
 
import static com.google.common.base.Preconditions.checkNotNull;

 import java.util.List;
 
 import org.jibble.pircbot.ExtendedPircBot;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableList;
 
 /**
  * Makes the bot join one or several channels after it successfully connects to the server.
  * 
  * @author Emmanuel Cron
  */
 public class JoinOnConnectPircModule extends AbstractPircModule {
   private static final Logger LOGGER = LoggerFactory.getLogger(JoinOnConnectPircModule.class);
 
   private List<String> channels;
 
   /**
    * Creates a new join on connect module.
    * 
    * @param channels the channels to join when the bot is connected
    */
   public JoinOnConnectPircModule(List<String> channels) {
    this.channels = ImmutableList.copyOf(checkNotNull(channels));
   }
 
   @Override
   public void onConnect(ExtendedPircBot bot) {
     for (String channel : channels) {
      LOGGER.info("Joining channel {}", channel);
       bot.joinChannel(channel);
     }
   }
 }

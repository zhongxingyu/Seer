 
 import com.lordralex.ralexbot.api.EventField;
 import com.lordralex.ralexbot.api.EventType;
 import com.lordralex.ralexbot.api.Listener;
 import com.lordralex.ralexbot.api.events.ActionEvent;
import com.lordralex.ralexbot.api.events.JoinEvent;
 import com.lordralex.ralexbot.api.events.MessageEvent;
import com.lordralex.ralexbot.api.events.NickChangeEvent;
 import com.lordralex.ralexbot.api.users.BotUser;
 import com.lordralex.ralexbot.settings.Settings;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Joshua
  */
 public class CensorListener extends Listener {
 
     List<String> censor = new ArrayList<>();
 
     @Override
     public void setup() {
         censor.clear();
         censor.addAll(Settings.getGlobalSettings().getStringList("censor"));
     }
 
     @Override
     @EventType(event = EventField.Message)
     public void runEvent(MessageEvent event) {
         String message = event.getMessage().toLowerCase();
         for (String word : censor) {
             if (message.contains(word.toLowerCase())) {
                 BotUser.getBotUser().kick(event.getSender().getNick(), event.getChannel().getName(), "Please keep it civil");
                 return;
             }
         }
     }

     @Override
     @EventType(event = EventField.Notice)
     public void runEvent(ActionEvent event) {
         String message = event.getAction().toLowerCase();
         for (String word : censor) {
             if (message.contains(word.toLowerCase())) {
                 BotUser.getBotUser().kick(event.getSender().getNick(), event.getChannel().getName(), "Please keep it civil");
                 return;
             }
         }
     }

    @Override
    @EventType(event = EventField.NickChange)
    public void runEvent(NickChangeEvent event) {
        String message = event.getNewNick().toLowerCase();
        for (String word : censor) {
            if (message.contains(word.toLowerCase())) {
                for (String chan : BotUser.getBotUser().getChannels()) {
                    BotUser.getBotUser().kick(event.getNewNick(), chan, "Please keep it civil");
                }
                return;
            }
        }
    }

    @Override
    @EventType(event = EventField.Join)
    public void runEvent(JoinEvent event) {
        String message = event.getSender().getNick().toLowerCase();
        for (String word : censor) {
            if (message.contains(word.toLowerCase())) {
                BotUser.getBotUser().kick(event.getSender().getNick(), event.getChannel().getName(), "Please keep it civil");
                return;
            }
        }
    }
 }

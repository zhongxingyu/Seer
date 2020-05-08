 package novemberkilo.irc.bot.gaming;
 
 import org.pircbotx.Configuration;
 import org.pircbotx.PircBotX;
 
 /**
  * Created with IntelliJ IDEA.
  * User: novemberkilo
  * Date: 9/30/13
  * Time: 7:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameBot {
     public static void main(String[] args) throws Exception {
         Configuration configuration;
 
        if (args.length != 5) {
            System.err.println("Required args: bot_name, bot_password, host_mask, host, channel");
            return;
        }
        if ("-".equals(args[2])) {
        configuration = new Configuration.Builder()
                .setName(args[0])
                .setNickservPassword(args[1])
                .setLogin(args[2]) //login part of hostmask, eg name:login@host
                .setAutoNickChange(true)
                .setCapEnabled(true)
                .addListener(new RollCommandListener(new RollCommandParser()))
                .addListener(new LogWhatYouHear(new SysOutLoggerController()))
                .setServerHostname(args[3])
                .addAutoJoinChannel(args[4])
                .buildConfiguration();
        } else {
             configuration = new Configuration.Builder()
                     .setName(args[0])
                     .setNickservPassword(args[1])
                     .setAutoNickChange(true)
                     .setCapEnabled(true)
                     .addListener(new RollCommandListener(new RollCommandParser()))
                     .addListener(new LogWhatYouHear(new SysOutLoggerController()))
                     .setServerHostname(args[3])
                     .addAutoJoinChannel(args[4])
                     .buildConfiguration();
         }
         PircBotX bot = new PircBotX(configuration);
         bot.startBot();
         System.out.println("nick: " + bot.getNick());
     }
 }

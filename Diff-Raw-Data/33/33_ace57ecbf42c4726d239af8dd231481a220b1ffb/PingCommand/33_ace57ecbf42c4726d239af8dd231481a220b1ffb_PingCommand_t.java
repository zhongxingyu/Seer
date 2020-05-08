 package commands;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import backend.Bot;
 
 public class PingCommand extends Command {
 
 	@Override
 	public void execute(Bot bot, Channel chan, User user, String message) {
		String s = "";
		for(User u : chan.getUsers()) {
			s += u.getNick() + " ";
		}
		passMessage(bot, chan, user, "PING! " + s);
 	}
 
 	@Override
 	protected void initialize() {
 		addAlias("ping");
 		setName("Ping");
 		setHelpText("Ping all users in the channel. Don't annoy them too much, now!");
 	}
 
 }

 package commands;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import backend.Bot;
import backend.Util;
 
 public class ChangeCommand extends Command{
 
 	//TODO change variables like verbosity etc
 	@Override
 	public void execute(Bot bot, Channel chan, User user, String message) {
		//verbose
		//parseself
		if(!Util.checkArgs(message, 2)) {
			passMessage(bot, chan, user, "Invalid format: " + format());
			return;
		}
		String[] args = Util.getArgs(message, 2);
 	}
 
 	@Override
 	protected void initialize() {
 		addAlias("change");
 		setName("ChangeInternals");
 		setHelpText("Change internal variables! Ooooh la la!");
 	}
	
	protected String format() {
		return super.format() + " [internal_var]";
	}
 }

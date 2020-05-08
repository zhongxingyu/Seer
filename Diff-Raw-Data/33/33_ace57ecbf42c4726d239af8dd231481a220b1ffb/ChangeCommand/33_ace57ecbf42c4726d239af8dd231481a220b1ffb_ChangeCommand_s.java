 package commands;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import backend.Bot;
 
 public class ChangeCommand extends Command{
 
 	//TODO change variables like verbosity etc
 	@Override
 	public void execute(Bot bot, Channel chan, User user, String message) {
		
 	}
 
 	@Override
 	protected void initialize() {
 		addAlias("change");
 		setName("ChangeInternals");
 		setHelpText("Change internal variables! Ooooh la la!");
 	}
 }

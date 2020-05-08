 package jisssea.controller.commands;
 
 import static jisssea.util.IrcUtility.getSafeCorrespondant;
 import static jisssea.util.IrcUtility.isValidChannel;
 import static jisssea.util.IrcUtility.isValidTarget;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import jisssea.bot.Bot;
 import jisssea.bot.BotRegistry;
 import jisssea.controller.Controller;
 import jisssea.controller.Pipe;
import jisssea.controller.messages.MessageMessage;
 import jisssea.controller.messages.UserMessage;
 import jisssea.util.Procedure;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class PMCommand extends RegexCommand {
 
 	private static final Log log = LogFactory.getLog(PMCommand.class);
 
 	private static final Pattern p = Pattern.compile("/pm (.*?) (.*)");
 
 	@Override
 	protected Pattern pattern() {
 		return p;
 	}
 
 	@Override
 	protected void guardedAct(Matcher m, UserMessage msg, BotRegistry irc, Controller ctrl) {
 
 		final String target = m.group(1);
 		final String messageBody = m.group(2);
 		final Bot network = irc.getTargetContext(target);
 		final String user = getSafeCorrespondant(target);
 
		log.debug("Sending: " + messageBody + " as a PM to " + target);

 		// are we already talking to this user?
 		// or do we need to create a new window
 		final Set<String> correspondants = new HashSet<String>();
 		ctrl.foreachPipe(new Procedure<Pipe>() {
 			@Override
 			public boolean f(Pipe a) {
 				for (String correspondantName : a.getDefaultPredicate().correspondants) {
					if (!isValidChannel(correspondantName))
 						correspondants.add(correspondantName);
 				}
 				return true;
 			}
 		});
		log.debug("PM Checking: " + correspondants + " contains " + user);
		if (!correspondants.contains(network.getServerName() + ":" + user)) {
			final Pipe pipe = ctrl.createPipe((isValidTarget(target)) ? target : (network.getServerName() + ":" + target));
			ctrl.selectPipe(pipe);
 		}
 
		// send fake message to self
		ctrl.message(new MessageMessage(network, user, network.getNick(), network.getLogin(), network.getName(), messageBody));

 		// send the message
		network.sendMessage(user, messageBody);
 	}
 }

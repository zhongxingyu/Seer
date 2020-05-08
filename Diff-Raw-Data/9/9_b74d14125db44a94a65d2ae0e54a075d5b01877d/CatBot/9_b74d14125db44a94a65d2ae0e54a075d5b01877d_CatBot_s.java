 package uk.co.harcourtprogramming.internetrelaycats;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jibble.pircbot.PircBot;
 
 /**
  * <p>Internal wrapper for {@link PircBot} that allows us to hide much of
  * the 'functionality'</p>
  */
 public class CatBot extends PircBot
 {
 
 	private BasicRelayCat inst = null;
 
 	/**
 	 * Shared logger with {@link BasicRelayCat}
 	 */
	private final static Logger log = Logger.getLogger("IntertnetRelayCats.RelayCat");
 
 	/**
 	 * Create a new bot with a given name
 	 * @param name the name of the bot
 	 */
 	protected CatBot(String name)
 	{
 		this.setName(name);
 	}
 
 	final void setInst(BasicRelayCat inst)
 	{
 		this.inst = inst;
 	}
 
 	/**
 	 * Unified function for handling input data
 	 * @param action whether the input is an action (or a message)
 	 * @param sender the nick which sent the input
 	 * @param channel the channel the input was received in (or null)
 	 * @param data the text of the message
 	 */
 	public void onInput(boolean action, String sender, String channel,
 		String data)
 	{
 		onInput(new Message(data, sender, channel, action, inst));
 	}
 
 	public void onInput(Message m)
 	{
 		log.log(Level.FINE, "Input recieved: {0}", m);
 		synchronized (inst.getSrvs())
 		{
 			for (MessageService s : inst.getMsrvs())
 			{
 				log.log(Level.FINE, "Input dispatched to {0}",
 					s.toString());
 				try
 				{
 					s.handle(m);
 				}
 				catch (Throwable ex)
 				{
 					log.log(Level.SEVERE,
 						"Error whilst passing input to " + s.toString(), ex);
 				}
 				if (m.isDisposed()) break;
 			}
 		}
 	}
 
 	@Override
 	public void onMessage(String channel, String sender, String login,
 		String hostname, String message)
 	{
 		onInput(false, sender, channel, message);
 	}
 
 	@Override
 	public void onPrivateMessage(String sender, String login, String hostname,
 		String message)
 	{
 		onInput(false, sender, null, message);
 	}
 
 	@Override
 	public void onAction(String sender, String login, String hostname,
 		String target, String action)
 	{
 		onInput(true, sender, target.equals(getNick()) ? null : target, action);
 	}
 
 }

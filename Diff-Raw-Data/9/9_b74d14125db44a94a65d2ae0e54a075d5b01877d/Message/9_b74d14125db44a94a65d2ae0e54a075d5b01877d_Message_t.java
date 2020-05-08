 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.harcourtprogramming.internetrelaycats;
 import org.jibble.pircbot.Colors;
 import org.jibble.pircbot.User;
 /**
  *
  * @author Benedict
  */
 @SuppressWarnings(value = "PublicInnerClass")
 public class Message implements RelayCat
 {
 	/**
 	 * <p>The message that was sent</p>
 	 */
 	private final String message;
 	/**
 	 * <p>The nick of the sender</p>
 	 */
 	private final String nick;
 	/**
 	 * <p>The nick of the {@link BasicRelayCat}</p>
 	 */
 	private final String me;
 	/**
 	 * <p>The channel that the message arrived in.</p>
 	 * <p>This is null if the message arrived directly.</p>
 	 */
 	private final String channel;
 	/**
 	 * <p>Whether this input was an action (true) or a message (false)</p>
 	 */
 	private final boolean action;
 	/**
 	 * <p>Whether a service has marked this message for disposal</p>
 	 */
 	private boolean dispose = false;
 	/**
 	 * The instance that created this message class
 	 */
 	private final BasicRelayCat inst;
 
 	/**
 	 * Creates a new message object
 	 * @param message the input data
 	 * @param nick the source nick
 	 * @param channel the source channel (or null if sent directly)
 	 * @param action whether this was an action or a message
 	 * @param inst
 	 */
 	protected Message(String message, String nick, String channel,
 		boolean action, BasicRelayCat inst)
 	{
 		this.inst = inst;
 		this.me = inst.getNick();
 		this.message = Colors.removeFormattingAndColors(message);
 		this.nick = nick;
 		this.channel = channel;
 		this.action = action;
 	}
 
 	/**
 	 * @return if this input is an action (otherwise, it is a message)
 	 */
 	public boolean isAction()
 	{
 		return action;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getMessage()
 	{
 		return message;
 	}
 
 	/**
 	 * @return the channel this input came via, or null if it was sent directly
 	 */
 	public String getChannel()
 	{
 		return channel;
 	}
 
 	/**
 	 * @return the nick of the user that sent this message
 	 */
 	public String getSender()
 	{
 		return nick;
 	}
 
 	@Override
 	public String getNick()
 	{
 		return me;
 	}
 
 	/**
 	 * Convenience method for messaging the sender directly
 	 * @param message the message text
 	 * @see #message(java.lang.String, java.lang.String) message
 	 */
 	public synchronized void reply(String message)
 	{
 		message(nick, message);
 	}
 
 	/**
 	 * <p>Convenience method for sending action to the same scope as this
 	 * message arrived</p>
 	 * @param action the action text to send
 	 * @see #act(java.lang.String, java.lang.String) act()
 	 * @see #replyToAll(java.lang.String) replyToAll()
 	 */
 	public synchronized void act(String action)
 	{
 		final String target = this.channel == null ? this.nick : this.channel;
 		act(target, action);
 	}
 
 	/**
 	 * <p>Convenience method for messaging the user or channel this message
 	 * was received from</p>
 	 * @param message the message text
 	 * @see #message(java.lang.String, java.lang.String) message()
 	 */
 	public synchronized void replyToAll(String message)
 	{
 		if (channel == null)
 		{
 			message(nick, message);
 		}
 		else
 		{
 			message(channel, message);
 		}
 	}
 
 	/**
 	 * <p>Marks this message as handled</p>
 	 * <p>It will be passed to no more Services,
 	 * and the messaging and channel functions of this class will perform
 	 * no actions.</p>
 	 */
 	public void dispose()
 	{
 		dispose = true;
 	}
 
 	@Override
 	public void message(String target, String message)
 	{
 		if (dispose) return;
 		inst.message(target, message);
 	}
 
 	@Override
 	public void act(String target, String message)
 	{
 		if (dispose) return;
		inst.act(target, message);
 	}
 
 	@Override
 	public void join(String channel)
 	{
 		if (dispose) return;
 		inst.join(channel);
 	}
 
 	@Override
 	public void leave(String channel)
 	{
 		if (dispose) return;
 		inst.leave(channel);
 	}
 
 	@Override
 	public User[] names(String channel)
 	{
 		return inst.names(channel);
 	}
 
 	@Override
 	public String[] channels()
 	{
 		return inst.channels();
 	}
 
 	public boolean isDisposed()
 	{
 		return dispose;
 	}
 }

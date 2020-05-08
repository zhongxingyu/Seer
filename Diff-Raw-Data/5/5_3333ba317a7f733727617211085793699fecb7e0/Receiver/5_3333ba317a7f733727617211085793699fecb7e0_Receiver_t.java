 package fr.ribesg.alix.api;
 /**
  * The Receiver interface represents something on the IRC network that can
  * receive messages.
  *
  * @author Ribesg
  */
 public interface Receiver {
 
 	/**
	 * Sends a message in this Receiver.
 	 * <p/>
 	 * Will call
 	 * {@link Server#sendRaw(fr.ribesg.alix.api.enums.Command, String...)}
 	 * with appropriate parameters
 	 *
 	 * @param message The message to send
 	 */
 	public void sendMessage(String message);
 
 	/**
	 * Sends an action to this Receiver.
 	 * An Action is when somebody uses the command /me is a teapot
 	 * <p/>
 	 * Will call
 	 * {@link Server#sendRaw(fr.ribesg.alix.api.enums.Command, String...)}
 	 * with appropriate parameters
 	 *
 	 * @param action The Action text to send
 	 */
 	public void sendAction(String action);
 }

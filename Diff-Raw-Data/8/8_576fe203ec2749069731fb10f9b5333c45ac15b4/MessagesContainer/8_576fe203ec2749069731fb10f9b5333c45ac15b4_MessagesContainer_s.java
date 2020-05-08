 package de.ptb.epics.eve.viewer.messages;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import de.ptb.epics.eve.viewer.MessageSource;
 
 
 /**
  * <code>MessageContainer</code> contains a list of messages and functions as 
  * an update provider.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class MessagesContainer implements IMessagesContainerUpdateProvider {
 
 	private static Logger logger = 
 			Logger.getLogger(MessagesContainer.class.getName());
 	
 	private final List<ViewerMessage> messages;
 	
 	// Observable
 	private final List<IMessagesContainerUpdateListener> listener;
 	
 	// indicators of message types
 	private boolean showEngineMessages = true;
 	private boolean showApplicationMessages = true;
 	
 	private Levels level = Levels.MINOR;
 	
 	/**
 	 * Constructor.
 	 */
 	public MessagesContainer() {
 		this.messages = new LinkedList<ViewerMessage>();
 		this.listener = new ArrayList<IMessagesContainerUpdateListener>();
 		
 		if (logger.isDebugEnabled()) {
 			this.level = Levels.DEBUG;
 		}
 	}
 	
 	/**
 	 * Adds a {@link de.ptb.epics.eve.viewer.messages.ViewerMessage} to the 
 	 * message container.
 	 * 
 	 * @param viewerMessage the 
 	 * 		  {@link de.ptb.epics.eve.viewer.messages.ViewerMessage}
 	 */
	public void addMessage(final ViewerMessage viewerMessage) {
 		this.messages.add(0, viewerMessage);
 		// inform observers
 		for(final IMessagesContainerUpdateListener imcul : this.listener) {
 			imcul.addElement(viewerMessage);
 		}
 	}
 	
 	/**
 	 * Clears all messages.
 	 */
	public void clear() {
 		this.messages.clear();
 		// inform observers
 		for(final IMessagesContainerUpdateListener imcul : this.listener) {
 			imcul.update();
 		}
 	}
 	
 	/**
 	 * Returns a list of viewer messages.
 	 *  
 	 * @return a list of {@link de.ptb.epics.eve.viewer.messages.ViewerMessage}s
 	 */
	public List<ViewerMessage> getList() {
 		final List<ViewerMessage> returnList = new ArrayList<ViewerMessage>();
 		
 		for(ViewerMessage message : this.messages) {
 			if (this.showApplicationMessages == false && 
 				message.getMessageSource().equals(MessageSource.VIEWER)) {
 					continue;
 			}
 			if (this.showEngineMessages == false && 
 				!message.getMessageSource().equals(MessageSource.VIEWER)) {
 					continue;
 			}
 			if (this.level.compareTo(message.getMessageType()) > 0) {
 				continue;
 			}
 			returnList.add(message);
 		}
 		Collections.sort(returnList);
 		return returnList;
 	}
 
 	/**
 	 * Sets whether messages from the given 
 	 * {@link de.ptb.epics.eve.viewer.messages.Sources} should be shown.
 	 * 
 	 * @param source the source to set
 	 * @param selected <code>true</code> if messages from the given source 
 	 * 			should be shown, <code>false</code> otherwise
 	 */
 	public void setSource(Sources source, boolean selected) {
 		switch(source) {
 		case ENGINE:
 			this.showEngineMessages = selected;
 			logger.debug("show engine messages: " + selected);
 			break;
 		case VIEWER:
 			this.showApplicationMessages = selected;
 			logger.debug("show application messages: " + selected);
 			break;
 		}
 		// inform observers
 		for (final IMessagesContainerUpdateListener imcul : this.listener) {
 			imcul.update();
 		}
 	}
 	
 	/**
 	 * Checks whether messages from the given
 	 * {@link de.ptb.epics.eve.viewer.messages.Sources} are shown.
 	 * 
 	 * @param source the source to check
 	 * @return <code>true</code> if messages from the given source are shown, 
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean isSourceShown(Sources source) {
 		switch(source) {
 		case ENGINE:
 			return this.showEngineMessages;
 		case VIEWER:
 			return this.showApplicationMessages;
 		}
 		return false;
 	}
 	
 	/**
 	 * Sets  whether messages of level given by 
 	 * {@link de.ptb.epics.eve.viewer.messages.Levels} (and below) should be 
 	 * shown.
 	 * 
 	 * @param level the level to set
 	 */
 	public void setLevel(Levels level) {
 		this.level = level;
 		logger.debug("message level: " + this.level);
 		// inform observers
 		for (final IMessagesContainerUpdateListener imcul : this.listener) {
 			imcul.update();
 		}
 	}
 
 	/**
 	 * Returns the level as in {@link de.ptb.epics.eve.viewer.messages.Levels}.
 	 * 
 	 * @return the level as in {@link de.ptb.epics.eve.viewer.messages.Levels}
 	 */
 	public Levels getLevel() {
 		return this.level;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void addMessagesContainerUpdateListener(
 			final IMessagesContainerUpdateListener listener) {
 		this.listener.add(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void removeMessagesContainerUpdateListener(
 			final IMessagesContainerUpdateListener listener) {
 		this.listener.remove(listener);
 	}
 }

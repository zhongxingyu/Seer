 package com.senselessweb.soundcloud.web.service.impl;
 
import java.io.Serializable;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.context.annotation.ScopedProxyMode;
 import org.springframework.stereotype.Service;
 
 import com.senselessweb.soundcloud.domain.sources.MediaSource;
 import com.senselessweb.soundcloud.mediasupport.service.MediaPlayer;
 import com.senselessweb.soundcloud.mediasupport.service.MediaPlayer.State;
 import com.senselessweb.soundcloud.mediasupport.service.MessageListener;
 import com.senselessweb.soundcloud.mediasupport.service.Playlist.ChangeEvent;
 import com.senselessweb.soundcloud.web.service.Event;
 import com.senselessweb.soundcloud.web.service.EventQueue;
 
 /**
  * Default event queue implementation.
  * 
  * @author thomas
  */
 @Service
 @Scope(proxyMode=ScopedProxyMode.INTERFACES, value="session")
public class EventQueueImpl implements EventQueue, MessageListener, Serializable
 {
 
 	/**
	 * The serialVersionUID
	 */
	private static final long serialVersionUID = -3366916995905553888L;
	
	/**
 	 * The message queue
 	 */
	private final transient BlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();
 	
 	/**
 	 * @param mediaPlayer The media player
 	 */
 	@Autowired public EventQueueImpl(final MediaPlayer mediaPlayer)
 	{
 		mediaPlayer.addMessageListener(this);
 	}
 
 	/**
 	 * @see com.senselessweb.soundcloud.web.service.EventQueue#getNextEvent()
 	 */
 	@Override
 	public Event getNextEvent()
 	{
 		// Throw anything into the queue so that a second waiting thread stops waiting 
 		final Event poison = new Event("poison-" + Thread.currentThread().getId());
 		this.queue.add(poison);
 		
 		try
 		{
 			final Event event = this.queue.take();
 			
 			// Won't consume my own poison :)
 			if (!event.equals(poison)) return event;
 			
 			// Continue waiting
 			return this.queue.take();
 		} 
 		catch (final InterruptedException e)
 		{
 			throw new RuntimeException("Interrupted while waiting for the next event : " + e, e);
 		}
 	}
 
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MessageListener#stateChanged(com.senselessweb.soundcloud.mediasupport.service.MediaPlayer.State)
 	 */
 	@Override
 	public void stateChanged(final State newState)
 	{
 		this.queue.add(new Event("stateChanged", "newState", newState.name()));
 	}
 
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MessageListener#error(java.lang.String)
 	 */
 	@Override
 	public void error(final String message)
 	{
 		this.queue.add(new Event("error", "message", message));
 	}
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MessageListener#playlistChanged(ChangeEvent, int)
 	 */
 	@Override
 	public void playlistChanged(final ChangeEvent event, final int current)
 	{
 		this.queue.add(new Event("playlistChanged", "event", event.name(), "current", String.valueOf(current)));
 	}
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MessageListener#tag(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void tag(final String tag, final String value)
 	{
 		// Unused as long as the DisplayDataController handles all tag request
 		// this.queue.add(new Event("tag", tag, value));
 	}
 	
 	/**
 	 * @see com.senselessweb.soundcloud.mediasupport.service.MessageListener#newSource(com.senselessweb.soundcloud.domain.sources.MediaSource)
 	 */
 	@Override
 	public void newSource(final MediaSource source) { /* unused */ }
 	
 }

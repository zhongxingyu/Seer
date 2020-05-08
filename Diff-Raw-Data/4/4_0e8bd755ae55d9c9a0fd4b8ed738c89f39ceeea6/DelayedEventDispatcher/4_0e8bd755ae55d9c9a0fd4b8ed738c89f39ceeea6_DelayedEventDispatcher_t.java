 package org.blockout.logic.handler;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 
 import javax.inject.Inject;
 
 import org.blockout.world.event.IEvent;
 import org.blockout.world.state.IStateMachine;
 import org.blockout.world.state.IStateMachineListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 
 /**
  * This class caches commit events until there duration has been expired. The
  * cached events are then forwarded through the {@link IEventHandler} interface.
  * This enables the events to take some time until completion, e.g. walking from
  * tile to tile.
  * 
  * @author Marc-Christian Schulze
  * 
  */
 public class DelayedEventDispatcher implements IStateMachineListener {
 
 	private static final Logger				logger;
 	static {
 		logger = LoggerFactory.getLogger( DelayedEventDispatcher.class );
 	}
 
 	private static final int				THREAD_POOL_SIZE	= Runtime.getRuntime().availableProcessors();
 	protected ScheduledThreadPoolExecutor	executor;
 	protected List<IEventHandler>			eventHandler;
 	protected Map<IEvent<?>, Long>			activeEvents;
 	protected IStateMachine					stateMachine;
 
 	public DelayedEventDispatcher() {
 		eventHandler = new CopyOnWriteArrayList<IEventHandler>();
 		activeEvents = Collections.synchronizedMap( new HashMap<IEvent<?>, Long>() );
 		logger.info( "Starting " + getClass().getName() + " with " + THREAD_POOL_SIZE + " threads." );
 		executor = new ScheduledThreadPoolExecutor( THREAD_POOL_SIZE, new ThreadFactory() {
 
 			private int	threadCount;
 
 			@Override
 			public Thread newThread( final Runnable r ) {
 				return new Thread( r, "DelayedEventDispatchThread#" + (threadCount++) );
 			}
 		} );
 	}
 
 	@Inject
 	public void addEventHandler( final Set<IEventHandler> handler ) {
 		eventHandler.addAll( handler );
 	}
 
 	@Override
 	public void init( final IStateMachine stateMachine ) {
 		this.stateMachine = stateMachine;
 	}
 
 	@Override
 	public void eventCommitted( final IEvent<?> event ) {
 		Preconditions.checkNotNull( event );
 		if ( event.getDuration() == 0 ) {
 			logger.debug( "Direct event " + event + " finished." );
 			activeEvents.remove( event );
 			fireEventFinished( event );
 			return;
 		}
 
 		Long startTime = activeEvents.get( event );
		if ( startTime == null ) {
			logger.warn( "Received commit without prior push event. Discarding " + event );
			return;
		}
 		final long remainingMillis = (startTime + event.getDuration()) - System.currentTimeMillis();
 		if ( remainingMillis <= 0 ) {
 			logger.debug( "Delayed event " + event + " finished. Deviance: " + (-remainingMillis) + " ms" );
 			activeEvents.remove( event );
 			fireEventFinished( event );
 			return;
 		}
 
 		logger.debug( "Rescheduled event " + event + " in: " + remainingMillis + " ms" );
 		executor.schedule( new Runnable() {
 
 			@Override
 			public void run() {
 				eventCommitted( event );
 			}
 		}, remainingMillis, TimeUnit.MILLISECONDS );
 	}
 
 	@Override
 	public void eventPushed( final IEvent<?> event ) {
 		Preconditions.checkNotNull( event );
 		activeEvents.put( event, System.currentTimeMillis() );
 		fireEventStarted( event );
 	}
 
 	@Override
 	public void eventRolledBack( final IEvent<?> event ) {
 		Preconditions.checkNotNull( event );
 		activeEvents.remove( event );
 	}
 
 	protected void fireEventStarted( final IEvent<?> event ) {
 		for ( IEventHandler handler : eventHandler ) {
 			handler.eventStarted( stateMachine, event );
 		}
 	}
 
 	protected void fireEventFinished( final IEvent<?> event ) {
 		for ( IEventHandler handler : eventHandler ) {
 			handler.eventFinished( stateMachine, event );
 		}
 	}
 }

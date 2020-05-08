 package manelsim;
 
 import manelsim.Time.Unit;
 
 /**
  * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
  */
 public final class EventScheduler {
 
 	private static Time emulationStart = null;
 	private static Time emulationEnd = null;
 	private static EventSourceMultiplexer eventSourceMultiplexer = null;
 	private static boolean stopOnError = true;
 	private static long processCount = 0;
 	private static Time now = new Time(0L, Unit.MILLISECONDS);
 
 	private EventScheduler() { }
 	
 	public static void reset() {
 		emulationStart = null;
 		emulationEnd = null;
 		eventSourceMultiplexer = null;
 		processCount = 0;
 		now = new Time(0L, Unit.MILLISECONDS);
 	}
 	
 	public static void setup(Time emulationStart, Time emulationEnd, EventSourceMultiplexer eventSource, boolean stopOnError) {
 		reset();
 		EventScheduler.stopOnError = stopOnError;
 		EventScheduler.emulationStart = emulationStart;
 		EventScheduler.emulationEnd = emulationEnd;
 		EventScheduler.eventSourceMultiplexer = eventSource;
 	}
 	
 	private static boolean isConfigured() {
 		return !(emulationStart == null || emulationEnd == null || eventSourceMultiplexer == null);
 	}
 	
 	public static void start() {
 		
 		if(!isConfigured()) {
 			throw new IllegalStateException("EventScheduler is not configured. " +
 					"Are you sure you called EventScheduler.setup()?");
 		}
 
 		Event nextEvent;
 		
 		while ((nextEvent = eventSourceMultiplexer.getNextEvent()) != null && isEarlierThanEmulationEnd(now())) {
 			Time eventTime = nextEvent.getScheduledTime();
 
 			if (eventTime.isEarlierThan(now())) {
 				String msg = "ERROR: emulation time(" + now()
 						+ ") " + "already ahead of event time("
 						+ eventTime
 						+ "). Event is outdated and will not be processed.";
 
 				if(stopOnError) {
 					throw new RuntimeException(msg);
 				} else {
 					System.err.println(msg);
 				}
 			}
 
 			if (isEarlierThanEmulationEnd(eventTime)) {
 				if(isLaterThanEmulationStart(eventTime)) {
 					now = eventTime;
 					processEvent(nextEvent);
 					processCount++;
 				}
 			} else {
 				now = emulationEnd;
 			}
 		}
 
 	}
 
 	private static boolean isLaterThanEmulationStart(Time eventTime) {
 		return !eventTime.isEarlierThan(emulationStart);
 	}
 
 	private static boolean isEarlierThanEmulationEnd(Time eventTime) {
 		return eventTime.isEarlierThan(emulationEnd);
 	}
 
 	private static void processEvent(Event nextEvent) {
 		nextEvent.process();
 	}
 	
 	public static void schedule(Event event) {
 		eventSourceMultiplexer.addNewEvent(event);
 	}
 	
 	/**
 	 * 
 	 * @return the number of {@link Event}s processed.
 	 */
 	public static long processCount() {
 		return processCount;
 	}
 
 	public static Time now() {
 		return now;
 	}
 
 }

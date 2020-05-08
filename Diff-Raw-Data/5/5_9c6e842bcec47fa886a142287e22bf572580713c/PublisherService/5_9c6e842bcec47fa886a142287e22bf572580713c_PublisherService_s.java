 package pegasus.eventbus.services.rabbit.status;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.client.EventManager;
 import pegasus.eventbus.services.rabbit.status.monitors.topten.*;
 import pegasus.eventbus.services.rabbit.status.monitors.volume.*;
 
 public class PublisherService {
 
 	private static final Logger                    LOG                                = LoggerFactory.getLogger(PublisherService.class);
 	public static EventManager eventManager;
 	public static RabbitManagementApiHelper apiHelper;
 	
 	private ScheduledExecutorService scheduler ;
 	private int interval;
 	public PublisherService(EventManager eventManager, RabbitManagementApiHelper apiHelper, int interval) {
 		PublisherService.eventManager = eventManager;
 		PublisherService.apiHelper = apiHelper;
 		this.interval = interval;
 	}
 
 	public void start(){
 		LOG.info("Rabbit Status Publisher Service starting...");
 		
 		List<Runnable> monitors = getMonitorPublishers();
 		
 		scheduler = Executors.newScheduledThreadPool(monitors.size());
 		int intervalBetweenMonitors = interval / monitors.size();
 		int delay = 0;
 		for(Runnable monitor : monitors){
 			scheduler.scheduleAtFixedRate(monitor, delay, interval, TimeUnit.MILLISECONDS);
 			delay += intervalBetweenMonitors;
 		}
 		
 		LOG.info("Rabbit Status Publisher Service started.");
 	}
 	
 	private List<Runnable> getMonitorPublishers() {
 		ArrayList<Runnable> publishers = new ArrayList<Runnable>();
 		
 		publishers.add( new Publisher( new PublishedMessagesPerSecondMonitor()));
 		publishers.add( new Publisher( new DeliveredMessagesPerSecondMonitor()));
 		publishers.add( new Publisher( new UnacknowledgedMessagesMonitor()));
 		publishers.add( new Publisher( new QueuedMessagesMonitor()));
 		publishers.add( new Publisher( new TotalMessagesMonitor()));
 		publishers.add( new Publisher( new ConnectionCountMonitor()));
 		publishers.add( new Publisher( new ChannelCountMonitor()));
 		publishers.add( new Publisher( new QueueCountMonitor()));
		publishers.add( new Publisher( new BusiestQueuesMonitor()));
 		return publishers;
 	}
 
 	public void stop(){
 		LOG.info("Rabbit Status Publisher Service started.");
 		if(scheduler != null && !scheduler.isShutdown()){
 			scheduler.shutdownNow();
 		}
 	}
 }

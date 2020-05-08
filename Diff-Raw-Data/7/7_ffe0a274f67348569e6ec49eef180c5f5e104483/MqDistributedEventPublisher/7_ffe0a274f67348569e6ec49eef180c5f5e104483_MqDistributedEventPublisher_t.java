 package de.knallisworld.springcontextsync.event.integration;
 
 import de.knallisworld.springcontextsync.Application;
 import de.knallisworld.springcontextsync.event.DistributedEvent;
 import de.knallisworld.springcontextsync.event.listener.DistributedEventPublisher;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Profile;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 
 @Component
 @Profile("mq")
 public class MqDistributedEventPublisher implements DistributedEventPublisher {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(MqDistributedEventPublisher.class);
 
 	@Autowired
 	private Application application;
 
 	@Autowired
 	private MqEventSender sender;
 
 	@PostConstruct
 	public void afterInitialized() {
 		LOGGER.info("Using MqDistributedEventPublisher.");
 	}
 
 	/**
 	 * see #publishEvent
 	 *
 	 * @param userId
 	 * @param action
 	 * @param category
 	 */
 	@Override
 	public void publishEvent(final String userId, final String action, final String category) {
 		final DistributedEvent event = new DistributedEvent(this, action, category, userId);
 		publishEvent(event);
 	}
 
 	/**
 	 * Delegate the event to MqEventSender.
 	 *
 	 * @param event
 	 */
 	@Override
 	public void publishEvent(final DistributedEvent event) {
 		event.setApplicationId(application.getId());
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Publishing (appId={})...", event.getApplicationId());
 		}
 
		try {
			sender.send(event);

		} catch (Exception e) {
			LOGGER.warn("Could not send event over MQ. Service down? Original reason: " + e.getMessage());
		}
 	}
 }

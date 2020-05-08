 package edu.rit.asksg.dataio;
 
 import edu.rit.asksg.common.Log;
 import edu.rit.asksg.domain.Conversation;
 import edu.rit.asksg.domain.Service;
 import edu.rit.asksg.domain.SocialSubscription;
 import edu.rit.asksg.domain.config.ProviderConfig;
 import edu.rit.asksg.repository.SocialSubscriptionRepository;
 import edu.rit.asksg.service.ConversationService;
 import org.joda.time.LocalDateTime;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Async;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Collection;
 import java.util.List;
 
 @Component
 public class SubscriptionWorker implements AsyncWorker {
 
 	@Log
 	Logger log;
 
 	@Autowired
 	ConversationService conversationService;
 
 	@Async
     @Transactional
 	public void work(final Service service) {
 		final String threadName = Thread.currentThread().getName();
 		log.debug("Subscription worker executing on " + threadName + " for service " + service.getName());
 
 		if (service instanceof SubscriptionProvider) {
 			for (SocialSubscription socialSubscription : service.getConfig().getSubscriptions()) {
 				try {
 					ProviderConfig serviceConfig = service.getConfig();
 
 					if (serviceConfig.getLastUpdate().plus(serviceConfig.getUpdateFrequency()).compareTo(LocalDateTime.now()) < 0) {
 						// We can reset the counter
 						serviceConfig.setLastUpdate(LocalDateTime.now());
 						serviceConfig.setCurrentCalls(0);
 					}
 
					if (serviceConfig.getCurrentCalls() < serviceConfig.getMaxCalls() || serviceConfig.getMaxCalls() == 0) {
 						// We can still make calls to this service, but count this call
 						serviceConfig.incrementCalls();
 						conversationService.saveConversations(((SubscriptionProvider) service).getContentFor(socialSubscription));
 					} else {
 						// We can't make calls, so bail out of this function.
 						log.debug("Subscription worker on " + threadName + " for service " + service.getName() + " operation canceled -- API limit reached.");
 					}
 				} catch (Exception e) {
 					log.error(e.getLocalizedMessage(), e);
 				}
 			}
 		}
 		log.debug("Subscription worker on " + threadName + " for service " + service.getName() + " completed.");
 
 
 	}
 
 
 }

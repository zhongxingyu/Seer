 package uk.ac.ox.oucs.search.producer;
 
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 
 /**
  * ContentProducerFactory is in charge of registering every {@link EntityContentProducer} available in the application
  * and provide them when needed based on either a reference or an event.
  *
  * @author Colin Hebert
  */
 public class ContentProducerFactory {
     private static final Logger logger = LoggerFactory.getLogger(ContentProducerFactory.class);
     private final Collection<EntityContentProducer> contentProducers = new HashSet<EntityContentProducer>();
 
     /**
      * Register an {@link EntityContentProducer} for a later use
      *
      * @param contentProducer entityContentProducer to register
      */
     public void addContentProducer(EntityContentProducer contentProducer) {
         logger.info(contentProducer.getClass() + " registered to provide content to the search index from " + contentProducer.getTool());
         contentProducers.add(contentProducer);
     }
 
     /**
      * Obtains an {@link EntityContentProducer} based on the given reference.
      *
      * @param reference reference with which the contentProducer will be working
      * @return an {@link EntityContentProducer} matching the reference, or null if nothing has been found
      */
     public EntityContentProducer getContentProducerForElement(String reference) {
         if (logger.isDebugEnabled())
             logger.debug("Looking for a contentProducer for '" + reference + "'");
         for (EntityContentProducer contentProducer : contentProducers) {
             try {
                 if (contentProducer.matches(reference)) {
                     if (logger.isDebugEnabled())
                         logger.debug("The content producer '" + contentProducer + "' matches the reference '" + reference + "'");
                     return contentProducer;
                 }
             } catch (Exception e) {
                 logger.warn("The content producer '" + contentProducer + "' has thrown an exception", e);
             }
         }
         logger.info("Couldn't find a content producer for reference '" + reference + "'");
         return null;
     }
 
     /**
      * Obtains an {@link EntityContentProducer} based on the given event.
      *
      * @param event event with which the contentProducer will be working
      * @return an {@link EntityContentProducer} matching the event, or null if nothing has been found
      */
     public EntityContentProducer getContentProducerForEvent(Event event) {
         for (EntityContentProducer contentProducer : contentProducers) {
             try {
                 if (contentProducer.matches(event)) {
                     if (logger.isDebugEnabled())
                         logger.debug("The content producer '" + contentProducer + "' matches the event '" + event + "'");
                     return contentProducer;
                 }
             } catch (Exception e) {
                 //If the matches method throws an exception, log it and continue to look for a contentProducer
                logger.warn("The content producer '" + contentProducer + "' has thrown an exception", e);
             }
         }
        logger.info("Couldn't find a content producer for event '" + event + "'");
         return null;
     }
 
     /**
      * Get the list of registered {@link EntityContentProducer}
      *
      * @return an unmodifiable collection of {@link EntityContentProducer} automatically registered
      */
     public Collection<EntityContentProducer> getContentProducers() {
         return Collections.unmodifiableCollection(contentProducers);
     }
 }

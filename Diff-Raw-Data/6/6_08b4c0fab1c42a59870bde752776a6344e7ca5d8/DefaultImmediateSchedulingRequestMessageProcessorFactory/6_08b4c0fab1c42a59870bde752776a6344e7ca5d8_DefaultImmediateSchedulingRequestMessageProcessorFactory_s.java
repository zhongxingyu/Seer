 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package co.altruix.pcc.impl.immediatereschedulingrequestprocessor;
 
 import java.util.Properties;
 
import co.altruix.pcc.api.immediatereschedulingrequestprocessor.ImmediateSchedulingRequestMessageProcessor;
import co.altruix.pcc.api.immediatereschedulingrequestprocessor.ImmediateSchedulingRequestMessageProcessorFactory;
 
 /**
  * @author DP118M
  * 
  */
 public final class DefaultImmediateSchedulingRequestMessageProcessorFactory
         implements ImmediateSchedulingRequestMessageProcessorFactory {
 
     private Properties configuration;
 
     public DefaultImmediateSchedulingRequestMessageProcessorFactory(
             final Properties aConfiguration) {
         this.configuration = aConfiguration;
     }
 
     public ImmediateSchedulingRequestMessageProcessor create() {
         final ImmediateSchedulingRequestMessageProcessor returnValue =
                 new DefaultImmediateSchedulingRequestMessageProcessor();
 
         returnValue.setAllCalendarsFeedUrl(this.configuration
                 .getProperty("allCalendarsFeedUrl"));
         returnValue.setCalendarScope(this.configuration
                 .getProperty("calendarScope"));
         returnValue.setClientId(this.configuration.getProperty("clientId"));
         returnValue.setClientSecret(this.configuration
                 .getProperty("clientSecret"));
         returnValue.setConsumerKey(this.configuration
                 .getProperty("consumerKey"));
         returnValue.setTaskJugglerPath(this.configuration
                 .getProperty("taskJugglerPath"));
 
         return returnValue;
     }
 
 }

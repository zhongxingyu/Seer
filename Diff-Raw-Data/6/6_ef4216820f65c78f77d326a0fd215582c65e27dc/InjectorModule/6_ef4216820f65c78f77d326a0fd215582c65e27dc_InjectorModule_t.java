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
 
 package co.altruix.scheduler.impl.di;
 
 import at.silverstrike.pcc.api.persistence.Persistence;
 import at.silverstrike.pcc.impl.persistence.DefaultPersistence;
 
 import co.altruix.pcc.api.mq.MqInfrastructureInitializerFactory;
import co.altruix.pcc.api.outgoingqueuechannel.OutgoingQueueChannelFactory;
 import co.altruix.pcc.impl.mq.DefaultMqInfrastructureInitializerFactory;
import co.altruix.pcc.impl.outgoingqueuechannel.DefaultOutgoingQueueChannelFactory;
 
 import com.google.inject.AbstractModule;
 
 /**
  * @author DP118M
  * 
  */
 class InjectorModule extends AbstractModule {
 
     @Override
     protected void configure() {
         bind(Persistence.class).toInstance(new DefaultPersistence());
         bind(MqInfrastructureInitializerFactory.class).toInstance(
                 new DefaultMqInfrastructureInitializerFactory());
        bind(OutgoingQueueChannelFactory.class).toInstance(
                new DefaultOutgoingQueueChannelFactory());
     }
 
 }

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
 
 package co.altruix.pcc.impl.di;
 
 import at.silverstrike.pcc.api.export2tj3.TaskJuggler3Exporter;
 import at.silverstrike.pcc.api.export2tj3.TaskJuggler3ExporterFactory;
 import at.silverstrike.pcc.api.gcaltasks2pcc.GoogleCalendarTasks2PccImporterFactory;
 import at.silverstrike.pcc.api.gcaltasks2pccimporter.GoogleCalendarTasks2PccImporter2Factory;
import at.silverstrike.pcc.api.gtasknoteparser.GoogleTaskNotesParserFactory;
 import at.silverstrike.pcc.api.gtaskrelevance.IsGoogleTaskRelevantCalculatorFactory;
 import at.silverstrike.pcc.api.persistence.Persistence;
 import at.silverstrike.pcc.api.projectscheduler.ProjectScheduler;
 import at.silverstrike.pcc.api.projectscheduler.ProjectSchedulerFactory;
 import at.silverstrike.pcc.impl.export2tj3.DefaultTaskJuggler3ExporterFactory;
 import at.silverstrike.pcc.impl.gcaltasks2pcc.DefaultGoogleCalendarTasks2PccImporterFactory;
 import at.silverstrike.pcc.impl.gcaltasks2pccimporter.DefaultGoogleCalendarTasks2PccImporter2Factory;
import at.silverstrike.pcc.impl.gtasknoteparser.DefaultGoogleTaskNotesParserFactory;
 import at.silverstrike.pcc.impl.gtaskrelevance.DefaultIsGoogleTaskRelevantCalculatorFactory;
 import at.silverstrike.pcc.impl.persistence.DefaultPersistence;
 import at.silverstrike.pcc.impl.projectscheduler.DefaultProjectSchedulerFactory;
 import co.altruix.pcc.api.dispatcher.DispatcherFactory;
 import co.altruix.pcc.api.immediatereschedulingrequestprocessor.ImmediateSchedulingRequestMessageProcessorFactory;
 import co.altruix.pcc.api.messageprocessorselector.MessageProcessorSelectorFactory;
 import co.altruix.pcc.api.mq.MqInfrastructureInitializerFactory;
 import co.altruix.pcc.api.queuechannel.QueueChannelFactory;
 import co.altruix.pcc.api.shutdownhook.ShutdownHookFactory;
 import co.altruix.pcc.impl.dispatcher.DefaultDispatcherFactory;
 import co.altruix.pcc.impl.immediatereschedulingrequestprocessor.DefaultImmediateSchedulingRequestMessageProcessorFactory;
 import co.altruix.pcc.impl.messageprocessorselector.DefaultMessageProcessorSelectorFactory;
 import co.altruix.pcc.impl.mq.DefaultMqInfrastructureInitializerFactory;
 import co.altruix.pcc.impl.queuechannel.DefaultQueueChannelFactory;
 import co.altruix.pcc.impl.shutdownhook.DefaultShutdownHookFactory;
 
 import com.google.inject.AbstractModule;
 
 /**
  * @author DP118M
  * 
  */
 class InjectorModule extends AbstractModule {
 
     @Override
     protected void configure() {
         bind(DispatcherFactory.class)
                 .toInstance(new DefaultDispatcherFactory());
         bind(ImmediateSchedulingRequestMessageProcessorFactory.class)
                 .toInstance(
                         new DefaultImmediateSchedulingRequestMessageProcessorFactory());
         bind(MessageProcessorSelectorFactory.class).toInstance(
                 new DefaultMessageProcessorSelectorFactory());
         bind(MqInfrastructureInitializerFactory.class).toInstance(
                 new DefaultMqInfrastructureInitializerFactory());
         bind(QueueChannelFactory.class).toInstance(
                 new DefaultQueueChannelFactory());
         bind(ShutdownHookFactory.class).toInstance(
                 new DefaultShutdownHookFactory());
         bind(Persistence.class).toInstance(
                 new DefaultPersistence());
         bind(GoogleCalendarTasks2PccImporterFactory.class).toInstance(
                 new DefaultGoogleCalendarTasks2PccImporterFactory());
         bind(ProjectScheduler.class).toInstance(getProjectScheduler());
         bind(TaskJuggler3Exporter.class).toInstance(getTaskJuggler3Exporter());
         bind(GoogleCalendarTasks2PccImporter2Factory.class).toInstance(
                 new DefaultGoogleCalendarTasks2PccImporter2Factory());
         bind(IsGoogleTaskRelevantCalculatorFactory.class).toInstance(
                 new DefaultIsGoogleTaskRelevantCalculatorFactory());
        bind(GoogleTaskNotesParserFactory.class).toInstance(
                new DefaultGoogleTaskNotesParserFactory());
     }
 
     private TaskJuggler3Exporter getTaskJuggler3Exporter() {
         final TaskJuggler3ExporterFactory factory =
                 new DefaultTaskJuggler3ExporterFactory();
 
         return factory.create();
     }
 
     private ProjectScheduler getProjectScheduler() {
         final ProjectSchedulerFactory factory =
                 new DefaultProjectSchedulerFactory();
 
         return factory.create();
     }
 }

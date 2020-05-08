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
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import at.silverstrike.pcc.api.embeddedfilereading.EmbeddedFileReader;
 import at.silverstrike.pcc.api.export2tj3.TaskJuggler3Exporter;
 import at.silverstrike.pcc.api.export2tj3.TaskJuggler3ExporterFactory;
 import at.silverstrike.pcc.api.gcaltasks2pcc.GoogleCalendarTasks2PccImporterFactory;
 import at.silverstrike.pcc.api.gcaltasks2pccimporter.GoogleCalendarTasks2PccImporter2Factory;
 import at.silverstrike.pcc.api.gtask2pcctaskconverter.GoogleTask2PccTaskConverterFactory;
 import at.silverstrike.pcc.api.gtaskexporter.GoogleTasksExporterFactory;
 import at.silverstrike.pcc.api.gtasknoteparser.GoogleTaskNotesParserFactory;
 import at.silverstrike.pcc.api.gtaskrelevance.IsGoogleTaskRelevantCalculatorFactory;
 import at.silverstrike.pcc.api.gtaskrelevance2.RelevantTaskSetCalculatorFactory;
 import at.silverstrike.pcc.api.gtasktitleparser.GoogleTaskTitleParserFactory;
 import at.silverstrike.pcc.api.persistence.Persistence;
 import at.silverstrike.pcc.api.projectscheduler.ProjectScheduler;
 import at.silverstrike.pcc.api.projectscheduler.ProjectSchedulerFactory;
 import at.silverstrike.pcc.api.tj3bookingsparser.BookingsFile2BookingsFactory;
 import at.silverstrike.pcc.api.tj3bookingsparser.Tj3BookingsParserFactory;
 import at.silverstrike.pcc.api.tj3deadlinesparser.Tj3DeadlinesFileParserFactory;
 import at.silverstrike.pcc.impl.embeddedfilereading.DefaultEmbeddedFileReaderFactory;
 import at.silverstrike.pcc.impl.export2tj3.DefaultTaskJuggler3ExporterFactory;
 import at.silverstrike.pcc.impl.gcaltasks2pcc.DefaultGoogleCalendarTasks2PccImporterFactory;
 import at.silverstrike.pcc.impl.gcaltasks2pccimporter.DefaultGoogleCalendarTasks2PccImporter2Factory;
 import at.silverstrike.pcc.impl.gtask2pcctaskconverter.DefaultGoogleTask2PccTaskConverterFactory;
 import at.silverstrike.pcc.impl.gtaskexporter.DefaultGoogleTasksExporterFactory;
 import at.silverstrike.pcc.impl.gtasknoteparser.DefaultGoogleTaskNotesParserFactory;
 import at.silverstrike.pcc.impl.gtaskrelevance.DefaultIsGoogleTaskRelevantCalculatorFactory;
 import at.silverstrike.pcc.impl.gtaskrelevance2.DefaultRelevantTaskSetCalculatorFactory;
 import at.silverstrike.pcc.impl.gtasktitleparser.DefaultGoogleTaskTitleParserFactory;
 import at.silverstrike.pcc.impl.persistence.DefaultPersistence;
 import at.silverstrike.pcc.impl.projectscheduler.DefaultProjectSchedulerFactory;
 import at.silverstrike.pcc.impl.tj3bookingsparser.DefaultBookingsFile2BookingsFactory;
 import at.silverstrike.pcc.impl.tj3bookingsparser.DefaultTj3BookingsParserFactory;
 import at.silverstrike.pcc.impl.tj3deadlinesparser.DefaultTj3DeadlinesFileParserFactory;
 import co.altruix.pcc.api.dispatcher.DispatcherFactory;
 import co.altruix.pcc.api.exporter2googlecalendar.Exporter2GoogleCalendarFactory;
 import co.altruix.pcc.api.googletasksimporter.GoogleTasksImporterFactory;
 import co.altruix.pcc.api.immediatereschedulingrequestprocessor.ImmediateSchedulingRequestMessageProcessorFactory;
 import co.altruix.pcc.api.messageprocessorselector.MessageProcessorSelectorFactory;
 import co.altruix.pcc.api.mq.MqInfrastructureInitializerFactory;
 import co.altruix.pcc.api.outgoingqueuechannel.OutgoingQueueChannelFactory;
 import co.altruix.pcc.api.plancalculator.PlanCalculatorFactory;
 import co.altruix.pcc.api.incomingqueuechannel.IncomingQueueChannelFactory;
 import co.altruix.pcc.api.shutdownhook.ShutdownHookFactory;
 import co.altruix.pcc.impl.dispatcher.DefaultDispatcherFactory;
 import co.altruix.pcc.impl.exporter2googlecalendar.DefaultExporter2GoogleCalendarFactory;
 import co.altruix.pcc.impl.googletasksimporter.DefaultGoogleTasksImporterFactory;
 import co.altruix.pcc.impl.immediatereschedulingrequestprocessor.DefaultImmediateSchedulingRequestMessageProcessorFactory;
 import co.altruix.pcc.impl.messageprocessorselector.DefaultMessageProcessorSelectorFactory;
 import co.altruix.pcc.impl.mq.DefaultMqInfrastructureInitializerFactory;
 import co.altruix.pcc.impl.outgoingqueuechannel.DefaultOutgoingQueueChannelFactory;
 import co.altruix.pcc.impl.plancalculator.DefaultPlanCalculatorFactory;
 import co.altruix.pcc.impl.incomingqueuechannel.DefaultIncomingQueueChannelFactory;
 import co.altruix.pcc.impl.shutdownhook.DefaultShutdownHookFactory;
 
 import com.google.inject.AbstractModule;
 
 /**
  * @author DP118M
  * 
  */
 class InjectorModule extends AbstractModule {
     private Properties configuration;
 
     public InjectorModule(final Properties aConfiguration) {
         this.configuration = aConfiguration;
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     @Override
     protected void configure() {
         final Map<Class, Object> interfacesByInstances =
                 new HashMap<Class, Object>();
 
         interfacesByInstances.put(DispatcherFactory.class,
                 new DefaultDispatcherFactory());
         interfacesByInstances.put(
                 ImmediateSchedulingRequestMessageProcessorFactory.class,
                 new DefaultImmediateSchedulingRequestMessageProcessorFactory(
                         this.configuration));
         interfacesByInstances.put(MessageProcessorSelectorFactory.class,
                 new DefaultMessageProcessorSelectorFactory());
         interfacesByInstances.put(MqInfrastructureInitializerFactory.class,
                 new DefaultMqInfrastructureInitializerFactory());
         interfacesByInstances.put(IncomingQueueChannelFactory.class,
                 new DefaultIncomingQueueChannelFactory());
         interfacesByInstances.put(ShutdownHookFactory.class,
                 new DefaultShutdownHookFactory());
         interfacesByInstances.put(Persistence.class, new DefaultPersistence());
         interfacesByInstances.put(GoogleCalendarTasks2PccImporterFactory.class,
                 new DefaultGoogleCalendarTasks2PccImporterFactory());
         interfacesByInstances
                 .put(ProjectScheduler.class, getProjectScheduler());
         interfacesByInstances.put(TaskJuggler3Exporter.class,
                 getTaskJuggler3Exporter());
         interfacesByInstances.put(
                 GoogleCalendarTasks2PccImporter2Factory.class,
                 new DefaultGoogleCalendarTasks2PccImporter2Factory());
         interfacesByInstances.put(IsGoogleTaskRelevantCalculatorFactory.class,
                 new DefaultIsGoogleTaskRelevantCalculatorFactory());
         interfacesByInstances.put(GoogleTaskNotesParserFactory.class,
                 new DefaultGoogleTaskNotesParserFactory());
         interfacesByInstances.put(GoogleTask2PccTaskConverterFactory.class,
                 new DefaultGoogleTask2PccTaskConverterFactory());
         interfacesByInstances.put(GoogleTaskTitleParserFactory.class,
                 new DefaultGoogleTaskTitleParserFactory());
         interfacesByInstances.put(EmbeddedFileReader.class,
                 new DefaultEmbeddedFileReaderFactory().create());
         interfacesByInstances.put(Tj3DeadlinesFileParserFactory.class,
                 new DefaultTj3DeadlinesFileParserFactory());
         interfacesByInstances.put(Tj3BookingsParserFactory.class,
                 new DefaultTj3BookingsParserFactory());
         interfacesByInstances.put(BookingsFile2BookingsFactory.class,
                 new DefaultBookingsFile2BookingsFactory());
         interfacesByInstances.put(OutgoingQueueChannelFactory.class,
                 new DefaultOutgoingQueueChannelFactory());
         interfacesByInstances.put(GoogleTasksImporterFactory.class,
                 new DefaultGoogleTasksImporterFactory());
         interfacesByInstances.put(PlanCalculatorFactory.class,
                 new DefaultPlanCalculatorFactory());
         interfacesByInstances.put(Exporter2GoogleCalendarFactory.class,
                 new DefaultExporter2GoogleCalendarFactory());
         interfacesByInstances.put(GoogleTasksExporterFactory.class,
                 new DefaultGoogleTasksExporterFactory());
         interfacesByInstances.put(RelevantTaskSetCalculatorFactory.class,
                 new DefaultRelevantTaskSetCalculatorFactory());
 
         for (final Class clazz : interfacesByInstances.keySet()) {
             final Object instance = interfacesByInstances.get(clazz);
 
             bind(clazz).toInstance(instance);
         }
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

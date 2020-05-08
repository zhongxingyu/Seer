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
 
 package co.altruix.schedulingrequestmessageprocessor.defect201110141;
 
 import at.silverstrike.pcc.api.gtaskexporter.GoogleTasksExporterFactory;
 import at.silverstrike.pcc.api.persistence.Persistence;
 import at.silverstrike.pcc.impl.gtaskexporter.DefaultGoogleTasksExporterFactory;
 import at.silverstrike.pcc.impl.mockpersistence.MockPersistenceAdapter;
 
 import co.altruix.pcc.api.gcaleventimporter.GoogleCalendarEventImporterFactory;
 import co.altruix.pcc.api.googletasksimporter.GoogleTasksImporterFactory;
 
 import com.google.inject.AbstractModule;
 
 /**
  * @author DP118M
  * 
  */
 class MockInjectorModuleDefect201110141 extends AbstractModule {
 
     @Override
     protected void configure() {
         bind(GoogleTasksExporterFactory.class).toInstance(
                 new DefaultGoogleTasksExporterFactory());
         bind(GoogleCalendarEventImporterFactory.class).toInstance(
                 new MockGoogleCalendarEventImporterFactory());
         bind(GoogleTasksImporterFactory.class).toInstance(
                 new MockGoogleTasksImporterFactory());
         bind(Persistence.class).toInstance(new MockPersistenceAdapter() {
         });
     }
 
 }

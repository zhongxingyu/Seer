 /*
  * #%L
  * Bitrepository Protocol
  * *
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.integrityclient;
 
 import org.bitrepository.access.getfileids.BasicGetFileIDsClient;
import org.bitrepository.collection.settings.standardsettings.Settings;
 import org.bitrepository.common.ConfigurationFactory;
 import org.bitrepository.common.ModuleCharacteristics;
 import org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage;
 import org.bitrepository.integrityclient.cache.DatabaseBackedCachedIntegrityInformationStorage;
 import org.bitrepository.integrityclient.collection.DelegatingIntegrityInformationCollector;
 import org.bitrepository.integrityclient.collection.IntegrityInformationCollector;
 import org.bitrepository.integrityclient.collection.IntegrityInformationScheduler;
 import org.bitrepository.integrityclient.collection.TimerIntegrityInformationScheduler;
 import org.bitrepository.integrityclient.configuration.integrityclientconfiguration.IntegrityClientConfiguration;
 import org.bitrepository.protocol.messagebus.MessageBus;
 
 /**
  * Provides access to the different component in the integrity module (Spring/IOC wannabe)
  */
 public final class IntegrityClientComponentFactory {
 
     //---------------------Singleton-------------------------
     private static IntegrityClientComponentFactory instance;
 
     /**
      * The singletonic access to the instance of this class
      * @return The one and only instance
      */
     public static synchronized IntegrityClientComponentFactory getInstance() {
         if (instance == null) {
             instance = new IntegrityClientComponentFactory();
         }
         return instance;
     }
 
     /**
      * The singleton constructor
      */
     private IntegrityClientComponentFactory() {
     }
 
     // --------------------- Components-----------------------
     /** The module characteristics used for identification of configuration. */
     private static final ModuleCharacteristics MODULE_CHARACTERISTICS = new ModuleCharacteristics("integrityclient");
     /** The configuration loaded for this class. */
     private IntegrityClientConfiguration integrityClientConfiguration;
     /** The integrity information scheduler. */
     private IntegrityInformationScheduler integrityInformationScheduler;
     /** The integrity information collector. */
     private IntegrityInformationCollector integrityInformationCollector;
     /** The integrity information collector. */
     private CachedIntegrityInformationStorage cachedIntegrityInformationStorage;
 
     /**
      * Gets you a <code>ModuleCharacteristics</code> object defining the generic characteristics of this module
      * @return A <code>ModuleCharacteristics</code> object defining the generic characteristics of this module
      */
     public ModuleCharacteristics getModuleCharacteristics() {
         return MODULE_CHARACTERISTICS;
     }
 
     /**
      * Gets you an <code>IntegrityInformationScheduler</code> that schedules integrity information collection.
      * @return an <code>IntegrityInformationScheduler</code> that schedules integrity information collection.
      */
     public IntegrityInformationScheduler getIntegrityInformationScheduler() {
         if (integrityInformationScheduler == null) {
             integrityInformationScheduler = new TimerIntegrityInformationScheduler(
                     getIntegrityClientConfiguration().getCollectionConfiguration());
         }
         return integrityInformationScheduler;
     }
 
     /**
      * Gets you an <code>IntegrityInformationCollector</code> that collects integrity information.
      * @return an <code>IntegrityInformationCollector</code> that collects integrity information.
      */
    public IntegrityInformationCollector getIntegrityInformationCollector(MessageBus messageBus, Settings settings) {
         if (integrityInformationCollector == null) {
             integrityInformationCollector = new DelegatingIntegrityInformationCollector(
                     getCachedIntegrityInformationStorage(),
                     // TODO: Hardcoded implementation
                     new BasicGetFileIDsClient(messageBus, settings),
                     // TODO: No implementation
                     null);
         }
         return integrityInformationCollector;
     }
 
     /**
      * Gets you a <code>CachedIntegrityInformationStorage</code> that collects integrity information.
      * @return an <code>CachedIntegrityInformationStorage</code> that collects integrity information.
      */
     public CachedIntegrityInformationStorage getCachedIntegrityInformationStorage() {
         if (cachedIntegrityInformationStorage == null) {
             cachedIntegrityInformationStorage = new DatabaseBackedCachedIntegrityInformationStorage(
                     getIntegrityClientConfiguration().getStorageConfiguration());
         }
         return cachedIntegrityInformationStorage;
     }
 
     /**
      * Gets you the configuration for this module. The configuration object is loaded from file the first time this
      * method is called, and cannot be reloaded.
      * @return Gets you the configuration for this module
      */
     private IntegrityClientConfiguration getIntegrityClientConfiguration() {
         if (integrityClientConfiguration == null) {
             ConfigurationFactory configurationFactory = new ConfigurationFactory();
             integrityClientConfiguration =
                 configurationFactory.loadConfiguration(getModuleCharacteristics(), IntegrityClientConfiguration.class);
         }
         return integrityClientConfiguration;
     }
 }

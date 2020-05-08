 /*
  * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.qipki.ca.bootstrap;
 
 import java.io.IOException;
 
 import org.codeartisans.java.toolbox.Strings;
 import org.codeartisans.java.toolbox.network.FreePortFinder;
 
 import org.qi4j.api.common.InvalidApplicationException;
 import org.qi4j.api.common.Visibility;
 import org.qi4j.api.structure.Application.Mode;
 import org.qi4j.bootstrap.*;
 import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.fileconfig.FileConfiguration;
 import org.qi4j.library.fileconfig.FileConfigurationOverride;
 import org.qi4j.library.jmx.JMXAssembler;
 import org.qi4j.library.jmx.JMXConnectorConfiguration;
 import org.qi4j.library.jmx.JMXConnectorService;
 import org.qi4j.library.scheduler.bootstrap.SchedulerAssembler;
 
 import static org.qipki.ca.bootstrap.CaAssemblyNames.*;
 import org.qipki.commons.bootstrap.CryptoValuesModuleAssembler;
 import org.qipki.core.bootstrap.persistence.InMemoryPersistenceAssembler;
 import org.qipki.core.bootstrap.persistence.PersistenceAssembler;
 import org.qipki.core.reindex.AutomaticReindexerConfiguration;
 import org.qipki.crypto.bootstrap.CryptoEngineModuleAssembler;
 
 public class QiPkiEmbeddedCaAssembler
         implements ApplicationAssembler
 {
 
     private final String appName;
     private final String appVersion;
     private final Mode appMode;
     private FileConfigurationOverride fileConfigOverride;
     private PersistenceAssembler persistenceAssembler;
     private boolean jmxManagement;
     private Integer jmxPort;
     private Assembler presentationTestsAssembler;
 
     public QiPkiEmbeddedCaAssembler( String appName, String appVersion, Mode appMode )
     {
         this.appName = appName;
         this.appVersion = appVersion;
         if ( appMode == null ) {
             this.appMode = Mode.development;
         } else {
             this.appMode = appMode;
         }
     }
 
     public final QiPkiEmbeddedCaAssembler withFileConfigurationOverride( FileConfigurationOverride fileConfigOverride )
     {
         this.fileConfigOverride = fileConfigOverride;
         return this;
     }
 
     public final QiPkiEmbeddedCaAssembler withPersistenceAssembler( PersistenceAssembler persistenceAssembler )
     {
         this.persistenceAssembler = persistenceAssembler;
         return this;
     }
 
     public final QiPkiEmbeddedCaAssembler withJMXManagement()
     {
         this.jmxManagement = true;
         return this;
     }
 
     public final QiPkiEmbeddedCaAssembler withJMXPort( Integer jmxPort )
     {
         this.jmxManagement = true;
         this.jmxPort = jmxPort;
         return this;
     }
 
     public final QiPkiEmbeddedCaAssembler withPresentationTestsAssembler( Assembler assembler )
     {
         this.presentationTestsAssembler = assembler;
         return this;
     }
 
     @Override
     @SuppressWarnings( "unchecked" )
     public final ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
             throws AssemblyException
     {
         ApplicationAssembly app = applicationFactory.newApplicationAssembly();
         if ( !Strings.isEmpty( appName ) ) {
             app.setName( appName );
         }
         app.setMode( appMode );
         if ( !Strings.isEmpty( appVersion ) ) {
             app.setVersion( appVersion );
         }
 
         LayerAssembly presentation = app.layer( LAYER_PRESENTATION );
         if ( presentationTestsAssembler != null ) {
             presentationTestsAssembler.assemble( presentation.module( MODULE_TESTS_IN_PRESENTATION ) );
         }
 
         LayerAssembly configuration = app.layer( LAYER_CONFIGURATION );
         {
             ModuleAssembly config = configuration.module( MODULE_CONFIGURATION );
            config.services( FileConfiguration.class ).visibleIn( Visibility.application );
             if ( fileConfigOverride != null ) {
                config.services( FileConfiguration.class ).setMetaInfo( fileConfigOverride );
             }
             config.services( MemoryEntityStoreService.class ).visibleIn( Visibility.module );
             config.entities( AutomaticReindexerConfiguration.class ).visibleIn( Visibility.application );
         }
 
         if ( jmxManagement ) {
             final LayerAssembly management = app.layer( LAYER_MANAGEMENT );
             {
                 ModuleAssembly config = configuration.module( MODULE_CONFIGURATION );
 
                 ModuleAssembly jmx = management.module( MODULE_JMX );
                 new JMXAssembler().assemble( jmx );
                 jmx.services( JMXConnectorService.class ).instantiateOnStartup();
                 config.entities( JMXConnectorConfiguration.class ).visibleIn( Visibility.application );
                 JMXConnectorConfiguration jmxConfigDefaults = config.forMixin( JMXConnectorConfiguration.class ).declareDefaults();
                 jmxConfigDefaults.enabled().set( Boolean.TRUE );
                 if ( jmxPort != null && jmxPort != -1 ) {
                     jmxConfigDefaults.port().set( jmxPort );
                 } else {
                     try {
                         jmxConfigDefaults.port().set( FreePortFinder.findRandom() );
                     } catch ( IOException ex ) {
                         throw new AssemblyException( "No default JMX port provided and unable to dynamicaly find a free one", ex );
                     }
                 }
             }
         }
 
         LayerAssembly application = app.layer( LAYER_APPLICATION );
         {
             new CaDCIModuleAssembler().assemble(
                     application.module( MODULE_CA_DCI ) );
         }
 
         LayerAssembly domain = app.layer( LAYER_DOMAIN );
         {
             new CaDomainModuleAssembler().assemble(
                     domain.module( MODULE_CA_DOMAIN ) );
         }
 
         LayerAssembly crypto = app.layer( LAYER_CRYPTO );
         {
             ModuleAssembly config = configuration.module( MODULE_CONFIGURATION );
 
             new CryptoEngineModuleAssembler( Visibility.application ).withConfigModule( config ).
                     withConfigVisibility( Visibility.application ).
                     assemble( crypto.module( MODULE_CRYPTO_ENGINE ) );
             new CryptoValuesModuleAssembler( Visibility.application ).assemble(
                     crypto.module( MODULE_CRYPTO_VALUES ) );
         }
 
         LayerAssembly infrastructure = app.layer( LAYER_INFRASTRUCTURE );
         {
             ModuleAssembly config = configuration.module( MODULE_CONFIGURATION );
 
             // Persistence
             PersistenceAssembler persistAss = this.persistenceAssembler;
             if ( persistAss == null ) {
                 persistAss = new InMemoryPersistenceAssembler();
             }
             persistAss.withConfigModule( config );
             persistAss.assemble( infrastructure.module( MODULE_PERSISTENCE ) );
 
             if ( false ) { // FIXME Deactivated
                 // Job Scheduler
                 ModuleAssembly scheduler = infrastructure.module( MODULE_SCHEDULER );
                 new SchedulerAssembler().withConfigAssembly( config ).
                         withConfigVisibility( Visibility.application ).
                         withTimeline().
                         visibleIn( Visibility.application ).
                         assemble( scheduler );
             }
         }
 
         onAssemble( app );
 
         presentation.uses( application, crypto, configuration );
         application.uses( domain, crypto, configuration );
         domain.uses( crypto, configuration, infrastructure );
         crypto.uses( configuration );
         infrastructure.uses( configuration );
 
         onLayerUses( app );
 
         // Management Layer uses all application layers
         if ( jmxManagement ) {
             final LayerAssembly management = app.layer( LAYER_MANAGEMENT );
             app.visit( new AssemblyVisitorAdapter<InvalidApplicationException>()
             {
 
                 @Override
                 public void visitLayer( LayerAssembly eachLayer )
                         throws InvalidApplicationException
                 {
                     if ( !management.name().equals( eachLayer.name() ) ) {
                         management.uses( eachLayer );
                     }
                 }
 
             } );
         }
 
         return app;
     }
 
     protected void onAssemble( ApplicationAssembly applicationAssembly )
             throws AssemblyException
     {
     }
 
     protected void onLayerUses( ApplicationAssembly app )
     {
     }
 
 }

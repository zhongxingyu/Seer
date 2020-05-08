 /*******************************************************************************
  * Copyright 2011 John Casey
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.commonjava.web.config.io;
 
 import static org.commonjava.web.config.section.ConfigurationSectionListener.DEFAULT_SECTION;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import javax.inject.Named;
 
 import org.commonjava.web.config.ConfigurationException;
 import org.commonjava.web.config.ConfigurationReader;
 import org.commonjava.web.config.ConfigurationRegistry;
 import org.commonjava.web.config.DefaultConfigurationRegistry;
 import org.commonjava.web.config.section.ConfigurationSectionListener;
 
 @Named( "single-section" )
 public class SingleSectionConfigReader
     implements ConfigurationReader
 {
 
     private final ConfigurationRegistry dispatch;
 
     public SingleSectionConfigReader( final ConfigurationSectionListener<?> listener )
         throws ConfigurationException
     {
         dispatch = new DefaultConfigurationRegistry( listener );
     }
 
     public SingleSectionConfigReader( final Object target )
         throws ConfigurationException
     {
         dispatch = new DefaultConfigurationRegistry( target );
     }
 
     @Override
     public void loadConfiguration( final InputStream stream )
         throws ConfigurationException
     {
        Properties props = new Properties();
         try
         {
             props.load( stream );
         }
         catch ( final IOException e )
         {
            throw new ConfigurationException( "Failed to read configuration. Error: %s", e,
                                              e.getMessage() );
         }
 
         if ( !dispatch.sectionStarted( DEFAULT_SECTION ) )
         {
             return;
         }
 
         for ( final Object k : props.keySet() )
         {
            String key = (String) k;
             dispatch.parameter( DEFAULT_SECTION, key, props.getProperty( key ) );
         }
 
         dispatch.sectionComplete( DEFAULT_SECTION );
         dispatch.configurationParsed();
     }
 
 }

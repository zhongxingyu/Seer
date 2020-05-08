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
 package org.commonjava.web.config;
 
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.commonjava.web.config.section.ConfigurationSectionListener;
 
 public class DefaultConfigurationRegistry
     implements ConfigurationRegistry
 {
    private final Collection<ConfigurationListener> listeners;
 
     private Map<String, ConfigurationSectionListener<?>> sectionMap;
 
     public DefaultConfigurationRegistry()
     {
        listeners = new ArrayList<ConfigurationListener>();
     }
 
     public DefaultConfigurationRegistry( final Class<?>... types )
         throws ConfigurationException
     {
         this( new DefaultConfigurationListener( types ) );
     }
 
     public DefaultConfigurationRegistry( final ConfigurationSectionListener<?>... sectionListeners )
         throws ConfigurationException
     {
         this( new DefaultConfigurationListener( sectionListeners ) );
     }
 
     public DefaultConfigurationRegistry( final ConfigurationListener... listeners )
         throws ConfigurationException
     {
         this.listeners = Arrays.asList( listeners );
         mapSectionListeners();
     }
 
     public DefaultConfigurationRegistry( final Object... data )
         throws ConfigurationException
     {
        listeners = new ArrayList<ConfigurationListener>();
         for ( final Object d : data )
         {
             if ( d instanceof ConfigurationListener )
             {
                 with( (ConfigurationListener) d );
             }
             else if ( d instanceof ConfigurationSectionListener<?> )
             {
                 with( (ConfigurationSectionListener<?>) d );
             }
             else if ( d instanceof Class<?> )
             {
                 with( (Class<?>) d );
             }
             else
             {
                 throw new ConfigurationException( "Invalid input for configuration registry: %s", d );
             }
         }
     }
 
     public DefaultConfigurationRegistry with( final ConfigurationListener listener )
         throws ConfigurationException
     {
        listeners.add( listener );
         mapListener( listener );
         return this;
     }
 
     public DefaultConfigurationRegistry with( final ConfigurationSectionListener<?> listener )
         throws ConfigurationException
     {
        final DefaultConfigurationListener dcl = new DefaultConfigurationListener( listener );
        listeners.add( dcl );
        mapListener( dcl );
         return this;
     }
 
     public DefaultConfigurationRegistry with( final Class<?> type )
         throws ConfigurationException
     {
        final DefaultConfigurationListener dcl = new DefaultConfigurationListener( type );
        listeners.add( dcl );
        mapListener( dcl );
         return this;
     }
 
     @Override
     public void configurationParsed()
         throws ConfigurationException
     {
         if ( listeners != null )
         {
             for ( final ConfigurationListener listener : listeners )
             {
                 listener.configurationComplete();
             }
         }
         else
         {
             // TODO: Log to debug level!
         }
     }
 
     @Override
     public boolean sectionStarted( final String name )
         throws ConfigurationException
     {
         final ConfigurationSectionListener<?> listener = sectionMap.get( name );
         if ( listener != null )
         {
             listener.sectionStarted( name );
             return true;
         }
 
         return false;
     }
 
     @Override
     public void sectionComplete( final String name )
         throws ConfigurationException
     {
         final ConfigurationSectionListener<?> listener = sectionMap.get( name );
         if ( listener != null )
         {
             listener.sectionComplete( name );
         }
     }
 
     @Override
     public void parameter( final String section, final String name, final String value )
         throws ConfigurationException
     {
         final ConfigurationSectionListener<?> secListener = sectionMap.get( section );
         secListener.parameter( name, value );
     }
 
     protected synchronized void mapSectionListeners()
         throws ConfigurationException
     {
         if ( listeners != null )
         {
             for ( final ConfigurationListener listener : listeners )
             {
                 mapListener( listener );
             }
         }
         else
         {
             // TODO: Log to debug level!
         }
     }
 
     private void mapListener( final ConfigurationListener listener )
         throws ConfigurationException
     {
         if ( sectionMap == null )
         {
             sectionMap = new HashMap<String, ConfigurationSectionListener<?>>();
         }
 
         final Map<String, ConfigurationSectionListener<?>> parsers = listener.getSectionListeners();
         for ( final Map.Entry<String, ConfigurationSectionListener<?>> entry : parsers.entrySet() )
         {
             final String section = entry.getKey();
             if ( sectionMap.containsKey( section ) )
             {
                 throw new ConfigurationException(
                                                   "Section collision! More than one ConfigurationParser bound to section: %s\n\t%s\n\t%s",
                                                   section, sectionMap.get( section ), entry.getValue() );
             }
 
             sectionMap.put( section, entry.getValue() );
         }
     }
 
 }

 /*
  * #%L
  * Bitrepository Protocol
  * 
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
 package org.bitrepository.common.settings;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bitrepository.settings.collectionsettings.CollectionSettings;
 import org.bitrepository.settings.referencesettings.ReferenceSettings;
 
 /**
  * Used for accessing <code>Settings</code> objects. A <code>SettingsLoader</code> needs to be provides on 
  * instantiation for loading stored settings.
  */
 public class SettingsProvider {
     /** Defines the property which might be used to specify the collection to load the settings for */
     public static String COLLECTIONID_PROPERTY = "bitrepository.collectionid";
     /** The loader to use for acessing stored settings*/
     private final SettingsLoader settingsReader;
     /** Map of the loaded settings */
     private final Map<String,Settings> settingsMap = new HashMap<String,Settings>();
 
     /**
      * Creates a <code>SettingsProvider</code> which will use the provided <code>SettingsLoader</code> for loading the 
      * settings.
      * @param settingsReader Use for loading the settings.
      */
     public SettingsProvider(SettingsLoader settingsReader) {
         this.settingsReader = settingsReader;
     }
     
     /**
      * Loads the settings for the collection defined by the COLLECTIONID_PROPERTY system variable.
      * @param collectionID The collectionID to find settings for.
      * @return The settings for the indicated CollectionID
      */
     public synchronized Settings getSettings() {
         String collectionID = System.getProperty(COLLECTIONID_PROPERTY);
         if (!settingsMap.containsKey(collectionID)) {
             loadSettings(collectionID);
         }
         return settingsMap.get(collectionID);
     }
     
     /**
      * Will load the settings from disk if they haven't been loaded before
      * @param collectionID The collectionID to find settings for.
      * @return The settings for the indicated CollectionID
      */
     public synchronized Settings getSettings(String collectionID) {
         if (!settingsMap.containsKey(collectionID)) {
             loadSettings(collectionID);
         }
         return settingsMap.get(collectionID);
     }
     
     /**
     * Will load the settings from disk to this providers model. Will overwrite any settings already in the provider.
     * @param collectionID The collectionID to load the settings for.
     */
     public synchronized void loadSettings(String collectionID) {
         Settings settings = new Settings(
                settingsReader.loadSettings(collectionID, CollectionSettings.class),
                settingsReader.loadSettings(collectionID, ReferenceSettings.class)
         );
         settingsMap.put(collectionID, settings);
     }
 }

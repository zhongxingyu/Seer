 /*   
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s, 
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
/**
 * \file
 * \brief
 */

 
 package dk.dbc.opensearch.common.pluginframework;
 
 
 /**
  * This interface represents the base point for all plugins. It
  * defines two methods that are the common basis for all plugins that
  * perform tasks in the datadock or pti.
  * 
  * The pluginID must be given to the plugin at object creation
  * time. The taskname is defined at compile time but could be the
  * subject of configuration variables in the future.
  * 
  * \todo: make the taskname a configuration value instead of setting it compile time
  * 
  * Plugins in the opensearch plugin framework are expected to work
  * primarily with CargoContainers. Plugins are defined on the basis of
  * the format they're going to work with combined with th e type of
  * task they're going to perform. E.g. for the format 'faktalink' (DBC
  * material, formatted in XML), the tasks could likely be:
  * 
  * 1) harvest: reading one or more files from a path and putting the
  *    content of the files and known metadata about them into
  *    CargoContainers.
  *    
  * 2) annotate: lookup metadata relating to the data in a
  *    CargoContainer and annotate the metadata to the
  *    CargoContainer(s).
  *    
  * 3) index: convert the CargoContainer into a lucene index
  * 
  * 4) store: format the CargoContainer into a Fedora Digital Object
  *    and store the digital object in the Fedora Digital Repository.
  * 
  * 
  */
 public interface IPluggable
 {
     /**
      * getPluginType returns the - at compile time given - type of the
      * task the plugin is constructed to handle. The plugin type is used
      * by the thread handling to decide which type of plugin to call. It
      * is a private variable of the plugin classes.
      * 
      * @returns a PluginType defining containing the plugin
      */
     public PluginType getPluginType();
 }

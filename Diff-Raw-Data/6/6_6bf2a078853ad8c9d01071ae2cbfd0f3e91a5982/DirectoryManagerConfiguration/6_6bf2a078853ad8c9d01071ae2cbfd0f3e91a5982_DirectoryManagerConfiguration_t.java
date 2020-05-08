 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  */
 
 package no.feide.moria.directory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 import org.jdom.Attribute;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 /**
  * Configuration handler for the Directory Manager. Parses the configuration
  * file.
  */
 public class DirectoryManagerConfiguration {
 
     /** The location of the index file. */
     private String indexFilename;
 
     /** The index update frequency, in milliseconds. */
     private long indexUpdateFrequency;
 
     /** Internal representation of the backend class. */
     private Class backendFactoryClass;
 
     /** Internal representation of the backend configuration file structure. */
     private Element backendConfiguration = null;
 
     /**
      * The required configuration file property, for external reference.
      * Currently contains the value
      * <code>no.feide.moria.directory.configuration</code>.
      */
     public static final String CONFIGURATION_PROPERTY = "no.feide.moria.directory.configuration";
 
 
     /**
      * Constructor. Creates a new configuration object and reads the Directory
      * Manager configuration file. <br>
      * <br>
      * Note that the actual parsing of the configuration file is done by
      * <code>parseIndexConfig(Element)</code> and
      * <code>parseBackendConfig(Element)</code>.
      * @param config
      *            The Directory Manager configuration passed on from
      *            <code>DirectoryManager.setConfig(Properties)</code>. Must
      *            include the property given by
      *            <code>DirectoryManagerConfiguration.CONFIGURATION_PROPERTY</code>.
      * @throws NullPointerException
      *             If <code>config</code> is <code>null</code>.
      * @throws DirectoryManagerConfigurationException
      *             If the property given by
      *             <code>DirectoryManagerConfiguration.CONFIGURATION_PROPERTY</code>
      *             is not set or is an empty string. Also thrown if unable to
      *             read from or parse the configuration file.
      * @see DirectoryManager#setConfig(Properties)
      * @see #CONFIGURATION_PROPERTY
      * @see #parseBackendConfig(Element)
      * @see #parseIndexConfig(Element)
      */
     public DirectoryManagerConfiguration(final Properties config) {
 
         // Sanity check.
         if (config == null)
             throw new NullPointerException("Configuration properties cannot be NULL");
 
         // Preparing to read configuration from file.
         final String configFile = (String) config.get(CONFIGURATION_PROPERTY);
         if (configFile == null || configFile.equals(""))
             throw new DirectoryManagerConfigurationException("Property " + DirectoryManagerConfiguration.CONFIGURATION_PROPERTY + " not set)");
 
         // Read index (not the index files themselves, mind you) and backend
         // configuration.
         Element rootElement = null;
         try {
             rootElement = (new SAXBuilder()).build(new File(configFile)).getRootElement();
         } catch (IOException e) {
             throw new DirectoryManagerConfigurationException("Unable to read from configuration file \"" + configFile + '\"', e);
         } catch (JDOMException e) {
             throw new DirectoryManagerConfigurationException("Unable to parse configuration file \"" + configFile + '\"', e);
         }
         parseIndexConfig(rootElement);
         backendConfiguration = parseBackendConfig(rootElement);
 
     }
 
 
     /**
      * Gets the backend configuration element.
      * @return A copy of the backend configuration element, as per
      *         <code>Element.clone()</code>.
      * @see Element#clone()
      */
     public final Element getBackendElement() {
 
         return (Element) backendConfiguration.clone();
 
     }
 
 
     /**
      * Parses the subsection of the configuration file related to the index and
      * updates the configuration. <br>
      * <br>
      * If more than one <code>Index</code> element is found, only the first is
      * considered.
      * @param rootElement
      *            The root configuration element. Cannot be <code>null</code>.
      * @throws NullPointerException
      *             If <code>rootElement</code> is <code>null</code>.
      * @throws DirectoryManagerConfigurationException
      *             If index file (attribute <code>file</code> in element
      *             <code>Index</code>) is not set, or if index update
      *             frequency (attribute <code>update</code> in element
      *             <code>Index</code>) is not set or is less than zero.
      */
     private void parseIndexConfig(final Element rootElement) {
 
         // Sanity check.
         if (rootElement == null)
             throw new NullPointerException("Root element cannot be NULL");
 
         // Get the index element, with sanity checks.
         final Element indexElement = rootElement.getChild("Index");
 
         // Get index filename, with sanity checks.
         Attribute a = indexElement.getAttribute("file");
         if ((a == null) || (a.getValue() == null) || (a.getValue().equals("")))
             throw new DirectoryManagerConfigurationException("Index file not set in configuration file");
         indexFilename = a.getValue();
 
         // Get index update frequency, with sanity checks.
         a = indexElement.getAttribute("update");
         if ((a == null) || (a.getValue() == null) || (a.getValue().equals("")))
             throw new DirectoryManagerConfigurationException("Index update frequency not set in configuration file");
         indexUpdateFrequency = 1000 * Integer.parseInt(a.getValue());
         if (indexUpdateFrequency <= 0)
             throw new DirectoryManagerConfigurationException("Index update frequency must be greater than zero");
 
     }
 
 
     /**
      * Gets the serialized index file name.
      * @return The index file name.
      */
     public final String getIndexFilename() {
 
         return indexFilename;
 
     }
 
 
     /**
      * Gets the index update frequency.
      * @return The index update frequency, in milliseconds.
      */
     public final long getIndexUpdateFrequency() {
 
         return indexUpdateFrequency;
 
     }
 
 
     /**
      * Parses the subsection of the configuration file common to all backend
      * implementations and updates the configuration. <br>
      * <br>
      * This method will only consider the attribute <code>class</code> in the
      * <code>Backend</code> element; further parsing of the element is left to
      * the backend implementation. If more than one <code>Backend</code>
      * element is found, only the first is considered.
      * @param rootElement
      *            The root configuration element. Cannot be <code>null</code>.
      * @return The backend configuration element, as per
      *         <code>Element.clone()</code>.
      * @throws NullPointerException
      *             If <code>rootElement</code> is <code>null</code>.
      * @throws DirectoryManagerConfigurationException
      *             If backend factory class (attribute <code>class</code> in
      *             element <code>Backend</code>) is not set, or if the given
      *             backend factory class cannot be resolved.
      * @see Element#clone()
      * @see no.feide.moria.directory.backend.DirectoryManagerBackendFactory#setConfig(Element)
      */
     private Element parseBackendConfig(final Element rootElement) {
 
         // Sanity check.
         if (rootElement == null)
             throw new NullPointerException("Root element cannot be NULL");
 
         // Get the backend element, with sanity checks.
         final Element backendElement = rootElement.getChild("Backend");
 
         // Get backend class, with sanity checks.
         final Attribute a = backendElement.getAttribute("class");
        if ((a == null) || (a.getValue() == null) || (a.getValue().equals("")))
             throw new DirectoryManagerConfigurationException("Backend class not set in configuration file");
         try {
             backendFactoryClass = Class.forName(a.getValue());
         } catch (ClassNotFoundException e) {
             throw new DirectoryManagerConfigurationException("Backend factory class " + a.getValue() + " not found", e);
         }
 
         return (Element) backendElement.clone();
 
     }
 
 
     /**
      * Gets the backend factory class implementation.
      * @return The backend factory class.
      */
     public final Class getBackendFactoryClass() {
 
         return backendFactoryClass;
 
     }
 
 }

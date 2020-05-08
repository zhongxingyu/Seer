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
  * $Id$
  */
 
 package no.feide.moria.configuration;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import junit.framework.TestCase;
 
 /**
  * Test suite for ConfigurationManager class.
  *
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public final class ConfigurationManagerTest extends TestCase {
 
     private final String configBaseProperty = "no.feide.moria.configuration.base";
 
     /**
      * Initiate all tests.
      *
      * @return Junit test suite.
      */
     public static Test suite() {
         return new TestSuite(ConfigurationManagerTest.class);
     }
 
     /**
      * Test creation of a <code>ConfigurationManager</code> object.
      *
      * @throws ConfigurationManagerException should not be thrown
      */
     public void testCreateConfigurationManager() throws ConfigurationManagerException {
 
        /* Base config property is null. */
         try {
             new ConfigurationManager();
             fail("BaseConfigException should be raised, system property is null");
         } catch (BaseConfigException success) {
         }
        
        /* Base config property is an empty string. */
         System.setProperty(configBaseProperty, "");
         try {
             new ConfigurationManager();
             fail("BaseConfigException should be raised, system property is empty string");
         } catch (BaseConfigException success) {
         }
 
         /* Wrong reference to manager config */
         try {
             System.setProperty(configBaseProperty, "wrong");
             new ConfigurationManager();
             fail("ConfigurationManagerException should be raised, wrong property file");
         } catch (ConfigurationManagerException success) {
         }
 
         /* None-existing property files */
         try {
             System.setProperty(configBaseProperty, "/foo/bar/dont/exist");
             new ConfigurationManager();
             fail("ConfigurationManagerException should be raised, wrong property file");
         } catch (ConfigurationManagerException success) {
         }
 
         try {
             System.setProperty(configBaseProperty, "/foo/bar/dont/exist");
             new ConfigurationManager();
             fail("ConfigurationManagerException should be raised, wrong property file");
         } catch (ConfigurationManagerException success) {
         }
 
         /* Invalid file content */
         try {
             System.setProperty("no.feide.moria.configuration.base",
                     System.getProperty("no.feide.moria.configuration.test.dir") + "/moria-base-invalid.properties");
             new ConfigurationManager();
             fail("ConfigurationManagerException should be raised, invalid content of property file");
         } catch (ConfigurationManagerException success) {
         }
 
         /* Empty file */
         try {
             System.setProperty("no.feide.moria.configuration.base",
                     System.getProperty("no.feide.moria.configuration.test.dir") + "/empty");
             new ConfigurationManager();
             fail("ConfigurationManagerException should be raised, empty property file");
         } catch (ConfigurationManagerException success) {
         }
     }
 }

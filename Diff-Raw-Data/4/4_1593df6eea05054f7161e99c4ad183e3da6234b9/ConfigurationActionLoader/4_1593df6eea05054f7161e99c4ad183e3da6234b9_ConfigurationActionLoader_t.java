 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 1st December 2009
  *
  * Changelog:
  * - 01/12/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.rig.internal;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import au.edu.uts.eng.remotelabs.rigclient.main.RigClientDefines;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IAction;
 import au.edu.uts.eng.remotelabs.rigclient.rig.AbstractRig.ActionType;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Loads action instances from configuration.
  */
 public class ConfigurationActionLoader
 {
     /** Character that splits configuration. */
     public static final String SPLIT_CHAR = ";";
     
     /** Configuration. */
     private final IConfig config;
     
     /** Package prefixes to attempt to resolve class from. */
     private final List<String> packagePrefixes;
     
     /** Logger. */
     private final ILogger logger;
     
     /**
      * Constructor.
      */
     public ConfigurationActionLoader()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.config = ConfigFactory.getInstance();
         
         this.packagePrefixes = new ArrayList<String>();
         final String packagesConf = this.config.getProperty("Action_Package_Prefixes", "");
         this.packagePrefixes.addAll(Arrays.asList(packagesConf.split(ConfigurationActionLoader.SPLIT_CHAR)));
     }
     
     /**
      * Returns the configured action classes for a particular action type.
      * 
      * @param type action type
      * @return configured action type instances
      */
     public IAction[] getConfiguredActions(final ActionType type)
     {
         String conf = null;
         switch (type)
         {
             case ACCESS:
                 this.logger.debug("Loading the access action type configured instances.");
                 conf = this.config.getProperty("Access_Actions");
                 break;
             case SLAVE_ACCESS:
                 this.logger.debug("Loading the slave Access action type configured instances.");
                 conf = this.config.getProperty("Slave_Access_Actions");
                 break;
             case DETECT:
                 this.logger.debug("Loading the activity detection action type configured instances.");
                 conf = this.config.getProperty("Detection_Actions");
                 break;
             case NOTIFY:
                 this.logger.debug("Loading the notification action type configured instances.");
                 conf = this.config.getProperty("Notify_Actions");
                 break;
             case RESET:
                 this.logger.debug("Loading the reset action type configured instances.");
                 conf = this.config.getProperty("Reset_Actions");
                 break;
             case TEST:
                 this.logger.debug("Loading the test action type configured instances.");
                 conf = this.config.getProperty("Test_Actions");
                 break;
             default:
                 throw new IllegalStateException("This shouldn't happen and is probably bug. Initially thrown " +
                 		"in ConfigurationActionLoader->getConfiguredActions.");
         }
         
         return this.loadActions(conf, type);
     }
 
     /**
      * Parses the configuration string and attempts to load the classes. 
      * Class resolution first attempts to load the class directly then 
      * successively tries each configured package.
      * 
      * @param conf configuration string with classes separated with ';'
      * @param type action type to print errors
      * @return list of action classes, empty list of none found
      */
     private IAction[] loadActions(final String conf, final ActionType type)
     {
         final List<IAction> actions = new ArrayList<IAction>();
         
         if (conf == null || conf.equals(""))
         {
             this.logger.info("No configured actions of type " + type + ".");
             return new IAction[0];
         }
         this.logger.debug(type + " actions configuration string is " + conf + ".");
         
         final String classes[] = conf.split(ConfigurationActionLoader.SPLIT_CHAR);

         for (String clazz : classes)
         {
            Class<?> foundClazz = null;
             try
             {
                 try
                 {
                     foundClazz = Class.forName(clazz);
                 }
                 catch (ClassNotFoundException e)
                 {
                     this.logger.debug("Class " + clazz + " not found.");
                     for (String prefix : this.packagePrefixes)
                     {
                         if ("".equals(prefix)) continue; // Empty value configuration
                         
                         try
                         {
                             foundClazz = Class.forName(RigClientDefines.prependPackage(prefix, clazz));
                         }
                         catch (ClassNotFoundException e1)
                         {
                             this.logger.debug("Class " + RigClientDefines.prependPackage(prefix, clazz) + 
                                     " not found.");
                         }
                     }
                 }
                 
                 if (foundClazz == null)
                 {
                     this.logger.error("Unable to find action class " + clazz + " of type " + type + ".");
                     continue;
                 }
                 
                 final Object obj = foundClazz.newInstance();
                 if (obj instanceof IAction)
                 {
                     this.logger.info("Successfully found class " + foundClazz.getCanonicalName() + ", adding it as a " +
                     		type + " class.");
                     actions.add((IAction)obj);
                 }
                 else
                 {
                     this.logger.error("Instantiated class " + foundClazz.getCanonicalName() + " does not implement" +
                             " the IAction interface, so is not a correct action class.");
                 }
             }
             catch (InstantiationException e)
             {
                 this.logger.error("Failed instantiating " + foundClazz.getName() + " with error " + e.getMessage() 
                         + ".");
             }
             catch (IllegalAccessException e)
             {
                 this.logger.error("Illegal access to " + foundClazz.getName() + " with error " + e.getMessage() + ".");
             }
         }
         
         return actions.toArray(new IAction[actions.size()]);
     }
 }

 /*
  * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.util;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 /**
  * Class responsible for instantiating of the system logger. This way anybody
  * can use this default logger to output their log messages without worrying
  * what logger to use. 
  *
  * @author bastafidli
  */
 public class Log extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
 
    /**
     * Default file name of configuration file.
     */
    public static final String DEFAULT_LOGCONFIG_FILE_NAME = "osslog.properties";
 
    // Attributes ///////////////////////////////////////////////////////////////
 
    /**
     * Default Java logger to log messages to the application.
     */
    protected static transient Logger s_lgLogger = null;
    
    /**
     * Helper mutex to synchronize some methods.
     */
    protected static transient String s_strMutex = "log.mutex";
 
    /**
     * Name of the configuration file actually used to configure the logger.
     */
    protected static String s_strConfigFile;
    
    /**
     * Logger for this class. It will be initialized only after the log 
     * configuration is initialized.
     */
    protected static Logger s_logger;
    
    // Constructors /////////////////////////////////////////////////////////////
 
    /**
     * Static initializer
     */
    static 
    {
       InputStream isConfigFile = null;
       
       try
       {
          // At first, try to use config file from command line definition
         String strLogfilename;
          
          strLogfilename = System.getProperty("java.util.logging.config.file");
          try
          {
             // In case you are wondering while property name below was chosen
             // look at JavaDoc for java.util.logging.LogManager class which
             // defines this property as a standard property to specify logging
             // configuration
             if ((strLogfilename != null) && (strLogfilename.length() > 0))
             {
                isConfigFile = new FileInputStream(strLogfilename);
                s_strConfigFile = strLogfilename;
             }
          }
          catch (FileNotFoundException fnfExc1)
          {
             // Try to search for it, this will also setup s_strConfigFile
             isConfigFile = findConfigFile(strLogfilename);
             
             if (isConfigFile == null)
             {
                // We have to use system.out since the logger is not initialized yet
                System.out.println("File " + strLogfilename 
                   + " set in java.util.logging.config.file doesn't exist");
             }
          }
          if (isConfigFile == null)
          {
             try
             {
                // Open the default file
                isConfigFile = new FileInputStream(DEFAULT_LOGCONFIG_FILE_NAME);
                s_strConfigFile = DEFAULT_LOGCONFIG_FILE_NAME;
             }
             catch (FileNotFoundException fnfExc2)
             {
                // Try to search for it, this will also setup s_strConfigFile
                isConfigFile = findConfigFile(DEFAULT_LOGCONFIG_FILE_NAME);
 
                if (isConfigFile == null)
                {
                   // We have to use system.out since the logger is not initialized yet
                   System.out.println("Default log configuration file " 
                                      + DEFAULT_LOGCONFIG_FILE_NAME 
                                      + " doesn't exist");
                }
             }
          }
          if (isConfigFile != null)
          {
             LogManager.getLogManager().readConfiguration(isConfigFile);
          }
       }
       catch (IOException ioeExc)
       {
          // Logger is not initialized yet, use System.out.
          System.out.println("Cannot initialize logging subsystem.");
          ioeExc.printStackTrace();
       }
       finally
       {
          try
          {
             if (isConfigFile != null)
             {
                isConfigFile.close();
             }
          }
          catch (IOException ioeExc)
          {
             // Ignore this
          }
          finally
          {
             s_logger = Log.getInstance(Log.class);
             // Print this as info because otherwise we wouldn't know where to 
             // change it
            s_logger.log(Level.INFO, "Using log configuration file {0}", 
                        s_strConfigFile);
          }
       }
    }
    
    /**
     * Hidden constructor to disable creation of instances of this class.
     */
    protected Log(
    )
    {
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
 
    /**
     * Get instance of configured logger which can be used to log messages from 
     * the application.
     *
     * @param classIdentifier - identifier of class to get the logger for
     * @return Logger - instance of logger ready to use
     */
    public static Logger getInstance(
       Class classIdentifier
    )
    {
       return getInstance(classIdentifier.getName());
    }
 
    /**
     * Get instance of configured logger which can be used to log messages from 
     * the application.
     *
     * @param logIdentifier - identifier of log to get 
     * @return Logger - instance of logger ready to use
     */
    public static Logger getInstance(
       String logIdentifier
    )
    {
       // We may do any additional configuration here but for now we are fine
       // with the default configuration read from file specified by property
       // java.util.logging.config.file as specified in documentation for 
       // LogManager
       return Logger.getLogger(logIdentifier);
    }
 
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * Find and configuration file.
     * 
     * @param  strConfigFileName - name of the configuration file
     * @return InputStream - opened config file
     * @throws IOException - an error has occurred opening configuration file
     * @throws FileNotFoundException - file cannot be found
     */
    protected static InputStream findConfigFile(
       String strConfigFileName
    ) throws IOException,
             FileNotFoundException
    {
       InputStream isConfigFile = null;
       URL         urlDefaultPropertyFile;
 
       urlDefaultPropertyFile = ClassLoader.getSystemResource(strConfigFileName);
       if (urlDefaultPropertyFile == null)
       {
          // if there was not found configuration file by using 
          // ClassLoader.getSystemResource(), try to use 
          // getClass().getClassLoader().getResource()
          // This is here due to the fact that in J2EE server
          // ClassLoader.getSystemResource() search the configuration file
          // OUTSIDE of the packaged application (ear, war, jar) so that this
          // file can override the configuration file which is packaged INSIDE
          // the application which will be found belog with class loader 
          // for this class
          urlDefaultPropertyFile = Log.class.getClassLoader().getResource(
                                      strConfigFileName);
       }      
       if (urlDefaultPropertyFile == null)
       {
          throw new FileNotFoundException("Cannot find configuration file "
                                          + strConfigFileName);
       }
       else
       {
          isConfigFile = urlDefaultPropertyFile.openStream();
          s_strConfigFile = urlDefaultPropertyFile.toString();
       }
       
       return isConfigFile;
    }
 }

 /*
  * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
import java.util.Hashtable;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.opensubsystems.core.error.OSSConfigException;
 
 /**
  * Class containing methods for working with properties
  * 
  * @author bastafidli
  */
 public final class PropertyUtils extends OSSObject
 {
    // Attributes ///////////////////////////////////////////////////////////////
 
    /**
     * Map storing previous values of the configuration settings to eliminate
     * excessive log output. Use Hashtable because it is synchronized. 
     */
   protected static Map<String, String> s_previousValues = new Hashtable<>();
    
    // Cached values ////////////////////////////////////////////////////////////
    
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(PropertyUtils.class);
   
    // Constructors /////////////////////////////////////////////////////////////
    
    /** 
     * Private constructor since this class cannot be instantiated
     */
    private PropertyUtils(
    )
    {
       // Do nothing
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
 
    /**
     * Save the configuration to a file.
     * 
     * @param fileConfig - file to save the properties to
     * @param prpSettings - configuration settings to store to the file
     * @throws IOException - there was a problem saving configuration file.
     * @throws FileNotFoundException - file cannot be found
     */
    public static void save(
       File       fileConfig,
       Properties prpSettings
    ) throws IOException,
             FileNotFoundException
    {
       // Open the file
       OutputStream osConfigFile = null;
 
       try
       {
          // Open the file
          osConfigFile = new FileOutputStream(fileConfig);
 
          BufferedOutputStream bosConfigFile = null;
                  
          // Load the properties
          try
          {
             bosConfigFile = new BufferedOutputStream(osConfigFile);
             
             // TODO: Improve: Once this is invoked, all the comments from 
             // the original file are lost and the properties are in random
             // order. Figure out how to save it so we don't mess up the 
             // comments and order/grouping of properties
             prpSettings.store(bosConfigFile, 
                               "DO NOT MODIFY THIS FILE DIRECTLY.");
          }
          finally
          {
             // Close the file
             try
             {
                if (bosConfigFile != null)
                {
                   bosConfigFile.close();
                }
             }
             catch (IOException ioeExc)
             {
                // Ignore this
                s_logger.log(Level.WARNING, 
                             "Failed to close buffer for configuration file " 
                             + fileConfig.getCanonicalPath(), ioeExc);
             }
          }
       }
       finally
       {
          try
          {
             if (osConfigFile != null)
             {
                osConfigFile.close();
             }
          }
          catch (IOException ioeExc)
          {
             // Ignore this
             s_logger.log(Level.WARNING, 
                          "Failed to close configuration file " 
                          + fileConfig.getCanonicalPath(), ioeExc);
          }
       }         
    }
    
    /**
     * Retrieve integer property value of which should existing within 
     * a specified range.
     * 
     * @param prpSettings - properties to retrieve the setting from
     * @param strProperty - name of the property to retrieve
     * @param iDefaultValue - default value to use if a valid value is not 
     *                        specified
     * @param strDisplayName - user friendly name of the property  
     * @param iMinValue - inclusive minimal value of the range
     * @param iMaxValue - inclusive maximal value of the range
     * @return int - value of the property or default value if the value is not 
     *               specified
     */
    public static int getIntPropertyInRange(
       Properties prpSettings,
       String     strProperty,
       int        iDefaultValue,
       String     strDisplayName,
       int        iMinValue,
       int        iMaxValue
    )
    {
       String strParam;
       int    iValue;
       
       // Read the property, use the name of the property as default value to
       // detect if property is not set
       strParam = prpSettings.getProperty(strProperty, strProperty);
       if ((strParam.length() == 0) || (strParam.equals(strProperty)))
       {
          printConfigMessage(strProperty, Integer.toString(iDefaultValue),
                             strDisplayName + " is not set in property "
                             + strProperty + ", using default value " 
                             + iDefaultValue);
          iValue = iDefaultValue;
       }
       else
       {
          try
          {
             iValue = Integer.parseInt(strParam);
             if ((iValue < iMinValue) || (iValue > iMaxValue))
             {
                printConfigMessage(strProperty, Integer.toString(iDefaultValue),
                                   "Value of " + strProperty
                                   + " property is outside of valid range ("
                                   + iMinValue + " - " + iMaxValue 
                                   + "), using default value " 
                                   + iDefaultValue);
                iValue = iDefaultValue;
             }
             else
             {
                printConfigMessage(strProperty, Integer.toString(iValue), null);
             }
          }
          catch (NumberFormatException nfeExc)
          {
             printConfigMessage(strProperty, Integer.toString(iDefaultValue),
                                "Value of "  + strProperty 
                                + " property is incorrect ("
                                + strParam + ", valid range is "
                                + iMinValue + " - " + iMaxValue 
                                + "), using default value " 
                                + iDefaultValue);
             iValue = iDefaultValue;
          }
       }
       
       return iValue;
    }
 
    /**
     * Retrieve string property value and if the value is not specified or it is 
     * empty throw an exception.
     * 
     * @param prpSettings - properties to retrieve the setting from
     * @param strProperty - name of the property to retrieve
     * @param strDisplayName - user friendly name of the property  
     * @return String - value of the property 
     * @throws OSSConfigException - value for the requested property is not 
     *                              specified 
     */
    public static String getStringProperty(
       Properties prpSettings,
       String     strProperty,
       String     strDisplayName
    ) throws OSSConfigException
    {
       return getStringProperty(prpSettings, strProperty, strDisplayName, false);
    }
 
    /**
     * Retrieve string property value and if the value is not specified throw
     * an exception.
     * 
     * @param prpSettings - properties to retrieve the setting from
     * @param strProperty - name of the property to retrieve
     * @param strDisplayName - user friendly name of the property
     * @param bAllowEmpty - if true then empty value is allowed  
     * @return String - value of the property 
     * @throws OSSConfigException - value for the requested property is not 
     *                              specified 
     */
    public static String getStringProperty(
       Properties prpSettings,
       String     strProperty,
       String     strDisplayName,
       boolean    bAllowEmpty
    ) throws OSSConfigException
    {
       String     strParam;
       String     strValue;
       
       strParam = prpSettings.getProperty(strProperty, strProperty);
       if ((strParam.equals(strProperty)) 
          || ((!bAllowEmpty) && (strParam.length() == 0)))
       {
          throw new OSSConfigException(strDisplayName 
                                       + " is not set in property "
                                       + strProperty);
       }
       else
       {
          strValue = strParam;
       }
       printConfigMessage(strProperty, strValue, null);
       
       return strValue;
    }
 
    /**
     * Retrieve string property value and if the property is not specified or it
     * is specified as an empty value, use the default value instead.
     * 
     * @param prpSettings - properties to retrieve the setting from
     * @param strProperty - name of the property to retrieve
     * @param strDefaultValue - default value to use if a valid value is not 
     *                          specified
     * @param strDisplayName - user friendly name of the property  
     * @return String - value of the property or default value if the value is 
     *                  not specified
     */
    public static String getStringProperty(
       Properties prpSettings,
       String     strProperty,
       String     strDefaultValue,
       String     strDisplayName
    )
    {
       return getStringProperty(prpSettings, strProperty, strDefaultValue, 
                                strDisplayName, false);
    }
    
    /**
     * Retrieve string property value.
     * 
     * @param prpSettings - properties to retrieve the setting from
     * @param strProperty - name of the property to retrieve
     * @param strDefaultValue - default value to use if a valid value is not 
     *                          specified. If null is specified as a default 
     *                          value and no value is found then no config 
     *                          message will be printed into log
     * @param strDisplayName - user friendly name of the property  
     * @param bAllowEmpty - if true then empty value is allowed  
     * @return String - value of the property or default value if the value is 
     *                  not specified
     */
    public static String getStringProperty(
       Properties prpSettings,
       String     strProperty,
       String     strDefaultValue,
       String     strDisplayName,
       boolean    bAllowEmpty
    )
    {
       return getStringProperty(prpSettings, strProperty, strDefaultValue,
                                strDisplayName, bAllowEmpty, true);
    }
    
    /**
     * Retrieve string property value.
     * 
     * @param prpSettings - properties to retrieve the setting from
     * @param strProperty - name of the property to retrieve
     * @param strDefaultValue - default value to use if a valid value is not 
     *                          specified. If null is specified as a default 
     *                          value and no value is found then no config 
     *                          message will be printed into log
     * @param strDisplayName - user friendly name of the property  
     * @param bAllowEmpty - if true then empty value is allowed  
     * @param bPrintMessage - if true then message about what value was read will
     *                        be printed into the log, if false nothing wll be
     * @return String - value of the property or default value if the value is 
     *                  not specified
     */
    public static String getStringProperty(
       Properties prpSettings,
       String     strProperty,
       String     strDefaultValue,
       String     strDisplayName,
       boolean    bAllowEmpty,
       boolean    bPrintMessage
    )
    {
       String strParam;
       String strValue;
       
       // Read the property, use the name of the property as default value to
       // detect if property is not set
       strParam = prpSettings.getProperty(strProperty, strProperty);
       if ((strParam.equals(strProperty)) 
          || ((!bAllowEmpty) && (strParam.length() == 0)))
       {
          if ((strDefaultValue != null) && (bPrintMessage))
          {
             printConfigMessage(strProperty, strDefaultValue,  
                                strDisplayName + " is not set in property "
                                + strProperty + ", using default value " 
                                + strDefaultValue);
          }
          strValue = strDefaultValue;
       }
       else
       {
          strValue = strParam;
       }
       
       if ((strValue != null) && (bPrintMessage))
       {
          printConfigMessage(strProperty, strValue, null);  
       }
       
       return strValue;
    }
 
    /**
     * Retrieve boolean property value and if the value is not specified throw
     * an exception.
     * 
     * @param prpSettings - properties to retrieve the setting from
     * @param strProperty - name of the property to retrieve
     * @param strDisplayName - user friendly name of the property
     * @return boolean - value of the property 
     * @throws OSSConfigException - value for the requested property is not 
     *                              specified 
     */
    public static Boolean getBooleanProperty(
       Properties prpSettings,
       String     strProperty,
       String     strDisplayName
    ) throws OSSConfigException
    {
       String  strParam;
       Boolean bValue;
       
       strParam = prpSettings.getProperty(strProperty, strProperty);
       if ((strParam.equals(strProperty)) || (strParam.length() == 0))
       {
          throw new OSSConfigException(strDisplayName 
                                       + " is not set in property "
                                       + strProperty);
       }
       else
       {
          bValue = GlobalConstants.isTrue(strParam)
                   ? Boolean.TRUE : Boolean.FALSE;
       }
       printConfigMessage(strProperty, bValue.toString(), null);  
       
       return bValue;
    }
 
    /**
     * Retrieve boolean property value and if the value is not specified return 
     * the default value.
     * 
     * @param prpSettings - properties to retrieve the setting from
     * @param strProperty - name of the property to retrieve
     * @param bDefaultValue - default value to use if a valid value is not 
     *                        specified. If null is specified as a default 
     *                        value and no value is found then no config 
     *                        message will be printed into log
     * @param strDisplayName - user friendly name of the property
     * @return boolean - value of the property or default value if the value is 
     *                   not specified
     */
    public static Boolean getBooleanProperty(
       Properties prpSettings,
       String     strProperty,
       Boolean    bDefaultValue,
       String     strDisplayName
    ) 
    {
       String  strParam;
       Boolean bValue;
       
       strParam = prpSettings.getProperty(strProperty, strProperty);
       if ((strParam.equals(strProperty)) || (strParam.length() == 0))
       {
          if (bDefaultValue != null)
          {
             printConfigMessage(strProperty, bDefaultValue.toString(),   
                                strDisplayName + " is not set in property "
                                + strProperty + ", using default value " 
                                + bDefaultValue);
          }
          bValue = bDefaultValue;
       }
       else
       {
          bValue = GlobalConstants.isTrue(strParam) 
                   ? Boolean.TRUE : Boolean.FALSE;
       }
       if (bValue != null)
       {
          printConfigMessage(strProperty, bValue.toString(), null);  
       }
       
       return bValue;
    }
 
    /**
     * Retrieve boolean property value as a string representation of Boolean.TRUE
     * or Boolean.FALSE.
     * 
     * @param prpSettings - properties to retrieve the setting from
     * @param strProperty - name of the property to retrieve
     * @param bDefaultValue - default value to use if a valid value is not 
     *                        specified. If null is specified as a default 
     *                        value and no value is found then no config 
     *                        message will be printed into log
     * @param strDisplayName - user friendly name of the property
     * @return String - value of the property or default value if the value is 
     *                  not specified. If default value is null then empty string 
     *                  is returned.
     */
    public static String getBooleanPropertyAsString(
       Properties prpSettings,
       String     strProperty,
       Boolean    bDefaultValue,
       String     strDisplayName
    ) 
    {
       String  strReturn = "";
       Boolean bReturn;
    
       bReturn = getBooleanProperty(prpSettings, strProperty, bDefaultValue, 
                                    strDisplayName); 
       if (bReturn != null)
       {
          strReturn = bReturn.toString();
       }
       
       return strReturn;
    }
    
    /**
     * Log configuration message to the logger if the value of the property
     * is read for the first time or has changed.
     * 
     * @param strProperty - property value of which is read
     * @param strValue - value of the property
     * @param strMessage - message to log, if null a default message will be
     *                     logged
     */
    public static void printConfigMessage(
       String strProperty,
       String strValue,
       String strMessage
    )
    {
       String strPrevious;
       
       if (strValue == null)
       {
          strValue = "null";
       }
       
       // No need to synchronize because s_previousValues is synchronized
       strPrevious = s_previousValues.get(strProperty);
       if ((strPrevious == null) || (!strPrevious.equals(strValue)))
       {
          s_previousValues.put(strProperty, strValue);
          if (strMessage != null)
          {
             s_logger.config(strMessage);
          }
          else
          {
             s_logger.log(Level.CONFIG, "{0} = {1}", 
                          new Object[]{strProperty, strValue});
          }
       }
    }
 }

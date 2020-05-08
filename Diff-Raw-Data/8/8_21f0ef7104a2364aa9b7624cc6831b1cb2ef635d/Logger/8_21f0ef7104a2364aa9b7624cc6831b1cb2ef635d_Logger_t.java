 /**
  * Copyright 2010 David L. Whitehurst
  * 
  * Licensed under the Apache License, Version 2.0 
  * (the "License"); You may not use this file except 
  * in compliance with the License. You may obtain a 
  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, 
  * software distributed under the License is distributed on an 
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific 
  * language governing permissions and limitations under the 
  * License.
  * 
  */
 package org.dlw.ai.blackboard.util;
 
 import org.apache.commons.logging.Log;
 
 /**
  * This class is used to wrap and protect log-level use
  * 
  * @author dlwhitehurst
  *
  */
 public class Logger implements Log {
     
     /**
      * Attribute log
      */
     private Log log;
     
     /**
      * Static class instance
      */
     private static Logger instance;
     
     /**
      * Private constructor
      */
     private Logger() { }
     
     /**
      * Public method to wrap the log inside of this protected wrapper
      * 
      * @param log
      *    the Apache Log object
      */
     public void wrap(Log log) {
         this.log = log;
     }
 
     /**
      * Classic singleton get instance
      * @return {@link Logger}
      */
     public static Logger getInstance() {
         
         if (instance == null) {
             instance = new Logger();
         }
         return instance;
     }
     
     public void debug(Object message) {
         // TODO Auto-generated method stub
         
     }
 
     public void debug(Object message, Throwable t) {
         // TODO Auto-generated method stub
         
     }
 
     public void error(Object message) {
        if (log.isInfoEnabled()) {
            log.info(message);
        } else {
            System.err.println(SystemConstants.INFO_LEVEL_FATAL);
            System.exit(0);           
        }
     }
 
     public void error(Object message, Throwable t) {
         // TODO Auto-generated method stub
         
     }
 
     public void fatal(Object message) {
         // TODO Auto-generated method stub
         
     }
 
     public void fatal(Object message, Throwable t) {
         // TODO Auto-generated method stub
         
     }
 
     /**
      * Overridden public method for info-level logging
      */
     public void info(Object message) {
         if (log.isInfoEnabled()) {
             log.info(message);
         } else {
             System.err.println(SystemConstants.INFO_LEVEL_FATAL);
             System.exit(0);           
         }
     }
 
     public void info(Object message, Throwable t) {
         // TODO Auto-generated method stub
         
     }
 
     public boolean isDebugEnabled() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isErrorEnabled() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isFatalEnabled() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isInfoEnabled() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isTraceEnabled() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public boolean isWarnEnabled() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public void trace(Object message) {
         // TODO Auto-generated method stub
         
     }
 
     public void trace(Object message, Throwable t) {
         // TODO Auto-generated method stub
         
     }
 
     public void warn(Object message) {
         // TODO Auto-generated method stub
         
     }
 
     public void warn(Object message, Throwable t) {
         // TODO Auto-generated method stub
         
     }
 
 }

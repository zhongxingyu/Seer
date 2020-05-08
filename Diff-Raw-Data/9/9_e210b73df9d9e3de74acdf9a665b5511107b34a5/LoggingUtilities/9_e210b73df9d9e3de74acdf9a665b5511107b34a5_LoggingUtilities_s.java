 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.frontend;
 
 import java.io.OutputStream;
 import java.util.logging.*;
 
 /**
  * Wraps a logging system to provide simple methods for all classes in the imageanalysistools package to get a uniform logger, control its
  * level of logging detail, and add handlers.
  *
  * @author Colin J. Fuller
  */
 public class LoggingUtilities {
 
     static java.util.List<Handler> addedHandlers;
 
     static Logger loggerReference = null;
 
     public static final String LOGGER_NAME = "edu.stanford.cfuller.imageanalysistools";
 
     static {
         addedHandlers = new java.util.Vector<Handler>();
         loggerReference = java.util.logging.Logger.getLogger(LOGGER_NAME);
     }
 
     /**
      * Modifies the logging detail level of the handlers associated with this package.
      * @param l     The new level of logging detail.
      */
     protected static void setHandlerLevels(Level l) {
         for (Handler h : addedHandlers) {h.setLevel(l);}
     }
 
     /**
      * Adds a handler that will log all messages to a given Output Stream.
      * @param o     The OutputStream to which to log.
      */
     public static void logToStream(OutputStream o) {
 
         StreamHandler sh = new StreamHandler(o, new SimpleFormatter());
         addedHandlers.add(sh);
 
         getLogger().addHandler(sh);
         
     }
 
     /**
      * Adds a handler to the logger for this package.
      * @param h The handler to add.
      */
     public static void addHandler(Handler h) {
 
        addedHandlers.add(h);
        getLogger().addHandler(h);
 
         
     }
 
     /**
      * Causes all messages to be logged.
      */
     public static void logDebugMessages() {
 
         getLogger().setLevel(Level.ALL);
         setHandlerLevels(Level.ALL);
     }
 
     /**
      * Causes only informational messages and errors/warnings to be logged.
      */
     public static void hideDebugMessages() {
 
         getLogger().setLevel(Level.INFO);
         setHandlerLevels(Level.INFO);
 
 
     }
 
     /**
      * Causes informational and debugging messages to be hidden.
      */
     public static void logWarningsAndErrorsOnly() {
 
         getLogger().setLevel(Level.WARNING);
         setHandlerLevels(Level.WARNING);
 
     }
 
     /**
      * Remove all log handlers that have been added.
      */
     public static void removeAllAddedLoggingStreams() {
 
         for (Handler h : addedHandlers) {
 
             getLogger().removeHandler(h);
 
             
         }
         
         addedHandlers.clear();
 
     }
 
 
     /**
      * Get a reference to the underlying logger used by this class.
      * @return  A reference to the logger.
      */
     public static Logger getLogger() {
         return java.util.logging.Logger.getLogger(LOGGER_NAME);
     }
 
 
 }

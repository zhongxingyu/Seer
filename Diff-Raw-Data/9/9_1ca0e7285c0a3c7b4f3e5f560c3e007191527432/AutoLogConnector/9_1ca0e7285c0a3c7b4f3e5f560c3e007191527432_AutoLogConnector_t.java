 /**
  * Copyright (C) 2012 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.logger.helper;
 
 import com.crashnote.core.Lifecycle;
 import com.crashnote.core.model.excp.CrashnoteException;
 import com.crashnote.jul.impl.JulConnector;
 import com.crashnote.log4j.impl.Log4jConnector;
 import com.crashnote.logback.impl.LogbackConnector;
 import com.crashnote.logger.config.LoggerConfig;
 import com.crashnote.logger.report.LoggerReporter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Utility class to connect/attach log appenders to an according logging framework(s).
  */
 public class AutoLogConnector
     implements Lifecycle {
 
     // VARS =======================================================================================
 
     private boolean started;
 
     private final List<LogConnector<LoggerConfig, ?>> connectors;
 
 
     // SETUP ======================================================================================
 
     public AutoLogConnector(final LoggerConfig config, final LoggerReporter reporter) {
 
         connectors = new ArrayList<LogConnector<LoggerConfig, ?>>(3);
 
        @SuppressWarnings({"unchecked", "rawtypes"})
         final Class<LogConnector>[] connectorsSrc =
             new Class[]{LogbackConnector.class, Log4jConnector.class, JulConnector.class};
 
         for (final Class<LogConnector> cls : connectorsSrc) {
             connect(cls, config, reporter);
         }
     }
 
 
     // LIFECYCLE ==================================================================================
 
     @Override
     public boolean start() {
         if (!started) {
             started = true;
 
             // make sure there is at least one connector
             if (connectors.isEmpty())
                 throw new CrashnoteException("unable to start: no log connector could be initialized");
         }
 
         return started;
     }
 
     @Override
     public boolean stop() {
         if (started) {
             started = false;
 
             // stop each connector one by one
             for (final LogConnector<LoggerConfig, ?> c : connectors)
                 c.stop();
         }
 
         return started;
     }
 
 
     // INTERNAL ===================================================================================
 
     private void connect(final Class<LogConnector> cls,
                          final LoggerConfig config, final LoggerReporter reporter) {
         try {
             // instantiate
             @SuppressWarnings("unchecked")
             final LogConnector<LoggerConfig, ?> c = cls.newInstance();
             try {
                 // start-up
                 c.start(config, reporter);
                 connectors.add(c);
             } catch (Throwable e) {
                 c.err("unable to connect to logger", e);
             }
         } catch (Throwable e) {
             // ignore
         }
     }
 
 
     // GET ========================================================================================
 
     public List<LogConnector<LoggerConfig, ?>> getConnectors() {
         return connectors;
     }
 }

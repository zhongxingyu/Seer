 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.medic.log.impl.logback;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.virgo.medic.log.LoggingConfiguration;
 import org.xml.sax.InputSource;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import ch.qos.logback.core.status.Status;
 

 public final class JoranLoggerContextConfigurer implements LoggerContextConfigurer {
 
     public void applyConfiguration(LoggingConfiguration configuration, LoggerContext loggerContext) throws LoggerContextConfigurationFailedException {
         JoranConfigurator configurator = new JoranConfigurator();
         configurator.setContext(loggerContext);
         String configurationString = configuration.getConfiguration();
         try {
             configurator.doConfigure(new InputSource(new StringReader(configurationString)));
             List<Status> statusList = loggerContext.getStatusManager().getCopyOfStatusList();
             List<String> failureMessages = new ArrayList<String>();
             for (Status status : statusList) {
                 if (Status.INFO != status.getLevel()) {
                    failureMessages.add(status.getMessage());
                 }
             }
             reportFailureIfNecessary(failureMessages);
         } catch (JoranException je) {
             throw new LoggerContextConfigurationFailedException("Configuration failed", je);
         }
     }
 
     private static void reportFailureIfNecessary(List<String> failureMessages) throws LoggerContextConfigurationFailedException {
         if (!failureMessages.isEmpty()) {
             throw new LoggerContextConfigurationFailedException("Configuration failed with the following problems: " + failureMessages);
         }
     }
 }

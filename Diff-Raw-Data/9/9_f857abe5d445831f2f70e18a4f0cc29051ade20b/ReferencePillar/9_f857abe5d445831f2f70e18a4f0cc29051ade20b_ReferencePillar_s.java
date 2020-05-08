 /*
  * #%L
  * Bitmagasin integrationstest
  * 
  * $Id: ReferencePillar.java 685 2012-01-06 16:35:17Z jolf $
  * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/ReferencePillar.java $
  * %%
  * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.pillar.referencepillar;
 
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.database.DBConnector;
 import org.bitrepository.common.database.DBSpecifics;
 import org.bitrepository.common.database.DatabaseSpecificsFactory;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.pillar.Pillar;
 import org.bitrepository.pillar.common.PillarAlarmDispatcher;
 import org.bitrepository.pillar.common.PillarContext;
 import org.bitrepository.pillar.referencepillar.archive.ReferenceArchive;
 import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.bitrepository.service.audit.AuditTrailContributerDAO;
 import org.bitrepository.service.audit.AuditTrailManager;
 import org.bitrepository.service.contributor.ContributorContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.jms.JMSException;
 
 /**
  * Reference pillar. This very simply starts the PillarMediator, which handles all the communications.
  */
 public class ReferencePillar implements Pillar {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The messagebus for the pillar.*/
     private final MessageBus messageBus;
     /** The mediator for the messages.*/
     private final ReferencePillarMediator mediator;
     /** The archive for the data.*/
     private final ReferenceArchive archive;
 
     /**
      * Constructor.
      * @param messageBus The messagebus for the communication.
      * @param settings The settings for the pillar.
      */
     public ReferencePillar(MessageBus messageBus, Settings settings) {
         ArgumentValidator.checkNotNull(messageBus, "messageBus");
         ArgumentValidator.checkNotNull(settings, "settings");
         
         this.messageBus = messageBus;
         
         log.info("Starting the reference pillar!");
         
         archive = new ReferenceArchive(settings.getReferenceSettings().getPillarSettings().getFileDir());
         DBSpecifics dbSpecifics = DatabaseSpecificsFactory.retrieveDBSpecifics(
                settings.getReferenceSettings().getPillarSettings().getAuditContributerDatabaseUrl());
         AuditTrailManager audits = new AuditTrailContributerDAO(settings, new DBConnector(dbSpecifics, 
                 settings.getReferenceSettings().getPillarSettings().getAuditContributerDatabaseUrl()));
         ContributorContext contributorContext = new ContributorContext(messageBus, settings);
         PillarAlarmDispatcher alarms = new PillarAlarmDispatcher(contributorContext);
         PillarContext context = new PillarContext(settings, messageBus, alarms, audits);
         mediator = new ReferencePillarMediator(context, archive);
         mediator.start();
         log.info("ReferencePillar started!");
     }
     
     /**
      * Closes the ReferencePillar.
      */
     public void close() {
         try {
             mediator.close();
             messageBus.close();
             archive.close();
         } catch (JMSException e) {
             log.warn("Could not close the messagebus.", e);
         }
     }
 }

 /*
  * #%L
  * bitrepository-access-client
  * *
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
 package org.bitrepository.pillar.messagehandler;
 
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.bitrepository.bitrepositoryelements.AlarmConcerning;
 import org.bitrepository.bitrepositoryelements.AlarmConcerning.BitRepositoryCollections;
 import org.bitrepository.bitrepositoryelements.AlarmConcerning.Components;
 import org.bitrepository.bitrepositoryelements.AlarmDescription;
import org.bitrepository.bitrepositoryelements.AlarmcodeType;
 import org.bitrepository.bitrepositoryelements.ComponentTYPE;
 import org.bitrepository.bitrepositoryelements.ComponentTYPE.ComponentType;
 import org.bitrepository.bitrepositoryelements.PriorityCodeType;
 import org.bitrepository.bitrepositoryelements.RiskAreaType;
 import org.bitrepository.bitrepositoryelements.RiskImpactScoreType;
 import org.bitrepository.bitrepositoryelements.RiskProbabilityScoreType;
 import org.bitrepository.bitrepositoryelements.RiskTYPE;
 import org.bitrepository.bitrepositorymessages.Alarm;
 import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
 import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
 import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
 import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
 import org.bitrepository.bitrepositorymessages.GetStatusRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
 import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
 import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
 import org.bitrepository.bitrepositorymessages.PutFileRequest;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.pillar.PillarSettings;
 import org.bitrepository.pillar.ReferenceArchive;
 import org.bitrepository.pillar.ReferencePillarMessageFactory;
 import org.bitrepository.pillar.audit.MemorybasedAuditTrailManager;
 import org.bitrepository.protocol.ProtocolConstants;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.bitrepository.protocol.messagebus.MessageListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This instance handles the conversations for the reference pillar.
  * It only responds to requests. It does not it self start conversations, though it might send Alarms when something 
  * is not right.
  * All other messages than requests are considered garbage.
  * Every message (even garbage) is put into the audit trails.
  */
 public class PillarMediator implements MessageListener {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
 
     /** The settings.*/
     final PillarSettings settings;
     /** The messagebus. Package protected on purpose.*/
     final MessageBus messagebus;
     /** The archive. Package protected on purpose.*/
     final ReferenceArchive archive;
     /** The message factory. Package protected on purpose.*/
     final ReferencePillarMessageFactory msgFactory;
     /** The handler of the audits. Package protected on purpose.*/
     private final MemorybasedAuditTrailManager audits;
 
     // THE MESSAGE HANDLERS!
     private Map<String, PillarMessageHandler> handlers = new HashMap<String, PillarMessageHandler>();
 
     /**
      * Constructor.
      * Sets the parameters of this mediator, and adds itself as a listener to the destinations.
      * 
      * @param messagebus The messagebus for this instance.
      * @param pSettings The settings for the reference pillar.
      * @param refArchive The archive for the reference pillar.
      * @param messageFactory The message factory.
      */
     public PillarMediator(MessageBus messagebus, PillarSettings pSettings, 
             ReferenceArchive refArchive, ReferencePillarMessageFactory messageFactory) {
         this.messagebus = messagebus;
         this.archive = refArchive;
         this.msgFactory = messageFactory;
         this.settings = pSettings;
         this.audits = new MemorybasedAuditTrailManager();
 
         // Initialise the messagehandlers.
         this.handlers.put(IdentifyPillarsForGetFileRequest.class.getName(), new GetFileIdentificationMessageHandler(this));
         this.handlers.put(GetFileRequest.class.getName(), new GetFileMessageHandler(this));
         this.handlers.put(IdentifyPillarsForGetFileIDsRequest.class.getName(), new GetFileIDsIdentificationMessageHandler(this));
         this.handlers.put(GetFileIDsRequest.class.getName(), new GetFileIDsMessageHandler(this));
         this.handlers.put(IdentifyPillarsForGetChecksumsRequest.class.getName(), new GetChecksumsIdentificationMessageHandler(this));
         this.handlers.put(GetChecksumsRequest.class.getName(), new GetChecksumsMessageHandler(this));
         
         this.handlers.put(IdentifyPillarsForPutFileRequest.class.getName(), new PutFileIdentificationMessageHandler(this));
         this.handlers.put(PutFileRequest.class.getName(), new PutFileMessageHandler(this));
 
         // add to both the general topic and the local queue.
         messagebus.addListener(settings.getBitRepositoryCollectionTopicID(), this);
         messagebus.addListener(settings.getLocalQueue(), this);
     }
 
     public void handleException(Exception e) {
         // TODO ?? send alarm?
         log.error("Received exception '" + e.getMessage() + "'.", e);
     }
     
     /**
      * Method for sending an Alarm when something bad happens.
      * @param alarmConcerning What the alarm is concerning.
      * @param alarmDescription The description of the alarm, e.g. What caused the alarm.
      */
     public void sendAlarm(AlarmConcerning alarmConcerning, AlarmDescription alarmDescription) {
         log.warn("Sending alarm, concerning: '" + alarmConcerning + "', with description: '" + alarmDescription + "'");
         
         Alarm alarm = new Alarm();
         
         ComponentTYPE ct = new ComponentTYPE();
         ct.setComponentComment("ReferencePillar");
         ct.setComponentID(settings.getPillarId());
         ct.setComponentType(ComponentType.PILLAR);
         alarm.setAlarmRaiser(ct);
         
         alarm.setAlarmConcerning(alarmConcerning);
         alarm.setAlarmDescription(alarmDescription);
         
         alarm.setAuditTrailInformation("ReferencePillar: " + settings.getPillarId());
         alarm.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
         alarm.setCorrelationID(UUID.randomUUID().toString());
         alarm.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
         alarm.setReplyTo(settings.getLocalQueue());
         alarm.setTo(settings.getBitRepositoryCollectionTopicID() + "-ALARM");
         alarm.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
         
         messagebus.sendMessage(alarm);
     }
     
     /**
      * Method for sending an alarm when a received message does not have a handler.
      * 
      * @param message The message which does not have a handler.
      */
     private void noHandlerAlarm(Object message) {
         String msg = "Cannot handle message of type '" + message.getClass().getCanonicalName() + "'";
         log.warn(msg + ": " + message.toString());
         
         // create the Concerning part of the alarm.
         AlarmConcerning ac = new AlarmConcerning();
         BitRepositoryCollections brcs = new BitRepositoryCollections();
         brcs.getBitRepositoryCollectionID().add(settings.getBitRepositoryCollectionID());
         ac.setBitRepositoryCollections(brcs);
         ac.setMessages(msg);
         ac.setFileInformation(null);
         Components comps = new Components();
         ComponentTYPE compType = new ComponentTYPE();
         compType.setComponentComment("ReferencePillar");
         compType.setComponentID(settings.getPillarId());
         compType.setComponentType(ComponentType.PILLAR);
         comps.getContributor().add(compType);
         comps.getDataTransmission().add(settings.getMessageBusConfiguration().toString());
         ac.setComponents(comps);
         
         // create a descriptor.
         AlarmDescription ad = new AlarmDescription();
        ad.setAlarmCode(AlarmcodeType.GENERAL);
         ad.setAlarmText(msg);
         ad.setOrigDateTime(CalendarUtils.getXmlGregorianCalendar(new Date()));
         ad.setPriority(PriorityCodeType.OTHER);
         RiskTYPE rt = new RiskTYPE();
         rt.setRiskArea(RiskAreaType.AVAILABILITY);
         rt.setRiskImpactScore(RiskImpactScoreType.HIGH_IMPACT);
         rt.setRiskProbabilityScore(RiskProbabilityScoreType.MEDIUM_PROPABILITY);
         ad.setRisk(rt);
         
         sendAlarm(ac, ad);
     }
     
     @Override
     public void onMessage(Alarm message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetAuditTrailsRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<GetAuditTrailsRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message);
         }
     }
 
     @Override
     public void onMessage(GetAuditTrailsProgressResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetAuditTrailsFinalResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetChecksumsFinalResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetChecksumsRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<GetChecksumsRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message);
         }
     }
 
     @Override
     public void onMessage(GetChecksumsProgressResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetFileFinalResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetFileIDsFinalResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetFileIDsRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<GetFileIDsRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message);
         }    
     }
 
     @Override
     public void onMessage(GetFileIDsProgressResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetFileRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<GetFileRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message);
         }    
     }
 
     @Override
     public void onMessage(GetFileProgressResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetStatusRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<GetStatusRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message);
         }    
     }
 
     @Override
     public void onMessage(GetStatusProgressResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(GetStatusFinalResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(IdentifyPillarsForGetChecksumsResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(IdentifyPillarsForGetChecksumsRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<IdentifyPillarsForGetChecksumsRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message.getClass());
         }    
     }
 
     @Override
     public void onMessage(IdentifyPillarsForGetFileIDsResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<IdentifyPillarsForGetFileIDsRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message.getClass());
         }    
     }
 
     @Override
     public void onMessage(IdentifyPillarsForGetFileResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(IdentifyPillarsForGetFileRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<IdentifyPillarsForGetFileRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message.getClass());
         }    
     }
 
     @Override
     public void onMessage(IdentifyPillarsForPutFileResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(IdentifyPillarsForPutFileRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<IdentifyPillarsForPutFileRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message.getClass());
         }
     }
 
     @Override
     public void onMessage(PutFileFinalResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 
     @Override
     public void onMessage(PutFileRequest message) {
         log.info("Received: " + message);
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
 
         PillarMessageHandler<PutFileRequest> handler = handlers.get(message.getClass().getName());
         if(handler != null) {
             handler.handleMessage(message);
         } else {
             noHandlerAlarm(message.getClass());
         }
     }
 
     @Override
     public void onMessage(PutFileProgressResponse message) {
         audits.addMessageReceivedAudit("Received: " + message.getClass() + " : " + message.getAuditTrailInformation());
         noHandlerAlarm(message);
     }
 }

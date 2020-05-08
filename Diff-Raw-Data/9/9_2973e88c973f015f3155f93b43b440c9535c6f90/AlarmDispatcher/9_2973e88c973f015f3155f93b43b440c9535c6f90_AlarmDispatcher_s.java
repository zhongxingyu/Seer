 /*
  * #%L
  * Bitrepository Integration
  * 
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
 package org.bitrepository.pillar;
 
import java.math.BigInteger;
import java.util.UUID;

 import org.bitrepository.bitrepositoryelements.Alarm;
 import org.bitrepository.bitrepositoryelements.AlarmCode;
 import org.bitrepository.bitrepositorymessages.AlarmMessage;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.protocol.ProtocolConstants;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.bitrepository.settings.collectionsettings.AlarmLevel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 /**
  * The class for dispatching alarms.
  */
 public class AlarmDispatcher {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
 
     /** The settings for this AlarmDispatcher.*/
     private final Settings settings;
     
     /** The messagebus for communication.*/
     private final MessageBus messageBus;
     
     /**
      * Constructor.
      * @param settings The settings for the dispatcher.
      * @param messageBus The bus for sending the alarms.
      */
     public AlarmDispatcher(Settings settings, MessageBus messageBus) {
         ArgumentValidator.checkNotNull(settings, "settings");
         ArgumentValidator.checkNotNull(messageBus, "messageBus");
         
         this.settings = settings;
         this.messageBus = messageBus;
     }
     
     /**
      * Method for sending an alarm based on an IllegalArgumentException.
      * Is only send if the alarm level is 'WARNING', otherwise the exception is just logged.
      * @param exception The exception to base the alarm upon.
      */
     public void handleIllegalArgumentException(IllegalArgumentException exception) {
         ArgumentValidator.checkNotNull(exception, "IllegalArgumentException exception");
         if(settings.getCollectionSettings().getPillarSettings().getAlarmLevel() != AlarmLevel.WARNING) {
             log.warn("IllegalArgumentException caught, but we do not issue alarms for this, when the alarm level is '"
                     + settings.getCollectionSettings().getPillarSettings().getAlarmLevel() + "'", exception);
             return;
         }
         
         // create a descriptor.
         Alarm ad = new Alarm();
         ad.setAlarmCode(AlarmCode.INCONSISTENT_REQUEST);
         ad.setAlarmText(exception.getMessage());
         
         sendAlarm(ad);
     }
     
     /**
      * Sends an alarm for a RuntimeException. Such exceptions are sent unless the AlarmLevel is 'EMERGENCY',
      * otherwise the exception is just logged.
      * @param exception The exception causing the alarm.
      */
     public void handleRuntimeExceptions(RuntimeException exception) {
         ArgumentValidator.checkNotNull(exception, "RuntimeException exception");
         if(settings.getCollectionSettings().getPillarSettings().getAlarmLevel() == AlarmLevel.EMERGENCY) {
             log.error("RuntimeException caught, but we do not issue alarms for this, when the alarm level is '"
                     + settings.getCollectionSettings().getPillarSettings().getAlarmLevel() + "'", exception);
             return;
         }
         
         log.error("Sending alarm for RunTimeException", exception);
         
         // create a descriptor.
         Alarm alarm = new Alarm();
         alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
         alarm.setAlarmText(exception.getMessage());
         alarm.setAlarmRaiser(settings.getReferenceSettings().getPillarSettings().getPillarID());
         
         sendAlarm(alarm);
     }
     
     /**
      * Method for creating and sending an Alarm about the checksum being invalid.
      * @param fileId The id of the file, which has an invalid checksum.
      * @param alarmText The test for the alarm message.
      */
     public void sendInvalidChecksumAlarm(String fileId, String alarmText) {
         log.warn("Sending invalid checksum for the file '" + fileId + "' with the message: " + alarmText);
         Alarm alarm = new Alarm();
         alarm.setAlarmText(alarmText);
         alarm.setAlarmCode(AlarmCode.CHECKSUM_ALARM);
         alarm.setFileID(fileId);
         alarm.setOrigDateTime(CalendarUtils.getNow());
         sendAlarm(alarm);
     }
     
     /**
      * Method for sending an Alarm when something bad happens.
      * @param alarm The alarm to send to the destination for the alarm service.
      */
     public void sendAlarm(Alarm alarm) {
         ArgumentValidator.checkNotNull(alarm, "alarm");
         AlarmMessage message = new AlarmMessage();
         alarm.setAlarmRaiser(settings.getReferenceSettings().getPillarSettings().getPillarID());
         alarm.setOrigDateTime(CalendarUtils.getNow());
 
         message.setAlarm(alarm);
         message.setCollectionID(settings.getCollectionID());
         message.setCorrelationID(UUID.randomUUID().toString());
         message.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
         message.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
         message.setTo(settings.getAlarmDestination());
         message.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
         
         messageBus.sendMessage(message);
     }
 }

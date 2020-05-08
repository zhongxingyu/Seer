 /*
  * #%L
  * bitrepository-access-client
  * *
  * $Id: AccessMessageHandler.java 249 2011-08-02 11:05:51Z mss $
  * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-integration/src/main/java/org/bitrepository/pillar/messagehandler/AccessMessageHandler.java $
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
 
 import java.util.Date;
 
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
 import org.bitrepository.common.utils.CalendarUtils;
 
 /**
  * Abstract level for message handling. 
  */
 public abstract class PillarMessageHandler<T> {
 
     /** The mediator.*/
     protected final PillarMediator mediator;
     
     /**
      * Constructor. Only accessible by inheritors of this interface.
      * @param mediator
      */
     protected PillarMessageHandler(PillarMediator mediator) {
         this.mediator = mediator;
     }
     
     abstract void handleMessage(T message);
     
     /**
      * Validates that it is the correct BitrepositoryCollectionId.
      * @param bitrepositoryCollectionId The collection id to validate.
      */
     protected void validateBitrepositoryCollectionId(String bitrepositoryCollectionId) {
         if(!bitrepositoryCollectionId.equals(mediator.settings.getBitRepositoryCollectionID())) {
             throw new IllegalArgumentException("The message had a wrong BitRepositoryIdCollection: "
                     + "Expected '" + mediator.settings.getBitRepositoryCollectionID() + "' but was '" 
                     + bitrepositoryCollectionId + "'.");
         }
     }
 
     /**
      * Validates that it is the correct pillar id.
      * @param pillarId The pillar id.
      */
     protected void validatePillarId(String pillarId) {
         if(!pillarId.equals(mediator.settings.getPillarId())) {
             throw new IllegalArgumentException("The message had a wrong PillarId: "
                     + "Expected '" + mediator.settings.getPillarId() + "' but was '" 
                     + pillarId + "'.");
         }
     }
     
     /**
      * Method for sending an alarm based on an IllegalArgumentException.
      * @param exception The exception to base the alarm upon.
      */
     public void alarmIllegalArgument(IllegalArgumentException exception) {
         // create the Concerning part of the alarm.
         AlarmConcerning ac = new AlarmConcerning();
         BitRepositoryCollections brcs = new BitRepositoryCollections();
         brcs.getBitRepositoryCollectionID().add(mediator.settings.getBitRepositoryCollectionID());
         ac.setBitRepositoryCollections(brcs);
         ac.setMessages(exception.getMessage());
         ac.setFileInformation(null);
         Components comps = new Components();
         ComponentTYPE compType = new ComponentTYPE();
         compType.setComponentComment("ReferencePillar");
         compType.setComponentID(mediator.settings.getPillarId());
         compType.setComponentType(ComponentType.PILLAR);
         comps.getContributor().add(compType);
         comps.getDataTransmission().add(mediator.settings.getMessageBusConfiguration().toString());
         ac.setComponents(comps);
         
         // create a descriptor.
         AlarmDescription ad = new AlarmDescription();
        ad.setAlarmCode(AlarmcodeType.UNKNOWN_USER);
         ad.setAlarmText(exception.getMessage());
         ad.setOrigDateTime(CalendarUtils.getXmlGregorianCalendar(new Date()));
         ad.setPriority(PriorityCodeType.OTHER);
         RiskTYPE rt = new RiskTYPE();
         rt.setRiskArea(RiskAreaType.CONFIDENTIALITY);
         rt.setRiskImpactScore(RiskImpactScoreType.CRITICAL_IMPACT);
         rt.setRiskProbabilityScore(RiskProbabilityScoreType.HIGH_PROPABILITY);
         ad.setRisk(rt);
         
         mediator.sendAlarm(ac, ad);
     }
 }

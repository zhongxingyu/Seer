 /*
  * #%L
  * Bitmagasin integrationstest
  * 
  * $Id$
  * $HeadURL$
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
 package org.bitrepository.access;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bitrepository.access.exception.AccessException;
 import org.bitrepository.bitrepositorymessages.GetFileComplete;
 import org.bitrepository.bitrepositorymessages.GetFileRequest;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileReply;
 import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
 import org.bitrepository.protocol.MessageBus;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.bitrepository.protocol.http.HTTPFileExchange;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The client for sending and handling 'Get' messages.
  * 
  * TODO It would be good manner to replace the maps with a database.
  * 
  * @author jolf
  */
 public class GetClient {
     /** The log for this instance.*/
     private Logger log = LoggerFactory.getLogger(GetClient.class);
     /** The connection to the message bus.*/
     private MessageBus messageBus;
     /** The queue to talk. TODO replace with a configuration.*/
     String queue;
     /** The GetClientServer, which receives the messages.*/
     private GetClientServer server;
     /** The directory where the retrieved files should be placed.*/
     File fileDir;
     
     /** Container for the missing replies for fastest content.
      * Maps between file and the a list of missing pillars.*/
     private Map<String, List<String>> outstandingReplyForFastest = 
         Collections.synchronizedMap(new HashMap<String, List<String>>());
     /** Container for the time to deliver files for the pillars.*/
     private Map<String, Map<String, Long>> timeForFastest =
         Collections.synchronizedMap(new HashMap<String, Map<String, Long>>());
     /** The list of fileids awaiting to be retrieved.*/
     private List<String> awatingFileIds = Collections.synchronizedList(
             new ArrayList<String>());
     
     /**
      * Constructor.
      * @throws Exception If a connection to the messagebus could not be 
      * established.
      */
     public GetClient() throws Exception {
         log.info("Initialising the GetClient");
         // TODO
         // Load settings!
         // Establish connection to bus!
         
         server = new GetClientServer(this);
         fileDir = new File("fileDir");
         if(fileDir.isFile()) {
             throw new AccessException("The file directory '" 
                     + fileDir.getAbsolutePath() + "' already exists as a file, "
                     + "and not as a directory, which is required.");
         }
         if(!fileDir.exists() || !fileDir.isDirectory()) {
             fileDir.mkdirs();
             if(!fileDir.isDirectory()) {
                 throw new AccessException("The file directory '" 
                         + fileDir.getAbsolutePath() + "' is cannot be "
                         + "instantiated as a directory.");
             }
         }
         
         // TODO use a settings. Temporarily use the current time.
         queue = "" + (new Date().getTime());
         messageBus = ProtocolComponentFactory.getInstance().getMessageBus();
         messageBus.addListener(queue, server);
     }
     
     /**
      * Retrieve a given file as fast as possible.
      * Starts by requesting the time for delivery of the file to the relevant
      * pillars. When these pillars have answered, the fastest (lowest 
      * delivery time) is chosen and a request for delivery is sent.
      * 
      * TODO handle the case when a given pillar does not answer within a 
      * reasonable time frame. 
      * 
      * @param fileId The id of the data to retrieve fastest.
      * @param slaId The id of the SLA to which the file belongs.
      * @param pillars The list of pillars which contains the file.
      * @throws Exception If the message cannot be sent.
      */
     public void getFileFastest(String fileId, String slaId, String... pillars) 
             throws Exception {
         // validate arguments
         if(fileId == null || fileId.isEmpty()) {
             throw new IllegalArgumentException("The String fileId may not be "
                     + "null or the empty string.");
         }
         if(slaId == null || slaId.isEmpty()) {
             throw new IllegalArgumentException("The String slaId may not be "
                     + "null or the empty string.");
         }
         if(pillars == null || pillars.length == 0) {
             throw new IllegalArgumentException("The String... pillars may not "
                     + "be null or a empty collection of strings.");
         }
 
         log.info("Requesting fastest retrieval of the file '" + fileId 
                 + "' which belong to the SLA '" + slaId + "'.");
         // create message requesting delivery time for the given file.
         IdentifyPillarsForGetFileRequest msg = new IdentifyPillarsForGetFileRequest();
         msg.setMinVersion((short) 1);
         msg.setVersion((short) 1);
         msg.setFileID(fileId);
         msg.setCorrelationID("TheCorrelationID_FOR_THIS_MESSAGE");
         msg.setSlaID(slaId);
         msg.setReplyTo(queue);
         
         // send the message.
         messageBus.sendMessage(queue, msg);
         
         // make a list with all the pillars to request for fastest delivery 
         // of this file. Set them to outstanding.
         List<String> repliers = new ArrayList<String>();
         for(String pillar : pillars) {
             repliers.add(pillar);
         }
         outstandingReplyForFastest.put(fileId, repliers);
     }
     
     /**
      * Method for sending a get file request to a specific pillar.
      * 
      * TODO where to retrieve the SLA id?
      * 
      * @param fileId The id for the file to retrieve.
      * @param pillarId The pillar where the file should be retrieved.
      */
     public void getFile(String fileId, String pillarId) {
         log.info("Requesting the file '" + fileId + "' from pillar '"
                 + pillarId + "'.");
         try {
             URL url = HTTPFileExchange.getURL(fileId);
             GetFileRequest msg = new GetFileRequest();
 //            msg.setSlaID("??");
             msg.setFileAddress(url.toExternalForm());
             msg.setFileID(fileId);
             msg.setPillarID(pillarId);
             msg.setReplyTo(queue);
             msg.setMinVersion((short) 1);
             msg.setVersion((short) 1);
             messageBus.sendMessage(queue, msg);
             
             // set the file to be awaiting retrieval.
             awatingFileIds.add(fileId);
         } catch (Exception e) {
             throw new AccessException("Problems sending a request for "
                     + "retrieving a specific file.", e);
         }
     }
     
     /**
      * Handles a reply for identifying the fastest pillar to deliver a given
      * file.
      * Removes the pillar responsible for the reply from the outstanding list
      * for the file in the reply. If no more pillars are outstanding, then the 
      * file is requested from fastest pillar.
      * 
      * @param reply The IdentifyPillarsForGetFileReply to handle.
      */
     public void handleReplyForFastest(IdentifyPillarsForGetFileReply reply) {
         // validate arguments
         if(reply == null) {
             throw new IllegalArgumentException("The "
                     + "IdentifyPillarsForGetFileReply reply may not be null.");
         }
         
         String fileId = reply.getFileID();
         if(!outstandingReplyForFastest.containsKey(fileId)) {
             // TODO decide whether to handle differently
             log.debug("Received reply for fastest delivery of file '" + fileId 
                     + "', but is not missing this file. Message ignored!");
             return;
         }
         log.debug("Received reply for fastest delivery of file '" + fileId 
                 + "': " + reply);
         List<String> missingRepliers = outstandingReplyForFastest.get(fileId);
         String pillarId = reply.getPillarID();
         // ensure that we actually expects to receive this reply.
         if(!missingRepliers.contains(pillarId)) {
             // TODO decide whether to handle differently. 
             // Perhaps it should stop instead? Or compare with previous value?
             log.info("The reply for fastest delivery of file '" + fileId 
                     + "' was by pillar '" + pillarId + "', which was not "
                     + "outstanding. Handling anyway.");
         }
         
        // save the time entry.
         saveTimeForFileDelivery(pillarId, fileId, 
                Long.parseLong((String) reply.getTimeToDeliver()));
         
         // remove pillar from outstanding.
         missingRepliers.remove(pillarId);
         
         // check if more pillars are missing, otherwise find fastest.
         if(missingRepliers.isEmpty()) {
             outstandingReplyForFastest.remove(fileId);
             requestFastestFileDelivery(fileId);
         } else {
             log.debug("Awaiting " + missingRepliers.size() + " pillars "
                     + "for file '" + fileId + "' before the fastest will be "
                     + "requested.");
             outstandingReplyForFastest.put(fileId, missingRepliers);
         }
     }
     
     /**
      * Method for completing the get.
      * 
      * @param msg The GetFileCompleteMessage.
      */
     public void completeGet(GetFileComplete msg) {
         if(msg == null) {
             throw new IllegalArgumentException("Cannot handle a null as "
                     +"GetFileComplete.");
         }
         
         String fileId = msg.getFileID();
         if(!awatingFileIds.contains(fileId)) {
             // not for me.
             log.debug("GetFileComplete for '" + fileId + "', but it is not "
                     + "awaited by me. Ignoring message.");
             return;
         }
         
         try {
             log.info("Downloading the file '" + fileId + "' from '" 
                     + msg.getFileAddress() + "'.");
             
             URL url = new URL(msg.getFileAddress());
             
             // TODO verify that this file is unique and does not exist yet.
             // Else handle the scenario.
             File outputFile = new File(fileDir, fileId);
             
             FileOutputStream outStream = null;
             try {
                 // download the file.
                 outStream = new FileOutputStream(outputFile);
                 HTTPFileExchange.downloadFromServer(outStream, url);
                 outStream.flush();
             } finally {
                 if(outStream != null) {
                     outStream.close();
                 }
             }
         } catch (Exception e) {
             throw new AccessException("Problems with retrieving the file '" 
                     + fileId + "'.", e);
         }
         
         // remove from list when download is completed.
         awatingFileIds.remove(fileId);
     }
     
     /**
      * Stores the time to deliver a specific file for a given pillar.
      * 
      * @param pillarId The id for the pillar.
      * @param fileId The id for the file.
      * @param time The time in milliseconds.
      */
     private synchronized void saveTimeForFileDelivery(String pillarId, 
             String fileId, Long time) {
         log.debug("Time for file delivery: It takes '" + time 
                 + "' milliseconds for pillar '" + pillarId + "' for file '"
                 + fileId + "'.");
         
         // Container for the time for each pillar. Maps pillarId to time. 
         Map<String, Long> deliveryTimes;
         
         // If an entry for the fileId already exists, then retrieve it and 
         // update it with the newest information. Otherwise create new map.
         if(timeForFastest.containsKey(fileId)) {
             deliveryTimes = timeForFastest.get(fileId);
         } else {
             deliveryTimes = new HashMap<String, Long>();
         }
         
         // make new entry (or update existing one) for this pillarId and time.
         deliveryTimes.put(pillarId, time);
         
         // (Re)Insert the map for this specific fileId.
         timeForFastest.put(fileId, deliveryTimes);
     }
     
     /**
      * Finds the fastest pillar for delivering a specific file and sends a
      * request for the file. 
      * @param fileId The file to retrieve as fast as possible.
      */
     private void requestFastestFileDelivery(String fileId) {
         log.debug("Requesting the fastest delivery of the file '" + fileId 
                 + "'.");
         String pillarId = getFastestPillarForFile(fileId);
         getFile(fileId, pillarId);
     }
     
     /**
      * Find the pillar which can deliver a given file fastest.
      * 
      * @param fileId The id of the file to retrieve.
      * @return The id for the pillar which can deliver the file fastest.
      */
     private String getFastestPillarForFile(String fileId) {
         // TODO handle case when the fileId cannot be found!
         Map<String, Long> deliveryTimes = timeForFastest.get(fileId);
         
         String pillar = "";
         Long minTime = Long.MAX_VALUE;
         for(Map.Entry<String, Long> entry : deliveryTimes.entrySet()) {
             // ignore invalid values (zero or less.)
             if(entry.getValue() <= 0) {
                 continue;
             }
             // If this entry is faster, then set as fastest.
             if(minTime > entry.getValue()) {
                 pillar = entry.getKey();
                 minTime = entry.getValue();
             }
         }
         
         log.debug("The pillar '" + pillar + "' can deliver the file '" 
                 + fileId + "' fastest, which is in '" + minTime 
                 + "' milliseconds.");
         
         return pillar;
     }
 }

 /* $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  * The Netarchive Suite - Software to harvest and preserve websites
  * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
  *   USA
  */
 package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.ObjectStatus;
 import dk.statsbiblioteket.doms.radiotv.extractor.ObjectStatusEnum;
 import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
 
 import javax.servlet.ServletConfig;
 import java.io.UnsupportedEncodingException;
 import java.io.File;
 
 import org.apache.log4j.Logger;
 
 public class FlashStatusExtractor {
 
     private static Logger log = Logger.getLogger(FlashStatusExtractor.class);
 
 
     public static ObjectStatus getStatus(String shardUrl, ServletConfig config) throws UnsupportedEncodingException, ProcessorException {
         String uuid = Util.getUuid(shardUrl);
         if (uuid == null) throw new IllegalArgumentException("Invalid url - no uuid found: '" + shardUrl + "'");
         TranscodeRequest request = new TranscodeRequest(uuid);
         boolean isDone = Util.getFlashFile(request, config).exists() && !ClipStatus.getInstance().isKnown(uuid);
         if (isDone) {
             log.debug("Found already fully ready result for '" + uuid + "'");
             ObjectStatus status = new ObjectStatus();
             status.setStatus(ObjectStatusEnum.DONE);
             status.setStreamId("flv:" + Util.getFlashFile(request, config).getName());
             status.setServiceUrl(config.getInitParameter(Constants.WOWZA_URL));
             return status;
         } else if (ClipStatus.getInstance().isKnown(uuid)) {
             log.debug("Already started transcoding '" + uuid + "'");
             request = ClipStatus.getInstance().get(uuid);
             final File flashFile = Util.getFlashFile(request, config);
             final long flashFileLength = flashFile.length();
             ObjectStatus status = new ObjectStatus();
             if (request != null && request.getFinalFileLengthBytes() != null && request.getFinalFileLengthBytes() != 0) {
                 double completionPercentage = 100.0 * flashFileLength/request.getFinalFileLengthBytes();
                 completionPercentage = Math.min(completionPercentage, 99.5);
                 status.setCompletionPercentage(completionPercentage);
             }
             status.setStatus(ObjectStatusEnum.STARTED);
             status.setFlashFileLengthBytes(flashFileLength);
             status.setStreamId("flv:" + flashFile.getName());
            if (status.getCompletionPercentage() < 0.00001) {
                 status.setPositionInQueue(Util.getQueuePosition(request, config));
             }
             return status;
         } else return null;
     }
 }

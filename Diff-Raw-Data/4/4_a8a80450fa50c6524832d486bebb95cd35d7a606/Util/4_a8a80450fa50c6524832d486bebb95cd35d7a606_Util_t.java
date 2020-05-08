 /* File:        $Id$
  * Revision:    $Revision$
  * Author:      $Author$
  * Date:        $Date$
  *
  * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletConfig;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Util {
 
     private static Logger log = Logger.getLogger(Util.class);
 
     /**
      * Pattern for extracting a uuid of a doms object from url-decoded
      * permanent url.
      */
     public static final String UUID_STRING = ".*uuid:(.*)";
     public static final Pattern UUID_PATTERN = Pattern.compile(UUID_STRING);
 
     private Util(){}
 
 
     public static String getDemuxFilename(TranscodeRequest request) {
         return request.getPid()+"_first.ts";
     }
 
     /**
      * Gets the uuid from a shard url
      * @param shardUrl
      * @return
      * @throws UnsupportedEncodingException
      */
     public static String getUuid(String shardUrl) throws UnsupportedEncodingException {
         String urlS = URLDecoder.decode(shardUrl, "UTF-8");
         Matcher m = UUID_PATTERN.matcher(urlS);
         if (m.matches()) {
              return m.group(1);
         } else return null;
     }
 
     /**
      * Converts a shard url to a doms url
      * e.g. http://www.statsbiblioteket.dk/doms/shard/uuid:ef8ea1b2-aaa8-412a-a247-af682bb57d25
      * to <DOMS_SERVER>/fedora/objects/uuid%3Aef8ea1b2-aaa8-412a-a247-af682bb57d25/datastreams/SHARD_METADATA/conteny
      *
      * @param pid
      * @return
      */
     public static URL getDomsUrl(String pid, ServletConfig config) throws ProcessorException {
         String urlS = Util.getInitParameter(config, Constants.DOMS_LOCATION) + "/objects/uuid:"+pid+"/datastreams/SHARD_METADATA/content";
         try {
             return new URL(urlS);
         } catch (MalformedURLException e) {
             throw new ProcessorException(e);
         }
     }
 
     public static String getFinalFilename(TranscodeRequest request) {
         return request.getPid() + ".mp4";
     }
 
     public static File getTempDir(ServletConfig config) {
         return new File(getInitParameter(config, Constants.TEMP_DIR_INIT_PARAM));
     }
 
     public static File getFinalDir(ServletConfig config) {
         return new File(Util.getInitParameter(config, Constants.FINAL_DIR_INIT_PARAM));
     }
 
     public static File getDemuxFile(TranscodeRequest request, ServletConfig config) {
         return new File(getTempDir(config), getDemuxFilename(request));
     }
 
     public static File getIntialFinalFile(TranscodeRequest request, ServletConfig config) {
         return new File(getTempDir(config), getFinalFilename(request));
     }
 
     public static File getFinalFinalFile(TranscodeRequest request, ServletConfig config) {
         return new File(getFinalDir(config), getFinalFilename(request));
     }
 
     public static File getFlashFile(TranscodeRequest request, ServletConfig config) {
         return new File(getFinalDir(config), request.getPid() + ".flv");
     }
 
     public static File getLockFile(TranscodeRequest request, ServletConfig config) {
         return new File(getTempDir(config), getLockFileName(request));
     }
 
     public static String getLockFileName(TranscodeRequest request) {
         return request.getPid() + "." + request.getServiceType() + ".lck";
     }
 
     public static File[] getAllLockFiles(ServletConfig config) {
         FilenameFilter filter = new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
                 return name.endsWith(".lck");
             }
         };
         return (getTempDir(config)).listFiles(filter);
     }
 
     public static void unlockRequest(TranscodeRequest request) {
         synchronized (request) {
             if (request.getThePool() != null && request.getLockObject() != null) {
                 try {
                     log.info("Unlocking request '" + request.getPid() + "' from '" + request.getLockObject() + "'");
                     request.getThePool().returnObject(request.getLockObject());
                     request.setLockObject(null);
                     request.setThePool(null);
                 } catch (Exception e) {
                     log.error(e);
                 }
             }
         }
     }
 
     public static String getStreamId(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         File outputFile = OutputFileUtil.getExistingMediaOutputFile(request, config);
         String filename = getRelativePath(new File(Util.getInitParameter(config, Constants.FINAL_DIR_INIT_PARAM)), outputFile);
         if (filename.endsWith(".mp4")) {
             return "mp4:" + filename;
         } else if (filename.endsWith(".mp3")) {
             return "mp3:" + filename;
         } else if (filename.endsWith(".flv")) {
             return "flv:" + filename;
         } else return null;      
     }
 
     public static String getRelativePath(File parent, File child) throws ProcessorException {
         try {
             String parentS = parent.getCanonicalPath();
             String childS = child.getCanonicalPath();
             if (!childS.startsWith(parentS)) {
                 throw new ProcessorException(parentS + " is not a parent of " + childS);
             }
             String result = childS.replaceFirst(parentS, "");
            while (result.startsWith(File.separator)) {
                result = result.replaceFirst(File.separator, "");
             }
             return result;
         } catch (IOException e) {
             throw new ProcessorException(e);
         }
     }
 
 
     public static int getQueuePosition(TranscodeRequest request, ServletConfig config) {
         return ProcessorChainThreadPool.getInstance(config).getPosition(request);
     }
 
 
     public static String getInitParameter(ServletConfig config, String paramName) {
         if (config.getServletContext() != null && config.getServletContext().getInitParameter(paramName) != null) {
             return config.getServletContext().getInitParameter(paramName);
         } else {
             return config.getInitParameter(paramName);
         }
     }
 
     public static String getAudioBitrate(ServletConfig config) {
         return getInitParameter(config, Constants.AUDIO_BITRATE);
     }
 
     public static String getVideoBitrate(ServletConfig config) {
         return getInitParameter(config, Constants.VIDEO_BITRATE);
     }
 
     public static String getPrimarySnapshotSuffix(ServletConfig config) {
         return getInitParameter(config, Constants.SNAPSHOT_PRIMARY_FORMAT);
     }
 
 
 }

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
 package dk.statsbiblioteket.doms.radiotv.extractor;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletException;
 import java.io.File;
 
 public class BESServlet extends com.sun.jersey.spi.container.servlet.ServletContainer {
 
     private static Logger log = Logger.getLogger(BESServlet.class);
 
     @Override
     public void init() throws ServletException {
         super.init();
         initializeDomsClient();
         Util.getTempDir(this.getServletConfig()).mkdirs();
         try {
             cleanup();
         } catch (Exception e) {
             log.error("Error during initial cleanup.", e);
         }
         log.info("initialized BES service version " + BroadcastExtractionService.besVersion);
     }
 
 	private void initializeDomsClient() {
 		if (false) {
 			throw new RuntimeException("DOMS not ready to use new interface yet!");
 		}
 		String domsWSAPIEndpointUrlString = Util.getInitParameter(this.getServletConfig(), Constants.DOMS_ENDPOINT);
 		String userName = Util.getInitParameter(this.getServletConfig(), Constants.DOMS_USERNAME);
 		String password = Util.getInitParameter(this.getServletConfig(), Constants.DOMS_PASSWORD);
 		DomsClient.initializeSingleton(domsWSAPIEndpointUrlString, userName, password);
 	}
 
     @Override
     public void destroy() {
         super.destroy();
         try {
             cleanup();
         } catch (Exception e) {
             log.error(e);
         }
         log.info("destroyed BES service");
     }
 
 
     private void cleanup() {
         log.info("Initiating cleanup of unfinished processes.");
         cleanupDigitvExtractionWorkDir();
         File[] lockFiles = Util.getAllLockFiles(this.getServletConfig());
         for (File lockFile: lockFiles) {
             log.info("Cleaning up after '" + lockFile.getAbsolutePath() + "'");
             switch (Util.getServiceTypeFromLockFile(lockFile)) {
 	            case BROADCAST_EXTRACTION:
 	                cleanupExtraction(lockFile);
 	                break;
 	            case DIGITV_BROADCAST_EXTRACTION:
 	                cleanupDigitvExtractionDelete(lockFile);
 	                break;
                 case PREVIEW_GENERATION:
                     cleanupPreview(lockFile);
                     break;
                 case THUMBNAIL_GENERATION:
                     cleanupSnapshots(lockFile);
                     break;
                 case PREVIEW_THUMBNAIL_GENERATION:
                     log.error("There should not be lock files specifically for preview thumbnail generation. " +
                             "File '" + lockFile.getAbsolutePath() + "' should not exist.");
                     break;
             }
         }
     }
 
     private void cleanupExtraction(File lockFile) {
         TranscodeRequest request = new TranscodeRequest(Util.getPidFromLockFile(lockFile));
         request.setServiceType(ServiceTypeEnum.BROADCAST_EXTRACTION);
         try {
             File mediaFile = OutputFileUtil.getExistingMediaOutputFile(request, this.getServletConfig());
             log.info("Cleaning up partial file '" + mediaFile.getAbsolutePath() + "'");
             if (mediaFile != null && !mediaFile.delete()) {
                 log.error("Could not delete file '" + mediaFile.getAbsolutePath() + "'");
             }
         } catch (Exception e) {
             log.info("No media file found corresponding to lockFile '" + lockFile.getAbsolutePath() + "'");
         }
         if (!lockFile.delete()) {
             log.error("Could not delete lock file: '" + lockFile.getAbsolutePath() + "'");
         }
     }
 
     private void cleanupDigitvExtractionWorkDir() {
        File workDir = new File(OutputFileUtil.getBaseOutputDir(ServiceTypeEnum.DIGITV_BROADCAST_EXTRACTION, this.getServletConfig()));
         if (workDir.exists() && workDir.isDirectory()) {
             for (File workFile : workDir.listFiles()) {
                 boolean deleted = workFile.delete();
                 if (deleted) {
                     log.info("Deleted work file: " + workFile.getAbsolutePath());
                 } else {
                     log.error("Could not delete work file: '" + workFile.getAbsolutePath() + "'");
                 }
             }
         }
     }
 
     private void cleanupDigitvExtractionDelete(File lockFile) {
         if (!lockFile.delete()) {
             log.error("Could not delete lock file: '" + lockFile.getAbsolutePath() + "'");
         }
 	}
 
 	private void cleanupPreview(File lockFile) {
         TranscodeRequest request = new TranscodeRequest(Util.getPidFromLockFile(lockFile));
         request.setServiceType(ServiceTypeEnum.PREVIEW_GENERATION);
         try {
             File mediaFile = OutputFileUtil.getExistingMediaOutputFile(request, this.getServletConfig());
             log.info("Cleaning up partial file '" + mediaFile.getAbsolutePath() + "'");
             if (mediaFile != null && !mediaFile.delete()) {
                 log.error("Could not delete file '" + mediaFile.getAbsolutePath() + "'");
             }
         } catch (Exception e) {
             log.info("No media file found corresponding to lockFile '" + lockFile.getAbsolutePath() + "'");
         }
         if (!lockFile.delete()) {
             log.error("Could not delete lock file: '" + lockFile.getAbsolutePath() + "'");
         }
     }
 
     private void cleanupSnapshots(File lockFile) {
         TranscodeRequest request = new TranscodeRequest(Util.getPidFromLockFile(lockFile));
         request.setServiceType(ServiceTypeEnum.THUMBNAIL_GENERATION);
         try {
             for (File mediaFile: OutputFileUtil.getOutputDir(request, this.getServletConfig()).listFiles()) {
                 if (!mediaFile.getName().contains("preview")) {
                     try {
                         log.info("Cleaning up partial file '" + mediaFile.getAbsolutePath() + "'");
                         if (mediaFile != null && !mediaFile.delete()) {
                             log.error("Could not delete file '" + mediaFile.getAbsolutePath() + "'");
                         }
                     } catch (Exception e) {
                         log.info("No media file found corresponding to lockFile '" + lockFile.getAbsolutePath() + "'");
                     }
                 }
             }
         } catch (Exception e) {
             log.error(e);
         }  finally {
             log.info("Deleting lockfile '" + lockFile.getAbsolutePath() + "'");
             if (!lockFile.delete()) {
                 log.error("Could not delete lock file: '" + lockFile.getAbsolutePath() + "'");
             }
         }
     }
 
 }

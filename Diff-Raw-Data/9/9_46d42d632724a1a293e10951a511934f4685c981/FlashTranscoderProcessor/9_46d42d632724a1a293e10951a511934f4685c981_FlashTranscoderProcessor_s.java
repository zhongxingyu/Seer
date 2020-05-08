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
 
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletConfig;
 import java.io.File;
 import java.io.IOException;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
 import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
 
 public class FlashTranscoderProcessor extends ProcessorChainElement {
 
     private static Logger log = Logger.getLogger(FlashTranscoderProcessor.class);
 
 
     @Override
     protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         switch (request.getClipType()) {
             case MUX:
                 seamlessClip(request, config);
                 break;
             case MPEG1:
                 mpegClip(request, config);
                 break;
             case MPEG2:
                 mpegClip(request, config);
                 break;
             case WAV:
                 throw new ProcessorException("WAV clipping not yet implemented");
         }
         Util.unlockRequest(request);
     }
 
     private void seamlessClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         Long offsetBytes = 0L;
         Long totalLengthBytes = 0L;
         String fileList = "";
         final int clipSize = request.getClips().size();
         int programNumber = 0;
         for (int iclip = 0; iclip < clipSize; iclip++ ) {
             TranscodeRequest.FileClip clip = request.getClips().get(iclip);
             final long fileLength = new File(clip.getFilepath()).length();  //This line has the side-effect of automounting the media file
             Long clipLength = clip.getClipLength();
             fileList += " " + clip.getFilepath() + " ";
             if (iclip == 0) {
                 programNumber = clip.getProgramId();
                 offsetBytes = clip.getStartOffsetBytes();
                 if (offsetBytes == null) offsetBytes = 0L;
                 if (clipLength != null && clipSize == 1) {
                     totalLengthBytes = clipLength;   //Program contained within file
                 } else {         //Otherwise always go to end of file
                     totalLengthBytes = fileLength - offsetBytes;
                 }
             } else if (iclip == clipSize - 1 && clipSize != 1) {   //last clip in multiclip program
                 if (clipLength != null) {
                     totalLengthBytes += clip.getClipLength();
                 } else {
                     totalLengthBytes += fileLength;
                 }
             } else {   //A file in the middle of a program so take the whole file
                 totalLengthBytes += fileLength;
             }
         }
         Long blocksize = 1880L;
 
         String clipperCommand = "cat " + fileList + " | dd bs=" + blocksize
                 + " skip=" + offsetBytes/blocksize + " count=" + totalLengthBytes/blocksize
                 + " | vlc - --program=" + programNumber + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                 + "--sout '#std{access=file, mux=ts, dst=-}' | "
                 + getFfmpegCommandLine(request, config);
         runClipperCommand(clipperCommand);
     }
 
     private void mpegClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
         long blocksize = 1880L;
         final int clipSize = request.getClips().size();
         if (clipSize > 1) throw new ProcessorException("Haven't implemented mpeg mulitclipping yet");
         TranscodeRequest.FileClip clip = request.getClips().get(0);
         String start = "";
         if (clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0L) start = "skip=" + clip.getStartOffsetBytes()/blocksize;
         String length = "";
         if (clip.getClipLength() != null && clip.getClipLength() != 0L) length = "count=" + clip.getClipLength()/blocksize;
         String command = "dd if=" + clip.getFilepath() + " bs=" + blocksize + " " + start + " " + length + "| "
                 + getFfmpegCommandLine(request, config);
         runClipperCommand(command);
     }
 
     private static String getFfmpegCommandLine(TranscodeRequest request, ServletConfig config) {
         String line = "ffmpeg -i - " + config.getInitParameter(Constants.FFMPEG_PARAMS)
                 + " -b " + config.getInitParameter(Constants.VIDEO_BITRATE) + "000"
                + " -ab" + config.getInitParameter(Constants.AUDIO_BITRATE) + "000"
                 + " " + getFfmpegAspectRatio(request, config)
                 + " " + " -vpre "  + config.getInitParameter(Constants.X264_PRESET)
                 + " " + Util.getFlashFile(request, config);
         return line;
     }
 
 
     private static String getFfmpegAspectRatio(TranscodeRequest request, ServletConfig config) {
         Double aspectRatio = request.getDisplayAspectRatio();
         String ffmpegResolution;
         Long height = Long.parseLong(config.getInitParameter(Constants.PICTURE_HEIGHT));
         if (aspectRatio != null) {
             long width = Math.round(aspectRatio*height);
             if (width%2 == 1) width += 1;
             ffmpegResolution = " -s " + width + "x" + height;
         } else {
             ffmpegResolution = " -s 320x240";
         }
         return ffmpegResolution;
     }
 
     private void runClipperCommand(String clipperCommand) throws ProcessorException {
         log.info("Executing '" + clipperCommand + "'");
         try {
             ExternalJobRunner runner = new ExternalJobRunner(new String[]{"bash", "-c", clipperCommand});
             log.info("Command '" + clipperCommand + "' returned with exit value '" + runner.getExitValue() + "'");
             if (runner.getExitValue() != 0) {
                 log.debug("Standard out:\n" + runner.getOutput());
                 log.debug("Standard err:\n" + runner.getError());
             }
         } catch (IOException e) {
             throw new ProcessorException(e);
         } catch (InterruptedException e) {
             throw new ProcessorException(e);
         }
     }
 
 }

 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.extractor.video;
 
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.media.FxMetadata;
 import com.flexive.shared.media.impl.FxMimeType;
 import com.google.common.collect.Maps;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The video extractor class
  * <p/>
  * It uses ffmpeg from the path, if not present it uses a jar to extract info from flv videos
  *
  * @author Laszlo Hernadi lhernadi@ucs.at
  * @since 3.1.3
  */
 public class VideoExtractor {
 
     private static final Log LOG = LogFactory.getLog(VideoExtractor.class);
 
     /**
      * Identify a file, returning metadata
      *
      * @param mimeType if not null it will be used to call the correct identify routine
      * @param file     the file to identify
      * @return metadata
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on errors
      */
     public static FxMetadata identify(String mimeType, File file) throws FxApplicationException {
         final VideoExtractorImpl extractor = new VideoExtractorImpl(mimeType, file);
         extractor.extractVideoData();
 
         return new FxVideoMetaDataImpl(file.getName(), mimeType, extractor.getMetaItems(), extractor.getLength());
     }
 
     /**
      * Inner class to deal with the actual audio data extraction
      */
     static class VideoExtractorImpl {
         private long length = 0L;
         private FxMimeType mimeType;
         private File file;
         private List<FxMetadata.FxMetadataItem> metaItems;
         private static final String FFMPEG_BINARY = "ffmpeg";
 
         /**
          * Constructor
          *
          * @param mimeType the mime type as a String
          * @param file     the video file
          */
         VideoExtractorImpl(String mimeType, File file) {
             this.mimeType = FxMimeType.getMimeType(mimeType);
             this.file = file;
         }
 
         public List<FxMetadata.FxMetadataItem> getMetaItems() {
             return metaItems;
         }
 
         /**
          * Main extraction method
          * Extraction is based on the given mime type
          */
         public void extractVideoData() {
             extractGeneral();
         }
 
         /**
          * Extracts the metaData using ffmpeg
          */
         private void extractGeneral() {
             File tmpPrev = null;
             FxSharedUtils.ProcessResult res;
             try {
                 tmpPrev = File.createTempFile("VideoPreview", ".jpg");
                 // add time offset to skip fade-in. If called on a shorter video, no preview will be generated
                 final String offset = "00:00:02.0";
                 // generate preview
                 res = FxSharedUtils.executeCommand(FFMPEG_BINARY, "-vstats", "-ss", offset, "-i", file.getAbsolutePath(), tmpPrev.getAbsolutePath());
             } catch (IOException e) {
                 if (LOG.isWarnEnabled()) {
                     LOG.warn("Failed to create video preview file: " + e.getMessage(), e);
                 }
                 res = FxSharedUtils.executeCommand(FFMPEG_BINARY, "-vstats", "-i", file.getAbsolutePath());
             }
             FFMpegParser curParser = new FFMpegParser(String.valueOf(res.getStdOut()) + String.valueOf(res.getStdErr()));
             metaItems = new ArrayList<FxMetadata.FxMetadataItem>(8);
             if (tmpPrev != null && tmpPrev.length() > 10)
                 metaItems.add(new FxMetadata.FxMetadataItem("previewFile", tmpPrev.getAbsolutePath()));
             if (!curParser.buildMetaItems(metaItems)) {
                 if (LOG.isInfoEnabled()) {
                     LOG.info("ffmpeg not found in path, skipping video file processing");
                 }
             }
         }
 
         public long getLength() {
             return length;
         }
     }
 
     /**
      * A class to parse the ffmpeg output creating the video and audio Stream(s)
      */
     private static class FFMpegParser {
         private static final Pattern DURATION = Pattern.compile("Duration: ([0-9:.]+)");
         private static final Pattern BITRATE = Pattern.compile("bitrate: ([0-9]+)");
 
         String duration;
         String bitrate;
         List<VideoStreamHolder> videoStream = new ArrayList<VideoStreamHolder>(2);
         List<AudioStreamHolder> audioStream = new ArrayList<AudioStreamHolder>(2);
 
 
         FFMpegParser(String output) {
             long start = System.nanoTime();
 
             int begin0 = output.indexOf("Input #0,");
             if (begin0 <= 0) return;
             begin0 = output.indexOf("Duration: ", begin0);
             if (begin0 <= 0) return;
             output = output.substring(begin0);
             int end0 = Math.max(output.indexOf("[ffmpeg_output"), output.indexOf("At least one output file must be specified"));
             if (end0 > 0) output = output.substring(0, end0);
 
             if (LOG.isInfoEnabled()) {
                 LOG.info("Parsing :\n" + output);
             }
             final Matcher m_dur = DURATION.matcher(output);
             if (m_dur.find()) {
                 duration = m_dur.group(1);
             }
             final Matcher m_bitrate = BITRATE.matcher(output);
             if (m_bitrate.find()) {
                 bitrate = m_bitrate.group(1);
             }
             String[] streams = output.split("Stream #");
             for (String tmp : streams) {
                 String[] subs = tmp.split(":", 3);
                 if (subs.length == 3) {
                     String key = subs[1].trim();
                     if (key.equals("Video")) {
                         videoStream.add(new VideoStreamHolder(subs[2], bitrate));
                     } else if (key.equals("Audio")) {
                         audioStream.add(new AudioStreamHolder(subs[2]));
                     }
                 }
             }
             if (LOG.isTraceEnabled()) {
                 LOG.trace(String.format("Parsing took : %7.3fms", ((System.nanoTime() - start) * 1E-6)));
             }
         }
 
         /**
          * Build the meta items of the current video
          * It recognize the video and audio Stream(s)
          *
          * @param metaItems the List to write the metaItems to
          * @return <code>true</code> if any value is written
          */
         public boolean buildMetaItems(List<FxMetadata.FxMetadataItem> metaItems) {
             boolean returnValue = false;
             if (duration != null) {
                 metaItems.add(new FxMetadata.FxMetadataItem("duration", duration.trim()));
                 returnValue = true;
             }
             metaItems.add(new FxMetadata.FxMetadataItem("videoStreams", "" + videoStream.size()));
             if (videoStream.size() > 0) {
                 returnValue = true;
                 int i = 1;
                 for (VideoStreamHolder curVs : videoStream) {
                     metaItems.add(new FxMetadata.FxMetadataItem("height" + i, (curVs.height != null ? curVs.height : "")));
                     metaItems.add(new FxMetadata.FxMetadataItem("width" + i, (curVs.width != null ? curVs.width : "")));
                     metaItems.add(new FxMetadata.FxMetadataItem("videodatarate" + i, (curVs.bitRate != null ? curVs.bitRate : "")));
                     metaItems.add(new FxMetadata.FxMetadataItem("framerate" + i, (curVs.fps != null ? curVs.fps : "")));
                     metaItems.add(new FxMetadata.FxMetadataItem("videoType" + i, (curVs.type != null ? curVs.type : "")));
                     metaItems.add(new FxMetadata.FxMetadataItem("videoEncoding" + i, (curVs.encoding != null ? curVs.encoding : "")));
                     i++;
                 }
             }
             metaItems.add(new FxMetadata.FxMetadataItem("audioStreams", "" + audioStream.size()));
             if (audioStream.size() > 0) {
                 returnValue = true;
                 int i = 1;
                 for (AudioStreamHolder curAudioStream : audioStream) {
                     metaItems.add(new FxMetadata.FxMetadataItem("audiodatarate" + i, curAudioStream.bitRate));
                     metaItems.add(new FxMetadata.FxMetadataItem("audioType" + i, (curAudioStream.type != null ? curAudioStream.type : "")));
                     metaItems.add(new FxMetadata.FxMetadataItem("audioEncoding" + i, (curAudioStream.encoding != null ? curAudioStream.encoding : "")));
                     metaItems.add(new FxMetadata.FxMetadataItem("frequency" + i, (curAudioStream.herz != null ? curAudioStream.herz : "")));
                     i++;
                 }
             }
             return returnValue;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public String toString() {
             return duration + "\t" + bitrate + " kb/s Video : [" + String.valueOf(videoStream) + "] Audio : " + String.valueOf(audioStream) + "]";
         }
     }
 
     /**
      * Holder class for a video stream
      */
     private static class VideoStreamHolder {
         /**
          * don't parse to int / double we need it as string and let groovy parse
          */
         String type;
         String encoding;
         String width = "";
         String height = "";
         String bitRate = "";
         String tbr;
         String tbn;
         String tbc;
         String fps = "";
 
         private final static Pattern res = Pattern.compile("([0-9]+)x([0-9]+)");
         private final static Pattern bitR = Pattern.compile("([0-9]+) kb/s");
 
         /**
          * Creates a VideoStreamHolder from a Video-stream info
          *
          * @param info           the videostream info as String
          * @param overallBitRate the bitrate of the whole file
          */
         VideoStreamHolder(String info, String overallBitRate) {
             String[] all = info.split(", ");
             type = all[0];
             encoding = all[1];
             bitRate = overallBitRate;
             Matcher m = res.matcher(all[2]);
             if (m.find()) {
                 width = m.group(1);
                 height = m.group(2);
             }
             m = bitR.matcher(all[3]);
             if (m.find()) {
                 bitRate = m.group(1);
             }
             Map<String, String> tmpValues = Maps.newHashMap();
            for (int i = 4; i < all.length; i++) {
                 String[] tmp = all[i].split(" ");
                 if (tmp.length == 2) {
                     tmpValues.put(tmp[1], tmp[0]);
                 }
             }
             String tmp = tmpValues.get("fps");
             if (tmp != null) fps = tmp;
             tbr = tmpValues.get("tbr");
             tbn = tmpValues.get("tbn");
             tbc = tmpValues.get("tbc");
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public String toString() {
             return type + ", " + encoding + " [" + width + "x" + height + " @" + bitRate + " kb/s" + " " + fps + " fps";
         }
     }
 
     /**
      * A holder class for an audio stream
      */
     private static class AudioStreamHolder {
         String type = null;
         String encoding = null;
         String herz = null;
         String bitRate = "";
 
         //    Format :     Stream #0.1: Audio: wmav2, 32000 Hz, 2 channels, (s16,)? 64 kb/s
         private static final Pattern HZ = Pattern.compile("([0-9]+) Hz");
         private static final Pattern BITRATE = Pattern.compile("([0-9]+) kb/s");
 
         /**
          * Creates a AudioStreamHolder from a Audio-stream info
          *
          * @param info the videostream info as String
          */
         AudioStreamHolder(String info) {
             String[] all = info.split(", ");
             if (all.length < 3) {
                 if (LOG.isWarnEnabled()) {
                     LOG.warn("Unknown audio format line: " + info);
                     type = encoding = "unknown";
                     return;
                 }
             }
             encoding = all[0];
             type = all[2];
             Matcher m = HZ.matcher(all[1]);
             if (m.find()) {
                 herz = m.group(1);
             }
             m = BITRATE.matcher(info);
             if (m.find()) {
                 bitRate = m.group(1);
             }
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public String toString() {
             return type + ", " + encoding + " @" + bitRate + " kb/s" + " " + herz + " Hz";
         }
     }
 
 }

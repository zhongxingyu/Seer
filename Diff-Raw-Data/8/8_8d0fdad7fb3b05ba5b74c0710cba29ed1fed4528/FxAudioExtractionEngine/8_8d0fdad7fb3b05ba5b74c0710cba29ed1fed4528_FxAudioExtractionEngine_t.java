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
 package com.flexive.extractor.audio;
 
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.media.FxMetadata;
 import com.flexive.shared.media.impl.FxMimeType;
 import javazoom.spi.vorbis.sampled.file.VorbisAudioFileFormat;
 import javazoom.spi.vorbis.sampled.file.VorbisAudioFormat;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.kc7bfi.jflac.FLACDecoder;
 import org.kc7bfi.jflac.FrameListener;
 import org.kc7bfi.jflac.frame.Frame;
 import org.kc7bfi.jflac.metadata.Metadata;
 import org.kc7bfi.jflac.metadata.StreamInfo;
 import org.tritonus.share.sampled.TAudioFormat;
 import org.tritonus.share.sampled.file.TAudioFileFormat;
 
 import javax.sound.sampled.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A Class to extract audio meta data information for various supported audio formats.
  * Currently the following audio formats are supported:
  * - .mp3, .ogg, .wav, etc
  * <p/>
  * Relies on the following libraries:
  * javax.sound
  * jlayer
  * jorbis
  * mp3spi
  *
  * @author Christopher Blasnik, cblasnik@flexive.com, unique computing solutions gmbh
  * @since 3.1.2
  */
 public class FxAudioExtractionEngine {
 
     private static final Log LOG = LogFactory.getLog(FxAudioExtractionEngine.class);
 
     /**
      * Identify a file, returning metadata
      *
      * @param mimeType if not null it will be used to call the correct identify routine
      * @param file     the file to identify
      * @return metadata
      * @throws FxApplicationException on errors
      */
     public static FxMetadata identify(String mimeType, File file) throws FxApplicationException {
        final AudioExtractorImpl extractor = new AudioExtractorImpl(mimeType, file);
         extractor.extractAudioData();
 
         return new FxAudioMetaDataImpl(mimeType, file.getName(), extractor.getMetaItems(), extractor.getLength());
     }
 
     /**
      * Inner class to deal with the actual audio data extraction
      */
    private static class AudioExtractorImpl {
         private long length = 0L;
         private FxMimeType mimeType;
         private File file;
         private List<FxMetadata.FxMetadataItem> metaItems;
 
         /**
          * Ctor
          *
          * @param mimeType the mime type as a String
          * @param file     the audio file
          */
        AudioExtractorImpl(String mimeType, File file) {
             this.mimeType = FxMimeType.getMimeType(mimeType);
             this.file = file;
         }
 
         /**
          * Main extraction method
          * Extraction is based on the given mime type
          */
         public void extractAudioData() {
             final String subType = mimeType.getSubType();
             // ogg and mp3
             if (subType.contains("ogg") || subType.contains("vorbis"))
                 extractOggAudioData();
             else if (subType.contains("mp3") || subType.contains("mpeg"))
                 extractMp3AudioData();
             else if (subType.contains("flac"))
                 extractFlacAudioData();
             else // the (current) rest
                 extractNativeAudioData();
         }
 
         /**
          * Extract data for files supported by Java's sound api, e.g. wave, aiff, snd, au
          */
         private void extractNativeAudioData() {
             metaItems = new ArrayList<FxMetadata.FxMetadataItem>(5);
             AudioInputStream audioInStream = null;
             Clip clip = null;
             try {
                 audioInStream = AudioSystem.getAudioInputStream(file);
                 clip = AudioSystem.getClip();
                 // length in microseconds
                 length = clip.getMicrosecondLength();
                 // audio format info
                 final AudioFormat audioFormat = clip.getFormat();
                 // encoding
                 metaItems.add(new FxMetadata.FxMetadataItem("encoding", audioFormat.getEncoding().toString()));
                 // channels (1 or 2, 1 = mono, int)
                 metaItems.add(new FxMetadata.FxMetadataItem("channels", String.valueOf(audioFormat.getChannels())));
                 // sample rate (float)
                 metaItems.add(new FxMetadata.FxMetadataItem("samplerate", String.valueOf(audioFormat.getSampleRate())));
 
             } catch (Exception e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Failed to get audio stream from file or could not recognise audio file format", e);
                 }
             } finally {
                 if (clip != null) {
                     clip.flush();
                 }
                 FxSharedUtils.close(audioInStream);
             }
         }
 
         /**
          * Extract audio (meta)data from an MP3-encoded audio file using JLayer / mp3spi
          * <p/>
          * mp3 parameters from http://www.javazoom.net/mp3spi/documents.html
          * <p/>
          * Standard parameters :
          * - duration : [Long], duration in microseconds.
          * - title : [String], Title of the stream.
          * - author : [String], Name of the artist of the stream.
          * - album : [String], Name of the album of the stream.
          * - date : [String], The date (year) of the recording or release of the stream.
          * - copyright : [String], Copyright message of the stream.
          * - comment : [String], Comment of the stream.
          * Extended MP3 parameters :
          * - mp3.version.mpeg : [String], mpeg version : 1,2 or 2.5
          * - mp3.version.layer : [String], layer version 1, 2 or 3
          * - mp3.version.encoding : [String], mpeg encoding : MPEG1, MPEG2-LSF, MPEG2.5-LSF
          * - mp3.channels : [Integer], number of channels 1 : mono, 2 : stereo.
          * - mp3.frequency.hz : [Integer], sampling rate in hz.
          * - mp3.bitrate.nominal.bps : [Integer], nominal bitrate in bps.
          * - mp3.length.bytes : [Integer], length in bytes.
          * - mp3.length.frames : [Integer], length in frames.
          * - mp3.framesize.bytes : [Integer], framesize of the first frame.
          * framesize is not constant for VBR streams.
          * - mp3.framerate.fps : [Float], framerate in frames per seconds.
          * - mp3.header.pos : [Integer], position of first audio header (or ID3v2 size).
          * - mp3.vbr : [Boolean], vbr flag.
          * - mp3.vbr.scale : [Integer], vbr scale.
          * - mp3.crc : [Boolean], crc flag.
          * - mp3.original : [Boolean], original flag.
          * - mp3.copyright : [Boolean], copyright flag.
          * - mp3.padding : [Boolean], padding flag.
          * - mp3.mode : [Integer], mode 0:STEREO 1:JOINT_STEREO 2:DUAL_CHANNEL 3:SINGLE_CHANNEL
          * - mp3.id3tag.genre : [String], ID3 tag (v1 or v2) genre.
          * - mp3.id3tag.track : [String], ID3 tag (v1 or v2) track info.
          * - mp3.id3tag.v2 : [InputStream], ID3v2 frames.
          * - mp3.shoutcast.metadata.key : [String], Shoutcast meta key with matching value.
          * For instance :
          * mp3.shoutcast.metadata.icy-irc=#shoutcast
          * mp3.shoutcast.metadata.icy-metaint=8192
          * mp3.shoutcast.metadata.icy-genre=Trance Techno Dance
          * mp3.shoutcast.metadata.icy-url=http://www.di.fm
          */
         private void extractMp3AudioData() {
             metaItems = new ArrayList<FxMetadata.FxMetadataItem>(12);
             Map<String, Object> properties;
             try {
                 final AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
                 final AudioFormat baseFormat = baseFileFormat.getFormat();
 
                 // TAudioFileFormat properties
                 if (baseFileFormat instanceof TAudioFileFormat) {
                     properties = baseFileFormat.properties();
                     // SAMPLERATE
                     Integer bitrate = properties.get("mp3.frequency.hz") != null ? (Integer) properties.get("mp3.frequency.hz") : 0;
                     if (bitrate != 0)
                         metaItems.add(new FxMetadata.FxMetadataItem("samplerate", String.valueOf(bitrate)));
 
                     // TITLE
                     String title = properties.get("title") != null ? (String) properties.get("title") : null;
                     if (title != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("songtitle", title));
 
                     // ARTIST
                     String artist = properties.get("author") != null ? (String) properties.get("author") : null;
                     if (artist != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("artist", artist));
 
                     // ALBUM
                     String album = properties.get("album") != null ? (String) properties.get("album") : null;
                     if (album != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("album", album));
 
                     // CHANNELS
                     Integer channels = properties.get("mp3.channels") != null ? (Integer) properties.get("mp3.channels") : null;
                     if (channels != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("channels", String.valueOf(channels)));
 
                     // LENGTH / DURATION
                     length = properties.get("duration") != null ? (Long) properties.get("duration") : 0L;
 
                     // ENCODING
                     String encoding = properties.get("mp3.version.encoding") != null ? (String) properties.get("mp3.version.encoding") : null;
                     if (encoding != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("encoding", encoding));
 
                     // YEAR
                     String year = properties.get("date") != null ? (String) properties.get("date") : null;
                     if (year != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("year", year));
 
                     // GENRE
                     String genre = properties.get("mp3.id3tag.genre") != null ? (String) properties.get("mp3.id3tag.genre") : null;
                     if (genre != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("genre", genre));
 
                     // COMMENT
                     String comment = properties.get("comment") != null ? (String) properties.get("comment") : null;
                     if (comment != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("audiofilecomment", new String(FxSharedUtils.getBytes(comment))));
 
                     // COPYRIGHT
                     String copyright = properties.get("copyright") != null ? (String) properties.get("copyright") : null;
                     if (copyright != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("copyright", new String(FxSharedUtils.getBytes(copyright))));
                 }
                 // TAudioFormat properties
                 if (baseFormat instanceof TAudioFormat) {
                     properties = baseFormat.properties();
                     // bitrate
                     Integer bitrate = properties.get("bitrate") != null ? (Integer) properties.get("bitrate") : null;
                     if (bitrate != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("bps", String.valueOf(bitrate)));
 
                     // variable bit rate flag
                     Boolean vbr = properties.get("vbr") != null ? (Boolean) properties.get("vbr") : null;
                     if (vbr != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("vbr", String.valueOf(vbr)));
                 }
 
             } catch (Exception e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("An error occurred while reading the mp3 properties", e);
                 }
             }
         }
 
         /**
          * Extract ogg vorbis audio metadata useing JLayer / vorbisspi
          */
         private void extractOggAudioData() {
             metaItems = new ArrayList<FxMetadata.FxMetadataItem>(6);
             Map<String, Object> properties;
             AudioInputStream audioInStream = null;
             Clip clip = null;
             try {
                 audioInStream = AudioSystem.getAudioInputStream(file);
                 clip = AudioSystem.getClip();
                 final AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);  // VorbisAudioFileFormat
                 final AudioFormat baseFormat = baseFileFormat.getFormat(); // VorbisAudioFormat
 
                 if (baseFileFormat instanceof VorbisAudioFileFormat) {
                     properties = baseFileFormat.properties();
                     final AudioFormat audioFormat = clip.getFormat();
 
                     // ENCODING
                     metaItems.add(new FxMetadata.FxMetadataItem("encoding", audioFormat.getEncoding().toString()));
 
                     // SAMPLERATE
                     Integer bitrate = properties.get("ogg.frequency.hz") != null ? (Integer) properties.get("ogg.frequency.hz") : 0;
                     if (bitrate != 0)
                         metaItems.add(new FxMetadata.FxMetadataItem("samplerate", String.valueOf(bitrate)));
 
                     // CHANNELS
                     Integer channels = properties.get("ogg.channels") != null ? (Integer) properties.get("ogg.channels") : null;
                     if (channels != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("channels", String.valueOf(channels)));
 
                     // LENGTH / DURATION
                     length = properties.get("duration") != null ? (Long) properties.get("duration") : 0L;
                 }
 
                 if (baseFormat instanceof VorbisAudioFormat) {
                     properties = baseFormat.properties();
 
                     // variable bit rate flag
                     Boolean vbr = properties.get("vbr") != null ? (Boolean) properties.get("vbr") : null;
                     if (vbr != null)
                         metaItems.add(new FxMetadata.FxMetadataItem("vbr", String.valueOf(vbr)));
                 }
 
             } catch (Exception e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("An error ocurred while parsing ogg vorbis metadata from an audio file", e);
                 }
             } finally {
                 if (clip != null) {
                     clip.flush();
                 }
                 FxSharedUtils.close(audioInStream);
             }
         }
 
         /**
          * Extract flac audio data using the JFlac library
          *
          * - still need to check out how to get remaining data (duration, etc.)
          */
         private void extractFlacAudioData() {
             metaItems = new ArrayList<FxMetadata.FxMetadataItem>(5);
             // Map<String, Object> properties;
             InputStream audioInStream = null;
             final AudioFormat audioFormat;
             try {
                 audioInStream = new FileInputStream(file);
                 final FlacAudioExtractor audioExtr = new FlacAudioExtractor();
                 audioExtr.analyse(audioInStream);
 
                 final StreamInfo streamInfo = audioExtr.getStreamInfo();
                 audioFormat = streamInfo.getAudioFormat();
 
                 int samplerate = streamInfo.getSampleRate();
                 metaItems.add(new FxMetadata.FxMetadataItem("samplerate", String.valueOf(samplerate)));
                 int channels = streamInfo.getChannels();
                 metaItems.add(new FxMetadata.FxMetadataItem("channels", String.valueOf(channels)));
 
                 // ENCODING
                 metaItems.add(new FxMetadata.FxMetadataItem("encoding", audioFormat.getEncoding().toString()));
 
             } catch (Exception e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("An error occurred decoding the FLAC audio file", e);
                 }
             } finally {
                 FxSharedUtils.close(audioInStream);
             }
         }
 
         /**
          * The Flac file audio extractor
          */
         class FlacAudioExtractor implements FrameListener {
 
             private StreamInfo streamInfo;
 
             FlacAudioExtractor() {
             }
 
             void analyse(InputStream audioInStream) throws Exception {
                 FLACDecoder decoder = new FLACDecoder(audioInStream);
                 decoder.addFrameListener(this);
                 decoder.decode();
             }
 
             @Override
             public void processMetadata(Metadata metadata) {
                 if (metadata instanceof StreamInfo) {
                     streamInfo = (StreamInfo) metadata;
                 }
             }
 
             @Override
             public void processFrame(Frame frame) {
             }
 
             @Override
             public void processError(String s) {
             }
 
             public StreamInfo getStreamInfo() {
                 return streamInfo;
             }
         }
 
         public long getLength() {
             return length;
         }
 
         public List<FxMetadata.FxMetadataItem> getMetaItems() {
             return metaItems;
         }
     }
 }

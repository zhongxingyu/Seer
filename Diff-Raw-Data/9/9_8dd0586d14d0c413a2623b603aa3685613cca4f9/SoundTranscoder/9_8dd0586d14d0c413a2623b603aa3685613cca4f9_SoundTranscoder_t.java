 ////////////////////////////////////////////////////////////////////////////////
 //
 //  ADOBE SYSTEMS INCORPORATED
 //  Copyright 2004-2007 Adobe Systems Incorporated
 //  All Rights Reserved.
 //
 //  NOTICE: Adobe permits you to use, modify, and distribute this file
 //  in accordance with the terms of the license agreement accompanying it.
 //
 ////////////////////////////////////////////////////////////////////////////////
 
 package flex2.compiler.media;
 
 import flex2.compiler.SymbolTable;
 import flex2.compiler.Transcoder;
 import flex2.compiler.TranscoderException;
 import flex2.compiler.common.PathResolver;
 import flex2.compiler.io.VirtualFile;
 import flex2.compiler.util.MimeMappings;
 import flash.swf.tags.DefineSound;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 
 /**
  * Transcodes sounds into DefineSounds for embedding.
  *
  * @author Clement Wong
  */
 public class SoundTranscoder extends AbstractTranscoder
 {
 	// MP3 sampling rate...
 	private static final int[][] mp3frequencies;
 
 	// Bitrate
 	private static final int[][] mp3bitrates;
 	private static final int[][] mp3bitrateIndices;
 
     private static final int DECODER_DELAY = 529;
 
 
     static
 	{
 		// [frequency_index][version_index]
 		mp3frequencies = new int[][]
 		{
 			{11025, 0, 22050, 44100},
 			{12000, 0, 24000, 48000},
 			{8000, 0, 16000, 32000},
 			{0, 0, 0, 0}
 		};
 
 		// [bits][version,layer]
 		mp3bitrates = new int[][]
 		{
 			{0, 0, 0, 0, 0},
 			{32, 32, 32, 32, 8},
 			{64, 48, 40, 48, 16},
 			{96, 56, 48, 56, 24},
 			{128, 64, 56, 64, 32},
 			{160, 80, 64, 80, 40},
 			{192, 96, 80, 96, 48},
 			{224, 112, 96, 112, 56},
 			{256, 128, 112, 128, 64},
 			{288, 160, 128, 144, 80},
 			{320, 192, 160, 160, 96},
 			{352, 224, 192, 176, 112},
 			{384, 256, 224, 192, 128},
 			{416, 320, 256, 224, 144},
 			{448, 384, 320, 256, 160},
 			{-1, -1, -1, -1, -1}
 		};
 
 		mp3bitrateIndices = new int[][]
 		{
 			// reserved, layer III, layer II, layer I
 			{-1, 4, 4, 3}, // version 2.5
 			{-1, -1, -1, -1}, // reserved
 			{-1, 4, 4, 3}, // version 2
 			{-1, 2, 1, 0}  // version 1
 		};
 	}
 
 	public SoundTranscoder()
 	{
 		super(new String[]{MimeMappings.MP3}, DefineSound.class, true);
 	}
 
     public boolean isSupportedAttribute( String attr )
     {
         return false;   // no attributes yet
     }
 
 	public TranscodingResults doTranscode( PathResolver context, SymbolTable symbolTable,
                                            Map<String, Object> args, String className, boolean generateSource )
         throws TranscoderException
 	{
         TranscodingResults results = new TranscodingResults( resolveSource( context, args ));
         String newName = (String) args.get( Transcoder.NEWNAME );
 
         TranscodeJob job = new TranscodeJob();
         results.defineTag = job.transcode(results.assetSource, newName);
 
         if (generateSource)
             generateSource( results, className, args );
         
         return results;
 	}
 
     private class TranscodeJob
     {
        private static final String US_ASCII = "US-ASCII";
         private BufferedInputStream in = null;
         private int encoderDelay = 0;
         private int fill = 0;
         private ByteArrayOutputStream soundDataStream = null;
 
         /**
          * 0 - version 2.5
          * 1 - reserved
          * 2 - version 2
          * 3 - version 1
          */
         private int version = -1;
 
         /**
          * 0 - reserved
          * 1 - layer III => 1152 samples
          * 2 - layer II  => 1152 samples
          * 3 - layer I   => 384  samples
          */
         private int layer = -1;
 
         private int samplingRate = -1;
 
         /**
          * 0 - stereo
          * 1 - joint stereo
          * 2 - dual channel
          * 3 - single channel
          */
         private int channelMode = -1;
 
         private int numFrames = 0;
         private int frameLength;
 
         private void processInfoTag () throws IOException, NotInMP3Format {
            in.mark(frameLength);
 
             for (int i=0; i<4; ++i) {
                 if (in.read() == -1) {
                     throw new NotInMP3Format();
                 }
             }
 
             byte[][] signatures = new byte[][]{
                    "Xing".getBytes(US_ASCII),
                    "Info".getBytes(US_ASCII)};
 
             int pos = 0;
             byte[] signature = null;
             int b;
 
             while ((b = in.read()) == 0)
             {
                 ++pos;
             }
 
             pos = 0;
 
             do
             {
                 if (pos == 0) {
                     for (byte[] s: signatures)
                     {
                         if (s[0] == b)
                         {
                             signature = s;
                             break;
                         }
                     }
 
                     if (signature == null) {
                         in.reset();
                         return;
                     }
                 }
                 else
                 {
                     if (signature[pos] != b)
                     {
                         in.reset();
                         return;
                     }
                 }
                 ++pos;
             } while (pos < 4 && (b = in.read()) != -1);
 
             for (int i=0; i<137; ++i) {
                 if (in.read() == -1) {
                     throw new NotInMP3Format();
                 }
             }
 
             if ((b = in.read()) == -1)
             {
                 throw new NotInMP3Format();
             }
             else
             {
                 encoderDelay = b << 4;
             }
             if ((b = in.read()) == -1)
             {
                 throw new NotInMP3Format();
             }
             else
             {
                 encoderDelay |= (b >> 4) & 15;
                 fill = b << 8;
             }
             if ((b = in.read()) == -1)
             {
                 throw new NotInMP3Format();
             }
             else
             {
                 fill |= b;
             }
         }
 
         private void processRemainingFrames () throws IOException, NotInMP3Format {
             while (readNextFrameHeader()) {
                 int b, c=0;
                 while (c < frameLength) {
                     b = in.read();
                     if (b == -1) {
                         // lame < 3.80 doesn't properly pad the final frame with ancillary data
                         soundDataStream.write(0);
                     } else {
                         soundDataStream.write(b);
                     }
                     ++c;
                 }
                 ++numFrames;
             }
         }
 
         private boolean readNextFrameHeader () throws IOException, NotInMP3Format {
             // look for the first 11-bit frame sync. skip everything before the frame sync
             int b, state = 0;
             int bits = 0, padding = 0;
 
             in.mark(4);
             while ((b = in.read()) != -1)
             {
                 if (state == 0)
                 {
                     if (b == 255)
                     {
                         state = 1;
                     }
                     else
                     {
                         in.mark(3);
                     }
                 }
                 else if (state == 1)
                 {
                     if ((b >> 5 & 0x7) == 7)
                     {
                         if (layer == -1) {
                             layer = b >> 1 & 0x3;
                             version = b >> 3 & 0x3;
                         } else if (layer != (b >> 1 & 0x3) ||
                                 version != (b >> 3 & 0x3)) {
                             throw new NotInMP3Format(); // TODO choose a better exception
                         }
                         state = 2;
                     }
                     else
                     {
                         state = 0;
                     }
                 } else if (state == 2) {
                     if (samplingRate == -1) {
                         samplingRate = b >> 2 & 0x3;
                     } else if (samplingRate != (b >> 2 & 0x3)) {
                         throw new NotInMP3Format(); // TODO choose a better exception
                     }
                     padding = b >> 1 & 0x1;
                     bits = b >> 4 & 0xf;
                     state = 3;
                 } else {
                     if (channelMode == -1) {
                         channelMode = b >> 6 & 0x3;
                     } else if (channelMode != (b >> 6 & 0x3)) {
                         throw new NotInMP3Format(); // TODO choose a better exception
                     }
 
                     in.reset();
 
                     int bitrateIndex = mp3bitrateIndices[version][layer];
                     int bitrate = bitrateIndex != -1 ? mp3bitrates[bits][bitrateIndex] * 1000 : -1;
 
                     int frequency = mp3frequencies[samplingRate][version];
 
                     if (bitrate <= 0 || frequency == 0)
                     {
                         state = 0;
                         continue;
                     }
 
                     frameLength = layer == 3 ?
                             (12 * bitrate / frequency + padding) * 4 :
                             144 * bitrate / frequency + padding;
 
                     return true;
                 }
             }
 
             return false;
         }
 
         private DefineSound transcode (VirtualFile source, String symbolName) throws NotInMP3Format, ExceptionWhileTranscoding, UnsupportedSamplingRate, CouldNotDetermineSampleFrameCount {
             InputStream in = null;
             byte[] soundData;
             try {
                 int size = (int) source.size();
                 in = source.getInputStream();
 
                 this.in = new BufferedInputStream(in);
                 soundDataStream = new ByteArrayOutputStream(size + 2);
 
                 // placeholder for number of samples to skip.
                 soundDataStream.write(0);
                 soundDataStream.write(0);
 
                 if (readNextFrameHeader()) {
                     processInfoTag();
                     processRemainingFrames();
                 } else {
                     throw new NotInMP3Format();
                 }
 
                 soundData = soundDataStream.toByteArray();
             } catch (IOException ex) {
                 throw new ExceptionWhileTranscoding(ex);
             } finally {
                 if (in != null) {
                     try {
                         in.close();
                     } catch (IOException ignored) {
                     }
                 }
             }
 
             if (soundData == null || soundData.length < 5)
             {
                 throw new NotInMP3Format();
             }
 
             // write number of samples to skip.
             final int totalDelay = encoderDelay + DECODER_DELAY;
             soundData[0] = (byte)(totalDelay & 255);
             soundData[1] = (byte)((totalDelay >> 8) & 255);
 
 
             DefineSound ds = new DefineSound();
             ds.format = 2; // MP3
 
             ds.data = soundData;
             ds.size = 1; // always 16-bit for compressed formats
             ds.name = symbolName;
 
             int frequency = mp3frequencies[samplingRate][version];
 
             /**
              * 1 - 11kHz
              * 2 - 22kHz
              * 3 - 44kHz
              */
             switch (frequency)
             {
             case 11025:
                 ds.rate = 1;
                 break;
             case 22050:
                 ds.rate = 2;
                 break;
             case 44100:
                 ds.rate = 3;
                 break;
             default:
                 throw new UnsupportedSamplingRate( frequency );
             }
 
             /**
              * 0 - mono
              * 1 - stereo
              */
             ds.type = channelMode == 3 ? 0 : 1;
 
             /**
              * assume that the whole thing plays in one SWF frame
              *
              * sample count = number of MP3 frames * number of samples per MP3
              */
             ds.sampleCount = numFrames * (layer == 3 ? 384 : 1152) - fill - encoderDelay;
 
             if (ds.sampleCount < 0)
             {
                 // frame count == -1, error!
                 throw new CouldNotDetermineSampleFrameCount();
             }
 
             return ds;
         }
     }
 
     public static final class CouldNotDetermineSampleFrameCount extends TranscoderException {
 
         private static final long serialVersionUID = 6530096120116320212L;}
     public static final class UnsupportedSamplingRate extends TranscoderException
     {
         private static final long serialVersionUID = -7440513635414375590L;
         public UnsupportedSamplingRate( int frequency )
         {
             this.frequency = frequency;
         }
         public int frequency;
     }
 
 	public static final class NotInMP3Format extends TranscoderException {
 
         private static final long serialVersionUID = -2480509003403956321L;}
 }

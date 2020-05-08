 /*
  * Copyright (c) 2011 by Samuel Berner (samuel.berner@gmail.com), all rights reserved
  * Created on 23.12.2011
  */
 package figurabia.ui.video.access;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import javax.sound.sampled.AudioFormat;
 
 import com.xuggle.xuggler.IAudioSamples;
 import com.xuggle.xuggler.ICodec;
 import com.xuggle.xuggler.IContainer;
 import com.xuggle.xuggler.IPacket;
 import com.xuggle.xuggler.IPixelFormat;
 import com.xuggle.xuggler.IStream;
 import com.xuggle.xuggler.IStreamCoder;
 import com.xuggle.xuggler.IVideoPicture;
 import com.xuggle.xuggler.IVideoResampler;
 import com.xuggle.xuggler.IAudioSamples.Format;
 import com.xuggle.xuggler.IPixelFormat.Type;
 import com.xuggle.xuggler.video.ConverterFactory;
 import com.xuggle.xuggler.video.IConverter;
 
 public class XugglerMediaInputStream {
 
     private final File file;
 
     private final IContainer container;
     private IStream audioStream;
     private IStream videoStream;
     private final IStreamCoder audioCoder;
     private final IStreamCoder videoCoder;
     private final IVideoResampler videoResampler;
     private final IVideoPicture resamplingTempPic;
     private final IConverter javaImageConverter;
 
     private final PacketSource packetSource;
 
     private final IAudioSamples samples;
 
     private final double audioTimeBase;
     private final double videoTimeBase;
     private final AudioFormat audioFormat;
     private final VideoFormat videoFormat;
     private final long audioFramesSampleNum;
     private final double frameTime;
     private final double audioPacketTimeStep;
 
     private long intendedAudioPosition = -1;
     private long intendedVideoPosition = -1;
     private long targetAudioPacket = -1;
     private int targetAudioBytePos = -1;
     private IPacket audioPacketReadPartially = null;
     private IPacket videoPacketReadPartially = null;
     private int audioPacketOffset = 0;
     private int videoPacketOffset = 0;
     private int samplesBytePos = 0;
     private int bytesPerSample;
 
     private List<IVideoPicture> createdVideoPictures = new ArrayList<IVideoPicture>();
 
     public static class PacketSource {
         private IContainer container;
         private Queue<IPacket> audioPackets = new LinkedList<IPacket>();
         private Queue<IPacket> videoPackets = new LinkedList<IPacket>();
         private int audioStreamIndex;
         private int videoStreamIndex;
 
         public PacketSource(IContainer container, int audioStreamIndex, int videoStreamIndex) {
             this.container = container;
             this.audioStreamIndex = audioStreamIndex;
             this.videoStreamIndex = videoStreamIndex;
         }
 
         public void reset() {
             for (IPacket packet : audioPackets)
                 packet.delete();
             audioPackets.clear();
             for (IPacket packet : videoPackets)
                 packet.delete();
             videoPackets.clear();
         }
 
         private IPacket readPacketOfStream(int index, int otherIndex, Queue<IPacket> otherPackets) {
             while (true) {
                 IPacket p = IPacket.make();
                 if (container.readNextPacket(p) < 0) {
                     p.delete();
                     return null;
                 }
                 if (p.getStreamIndex() == index) {
                     return p;
                 } else if (p.getStreamIndex() == otherIndex) {
                     otherPackets.add(p);
                 } else {
                     p.delete();
                 }
             }
         }
 
         /**
          * @return the next audio packet or null if end of file or error
          */
         public IPacket readNextAudioPacket() {
             if (audioPackets.isEmpty())
                 return readPacketOfStream(audioStreamIndex, videoStreamIndex, videoPackets);
             else
                 return audioPackets.poll();
         }
 
         /**
          * @return the next video packet or null if end of file or error
          */
         public IPacket readNextVideoPacket() {
             if (videoPackets.isEmpty())
                 return readPacketOfStream(videoStreamIndex, audioStreamIndex, audioPackets);
             else
                 return videoPackets.poll();
         }
     }
 
     public XugglerMediaInputStream(File file) throws IOException {
         this.file = file;
         // open the media file
         container = IContainer.make();
         int resultCode = container.open(new RandomAccessFile(file, "r"), IContainer.Type.READ, null);
         if (resultCode < 0)
             throw new BadVideoException("error opening container (code = " + resultCode);
 
         // retrieve some information about the media file
         int numStreams = container.getNumStreams();
         long duration = container.getDuration();
 
         System.out.println("number of streams in container: " + numStreams);
         System.out.println("duration: " + duration);
 
         for (int i = 0; i < numStreams; i++) {
             IStream stream = container.getStream(i);
             IStreamCoder coder = stream.getStreamCoder();
 
             System.out.println("Stream " + i + " is of type " + coder.getCodecType());
             if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                 System.out.println("audio sample rate: " + coder.getSampleRate());
                 System.out.println("channels: " + coder.getChannels());
                 System.out.println("format: " + coder.getCodec().getLongName());
                 audioStream = stream;
             } else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                 System.out.println("width: " + coder.getWidth());
                 System.out.println("height: " + coder.getHeight());
                 System.out.println("format: " + coder.getCodec().getLongName());
                 videoStream = stream;
             } else {
                 // ignore other streams
             }
         }
 
         if (audioStream == null)
             throw new BadVideoException("no audio stream found");
         if (videoStream == null)
             throw new BadVideoException("no video stream found");
 
         audioTimeBase = audioStream.getTimeBase().getValue();
         videoTimeBase = videoStream.getTimeBase().getValue();
 
         audioCoder = audioStream.getStreamCoder();
         videoCoder = videoStream.getStreamCoder();
 
         audioFormat = createJavaSoundFormat(audioCoder);
         videoFormat = createVideoFormat(videoCoder);
         double exactAudioFramesSampleNum = audioCoder.getSampleRate() / videoFormat.getFrameRate();
         audioFramesSampleNum = (long) Math.ceil(exactAudioFramesSampleNum);
         frameTime = 1000.0 / videoFormat.getFrameRate();
         bytesPerSample = audioFormat.getSampleSizeInBits() / 8 * audioFormat.getChannels();
 
         int audioFrameSize = audioCoder.getAudioFrameSize();
         int audioSampleRate = audioCoder.getSampleRate();
         audioPacketTimeStep = (double) audioFrameSize / (double) audioSampleRate;
 
         System.out.println("audio samples per video frame: " + audioFramesSampleNum);
 
         // initialize decoders
         int audioStreamOpenResultCode = audioCoder.open();
         if (audioStreamOpenResultCode < 0)
             throw new IllegalStateException("Could not open audio stream (error " + audioStreamOpenResultCode + ")");
 
         int videoStreamOpenResultCode = videoCoder.open();
         if (videoStreamOpenResultCode < 0)
             throw new IllegalStateException("Could not open video stream (error " + videoStreamOpenResultCode + ")");
 
         // initialize resampler
         if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
             // if the video stream is not in BGR24, we'll have to convert it
             videoResampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(),
                     IPixelFormat.Type.BGR24, videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
             if (videoResampler == null)
                 throw new BadVideoException("Could not create video sampler. Probably unsupported color space");
             resamplingTempPic = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(),
                     videoCoder.getHeight());
         } else {
             videoResampler = null;
             resamplingTempPic = null;
         }
 
         // initialize java image converter
         javaImageConverter = ConverterFactory.createConverter(ConverterFactory.XUGGLER_BGR_24,
                 IPixelFormat.Type.BGR24,
                 videoCoder.getWidth(), videoCoder.getHeight());
 
         // initialize buffers
         samples = IAudioSamples.make(audioCoder.getAudioFrameSize(), audioCoder.getChannels());
 
         packetSource = new PacketSource(container, audioStream.getIndex(), videoStream.getIndex());
     }
 
     private static AudioFormat createJavaSoundFormat(IStreamCoder audioCoder) {
         float sampleRate = (float) audioCoder.getSampleRate();
         int sampleSize = (int) IAudioSamples.findSampleBitDepth(audioCoder.getSampleFormat());
         int channels = audioCoder.getChannels();
         boolean signed = true; /* xuggler defaults to signed 16 bit samples */
         boolean bigEndian = false;
         return new AudioFormat(sampleRate, sampleSize, channels, signed, bigEndian);
     }
 
     private static VideoFormat createVideoFormat(IStreamCoder videoCoder) {
         String encoding = videoCoder.getCodec().getName();
         Dimension size = new Dimension(videoCoder.getWidth(), videoCoder.getHeight());
         double frameRate = videoCoder.getFrameRate().getValue();
         return new VideoFormat(encoding, size, frameRate);
     }
 
     public void readFrame(MediaFrame mf) {
        mf.endOfMedia = false;
         long DEBUG_startMillis = System.currentTimeMillis();
         int skippedVideoFrames = 0;
         int skippedAudioFrames = 0;
 
         // loop through the packets
         int audioBytePos = 0;
 
         boolean audioComplete = false;
         while (!audioComplete) {
             IPacket audioPacket = null;
             if (audioPacketReadPartially != null) {
                 audioPacket = audioPacketReadPartially;
             } else {
                 audioPacket = packetSource.readNextAudioPacket();
                 // end of media (or error)
                 if (audioPacket == null)
                     break;
             }
             long packetTimestamp = (long) (audioPacket.getTimeStamp() * audioPacket.getTimeBase().getValue() * 1000.0);
 
             //System.out.println("original timestamps: packet.pts = " + audioPacket.getPts() + "; samples.pts = "
             //        + samples.getPts() + "; packet.timestamp = " + audioPacket.getTimeStamp()
             //        + "; samples.timestamp = " + samples.getTimeStamp());
 
             while (audioPacketOffset < audioPacket.getSize()) {
                 // only decode packet if previous decoded data has been copied fully
                 if (samplesBytePos == 0) {
                     if (samples.isComplete()) {
                         //System.err.println("Samples was already complete!");
                         // only in case it was completed before, it needs to be reset
                         samples.setComplete(false, -1, -1, -1, Format.FMT_NONE, -1);
                     }
                     int bytesDecoded = audioCoder.decodeAudio(samples, audioPacket, audioPacketOffset);
                     if (bytesDecoded < 0) {
                         throw new IllegalStateException("could not decode audio. Error code " + bytesDecoded);
                     }
                     audioPacketOffset += bytesDecoded;
                 }
                 if (audioPacketOffset < audioPacket.getSize()) {
                     audioPacketReadPartially = audioPacket;
                 } else {
                     audioPacketReadPartially = null;
                     audioPacketOffset = 0;
                 }
 
                 if (samples.isComplete()) {
                     long samplesTimestamp = samples.getPts() / 1000L;
                     // first skip audio samples that are before the "intendedPosition" (position that was set with setPosition)
                     if (intendedAudioPosition != -1 && samplesTimestamp < targetAudioPacket) { //packetTimestamp + (long) (1.5 * frameTime) < intendedPosition)
                         // reset buffer, so it can start filling again (because we're skipping until intendedPosition)
                         //System.out.println("Skipping " + samples.getNumSamples() + " audio samples. ("
                         //        + samplesTimestamp + ")");
                         //samples.setComplete(false, audioFramesSampleNum, audioCoder.getSampleRate(),
                         //        audioCoder.getChannels(), audioCoder.getSampleFormat(), packet.getPts());
                         skippedAudioFrames++;
                         if (audioPacketOffset == 0) // because the packet was already completely processed (see if/else further up)
                             break;
                         continue;
                     }
                     if (intendedAudioPosition != -1) {
                         intendedAudioPosition = -1; // to reset (already skipped frames as necessary) 
                         samplesBytePos = targetAudioBytePos;
                     }
                     // CAUTION packetTimestamp can be wrong (the one from the next packet), because content of samples can be
                     // leftover from a packet that has been completely decoded, but the decoded samples have not all been used yet.
                     //System.out.println("Got " + samples.getNumSamples() + " audio samples! (" + samplesTimestamp + ")");
                     //// getByteArray copies the bytes
                     //byte[] audioBytes = samples.getData().getByteArray(0, samples.getSize());
                     byte[] dest = mf.audio.audioData;
                     int bytesToCopy = Math.min(samples.getSize() - samplesBytePos, dest.length - audioBytePos);
                     samples.get(samplesBytePos, dest, audioBytePos, bytesToCopy);
                     samplesBytePos += bytesToCopy;
                     audioBytePos += bytesToCopy;
                     // if "samples" could not be fully copied because the current frame is already filled up
                     if (audioBytePos == dest.length) {
                         audioBytePos = 0; // because it is full now
                         audioComplete = true;
                         break;
                     } else {
                         if (samplesBytePos != samples.getSize())
                             throw new IllegalStateException(
                                     "internal inconsistency: target array not full, but still uncopied bytes available");
                         samplesBytePos = 0; // because it has been fully copied over
                     }
                 }
 
                 // means that the packet was completely read (see if/else further up)
                 if (audioPacketOffset == 0)
                     break;
             }
         }
 
         IVideoPicture picture;
         if (videoResampler == null)
             picture = mf.video.videoPicture;
         else
             picture = resamplingTempPic;
 
         boolean videoComplete = false;
 
         while (!videoComplete) {
             IPacket videoPacket;
             if (videoPacketReadPartially != null) {
                 videoPacket = videoPacketReadPartially;
             } else {
                 videoPacket = packetSource.readNextVideoPacket();
                 if (videoPacket == null)
                     break;
             }
             if (videoPacket != null) {
                 //long packetTimestamp = (long) (videoPacket.getTimeStamp() * videoPacket.getTimeBase().getValue() * 1000.0);
 
                 while (videoPacketOffset < videoPacket.getSize()) {
                     if (picture.isComplete()) {
                         picture.setComplete(false, Type.NONE, -1, -1, -1);
                     }
                     int bytesDecoded = videoCoder.decodeVideo(picture, videoPacket, videoPacketOffset);
                     if (bytesDecoded < 0) {
                         throw new RuntimeException("could not decode video. Error code " + bytesDecoded);
                     }
                     videoPacketOffset += bytesDecoded;
 
                     if (videoPacketOffset < videoPacket.getSize()) {
                         videoPacketReadPartially = videoPacket;
                     } else {
                         videoPacketReadPartially = null;
                         videoPacketOffset = 0;
                     }
 
                     /*
                      * Some decoders will consume data in a packet, but will not be able to construct
                      * a full video picture yet.  Therefore you should always check if you
                      * got a complete picture from the decoder
                      */
                     if (picture.isComplete()) {
                         long pictureTimestamp = picture.getPts() / 1000L;
 
                         // first skip video frames that are before the "intendedPosition" (position that was set with setPosition)
                         if (intendedVideoPosition != -1 && pictureTimestamp < intendedVideoPosition) {
                             // reset buffer (because we're skipping frames until intendedPosition)
                             //System.out.println("Skipping a video picture. (" + pictureTimestamp + ")");
                             picture.setComplete(false, videoCoder.getPixelType(), videoCoder.getWidth(),
                                     videoCoder.getHeight(), videoPacket.getPts());
                             skippedVideoFrames++;
                             if (videoPacketOffset == 0) // because 0 means that it was processed completely (see if/else further up)
                                 break;
                             continue;
                         }
 
                         IVideoPicture newPic;
                         /*
                          * If the resampler is not null, that means we didn't get the
                          * video in BGR24 format and
                          * need to convert it into BGR24 format.
                          */
                         if (videoResampler != null) {
                             // we must resample
                             newPic = mf.video.videoPicture;
                             if (videoResampler.resample(newPic, picture) < 0)
                                 throw new RuntimeException("could not resample video from: "
                                         + file);
                         } else {
                             newPic = picture;
                         }
 
                         // TODO this security check should not be necessary, remove later
                         //if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
                         //    throw new RuntimeException("could not decode video" +
                         //            " as BGR 24 bit data in: " + file);
 
                         mf.video.bufferedImage = javaImageConverter.toImage(newPic);
 
                         //System.out.println("Received a video picture (" + pictureTimestamp + ")");
                         videoComplete = true;
                         break;
                     }
 
                     // means that the packet was completely read (see if/else further up)
                     if (audioPacketOffset == 0)
                         break;
 
                 }
             }
         }
 
        if (!videoComplete) {
            mf.endOfMedia = true;
        }

         if (skippedAudioFrames != 0 || skippedVideoFrames != 0) {
             System.out.println("TRACE: skipped video frames: " + skippedVideoFrames + "; skipped audio frames: "
                     + skippedAudioFrames);
         }
         long DEBUG_endMillis = System.currentTimeMillis();
         //System.err.println("TRACE: read media frame in " + (DEBUG_endMillis - DEBUG_startMillis) + "ms");
     }
 
     public long setPosition(long millis) {
         //TODO possibly implement alternative seeking method that preserves audio packet in front of key video packet (1. search backwards to video keyframe, 2. search backwards to audio keyframe)
         //     maybe check how seeking is done in Xuggler and maybe propose to mailing list (after checking for discussions of course)
         long micros = (millis - (long) frameTime) * 1000;
         if (micros < 0)
             micros = 0;
 
         // seek in microseconds (try to find the last position before or at the specified position)
         long statusCode = container.seekKeyFrame(-1, micros - 5000000, micros, micros, 0);
         if (statusCode < 0)
             throw new IllegalStateException("Seek to position " + millis + "ms failed with code " + statusCode
                     + " in file " + file);
         if (videoPacketReadPartially != null)
             videoPacketReadPartially.delete();
         videoPacketReadPartially = null;
         if (audioPacketReadPartially != null)
             audioPacketReadPartially.delete();
         audioPacketReadPartially = null;
         audioPacketOffset = 0;
         videoPacketOffset = 0;
         packetSource.reset();
         samplesBytePos = 0;
         long actualMillis = (long) (videoStream.getCurrentDts() * videoTimeBase * 1000.0);
         intendedAudioPosition = millis;
         intendedVideoPosition = millis;
         int audioFrameSize = audioCoder.getAudioFrameSize();
         long targetSample = (long) Math.floor((double) millis / 1000.0 / audioPacketTimeStep * audioFrameSize);
         targetAudioPacket = Math.round(Math.floor(targetSample / audioFrameSize) * audioPacketTimeStep * 1000.0);
         targetAudioBytePos = (int) ((targetSample % audioFrameSize) * bytesPerSample);
         return millis;
     }
 
     public long getPosition() {
         // TODO maybe time of audio stream has to be regarded as well
         return (long) (videoStream.getCurrentDts() * videoTimeBase * 1000.0);
     }
 
     public long getDuration() {
         return container.getDuration();
     }
 
     public VideoFormat getVideoFormat() {
         return videoFormat;
     }
 
     public AudioFormat getAudioFormat() {
         return audioFormat;
     }
 
     public MediaFrame createFrame() {
         AudioBuffer audio = new AudioBuffer();
         audio.audioData = new byte[(int) audioFramesSampleNum * bytesPerSample];
         VideoBuffer video = new VideoBuffer();
         video.videoPicture = IVideoPicture.make(IPixelFormat.Type.BGR24, videoCoder.getWidth(),
                 videoCoder.getHeight());
         createdVideoPictures.add(video.videoPicture.copyReference());
         return new MediaFrame(audio, video);
     }
 
     public void close() {
         audioCoder.close();
         videoCoder.close();
         container.close();
         for (IVideoPicture p : createdVideoPictures)
             p.delete();
     }
 
     public static void main(String[] args) {
 
         IContainer container = IContainer.make();
 
         int resultCode = container.open(args[0], IContainer.Type.READ, null);
         if (resultCode < 0)
             throw new IllegalStateException("error opening file (code = " + resultCode);
 
         // retrieve some information about the media file
         int numStreams = container.getNumStreams();
         long duration = container.getDuration();
 
         System.out.println("number of streams in container: " + numStreams);
         System.out.println("duration: " + duration);
 
         IStream audioStream = null;
         IStream videoStream = null;
 
         for (int i = 0; i < numStreams; i++) {
             IStream stream = container.getStream(i);
             IStreamCoder coder = stream.getStreamCoder();
 
             System.out.println("Stream " + i + " is of type " + coder.getCodecType());
             if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                 System.out.println("audio sample rate: " + coder.getSampleRate());
                 System.out.println("channels: " + coder.getChannels());
                 System.out.println("format: " + coder.getCodec().getLongName());
                 audioStream = stream;
             } else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                 System.out.println("width: " + coder.getWidth());
                 System.out.println("height: " + coder.getHeight());
                 System.out.println("format: " + coder.getCodec().getLongName());
                 System.out.println("frame rate: " + coder.getFrameRate());
                 videoStream = stream;
             } else {
 
             }
         }
 
         if (audioStream == null)
             throw new IllegalStateException("no audio stream found");
         if (videoStream == null)
             throw new IllegalStateException("no video stream found");
 
         // retrieve audio & video
         IStreamCoder audioCoder = audioStream.getStreamCoder();
         IStreamCoder videoCoder = videoStream.getStreamCoder();
 
         int audioStreamOpenResultCode = audioCoder.open();
         if (audioStreamOpenResultCode < 0)
             throw new IllegalStateException("Could not open audio stream (error " + audioStreamOpenResultCode + ")");
 
         IPacket packet = IPacket.make();
         int videoPacketCount = 0;
         int audioPacketCount = 0;
         double videoTimeBase = videoCoder.getTimeBase().getDouble() * 1000.0;
         double audioTimeBase = audioCoder.getTimeBase().getDouble() * 1000.0;
         double videoPacketDiff = 1.0 / videoCoder.getFrameRate().getDouble();
         double audioSampleRate = audioCoder.getSampleRate();
         double audioPacketDiff = audioCoder.getAudioFrameSize() / audioSampleRate;
         while (container.readNextPacket(packet) >= 0) {
             if (packet.getStreamIndex() == audioStream.getIndex()) {
                 IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());
                 int offset = 0;
 
                 while (offset < packet.getSize()) {
                     int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
                     if (bytesDecoded < 0) {
                         throw new IllegalStateException("could not decode audio. Error code " + bytesDecoded);
                     }
                     offset += bytesDecoded;
 
                     if (samples.isComplete()) {
                         double realTime = audioPacketCount++ * audioPacketDiff;
                         double fileTime = samples.getPts() * samples.getTimeBase().getDouble(); // * audioTimeBase / 1000.0;
                         System.out.println(String.format("%9.6f %9.6f %9.6f", realTime, fileTime, realTime - fileTime)
                                 + ": Got " + samples.getNumSamples()
                                 + " audio samples!"
                                 + (packet.isKeyPacket() ? " (key)" : ""));
                         // getByteArray copies the bytes
                         byte[] audioBytes = samples.getData().getByteArray(0, samples.getSize());
                     }
                 }
             } else if (packet.getStreamIndex() == videoStream.getIndex()) {
                 double realTime = videoPacketCount++ * videoPacketDiff;
                 double videoDts = packet.getPts();
                 double fileTime = videoDts * packet.getTimeBase().getDouble(); //* videoTimeBase / 1000.0;
                 System.out.println(String.format("%9.6f %9.6f %9.6f", realTime, fileTime, realTime - fileTime)
                         + ": Got video frame"
                         + (packet.isKeyPacket() ? " (key)" : ""));
             }
         }
     }
 }

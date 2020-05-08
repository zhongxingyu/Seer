 
 package org.projectvoodoo.audiomeasurementsplayer;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.kc7bfi.jflac.FLACDecoder;
 import org.kc7bfi.jflac.PCMProcessor;
 import org.kc7bfi.jflac.metadata.StreamInfo;
 import org.kc7bfi.jflac.util.ByteData;
 
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.util.Log;
 
 public class SamplePlayer {
 
     private static final String TAG = "Voodoo AudioMeasurementsPlayer SamplePlayer";
 
     private static final int SAMPLE_FREQUENCY = 44100;
     private static final int BUFFER_MILLISEC = 500;
 
     private Object playingLock = new Object();
     private InputStream mInputStream;
     private long mTotalSamples;
     private AudioTrack mTrack;
     private Sample lastPlayedSample;
 
     private long mLastTimePlaying;
 
     enum Sample {
         RMAA_CALIBRATION("rmaa-calibration-44100-16.flac"),
         RMAA_TEST("rmaa-test-44100-16.flac"),
         UDIAL("udial.flac"),
 
         ;
         String assetFileName;
 
         Sample(String fileName) {
             this.assetFileName = fileName;
         }
     }
 
     public synchronized void play(Sample sample, Runnable callback) {
         stop();
 
         Log.i(TAG, "Playing " + sample + ": " + sample.assetFileName);
         DecodingTask task = new DecodingTask(sample, callback);
         task.start();
         lastPlayedSample = sample;
     }
 
     public synchronized void stop() {
         if (mInputStream != null)
             try {
                 mInputStream.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
         if (mTrack != null) {
             mTrack.stop();
             mTrack.flush();
         }
 
         synchronized (playingLock) {
         }
     }
 
     public long getDurationMillisec() {
        return (long) ((double) mTotalSamples * 1000 / SAMPLE_FREQUENCY);
     }
 
     public boolean isPlaying() {
         if (mTrack == null)
             return false;
 
         if (mTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
             mLastTimePlaying = System.currentTimeMillis();
             return true;
         }
 
         if (System.currentTimeMillis() < mLastTimePlaying + BUFFER_MILLISEC + 50)
             return true;
 
         return false;
     }
 
     public long getProgressMillisec() {
         if (mTrack != null)
             try {
                return (long) ((double) mTrack.getPlaybackHeadPosition() * 1000 / SAMPLE_FREQUENCY);
             } catch (Exception e) {
             }
 
         return 0;
     }
 
     public String getLastPlayedSampleName() {
         if (lastPlayedSample != null)
             return lastPlayedSample.assetFileName;
 
         return "";
     }
 
     private class DecodingTask extends Thread {
         private Sample mSample;
         private AudioTrack mDecoderTrack;
         private Runnable mCallback;
 
         public DecodingTask(Sample sample, Runnable callback) {
             setPriority(MAX_PRIORITY);
             setName("Sample Decoder/Player");
             this.mSample = sample;
             mCallback = callback;
         }
 
         @Override
         public void run() {
             super.run();
             synchronized (playingLock) {
 
                 try {
                     mInputStream = App.context.getAssets().open(mSample.assetFileName);
                     FLACDecoder decoder = new FLACDecoder(mInputStream);
 
                     mDecoderTrack = mTrack = getAudioTrack();
                     decoder.addPCMProcessor(new AudioTrackOutput(mDecoderTrack));
                     mDecoderTrack.play();
                     mCallback.run();
                     decoder.decode();
                     mInputStream.close();
                     Log.i(TAG, "Finished");
 
                 } catch (IOException e) {
                     e.printStackTrace();
                 } catch (Exception e) {
                     // manual stop
                 }
 
                 mInputStream = null;
                 mDecoderTrack.stop();
             }
         }
 
         class AudioTrackOutput implements PCMProcessor {
             final AudioTrack track;
 
             public AudioTrackOutput(AudioTrack track) {
                 this.track = track;
             }
 
             @Override
             public void processStreamInfo(StreamInfo streamInfo) {
                 mTotalSamples = streamInfo.getTotalSamples();
             }
 
             @Override
             public void processPCM(ByteData pcm) {
                 track.write(pcm.getData(), 0, pcm.getLen());
             }
         }
     }
 
     private AudioTrack getAudioTrack() {
         mLastTimePlaying = 0;
         return new AudioTrack(
                 AudioManager.STREAM_MUSIC,
                 SAMPLE_FREQUENCY,
                 AudioFormat.CHANNEL_OUT_STEREO,
                 AudioFormat.ENCODING_PCM_16BIT,
                 SAMPLE_FREQUENCY * BUFFER_MILLISEC / 1000 * 4,
                 AudioTrack.MODE_STREAM);
     }
 
 }

 package javax.microedition.media;
 
 import net.intensicode.runme.util.Log;
 
 import javax.microedition.media.control.VolumeControl;
 import javax.sound.sampled.*;
 import java.io.*;
 
 public final class MuxmBackend implements Runnable
     {
     public VolumeControl volumeControl;
 
 
     public MuxmBackend( final InputStream aInputStream ) throws IOException
         {
         myModData = ResourceLoader.loadStream( aInputStream );
         myThread = new Thread( this );
         }
 
     public final void createModuleDataStream() throws UnsupportedAudioFileException, IOException
         {
         final ByteArrayInputStream inputStream = new ByteArrayInputStream( myModData );
         myModuleStream = AudioSystem.getAudioInputStream( inputStream );
         }
 
     public final void determinePlaybackFormat()
         {
         final AudioFormat baseFormat = myModuleStream.getFormat();
         final AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
         final float sampleRate = baseFormat.getSampleRate();
         final int channels = baseFormat.getChannels();
         final int frameSize = channels * 2;
         final int sizeInBits = 16;
         myPlaybackFormat = new AudioFormat( encoding, sampleRate, sizeInBits, channels, frameSize, sampleRate, false );
         }
 
     public final void openPlaybackLine() throws LineUnavailableException
         {
         final DataLine.Info info = new DataLine.Info( SourceDataLine.class, myPlaybackFormat );
         final SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine( info );
         dataLine.open( myPlaybackFormat );
         myPlaybackLine = dataLine;
         }
 
     public final void createVolumeControl()
         {
         for ( final javax.sound.sampled.Control control : myPlaybackLine.getControls() )
             {
            if ( control.getType().toString().equals( "Master Gain" ) )
                 {
                 volumeControl = new DirectVolumeControl( (FloatControl) control );
                 }
             }
        volumeControl = new FakeVolumeControl();
         }
 
     public final void createPlaybackStream()
         {
         myPlaybackStream = AudioSystem.getAudioInputStream( myPlaybackFormat, myModuleStream );
         }
 
     public final void startPlayback()
         {
         if ( !myThread.isAlive() ) myThread.start();
         }
 
     public final void pausePlayback() throws InterruptedException
         {
         myBailOutFlag = true;
         myThread.interrupt();
         myThread.join();
         }
 
     public final void disposePlaybackStream()
         {
         try
             {
             if ( myPlaybackStream != null ) myPlaybackStream.close();
             }
         catch ( final IOException e )
             {
             Log.error( e );
             }
         finally
             {
             myPlaybackStream = null;
             }
         }
 
     public final void dispose()
         {
         disposePlaybackStream();
         disposeVolumeControl();
         disposePlaybackLine();
         disposePlaybackFormat();
         disposeModuleStream();
         }
 
     private void disposeVolumeControl()
         {
         volumeControl = null;
         }
 
     private void disposePlaybackLine()
         {
         if ( myPlaybackLine == null ) return;
         myPlaybackLine.close();
         myPlaybackLine = null;
         }
 
     private void disposePlaybackFormat()
         {
         myPlaybackFormat = null;
         }
 
     private void disposeModuleStream()
         {
         try
             {
             if ( myModuleStream != null ) myModuleStream.close();
             }
         catch ( final IOException e )
             {
             Log.error( e );
             }
         finally
             {
             myModuleStream = null;
             }
         }
 
     // From Runnable
 
     public final void run()
         {
         final byte[] buffer = new byte[READ_WRITE_BUFFER_SIZE];
 
         try
             {
             myPlaybackLine.start();
 
             while ( !myBailOutFlag )
                 {
                 final int bytesRead = myPlaybackStream.read( buffer );
                 if ( bytesRead == -1 ) break;
 
                 int offset = 0;
                 while ( offset < bytesRead )
                     {
                     final int bytesWritten = myPlaybackLine.write( buffer, offset, bytesRead - offset );
                     offset += bytesWritten;
                     }
                 }
 
             myPlaybackLine.drain();
             }
         catch ( final IOException e )
             {
             Log.error( e );
             }
         finally
             {
             myBailOutFlag = false;
             myPlaybackLine.stop();
             }
         }
 
 
     private boolean myBailOutFlag;
 
     private AudioFormat myPlaybackFormat;
 
     private SourceDataLine myPlaybackLine;
 
     private AudioInputStream myPlaybackStream;
 
     private AudioInputStream myModuleStream;
 
     private final Thread myThread;
 
     private final byte[] myModData;
 
     private static final int READ_WRITE_BUFFER_SIZE = 4096;
     }

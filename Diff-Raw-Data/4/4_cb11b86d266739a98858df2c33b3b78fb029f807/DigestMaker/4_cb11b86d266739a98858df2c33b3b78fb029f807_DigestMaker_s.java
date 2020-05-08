 package org.treblefrei.kedr.database.musicdns;
 
 import org.treblefrei.kedr.audio.AudioDecoder;
 import org.treblefrei.kedr.audio.AudioDecoderException;
 import org.treblefrei.kedr.audio.DecodedAudioData;
 import org.treblefrei.kedr.database.musicdns.ofa.Ofa;
 import org.treblefrei.kedr.model.Album;
 import org.treblefrei.kedr.model.Track;
 
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 
 class DigestMakerRunnable implements Runnable {
     private Track track;
     private Map<Track, Digest> store;
 
     public DigestMakerRunnable(Track track, Map<Track, Digest> store) {
         this.track = track;
         this.store = store;
     }
 
     public void run() {
         DecodedAudioData audioData = null;
         try {
             audioData = AudioDecoder.getSamples(track.getFilepath(), 135);
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (AudioDecoderException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         track.setDuration(audioData.getDuration());
         track.setFormat(audioData.getFormat());
         synchronized (store) {
             try {
                store.put(track, new Digest(Ofa.createPrint(audioData)));
             } catch (AudioDecoderException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             } catch (FileNotFoundException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
 }
 
 public class DigestMaker {
 
 	public static Map<Track, Digest> getAlbumDigest(Album album) throws AudioDecoderException, FileNotFoundException {
         List<Track> tracks = album.getTracks();
         Map<Track, Digest> digests = new HashMap<Track, Digest>();
         for (Track track : tracks) {
             DecodedAudioData audioData = AudioDecoder.getSamples(track.getFilepath(), 135);
             track.setDuration(audioData.getDuration());
             track.setFormat(audioData.getFormat());
             digests.put(track, new Digest(Ofa.createPrint(audioData)));
         }
 		return digests;
 	}
 
     public static Map<Track, Digest> getAlbumDigestThreaded(Album album) {
         List<Track> tracks = album.getTracks();
         Map<Track, Digest> digests = new HashMap<Track, Digest>();
         ExecutorService executor = Executors.newCachedThreadPool();
         for (Track track : tracks) {
             executor.execute(new DigestMakerRunnable(track, digests));
         }
         executor.shutdown();
         try {
             executor.awaitTermination(1, TimeUnit.HOURS);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         return digests;
     }
 	 
 }
  

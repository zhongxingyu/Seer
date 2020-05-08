 import com.musicg.fingerprint.FingerprintSimilarityComputer;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.HashMap;
 
 /**
  *
  * @author Julia
  */
 public class MapManager implements Serializable {
 
<<<<<<< HEAD
    private HashMap<byte[],Action> database;
    private float THRESHOLD = 0.4f;
=======
     private HashMap<Sound, Action> database;
     private static final float THRESHOLD = 0.7f;
>>>>>>> 9e80d9f3112511d6f3383ac6ebe17b5369a5e799
 
     public MapManager() {
         try {
             FileInputStream fileIn = new FileInputStream("data");
             ObjectInputStream in = new ObjectInputStream(fileIn);
             database = (HashMap<Sound, Action>) in.readObject();
             in.close();
             fileIn.close();
         } catch (IOException i) {
             database = new HashMap<Sound, Action>();
         } catch (ClassNotFoundException c) {
         }
     }
 
     public void add(Sound sound, Action action) {
         database.put(sound, action);
         try {
             FileOutputStream fileOut = new FileOutputStream("data");
             ObjectOutputStream out = new ObjectOutputStream(fileOut);
             out.writeObject(database);
             out.close();
             fileOut.close();
         } catch (IOException i) {
             i.printStackTrace();
         }
     }
 
     public Action get(byte[] fingerprint) {
         FingerprintSimilarityComputer fsc;
         Action best = null;
         for (Sound sound : database.keySet()) {
             byte[] print = sound.getFingerprint();
             double ratio = ((double) fingerprint.length) / ((double) print.length);
             if (ratio > 1.25 || ratio < 0.75) {
                 System.out.println("Mismatched lengths");
                 continue;
             }
             fsc = new FingerprintSimilarityComputer(fingerprint, print);
             float score = fsc.getFingerprintsSimilarity().getSimilarity();
             System.out.println("Score: " + score);
             if (score > THRESHOLD) {
                 System.out.println("here");
                 best = find(print);
             }
         }
         return best;
     }
 
     private Action find(byte[] fingerprint) {
         for (Sound s : database.keySet()) {
             if (s.getFingerprint().equals(fingerprint)) return database.get(s);
         }
         return null;
     }
 }

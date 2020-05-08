 package ReactorEE.sound;
 
 import javazoom.jl.player.Player;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.util.List;
 
 public class MP3Player {
     protected Player player;
     protected List<String> tracks;

     Thread thread = new Thread() {
        public boolean isInterrupted = false;
 
         public void interrupt() {
             player.close();
             if(isDebug) System.out.println("DEBUG Killed thread " + this);
             isInterrupted = true;
         }
 
         public void run() {
             try {
                 if(tracks == null)
                     player.play();
                 else {
                     boolean hasEnded = false;
                     int index = 0;
 
                     while(!isInterrupted) {
                         player = createPlayer(tracks.get(index));
 
                         // 1 frame ~= 28 milliseconds
                         while(!hasEnded && !isInterrupted) hasEnded = !player.play(8);
 
                         hasEnded = false;
                         index = (index+1) % tracks.size();
                         player.close();
                     }
                 }
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     };
 
     public MP3Player(final String filename) {
         player = createPlayer(filename);
     }
 
     public MP3Player(List<String> tracks) {
         this.tracks = tracks;
     }
 
     public Thread play() {
 
         thread.start();
         return thread;
     }
 
 
     private Player createPlayer(String filename) {
         Player out = null;
         try {
             out = new Player(new BufferedInputStream(new FileInputStream(filename)));
         } catch (Exception e) {
             if(isDebug) System.out.println("DEBUG MP3 track not found");
             e.printStackTrace();
         }
         return out;
     }
 
     private static boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
             getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
 }

 package uk.co.brotherlogic.mdb.alarm;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import uk.co.brotherlogic.mdb.User;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
import uk.co.brotherlogic.mdb.record.RecordScore;
 import de.umass.lastfm.Authenticator;
 import de.umass.lastfm.Session;
 import de.umass.lastfm.Track;
 import de.umass.lastfm.scrobble.ScrobbleResult;
 
 public class Alarm
 {
    private final double minScore;
    private final int overallTime;
    private final long startTime;
    private final int maxLength;
 
    List<Record> records;
    int pointer = 0;
 
    Session cSession = null;
 
    String user, password, secret, key;
 
    public Alarm(double score, int time, int length)
    {
       minScore = score;
       overallTime = time;
       maxLength = length;
       startTime = System.currentTimeMillis();
 
       try
       {
          BufferedReader reader = new BufferedReader(new FileReader("password"));
          user = reader.readLine().trim();
          password = reader.readLine().trim();
          secret = reader.readLine().trim();
          key = reader.readLine().trim();
          reader.close();
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
    }
 
    private void getRecords() throws SQLException
    {
       records = new LinkedList<Record>();
       long sTime = System.currentTimeMillis();
       Collection<Record> recs = GetRecords.create().getRecords(GetRecords.SHELVED, "CD");
      RecordScore.scoreRecords(recs);
       long mTime = System.currentTimeMillis();
       System.out.println("Records = " + (mTime - sTime));
       for (Record r : recs)
       {
          boolean acceptable = false;
          if ((r.getRiploc() != null) && (r.getRiploc().trim().length() > 0))
                if (r.getScore() >= minScore)
                   acceptable = true;
          if (acceptable)
             records.add(r);
       }
       System.out.println("Scores = " + (System.currentTimeMillis() - mTime));
 
       Collections.shuffle(records);
    }
 
    private Song pickSong() throws SQLException
    {
       if (records == null)
          getRecords();
       Record r = records.get(pointer++);
 
       List<Integer> tracks = new LinkedList<Integer>();
       for (int i = 1; i <= r.getNumberOfFormatTracks(); i++)
          tracks.add(i);
       Collections.shuffle(tracks);
 
       //System.err.println(r.getTitle() + " - " + tracks.get(1));
 
       return new Song(r, tracks.get(1));
    }
 
    private boolean playSong(Song s)
    {
       boolean played = false;
       String command = "mplayer";
       File toPlay = null;
 
       if (new File(s.r.getRiploc()).exists())
          for (File f : new File(s.r.getRiploc()).listFiles())
             if (f.getName().contains(s.getResolveTrack()))
                toPlay = f;
 
       if (toPlay != null && toPlay.exists())
          try
          {
             Process p = Runtime.getRuntime().exec(new String[]
             { command, toPlay.getAbsolutePath() });
             BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
             for (String line = reader.readLine(); line != null; line = reader.readLine())
             {
                // Do Nothign
             }
             played = true;
          }
          catch (IOException e)
          {
             e.printStackTrace();
          }
          else
          {
         	 System.err.println(toPlay);
         	 System.err.println(s.getResolveTrack());
         	 System.err.println("Error playing: " + s);
          }
       return played;
    }
 
    public void registerSong(Song s) throws SQLException
    {
       if (cSession == null)
          cSession = Authenticator.getMobileSession(user, password, secret, key);
       int now = (int) (System.currentTimeMillis() / 1000);
       ScrobbleResult result = Track.scrobble(s.r.getFormTrackArtist(s.cdTrack),
             s.r.getFormTrackTitle(s.cdTrack), now, cSession);
       //System.err.println(result);
    }
 
    public void run()
    {
       while (System.currentTimeMillis() - startTime < overallTime)
          try
          {
             Song song = pickSong();
             if (playSong(song))
                registerSong(song);
          }
          catch (SQLException e)
          {
             e.printStackTrace();
          }
    }
    
    public static void main(String[] args) throws SQLException
    {
 	   long sTime = System.currentTimeMillis();
 	   Alarm al = new Alarm(10,1000,500);
 	   Song s = al.pickSong();
	   System.out.println(s.r.getAuthor() + " - " + s.r.getTitle());
 	   System.out.println(System.currentTimeMillis()-sTime);
    }
 }
 
 class Song
 {
    Record r;
    int cdTrack;
 
    public Song(Record rec, int track)
    {
       r = rec;
       cdTrack = track;
    }
    
    public String toString()
    {
 	   return "SONG " + cdTrack + ": " + r.getRiploc() + " = " + r;
    }
 
    public String getResolveTrack()
    {
       if (cdTrack < 10)
          return "00" + cdTrack;
       else if (cdTrack < 100)
          return "0" + cdTrack;
       else
          return "" + cdTrack;
    }
 }

 package uk.co.brotherlogic.mdbweb.record;
 
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import uk.co.brotherlogic.jarpur.TemplatePage;
 import uk.co.brotherlogic.mdb.Connect;
 import uk.co.brotherlogic.mdb.User;
 import uk.co.brotherlogic.mdb.artist.Artist;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
import uk.co.brotherlogic.mdb.record.RecordScore;
 import uk.co.brotherlogic.mdb.record.Track;
 
 public class Default extends TemplatePage
 {
    @Override
    protected Map<String, Object> convertParams(List<String> elems, Map<String, String> params)
    {
       Map<String, Object> paramMap = new TreeMap<String, Object>();
 
       try
       {
          int recordID = Integer.parseInt(elems.get(0));
          Record record = GetRecords.create().getRecord(recordID);
 
          // Have we tried to score the record?
          if (params.containsKey("score"))
          {
             System.out.println("FOUND SCORE");
             record.addScore(User.getUser("simon"), Integer.parseInt(params.get("score")));
 
             // Fix the score in the database
             Connect.getConnection().commitTrans();
          }
 
          paramMap.put("record", record);
          paramMap.put("artistmap", splitArtists(record));
         paramMap.put("sscore", RecordScore.getLastScore(record, User.getUser("simon")));
          paramMap.put("scorecount", record.getScoreCount(User.getUser("simon")));
          DateFormat df = DateFormat.getDateInstance();
          paramMap.put("scoredate", df.format(record.getLastScoreDate(User.getUser("simon"))));
 
       }
       catch (SQLException e)
       {
          e.printStackTrace();
       }
 
       return paramMap;
    }
 
    public String convertTime(int timeIn)
    {
       if (timeIn <= 0)
          return "";
 
       int hours = timeIn / (60 * 60);
       int minutes = (timeIn - hours * 60 * 60) / 60;
       int seconds = (timeIn - hours * 60 * 60 - minutes * 60);
 
       if (hours > 0)
          return hours + ":" + pad(minutes) + ":" + pad(seconds);
       else
          return pad(minutes) + ":" + pad(seconds);
    }
 
    @Override
    public Class generates()
    {
       return Record.class;
    }
 
    public boolean mismatch(Track t, Record r)
    {
       String[] tWords = t.getSortedTrackAuthor().replaceAll("[^0-9A-Za-z\\s]", "").split("\\s+");
       String[] rWords = r.getAuthor().replaceAll("[^0-9A-Za-z\\s]", "").split("\\s+");
 
       boolean match = true;
       for (int i = 0; i < rWords.length; i++)
       {
          boolean found = false;
          for (int j = 0; j < tWords.length; j++)
             if (rWords[i].equals(tWords[j]))
                found = true;
          if (!found)
          {
             System.err.println("NO MATCH = " + rWords[i]);
             match = false;
          }
       }
 
       return !match;
 
    }
 
    @Override
    public String linkParams(Object arg0)
    {
       return "" + (((Record) arg0).getNumber());
    }
 
    private String pad(int num)
    {
       if (num > 9)
          return "" + num;
       else
          return "0" + num;
    }
 
    public Boolean relatedExists(Track track)
    {
       try
       {
 
          return GetRecords.create().getRecordsWithTrack(track.getTitle()).size() > 1;
       }
       catch (SQLException e)
       {
          e.printStackTrace();
       }
       return false;
    }
 
    public String resolve(String number)
    {
       String[] elems = number.split(",");
       if (elems.length == 1)
          return elems[0];
 
       String retString = "";
       int num = Integer.parseInt(elems[0]);
       int last = Integer.parseInt(elems[0]);
       for (int i = 1; i <= elems.length; i++)
       {
          int nNum = -1;
          if (i < elems.length)
             nNum = Integer.parseInt(elems[i]);
 
          if (nNum == last + 1)
          {
             last = nNum;
          }
          else
          {
             if (num == last)
             {
                if (i < elems.length)
                   retString += last + ",";
                else
                   retString += last;
             }
             else if (i < elems.length)
                retString += num + "-" + last + ",";
             else
                retString += num + "-" + last;
 
             num = nNum;
             last = nNum;
          }
       }
 
       return retString;
    }
 
    private Map<String, List<Artist>> splitArtists(Record rec) throws SQLException
    {
       Map<String, List<Artist>> ret = new TreeMap<String, List<Artist>>(new Comparator<String>()
       {
 
          @Override
          public int compare(String arg0, String arg1)
          {
             String[] elems0 = arg0.split(",");
             String[] elems1 = arg1.split(",");
 
             for (int i = 0; i < Math.min(elems0.length, elems1.length); i++)
             {
                Integer int0 = Integer.parseInt(elems0[0]);
                Integer int1 = Integer.parseInt(elems1[0]);
 
                if (int0 != int1)
                   return int0.compareTo(int1);
             }
 
             return elems0.length - elems1.length;
          }
 
       });
 
       // First create a map from personnel to tracks
       Map<Artist, String> persToTracks = new HashMap<Artist, String>();
       for (Track track : rec.getTracks())
          for (Artist art : track.getPersonnel())
             if (persToTracks.containsKey(art))
                persToTracks.put(art,
                      persToTracks.get(art) + "," + Integer.toString(track.getTrackNumber()));
             else
                persToTracks.put(art, Integer.toString(track.getTrackNumber()));
 
       // Now transform into the return set
       for (Entry<Artist, String> arts : persToTracks.entrySet())
       {
          if (!ret.containsKey(arts.getValue()))
             ret.put(arts.getValue(), new LinkedList<Artist>());
          ret.get(arts.getValue()).add(arts.getKey());
       }
 
       return ret;
    }
 }
